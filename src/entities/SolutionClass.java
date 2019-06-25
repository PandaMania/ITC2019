package entities;

import entities.course.CourseClass;
import util.BitSets;

import java.util.ArrayList;
import java.util.BitSet;

public class SolutionClass {

    public ArrayList<SolutionStudent> students;
    public BitSet weeks;
    public BitSet days;
    public int classId;
    public int roomId;
    public int start;

    // Needed for some of the constraint checking. Not actually needed in the solution!
    public int length;

    public SolutionClass(int nStudents, int nWeeks, int nDays) {
        this.students = new ArrayList<>(nStudents);
        this.weeks = new BitSet(nWeeks);
        this.days = new BitSet(nDays);
    }

    public SolutionClass() {
        this.students = new ArrayList<>();
        this.weeks = new BitSet();
        this.days = new BitSet();
    }

    // Copy Constructor
    public SolutionClass(SolutionClass aClass) {
        students = new ArrayList<>();
        for (SolutionStudent oldStudent : aClass.students) {
            SolutionStudent newStudent = new SolutionStudent(oldStudent);
            students.add(newStudent);
        }
        this.weeks = (BitSet) aClass.weeks.clone();
        this.days = (BitSet) aClass.days.clone();
        this.classId = aClass.classId;
        this.roomId = aClass.roomId;
        this.start = aClass.start;
        this.length = aClass.length;
    }

    public static String serializeOther(CourseClass courseClass, int numDays, int numWeeks) {
        return String.format("<class id=\"%d\" days=\"%s\" start=\"%d\" weeks=\"%s\" room=\"%d\"></class>",
                courseClass.id, zeros(numDays),0, zeros(numWeeks),1);
    }

    private static String zeros(int n){
        StringBuilder b = new StringBuilder(n);
        for (int i = 0; i < n; i++) {
            b.append('0');
        }
        return b.toString();
    }

    // <class id="1" days="1010100" start="90" weeks="1111111111111" room="1">
    //		<student id="1"/>
    //		<student id="3"/>
    //	</class>
    public String serialize(int numDays, int numWeeks){
        // TODO: add student serialization
        return String.format("<class id=\"%d\" days=\"%s\" start=\"%d\" weeks=\"%s\" %s></class>",
                classId, BitSets.toBitString(days, numDays), start, BitSets.toBitString(weeks, numWeeks), getRoomId());
    }

    public String getRoomId() {
        return roomId != -1 ? String.format("room=\"%s\"",Integer.toString(roomId)) : "";
    }
}
