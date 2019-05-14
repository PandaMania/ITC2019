package entities;

import util.BitSets;

import java.util.ArrayList;
import java.util.BitSet;

public class SolutionClass {

    public BitSet students;
    public BitSet weeks;
    public BitSet days;
    public int classId;
    public int roomId;
    public int start;

    // Needed for some of the constraint checking. Not actually needed in the solution!
    public int length;

    public SolutionClass(int nStudents, int nWeeks, int nDays) {
        this.students = new BitSet(nStudents);
        this.weeks = new BitSet(nWeeks);
        this.days = new BitSet(nDays);
    }

    // Copy Constructor
    public SolutionClass(SolutionClass aClass) {
        this.students = (BitSet) aClass.students.clone();
        this.weeks = (BitSet) aClass.weeks.clone();
        this.days = (BitSet) aClass.days.clone();
        this.classId = aClass.classId;
        this.roomId = aClass.roomId;
        this.start = aClass.start;
    }

    // <class id="1" days="1010100" start="90" weeks="1111111111111" room="1">
    //		<student id="1"/>
    //		<student id="3"/>
    //	</class>
    public String serialize(){
        // TODO: add student serialization
        return String.format("<class id=\"%d\" days=\"%s\" start=\"%d\" weeks=\"%s\" room=\"%d\"></class>",
                classId, BitSets.toBitString(days), start, BitSets.toBitString(weeks), roomId);
    }
}
