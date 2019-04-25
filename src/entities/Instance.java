package entities;

import entities.course.Course;
import entities.distribution.Distribution;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Set;

public class Instance {
    public List<Course> courses = new ArrayList<>();
    public List<Student> students = new ArrayList<>();
    public List<Room> rooms = new ArrayList<>();
    public List<Distribution> distributions = new ArrayList<>();
    public HashMap<Set<Integer>, Integer> distances = new HashMap<>();
}
