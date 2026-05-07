package org.eventplanner.architecture;

import static com.tngtech.archunit.core.importer.ImportOption.Predefined.DO_NOT_INCLUDE_TESTS;
import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.constructors;
import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.methods;
import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.noClasses;
import static org.eventplanner.architecture.ArchUnitHelpers.areNotDeclaredInEnums;
import static org.eventplanner.architecture.ArchUnitHelpers.areNotLombokGenerated;
import static org.eventplanner.architecture.ArchUnitHelpers.areNotRecordOrEnumFunctions;
import static org.eventplanner.architecture.ArchUnitHelpers.haveAllConstructorParametersNullMarked;
import static org.eventplanner.architecture.ArchUnitHelpers.haveAllMethodParametersNullMarked;
import static org.eventplanner.architecture.ArchUnitHelpers.haveNullMarkedReturnType;
import static org.eventplanner.architecture.ArchUnitHelpers.isPrimitive;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.params.provider.Arguments.of;

import java.util.List;
import java.util.stream.Stream;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.springframework.http.ResponseEntity;
import org.springframework.web.servlet.view.RedirectView;

import com.tngtech.archunit.core.domain.JavaClasses;
import com.tngtech.archunit.core.importer.ClassFileImporter;
import com.tngtech.archunit.lang.ArchRule;
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
            .should(haveNullMarkedReturnType());
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
            .and(areNotDeclaredInEnums())
            .and(areNotRecordOrEnumFunctions())
            .and(areNotLombokGenerated())
            .and(areNotRecordOrEnumFunctions())
            .should(haveAllConstructorParametersNullMarked());
        rule.check(importedClasses);
    }
}
