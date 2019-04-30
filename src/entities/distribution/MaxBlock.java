package entities.distribution;

import entities.Instance;

import java.util.Collection;

public class MaxBlock extends Distribution {

    int M;
    int S;

    public MaxBlock(String params) {
        String[] split = params.split(",");
        M = Integer.parseInt(split[0]);
        S = Integer.parseInt(split[1]);
    }

    @Override
    public boolean validate(Instance instance, Collection<int[]> solution) {
        return false;
    }
}
