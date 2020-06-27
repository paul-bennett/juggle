package com.angellane.juggle.comparator;

import com.angellane.juggle.Accessibility;

import java.lang.reflect.Member;
import java.util.Comparator;

public class ByAccessibility implements Comparator<Member> {
    @Override
    public int compare(Member o1, Member o2) {
        return Math.negateExact(Accessibility.fromModifiers(o1.getModifiers())
                .compareTo(Accessibility.fromModifiers(o2.getModifiers())));
    }
}

