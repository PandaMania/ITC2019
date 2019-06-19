package entities.distribution;

import entities.Instance;
import entities.Solution;
import entities.SolutionClass;

import java.util.Collection;
import java.util.List;

public class SameRoom extends Distribution {
    @Override
    public boolean validate(Instance instance, Solution solution) {
        List<SolutionClass> solutionClasses = getClassInDistribution(solution);

        return forAny(solutionClasses, (Ci,Cj)->{
            return Ci.roomId==Cj.roomId;

        });
    }
}
