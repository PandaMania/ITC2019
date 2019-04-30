package entities.distribution;

import entities.Instance;

import java.util.Collection;

public class DifferentDays extends SameDays {

    @Override
    public boolean validate(Instance instance, Collection<int[]> solution) {
        return !super.validate(instance, solution);
    }
}
