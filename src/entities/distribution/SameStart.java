package entities.distribution;

import entities.Instance;
import entities.Solution;

import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;

public class SameStart extends Distribution{
    @Override
    public boolean validate(Instance instance, Solution solution) {
        return solution.classes.stream()
                .filter(c->idInDistribution.contains(c.classId))
                .map(c->c.start).distinct()
                .count() == 1;
    }
}
