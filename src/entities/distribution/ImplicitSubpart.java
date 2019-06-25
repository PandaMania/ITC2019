package entities.distribution;

import entities.Instance;
import entities.Solution;
import entities.SolutionStudent;
import entities.Student;
import entities.course.Course;
import entities.course.CourseClass;
import entities.course.CourseConfiguration;
import entities.course.SubPart;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

// All students must attend at least one lecture in each subpart of a config of a course
public class ImplicitSubpart extends ImplicitDistribution {

    public ImplicitSubpart() {
        super();
        type = "ImplicitSubPart";
    }

    private int mult = 1;

    @Override
    public boolean validate(Instance instance, Solution solution) {
        exceededBy = 0;
        return forAll(instance.students, student -> {
            List<Course> courses = instance.courses.stream().filter(c -> student.courses.contains(c.id)).collect(Collectors.toList());
            // for all courses a student wants to attend
            for(Course course : courses){
                // for every possible configuration of that course
                if(!inAConfig(course, student, solution)){
//                    exceededBy++;
                    return false;
                }
            }
            return true;
        }, true);
    }

    private boolean inAConfig(Course course, Student student, Solution solution) {
        int counts = 0;
        for (CourseConfiguration config : course.configs) {
            if(inAllSubparts(config, student, solution)){
                counts++;
                if(counts>1){
                    return false;
                }
            }
        }
        return counts == 1;
    }

    private boolean inAllSubparts(CourseConfiguration config, Student student, Solution solution) {
        // for all subparts of that configuration
        int violated = 0;
        for (SubPart subpart : config.subparts) {
            // for all classes in that subpart
            boolean in = inAtLeastOneClass(subpart, student, solution);
            if(!in){
                violated++;
            }
        }
        exceededBy += violated;
        return violated == 0;
//        return true;
    }

    /**
     * Checks whether a student is scheduled in at least one of the classes for a subpart
     * @param subpart
     * @param student
     * @param solution
     * @return
     */
    private boolean inAtLeastOneClass(SubPart subpart, Student student, Solution solution) {
        int counts = 0;
        for (CourseClass aClass : subpart.classes) {
            for (SolutionStudent solutionStudent : solution.getClassForId(aClass.id).students) {
                if(solutionStudent.id == student.id){
                    counts++;
                    if(counts > 1){
                        return false;
                    }
                }
            }
        }
        return counts == 1;
    }
}
