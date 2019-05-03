package entities.distribution;

import entities.Instance;
import entities.Solution;

import java.util.Collection;

public class MaxDayLoad extends Distribution {

    int S;

    public MaxDayLoad(String s) {
        S = Integer.parseInt(s);
    }

    @Override
    public boolean validate(Instance instance, Solution solution) {
        return false;
    }
}
