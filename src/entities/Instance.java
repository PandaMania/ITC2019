package entities;

import entities.course.Course;
import entities.course.CourseClass;
import entities.distribution.Distribution;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class Instance {
    public List<Course> courses = new ArrayList<>();
    public List<Student> students = new ArrayList<>();
    public List<Room> rooms = new ArrayList<>();
    public List<Distribution> distributions = new ArrayList<>();
    public HashMap<Set<Integer>, Integer> distances = new HashMap<>();
    public String name;
    public int days;
    public int slotsPerDay;
    public int weeks;
    public int optStudent;
    public int optDist;
    public int optRoom;
    public int optTime;

    public Stream<CourseClass> getClasses() {
        return courses.stream()
                .flatMap(course -> course.configs.stream())
                .flatMap(courseConfiguration -> courseConfiguration.subparts.stream())
                .flatMap(subPart -> subPart.classes.stream());
    }

    private List<CourseClass> flattened = null;
    public CourseClass getClassForId(int id){
        if (flattened == null) {
            flattened = getClasses().collect(Collectors.toList());
        }
        return flattened.get(id);
    }

    @Override
    public String toString() {
        return "Instance{" +
                courses.size() +        " courses, " +
                students.size() +       " students, " +
                rooms.size() +          " rooms, " +
                distributions.size() +  " distributions" +
                '}';
    }
}
