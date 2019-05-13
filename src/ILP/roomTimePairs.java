package ILP;

import java.util.ArrayList;

public class roomTimePairs {
    ArrayList<GRBroomObject> grBroomObject;
    ArrayList<GRBtimeObject> grBtimeObject;
    public roomTimePairs(ArrayList<GRBtimeObject> grbtimeObject, ArrayList<GRBroomObject> grBroomObject ){
        this.grBroomObject= grBroomObject;
        this.grBtimeObject= grbtimeObject;

    }

    public ArrayList<GRBroomObject> getGrBroomObject() {
        return grBroomObject;
    }


    public ArrayList<GRBtimeObject> getGrBtimeObject() {
        return grBtimeObject;
    }

}
