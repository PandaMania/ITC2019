package entities.distribution;

import entities.Instance;
import entities.Solution;
import entities.SolutionClass;
import entities.course.CourseClass;

public class ImplicitRooms extends ImplicitDistribution {


    public ImplicitRooms() {
        super();
        type = "ImplicitRooms";
    }
    /**
     * Each class has a list of rooms where it can be placed
     * @param instance
     * @param solution
     * @return
     */
    @Override
    public boolean validate(Instance instance, Solution solution) {
        exceededBy=0;
        return forAll(solution.classes, C -> {
            boolean valid = validRoom(instance, C);
            if(!valid){
                exceededBy++;
            }
            return valid;
        },true);
    }

    protected Boolean validRoom(Instance instance, SolutionClass C) {
        // get Class from instance
        int classId = C.classId;
        CourseClass courseClass = instance.getClassForId(classId);
        if (courseClass.roomNeeded) {
            // Check if assigned room is in the roomPenalties for class
            return courseClass.roomPenalties.keySet().contains(C.roomId);
        }
        return true;
    }
}
