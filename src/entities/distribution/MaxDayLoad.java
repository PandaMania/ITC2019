package entities.distribution;

import entities.Instance;
import entities.Solution;
import entities.SolutionClass;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

public class MaxDayLoad extends ExceedableDistribution {

    private int S;
    public MaxDayLoad(String s) {
        S = Integer.parseInt(s);
    }

    @Override
    public boolean validate(Instance instance, Solution solution) {
        List<SolutionClass> classes = getClassInDistribution(solution);
        // TODO: Optimize to only use w and d that are actually assigned in the classes
        exceededBy = 0;
        nrWeeks = instance.weeks;
        for (int w = 0; w < instance.weeks; w++) {
            for (int d = 0; d < instance.days; d++) {
                int load = DayLoad(d, w, classes);
                boolean holds = load <= S;
                if (!holds) {
                    exceededBy += load - S;
                }
            }
        }
        return exceededBy == 0;
    }

    private int DayLoad(int d, int w, List<SolutionClass> classes) {
        int load = 0;
        for (SolutionClass C : classes) {
            if (C.weeks.get(w) && C.days.get(d)) {
                load += C.length;
            }
        }
        return load;
    }
}
