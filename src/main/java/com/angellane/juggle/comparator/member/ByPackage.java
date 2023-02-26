package com.angellane.juggle.comparator.member;

import java.lang.reflect.Member;
import java.util.Comparator;
import java.util.List;

/**
 * Compares two class Members based on their package's position in a list.
 * Members that aren't in a package on the list are sorted last.
 */
public class ByPackage implements Comparator<Member> {
    // Using a List allows us to call indexOf to go from member -> index
    public final List<String> packageList;

    public ByPackage(List<String> packageNames) { packageList = packageNames; }

    private int getPriorityOrDefault(String packageName, int defaultPriority) {
        int index = packageList.indexOf(packageName);
        return index == -1 ? defaultPriority : index;
    }

    @Override
    public int compare(Member m1, Member m2) {
        int o1Pri = getPriorityOrDefault(m1.getDeclaringClass().getPackageName(), Integer.MAX_VALUE);
        int o2Pri = getPriorityOrDefault(m2.getDeclaringClass().getPackageName(), Integer.MAX_VALUE);

        return Integer.compare(o1Pri, o2Pri);
    }
}
