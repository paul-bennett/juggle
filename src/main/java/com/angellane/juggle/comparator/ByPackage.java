package com.angellane.juggle.comparator;

import com.angellane.juggle.candidate.Candidate;
import com.angellane.juggle.match.Match;
import com.angellane.juggle.query.Query;

import java.util.Comparator;
import java.util.List;

/**
 * Compares two class Members based on their package's position in a list.
 * Members that aren't in a package on the list are sorted last.
 */
public class ByPackage implements Comparator<Match<Candidate, Query>> {
    // Using a List allows us to call indexOf to go from member -> index
    public final List<String> packageList;

    public ByPackage(List<String> packageNames) { packageList = packageNames; }

    private int getPriorityOrDefault(String packageName, @SuppressWarnings("SameParameterValue") int defaultPriority) {
        int index = packageList.indexOf(packageName);
        return index == -1 ? defaultPriority : index;
    }

    @Override
    public int compare(Match<Candidate, Query> m1, Match<Candidate, Query> m2) {
        int o1Pri = getPriorityOrDefault(m1.candidate().packageName(), Integer.MAX_VALUE);
        int o2Pri = getPriorityOrDefault(m2.candidate().packageName(), Integer.MAX_VALUE);

        return Integer.compare(o1Pri, o2Pri);
    }
}
