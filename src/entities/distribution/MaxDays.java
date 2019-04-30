package entities.distribution;

import entities.Instance;

import java.util.Collection;

public class MaxDays extends Distribution {

    int D;

    public MaxDays(String d) {
        D = Integer.parseInt(d);
    }

    @Override
    public boolean validate(Instance instance, Collection<int[]> solution) {
        return false;
    }
}
