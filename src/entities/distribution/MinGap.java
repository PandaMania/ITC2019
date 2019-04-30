package entities.distribution;

import entities.Instance;

import java.util.Collection;

public class MinGap extends Distribution {

    int G;

    public MinGap(String g) {
        G = Integer.parseInt(g);
    }

    @Override
    public boolean validate(Instance instance, Collection<int[]> solution) {
        return false;
    }
}
