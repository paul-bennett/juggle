package com.angellane.juggle.comparator.member;

import com.angellane.juggle.CandidateMember;

import java.util.Comparator;

public class ByCanonicalName implements Comparator<CandidateMember> {
    @Override
    public int compare(CandidateMember o1, CandidateMember o2) {
        return o1.getMember().toString().compareTo(o2.getMember().toString());
    }
}
