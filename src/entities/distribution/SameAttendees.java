package entities.distribution;

import entities.Instance;
import entities.Solution;
import entities.SolutionClass;

import java.util.BitSet;
import java.util.List;
import util.BitSets;

public class SameAttendees extends Distribution {


    /*
(Ci.end + Ci.room.travel[Cj.room] ≤ Cj.start) ∨
(Cj.end + Cj.room.travel[Ci.room] ≤ Ci.start) ∨
((Ci.days and Cj.days) = 0) ∨ ((Ci.weeks and Cj.weeks) = 0)*/
    @Override
    public boolean validate(Instance instance, Solution solution) {
        List<SolutionClass> classes = getClassInDistribution(solution);

        return forAny(classes, (Ci, Cj) -> {
            int travel_ij = instance.distances[Ci.roomId][Cj.roomId];
            int travel_ji = instance.distances[Cj.roomId][Ci.roomId];

            return  (Ci.start + Ci.length + travel_ij <= Cj.start) ||
                    (Cj.start + Cj.length + travel_ji <= Ci.start) ||
                    ((BitSets.and(Cj.days, Ci.days).cardinality() == 0) || (BitSets.and(Ci.weeks, Cj.weeks).cardinality() == 0));
        });
    }
}
