package entities.distribution;

import entities.Instance;

import java.util.Collection;

public class SameWeeks extends Distribution {
    @Override
    public boolean validate(Instance instance, Collection<int[]> solution) {
        return false;
    }
}
