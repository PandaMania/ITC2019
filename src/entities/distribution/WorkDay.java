package entities.distribution;

import entities.Instance;
import entities.Solution;
import entities.SolutionClass;
import util.BitSets;

import java.util.Collection;
import java.util.List;

public class WorkDay extends Distribution {

    int S;

    public WorkDay(String s) {
        S = Integer.parseInt(s);
    }


    // ((Ci.days and Cj.days) = 0) ∨
    // ((Ci.weeks and Cj.weeks) = 0) ∨
    // (max(Ci.end,Cj.end)−min(Ci.start,Cj.start) ≤ S)

    @Override
    public boolean validate(Instance instance, Solution solution) {
        List<SolutionClass> classes = getClassInDistribution(solution);
        return forAny(classes, (Ci, Cj) -> {
            int CiEnd = Ci.start + Ci.length;
            int CjEnd = Cj.start + Cj.length;
            return (BitSets.and(Ci.days, Cj.days).cardinality() == 0) ||
                    ((BitSets.and(Ci.weeks, Cj.weeks).cardinality() == 0)) ||
                    (Math.max(CiEnd, CjEnd)-Math.min(Ci.start, Cj.start) <= S);
        });
    }
}
