package com.angellane.juggle.comparator;

import java.util.Comparator;

/**
 * Compares types by specificity, with the most specific ordered first.
 */
public class TypeComparator implements Comparator<Class<?>> {
    @Override
    public int compare(Class<?> o1, Class<?> o2) {
        // TODO: consider numeric promotions (boxing/unboxing should be irrelevant)

        boolean assignableOneFromTwo = o1.isAssignableFrom(o2);
        boolean assignableTwoFromOne = o2.isAssignableFrom(o1);

        return (assignableOneFromTwo == assignableTwoFromOne)
                ? 0
                : assignableTwoFromOne ? -1 : 1;
    }
}
