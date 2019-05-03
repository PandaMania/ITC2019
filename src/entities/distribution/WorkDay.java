package entities.distribution;

import entities.Instance;
import entities.Solution;

import java.util.Collection;

public class WorkDay extends Distribution {

    int S;

    public WorkDay(String s) {
        S = Integer.parseInt(s);
    }

    @Override
    public boolean validate(Instance instance, Solution solution) {
        return false;
    }
}
