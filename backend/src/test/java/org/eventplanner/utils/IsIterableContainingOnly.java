package org.eventplanner.utils;

import org.hamcrest.Description;
import org.hamcrest.Matcher;
import org.hamcrest.TypeSafeDiagnosingMatcher;

public class IsIterableContainingOnly<T> extends TypeSafeDiagnosingMatcher<Iterable<? super T>> {

    private final Matcher<? super T> elementMatcher;

    public IsIterableContainingOnly(Matcher<? super T> elementMatcher) {
        this.elementMatcher = elementMatcher;
    }

    @Override
    protected boolean matchesSafely(Iterable<? super T> collection, Description mismatchDescription) {
        if (isEmpty(collection)) {
            mismatchDescription.appendText("was empty");
            return false;
        }

        var missmatchFound = false;
        boolean isPastFirst = false;
        for (Object item : collection) {
            if (!elementMatcher.matches(item)) {
                missmatchFound = true;
                if (!isPastFirst) {
                    mismatchDescription.appendText("mismatches were: [");
                    isPastFirst = true;
                } else {
                    mismatchDescription.appendText(", ");
                }
                elementMatcher.describeMismatch(item, mismatchDescription);
            }
        }
        if (missmatchFound) {
            mismatchDescription.appendText("]");
        }
        return !missmatchFound;
    }

    private boolean isEmpty(Iterable<? super T> iterable) {
        return !iterable.iterator().hasNext();
    }

    @Override
    public void describeTo(Description description) {
        description
            .appendText("a collection containing only ")
            .appendDescriptionOf(elementMatcher);
    }

    public static <T> Matcher<Iterable<? super T>> allMatch(Matcher<? super T> itemMatcher) {
        return new IsIterableContainingOnly<>(itemMatcher);
    }
}

