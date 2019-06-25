package entities.distribution;


import entities.Instance;
import entities.Solution;
import entities.SolutionClass;
import entities.SolutionStudent;
import entities.course.Course;
import entities.course.CourseClass;

import java.util.Optional;

// If a class has a parent. That parent has to be scheduled
public class ImplicitParentClass extends ImplicitDistribution {
    public ImplicitParentClass() {
        super();
        type = "ImplicitParentClass";
    }
int mult = 1;
    @Override
    public boolean validate(Instance instance, Solution solution) {
        exceededBy = 0;
        return forAll(solution.classes, C->{
            //TODO FIX
            CourseClass courseClass = instance.getClassForId(C.classId);
            if (courseClass.parentId != null) {
                SolutionClass parentClass = solution.getClassForId(courseClass.parentId);

                int found = 0;
                for (SolutionStudent student : C.students) {
                    // id of the student is in the parent class?
                    if (!parentClass.students.stream().anyMatch(pc->pc.id==student.id)) {
                        // We found a student that is in the base class and not in the parent
                        found++;
                    }
                }
                exceededBy += found;
                return found == 0;
            }
            // class has no parent class
            return true;
        }, true);
    }
}
