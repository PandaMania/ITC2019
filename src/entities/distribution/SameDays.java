package entities.distribution;

import entities.Instance;
import entities.Solution;
import entities.SolutionClass;

import java.util.BitSet;
import java.util.List;
import java.util.stream.Collectors;

public class SameDays extends Distribution {
    @Override
    public boolean validate(Instance instance, Solution solution) {
        List<SolutionClass> solutionClasses = getClassInDistribution(solution);
        return forAny(solutionClasses, (i,j)->{
            BitSet or = new BitSet(i.days.length());
            or.or(i.days);
            or.or(j.days);
            return (or.equals(i.days)) || (or.equals(j.days));
        });

    }

}
