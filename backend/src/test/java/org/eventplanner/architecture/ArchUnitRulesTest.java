package org.eventplanner.architecture;

import static com.tngtech.archunit.core.importer.ImportOption.Predefined.DO_NOT_INCLUDE_TESTS;
import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.constructors;
import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.methods;
import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.noClasses;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.params.provider.Arguments.of;

import java.lang.annotation.Annotation;
import java.lang.reflect.Parameter;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Stream;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.lang.NonNull;
import org.springframework.lang.Nullable;
import org.springframework.web.servlet.view.RedirectView;

import com.tngtech.archunit.base.DescribedPredicate;
import com.tngtech.archunit.core.domain.JavaClass;
import com.tngtech.archunit.core.domain.JavaClasses;
import com.tngtech.archunit.core.domain.JavaCodeUnit;
import com.tngtech.archunit.core.domain.JavaConstructor;
import com.tngtech.archunit.core.domain.JavaMethod;
import com.tngtech.archunit.core.importer.ClassFileImporter;
import com.tngtech.archunit.lang.ArchCondition;
import com.tngtech.archunit.lang.ArchRule;
import com.tngtech.archunit.lang.ConditionEvent;
import com.tngtech.archunit.lang.ConditionEvents;
import com.tngtech.archunit.lang.SimpleConditionEvent;
import lombok.extern.slf4j.Slf4j;

@Slf4j
class ArchUnitRulesTest {
    private static JavaClasses importedClasses;

    @BeforeAll
    static void setUp() {
        importedClasses = new ClassFileImporter()
            .withImportOption(DO_NOT_INCLUDE_TESTS)
            .importPackages("org.eventplanner");
    }

    private static Stream<Arguments> forbiddenLayerDependencies() {
        Stream.Builder<Arguments> argumentsStream = Stream.builder();
        List.of(
                // domain must not know any other layer
                of("org.eventplanner.*.domain..", "org.eventplanner.*.application.."),
                of("org.eventplanner.*.domain..", "org.eventplanner.*.rest.."),
                of("org.eventplanner.*.domain..", "org.eventplanner.*.adapter.."),
                // application must not know adapters
                of("org.eventplanner.*.application..", "org.eventplanner.*.rest.."),
                of("org.eventplanner.*.application..", "org.eventplanner.*.adapter.."),
                // adapters must not know each other
                of("org.eventplanner.*.rest..", "org.eventplanner.*.adapter.."),
                of("org.eventplanner.*.adapter..", "org.eventplanner.*.rest.."),
                // adapters must not know internal application layers
                of("org.eventplanner.*.adapter..", "org.eventplanner.*.application.services.."),
                of("org.eventplanner.*.adapter..", "org.eventplanner.*.application.usecases.."),
                of("org.eventplanner.*.adapter..", "org.eventplanner.*.application.scheduled.."),
                // rest layer must not know internal application layers
                of("org.eventplanner.*.rest..", "org.eventplanner.*.application.services.."),
                of("org.eventplanner.*.rest..", "org.eventplanner.*.application.scheduled.."),
                of("org.eventplanner.*.rest..", "org.eventplanner.*.application.ports.."),
                // application and domain layer must not know external dependencies
                of("org.eventplanner.*.application..", "software.amazon.awssdk.."),
                of("org.eventplanner.*.application..", "jakarta.persistence.."),
                of("org.eventplanner.*.application..", "org.springframework.data.jpa.."),
                of("org.eventplanner.*.application..", "io.swagger.."),
                of("org.eventplanner.*.domain..", "software.amazon.awssdk.."),
                of("org.eventplanner.*.domain..", "org.springframework.data.jpa.."),
                of("org.eventplanner.*.domain..", "io.swagger..")
            )
            .forEach(argumentsStream::add);

        // contexts must not know each other
        var contexts = List.of("events");
        for (var context : contexts) {
            var others = contexts.stream().filter(c -> !context.equals(c)).toList();
            for (var other : others) {
                var pkgContext = "org.eventplanner." + context + "..";
                var pkgOther = "org.eventplanner." + other + "..";
                argumentsStream.add(of(pkgContext, pkgOther));
            }
        }
        return argumentsStream.build();
    }

    @ParameterizedTest
    @MethodSource("forbiddenLayerDependencies")
    void layerDependencyRulesShouldNotBeViolated(String testee, String forbiddenDependency) {
        ArchRule rule = noClasses()
            .that()
            .resideInAPackage(testee)
            .should()
            .dependOnClassesThat()
            .resideInAPackage(forbiddenDependency);

        rule.check(importedClasses);
    }

