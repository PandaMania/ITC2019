package entities.distribution;


import entities.Instance;
import entities.Solution;
import entities.SolutionClass;
import entities.course.CourseClass;

import java.util.Optional;

// If a class has a parent. That parent has to be scheduled
public class ImplicitParentClass extends ImplicitDistribution {
    public ImplicitParentClass() {
        super();
        type = "ImplicitParentClass";
    }
int mult = 5;
    @Override
    public boolean validate(Instance instance, Solution solution) {
        exceededBy = 0;
        return forAll(solution.classes, C->{
            CourseClass courseClass = instance.getClassForId(C.classId);
            // if the parent is defined, then it must be scheduled
            if (courseClass.parentId != null) {
                Optional<SolutionClass> first = solution.classes.stream().filter(cl -> cl.classId == Integer.parseInt(courseClass.parentId)).findFirst();
                if(!first.isPresent()){
                    exceededBy += 1*mult;
                    return false;
                }
            }
            return true;
        }, true);
    }
}
