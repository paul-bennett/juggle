package com.angellane.juggle.comparator.member;

import com.angellane.juggle.Accessibility;
import com.angellane.juggle.candidate.CandidateMember;

import java.util.Comparator;

public class ByAccessibility implements Comparator<CandidateMember> {
    @Override
    public int compare(CandidateMember o1, CandidateMember o2) {
        return Math.negateExact(Accessibility.fromModifiers(o1.member().getModifiers())
                .compareTo(Accessibility.fromModifiers(o2.member().getModifiers())));
    }
}

