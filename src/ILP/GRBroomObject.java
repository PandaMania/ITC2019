package ILP;

import entities.Room;
import gurobi.GRBVar;

public class GRBroomObject {
    GRBVar grbVar;
    int room;
    public GRBroomObject(GRBVar grbVar, int room){
        this.grbVar=grbVar;
        this.room= room;

    }

    public int getRoom() {
        return room;
    }

    public GRBVar getGrbVar() {
        return grbVar;
    }

    public void setGrbVar(GRBVar grbVar) {
        this.grbVar = grbVar;
    }

    public void setRoom(int room) {
        this.room = room;
    }

}
