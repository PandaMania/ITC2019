package entities.distribution;

import entities.Instance;
import entities.Solution;
import entities.SolutionClass;

import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;

public class SameStart extends Distribution{
    @Override
    public boolean validate(Instance instance, Solution solution) {
        List<SolutionClass> classes = getClassInDistribution(solution);
        if (classes.size() > 0) {
            int start = classes.get(0).start;
            return classes.stream().allMatch(c -> c.start == start);
        }
        return true;
//        return solution.classes.stream()
//                .filter(c->idInDistribution.contains(c.classId))
//                .map(c->c.start).distinct()
//                .count() == 1;
    }
}
