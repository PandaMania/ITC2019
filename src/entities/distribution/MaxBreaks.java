package entities.distribution;

import entities.Instance;
import entities.Solution;

import java.util.Collection;

public class MaxBreaks extends Distribution {
    int R;
    int S;

    public MaxBreaks(String params) {
        String[] split = params.split(",");
        R = Integer.parseInt(split[0]);
        S = Integer.parseInt(split[1]);
    }

    @Override
    public boolean validate(Instance instance, Solution solution) {
        return false;
    }
}
