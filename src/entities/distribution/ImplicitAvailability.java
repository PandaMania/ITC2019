package entities.distribution;

import entities.*;
import entities.course.CourseClass;
import util.BitSets;

public class ImplicitAvailability extends ImplicitDistribution {

    public ImplicitAvailability() {
        super();
        type = "ImplicitAvailability";
    }

    /**
     * A class cannot be placed in a room when its assigned time overlaps with an unavailability of the room
     *
     * @param instance
     * @param solution
     * @return
     */
    @Override
    public boolean validate(Instance instance, Solution solution) {
        exceededBy = 0;
        return forAll(solution.classes, (C) -> {

            Boolean available = available(instance, C);
            if (!available) {
                exceededBy++;
            }
            return available;
        }, true);
    }

    protected Boolean available(Instance instance, SolutionClass C) {
        CourseClass courseClass = instance.getClassForId(C.classId);
        if(!courseClass.roomNeeded){
            return true; // No room needed
        }
        int assignedRoom = C.roomId;

        Room r;
        try {
            r = instance.getRoom(assignedRoom);
        } catch (IllegalArgumentException e) {
            // Assigned Room does not exist
            return false;
        }
        // If any of the unavailable regions overlap with the assigned time return false
        for (Unavailability U : r.unaivailableweeks) {
            if(overlaps(C, U)){
                return false;
            }
        }
        return true;
    }

    public static boolean overlaps(SolutionClass C, Unavailability U){
        if (BitSets.and(C.weeks, U.weeks).cardinality() > 0 &&  // overlapping weeks
                BitSets.and(C.days, U.days).cardinality() > 0) {   // Overapping days
            if (C.start + C.length <= U.start) {
                        return false;
            } else if (U.start + U.length <= C.start) {
                        return false;
            } else if ((U.start >= C.start && U.start + U.length <= C.start + C.length) ||
                    (C.start >= U.start && C.start + C.length <= U.start + U.length)) {
                return true;
            }
            else{
                return true;
            }
        }
        return false;
    }
}