    @Test
    void controllersShouldReturnResponseEntities() {
        assertDoesNotThrow(() -> {
            ArchRule rule = methods()
                .that()
                .areAnnotatedWith("org.springframework.web.bind.annotation.RequestMapping")
                .or()
                .areAnnotatedWith("org.springframework.web.bind.annotation.GetMapping")
                .or()
                .areAnnotatedWith("org.springframework.web.bind.annotation.PostMapping")
                .or()
                .areAnnotatedWith("org.springframework.web.bind.annotation.PutMapping")
                .or()
                .areAnnotatedWith("org.springframework.web.bind.annotation.DeleteMapping")
                .should()
                .haveRawReturnType(ResponseEntity.class)
                .orShould()
                .haveRawReturnType(RedirectView.class);
            rule.check(importedClasses);
        });
    }

    @Test
    void allMethodReturnTypesShouldBeNullMarked() {
        ArchRule rule = methods()
            .that()
            .areDeclaredInClassesThat()
            .resideOutsideOfPackage("org.eventplanner..rest..")
            .and()
            .doNotHaveRawReturnType(isPrimitive())
            .and(areNotLombokGenerated())
            .and(areNotRecordOrEnumFunctions())
            .should()
            .beAnnotatedWith(NonNull.class)
            .orShould()
            .beAnnotatedWith(Nullable.class);
        rule.check(importedClasses);
    }

    @Test
    void allMethodParametersShouldBeNullMarked() {
        ArchRule rule = methods()
            .that()
            .areDeclaredInClassesThat()
            .resideOutsideOfPackage("org.eventplanner..rest..")
            .and(areNotLombokGenerated())
            .and(areNotRecordOrEnumFunctions())
            .should(haveAllMethodParametersNullMarked());
        rule.check(importedClasses);
    }

    @Test
    void allConstructorParametersShouldBeNullMarked() {
        ArchRule rule = constructors()
            .that()
            .areDeclaredInClassesThat()
            .resideOutsideOfPackage("org.eventplanner..rest..")
            .and(areNotLombokGenerated())
            .and(areNotRecordOrEnumFunctions())
            .should(haveAllConstructorParametersNullMarked());
        rule.check(importedClasses);
    }

    private DescribedPredicate<JavaClass> isPrimitive() {
        return new DescribedPredicate<>("that is a primitive") {
            @Override
            public boolean test(final JavaClass clazz) {
                return clazz.isPrimitive();
            }
        };
    }

    DescribedPredicate<JavaCodeUnit> areNotLombokGenerated() {
        return new DescribedPredicate<>("are not generated by lombok") {
            @Override
            public boolean test(final JavaCodeUnit unit) {
                return unit.tryGetAnnotationOfType("lombok.Generated").isEmpty();
            }
        };
    }

    DescribedPredicate<JavaCodeUnit> areNotRecordOrEnumFunctions() {
        return new DescribedPredicate<>("are not record functions") {
            @Override
            public boolean test(final JavaCodeUnit unit) {
                if (unit.getOwner().isRecord()) {
                    return !List.of("equals", "toString").contains(unit.getName());
                }
                if (unit.getOwner().isEnum()) {
                    return !List.of("values", "$values", "toString", "valueOf").contains(unit.getName());
                }
                return true;
            }
        };
    }

    ArchCondition<JavaConstructor> haveAllConstructorParametersNullMarked() {
        return new ArchCondition<>("have all parameters null marked") {
            @Override
            public void check(JavaConstructor constructor, ConditionEvents events) {
                var parameters = constructor.reflect().getParameters();
                var annotations = constructor.reflect().getParameterAnnotations();
                checkParametersAreNullMarked(constructor, parameters, annotations, events);
            }
        };
    }

    ArchCondition<JavaMethod> haveAllMethodParametersNullMarked() {
        return new ArchCondition<>("have all parameters null marked") {
            @Override
            public void check(JavaMethod method, ConditionEvents events) {
                var parameters = method.reflect().getParameters();
                var annotations = method.reflect().getParameterAnnotations();
                checkParametersAreNullMarked(method, parameters, annotations, events);
            }
        };
    }

    private void checkParametersAreNullMarked(
        JavaCodeUnit unit, Parameter[] parameters, Annotation[][] annotations, ConditionEvents events) {
        var anyViolation = false;
        for (int i = 0; i < parameters.length; i++) {
            var parameter = parameters[i];
            var annotationClasses = Arrays.stream(annotations[i])
                .map(Annotation::annotationType)
                .toList();
            if (!parameter.getName().equals("$enum$name")
                && !parameter.getType().isPrimitive()
                && !annotationClasses.contains(Value.class)
                && !annotationClasses.contains(Autowired.class)
                && !annotationClasses.contains(NonNull.class)
                && !annotationClasses.contains(Nullable.class)) {
                anyViolation = true;
                var message =
                    ConditionEvent.createMessage(unit, "parameter " + parameter.getName() + " is not null marked");
                events.add(new SimpleConditionEvent(unit, false, message));
            }
        }
        if (!anyViolation) {
            var message = ConditionEvent.createMessage(unit, "All parameters are null marked");
            events.add(new SimpleConditionEvent(unit, true, message));
        }
    }
}
