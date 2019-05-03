package entities.distribution;

import entities.Instance;
import entities.Solution;

import java.util.Collection;

public class MinGap extends Distribution {

    int G;

    public MinGap(String g) {
        G = Integer.parseInt(g);
    }

    @Override
    public boolean validate(Instance instance, Solution solution) {
        return false;
    }
}
