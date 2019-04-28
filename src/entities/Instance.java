package entities;

import entities.course.Course;
import entities.course.CourseClass;
import entities.distribution.Distribution;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Set;
import java.util.stream.Stream;

public class Instance {
    public List<Course> courses = new ArrayList<>();
    public List<Student> students = new ArrayList<>();
    public List<Room> rooms = new ArrayList<>();
    public List<Distribution> distributions = new ArrayList<>();
    public HashMap<Set<Integer>, Integer> distances = new HashMap<>();

    public Stream<CourseClass> getClasses() {
        return courses.stream()
                .flatMap(course -> course.configs.stream())
                .flatMap(courseConfiguration -> courseConfiguration.subparts.stream())
                .flatMap(subPart -> subPart.classes.stream());
    }
}
