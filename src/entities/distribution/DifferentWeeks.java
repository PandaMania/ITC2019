package entities.distribution;

import entities.Instance;
import entities.Solution;
import entities.SolutionClass;

import java.util.Collection;
import java.util.List;

public class DifferentWeeks extends Distribution {
// Please check the lambda thing here (Jonty)

    @Override
    public boolean validate(Instance instance, Solution solution) {
        List<SolutionClass> solutionClasses = getClassInDistribution(solution);

        return forAny(solutionClasses, (i,j)->{
            return i.weeks!=j.weeks;

        });


    }
}
