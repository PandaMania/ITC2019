package entities.distribution;

import entities.Instance;

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
    public boolean validate(Instance instance, Collection<int[]> solution) {
        return false;
    }
}
