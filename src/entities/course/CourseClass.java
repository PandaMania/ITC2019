package entities.course;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class CourseClass{
    public String id;
    public int limit;
    public boolean roomNeeded;
    // TODO: Maybe use entities.course.CourseClass instead
    public String parentId;
    // Room Id's to penalties
    // TODO: Maybe use Map<Room, Integer> instead?
    public Map<Integer, Integer> roomPenalties = new HashMap<>();
    public ArrayList<CourseTime> times = new ArrayList<>();
}
