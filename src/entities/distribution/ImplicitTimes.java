package entities.distribution;

import entities.Instance;
import entities.Solution;
import entities.SolutionClass;
import entities.course.CourseClass;
import entities.course.CourseTime;
import util.BitSets;

public class ImplicitTimes extends ImplicitDistribution {


    public ImplicitTimes() {
        super();
        type = "ImplicitTimes";
    }

    /**
     * Each Class has a set of times in which it can be scheduled.
     *
     * @param instance
     * @param solution
     * @return
     */
    @Override
    public boolean validate(Instance instance, Solution solution) {
        exceededBy=0;
        return forAll(solution.classes, C -> {
            boolean valid = validTime(instance, C);
            if (!valid) {
                exceededBy++;
            }
            return valid;
        },true);
    }

    protected Boolean validTime(Instance instance, SolutionClass C) {
        CourseClass courseClass = instance.getClassForId(C.classId);
        for (CourseTime time : courseClass.times) {
            // The courseTime should completely subsume the assigned time
            if (BitSets.and(time.days, C.days).cardinality() > 0 &&      // Same days
                    BitSets.and(time.weeks, C.weeks).cardinality() > 0 &&   // Same weeks
                    C.start >= time.start &&                                                // Starts within available time
                    C.start + C.length <= time.start + time.length                          // Ends within available time
            ) {
                return true;
            }
        }
        return false;
    }
}
