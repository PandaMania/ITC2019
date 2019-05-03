package entities.distribution;

import entities.Instance;
import entities.Solution;
import entities.SolutionClass;
import entities.course.CourseClass;

import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;

public class DifferentTime extends Distribution {
    @Override
    public boolean validate(Instance instance, Solution solution) {
        List<SolutionClass> solutionClasses = getClassInDistribution(solution);

        return forAny(solutionClasses, (i,j)->
        {
            int i_end = i.start + i.length;
            int j_end = j.start + j.length;
            return (i_end <= j.start) || (j_end <= i.start);
        });
    }
}
