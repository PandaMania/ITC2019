package entities.distribution;

import entities.Instance;

import java.util.Collection;

public class WorkDay extends Distribution {

    int S;

    public WorkDay(String s) {
        S = Integer.parseInt(s);
    }

    @Override
    public boolean validate(Instance instance, Collection<int[]> solution) {
        return false;
    }
}
