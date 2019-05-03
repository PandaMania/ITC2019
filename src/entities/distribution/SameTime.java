package entities.distribution;

import entities.Instance;
import entities.Solution;
import entities.SolutionClass;
import entities.course.CourseClass;

import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;

public class SameTime extends Distribution {
    @Override
    public boolean validate(Instance instance, Solution solution) {

        List<SolutionClass> solutionClasses = getClassInDistribution(solution);

        return forAny(solutionClasses, (C_i, C_j) -> {
            int I_end = C_i.start + C_i.length;
            int J_end = C_i.start + C_i.length;
            return (C_i.start <= C_j.start && J_end < I_end) || (C_j.start <= C_i.start && I_end <= J_end);
        });
    }
}
