package entities.distribution;

import entities.Instance;
import entities.Solution;

import java.util.Collection;

public class NotOverlap extends Distribution {
    @Override
    public boolean validate(Instance instance, Solution solution) {
        return false;
    }
}
