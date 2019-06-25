package entities;

import java.util.ArrayList;

public class SolutionStudent {
    public int id;
    public ArrayList<Integer> courses;


    public SolutionStudent(SolutionStudent oldStudent) {
        this.id = oldStudent.id;
    }

    public SolutionStudent() {

    }

    public SolutionStudent(Student s){
        id = s.id;
        courses = s.courses;
    }
}
