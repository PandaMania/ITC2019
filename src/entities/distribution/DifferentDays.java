package entities.distribution;

import entities.Instance;
import entities.Solution;
import entities.SolutionClass;
import util.BitSets;

import java.util.BitSet;
import java.util.Collection;
import java.util.List;

public class DifferentDays extends SameDays {

    @Override
    public boolean validate(Instance instance, Solution solution) {
        List<SolutionClass> solutionClasses = getClassInDistribution(solution);
        return forAny(solutionClasses, (i,j)->{
            BitSet or = BitSets.or(i.days, j.days);
            return !((or.equals(i.days)) || (or.equals(j.days)));
        });
    }
}
