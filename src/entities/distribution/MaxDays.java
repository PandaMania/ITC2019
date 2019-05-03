package entities.distribution;

import entities.Instance;
import entities.Solution;

import java.util.Collection;

public class MaxDays extends Distribution {

    int D;

    public MaxDays(String d) {
        D = Integer.parseInt(d);
    }

    @Override
    public boolean validate(Instance instance, Solution solution) {
        return false;
    }
}
