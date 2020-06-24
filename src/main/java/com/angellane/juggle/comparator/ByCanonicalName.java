package com.angellane.juggle.comparator;

import java.lang.reflect.Member;
import java.util.Comparator;

public class ByCanonicalName implements Comparator<Member> {
    @Override
    public int compare(Member o1, Member o2) {
        return o1.toString().compareTo(o2.toString());
    }
}
