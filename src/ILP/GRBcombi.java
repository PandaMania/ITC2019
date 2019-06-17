package ILP;

import entities.course.CourseClass;
import entities.course.CourseTime;
import gurobi.GRBVar;

public class GRBcombi {

    GRBVar grbVar;
    int room;
    CourseTime courseTime;
    CourseClass courseClass;
    String name;

    public GRBcombi( GRBVar GRBvar, int room, CourseTime courseTime, CourseClass courseClass){
        this.courseClass= courseClass;
        this.courseTime= courseTime;
        this.room= room;
        this.grbVar= GRBvar;
        this.name= "id " + courseClass.id + "weeks " + courseTime.weeks + "days " + courseTime.days + "start " + courseTime.start + "length" + courseTime.length + "room " + room ;
    }

    public GRBVar getGrbVar() {
        return grbVar;
    }

    public String getName() {
        return name;
    }

    public CourseClass getCourseClass() {
        return courseClass;
    }

    public CourseTime getCourseTime() {
        return courseTime;
    }

    public int getRoom() {
        return room;
    }
}
