package entities.distribution;

import entities.Instance;
import entities.Solution;

import java.util.Collection;

public class DifferentRoom extends SameRoom {

    @Override
    public boolean validate(Instance instance, Solution solution) {
        return !super.validate(instance, solution);
    }
}
