package entities.distribution;

import entities.Instance;
import entities.Solution;
import entities.SolutionClass;

import java.util.Collection;
import java.util.List;

public class MaxBlock extends Distribution {

    int M;
    int S;

    public MaxBlock(String params) {
        String[] split = params.split(",");
        M = Integer.parseInt(split[0]);
        S = Integer.parseInt(split[1]);
    }

    @Override
    public boolean validate(Instance instance, Solution solution) {
        List<SolutionClass> classes = getClassInDistribution(solution);
        return forAll(classes, C->{return false;});
    }
}
