package com.angellane.juggle.comparator;

import com.angellane.juggle.candidate.Candidate;
import com.angellane.juggle.match.Match;
import com.angellane.juggle.query.Query;

import java.util.Comparator;
import java.util.List;

/**
 * Compares two Matches based on their candidate's package position in a list.
 * Matches whose candidates aren't in a package on the list are sorted last.
 */
public class ByPackage<
        C extends Candidate, Q extends Query<C>, M extends Match<C,Q>
        >
        implements Comparator<M> {
    // Using a List allows us to call indexOf to go from candidate -> index
    public final List<String> packageList;

    public ByPackage(List<String> packageNames) { packageList = packageNames; }

    private int getPriorityOrDefault(String packageName,
                                     @SuppressWarnings("SameParameterValue")
                                     int defaultPriority) {
        int index = packageList.indexOf(packageName);
        return index == -1 ? defaultPriority : index;
    }

    @Override
    public int compare(M m1, M m2) {
        int m1Pri = getPriorityOrDefault(
                m1.candidate().packageName(), Integer.MAX_VALUE);
        int m2Pri = getPriorityOrDefault(
                m2.candidate().packageName(), Integer.MAX_VALUE);

        return Integer.compare(m1Pri, m2Pri);
    }
}
