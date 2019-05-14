package ILP;

import entities.course.Course;
import entities.course.CourseClass;
import entities.course.CourseTime;
import gurobi.GRBVar;

public class GRBtimeObject {
    CourseTime time;
    GRBVar grbVar;
    CourseClass courseClass;

    public GRBtimeObject(GRBVar grbVar, CourseTime time, CourseClass courseClass){
        this.time= time;
        this.grbVar= grbVar;
        this.courseClass= courseClass;

    }

    public CourseClass getCourseClass() {
        return courseClass;
    }

    public CourseTime getTime() {
        return time;
    }

    public GRBVar getGrbVar() {
        return grbVar;
    }

    public void setGrbVar(GRBVar grbVar) {
        this.grbVar = grbVar;
    }

    public void setTime(CourseTime time) {
        this.time = time;
    }

    @Override
    public String toString() {
        String toprint= "weeks= " + time.weeks + " days= " + time.days + " start= " + time.start + " duration= " + time.length;
        return toprint;
    }
}
