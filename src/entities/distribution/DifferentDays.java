package entities.distribution;

import entities.Instance;
import entities.Solution;

import java.util.Collection;

public class DifferentDays extends SameDays {

    @Override
    public boolean validate(Instance instance, Solution solution) {
        return !super.validate(instance, solution);
    }
}
