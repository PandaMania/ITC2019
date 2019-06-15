package entities;

import entities.course.Course;
import entities.course.CourseClass;
import entities.distribution.Distribution;

import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class Instance {
    public List<Course> courses = new ArrayList<>();
    public List<Student> students = new ArrayList<>();
    public List<Room> rooms = new ArrayList<>();
    public List<Distribution> distributions = new ArrayList<>();
    public int[][] distances;
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
        return flattened.get(id-1);
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

    // TODO: maybe we can optimize this by using the index as we are reading them in order.
    public Room getRoom(int assignedRoom) {
        Optional<Room> first = rooms.stream().filter(r -> r.id == assignedRoom).findFirst();
        if (first.isPresent()) {
            return first.get();
        }
        throw new IllegalArgumentException("No room for id: "+assignedRoom);
    }
}
