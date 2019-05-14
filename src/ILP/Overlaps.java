package ILP;

import entities.course.Course;
import entities.course.CourseTime;
import gurobi.*;


import java.util.ArrayList;
import java.util.BitSet;

public class Overlaps {

    public void computeAllOverlaps(ArrayList<roomTimePairs> times, GRBModel model){
    if(times!=null) {

        for(int j=0; j<times.size();j++) {
            for(int k=0; k<times.size(); k++){
                if(times.get(j)!=null){
                    orderbystart(times.get(j).grBtimeObject);
                    for (int i = 0; i < times.get(j).grBtimeObject.size(); i++) {
                        getforX(times.get(k).grBtimeObject, times.get(j).grBtimeObject.get(i), i+1, times.get(k).grBroomObject, times.get(j).grBroomObject.get(0), model);
                     //   getforX(times.get(j).grBtimeObject, times.get(j).grBtimeObject.get(i), i + 1);

                    }
                }
            }


        }

    }



    }




    public ArrayList<OverlappingPair> getforX(ArrayList<GRBtimeObject> times, GRBtimeObject X, int positionY){
        ArrayList<OverlappingPair> ListX= new ArrayList<OverlappingPair>();
        Boolean keepSearch=true;
        int StartX= X.time.start;
        int endX= StartX+ X.time.length;
        GRBLinExpr overlapConstraint;
        while(keepSearch){
            if(positionY>=times.size()-1){
                break;
            }
            if(times.get(positionY).courseClass!=X.courseClass){
                if(times.get(positionY).getTime().start < endX){
                    overlapConstraint= new GRBLinExpr();
                    overlapConstraint.addTerm(1, times.get(positionY).getGrbVar());
                    overlapConstraint.addTerm(1,X.getGrbVar());

                   // overlapConstraint.addTerm(1,);
                    
                    //ListX.add(new OverlappingPair(X, times.get(positionY)));
                }else
                    keepSearch=false;

            }
            positionY++;



        }
        return ListX;
    }

    public void getforX(ArrayList<GRBtimeObject> times, GRBtimeObject X, int positionY, ArrayList<GRBroomObject> grBroomObject, GRBroomObject grbVar, GRBModel model){
       // System.out.println(grBroomObject);
        ArrayList<OverlappingPair> ListX= new ArrayList<OverlappingPair>();
        Boolean keepSearch=true;
        int StartX= X.time.start;
        int endX= StartX+ X.time.length;
        GRBLinExpr overlapConstraint;
        int curr=0;
        while(keepSearch){

            if(positionY>=times.size()){
              //  System.out.println("curr= " + curr);
                break;
            }
            CourseTime i= times.get(positionY).time;
            CourseTime j= X.time;
          //  if(times.get(positionY).courseClass!=X.courseClass){
            BitSet a= BitSet.valueOf(i.days.getBytes());
            a.and(BitSet.valueOf(j.days.getBytes()));

            BitSet b= BitSet.valueOf(i.weeks.getBytes());
            b.and(BitSet.valueOf(j.weeks.getBytes()));
            int i_end = i.start+i.length;
            int j_end = j.start+j.length;


            if(a.cardinality()!=0) {
                if (b.cardinality() != 0) {
                    if (i_end >= j.start) {
                        if (j_end >= i.start) {
                            if(i.weeks== "111111111" && j.weeks=="111111111" && i.days =="1111000" && i.start==192 && i.length== 22 && j.days =="1111000" && j.start==192 && j.length== 22){
                                System.out.println(" look here you piece of shit " + grbVar.room + " " + grBroomObject.get(0).room);
                            }
                            curr++;

                            overlapConstraint = new GRBLinExpr();
                            overlapConstraint.addTerm(1, times.get(positionY).grbVar);
                            overlapConstraint.addTerm(1, X.grbVar);
                            overlapConstraint.addTerm(1, grbVar.grbVar);
                            overlapConstraint.addTerm(1, grBroomObject.get(0).grbVar);
                            try {
                                model.addConstr(overlapConstraint, GRB.LESS_EQUAL, 3, "Overlap" +" first" + times.get(positionY).toString() + " second " +   X.toString() + " room "+ grbVar.room + " other room " +grBroomObject.get(0).room  );
                            } catch (GRBException e) {
                                System.out.println("Error code: " + e.getErrorCode() + ". " +
                                        e.getMessage());
                            }
                        }
                    }
                }else{
                    System.out.println("fuck you");
                }
                                //ListX.add(new OverlappingPair(X, times.get(positionY)));
            }
            if(i.start== j.start & i.length== j.length ) {
                overlapConstraint = new GRBLinExpr();
                overlapConstraint.addTerm(1, times.get(positionY).grbVar);
                overlapConstraint.addTerm(1, X.grbVar);
                overlapConstraint.addTerm(1, grbVar.grbVar);
                overlapConstraint.addTerm(1, grBroomObject.get(0).grbVar);
                try {
                    model.addConstr(overlapConstraint, GRB.LESS_EQUAL, 3, "Overlap" + " first" + times.get(positionY).toString() + " second " + X.toString() + " room " + grbVar.room + " other room " + grBroomObject.get(0).room);
                } catch (GRBException e) {
                    System.out.println("Error code: " + e.getErrorCode() + ". " +
                            e.getMessage());
                }
            }

          //  }
            positionY++;



        }

    }

    public ArrayList<GRBtimeObject> orderbystart(ArrayList<GRBtimeObject> whole){
        //   ArrayList<GRBtimeObject> order= new ArrayList<GRBtimeObject>();
        ArrayList<GRBtimeObject> left = new ArrayList<GRBtimeObject>();
        ArrayList<GRBtimeObject> right = new ArrayList<GRBtimeObject>();
        int center;

        if (whole.size() == 1) {
            return whole;
        } else {
            center = whole.size()/2;
            // copy the left half of whole into the left.
            for (int i=0; i<center; i++) {
                left.add(whole.get(i));
            }

            //copy the right half of whole into the new arraylist.
            for (int i=center; i<whole.size(); i++) {
                right.add(whole.get(i));
            }

            // Sort the left and right halves of the arraylist.
            left  = orderbystart(left);
            right = orderbystart(right);

            // Merge the results back together.
            merge(left, right, whole);
        }
        return whole;

    }
    private void merge(ArrayList<GRBtimeObject> left, ArrayList<GRBtimeObject> right, ArrayList<GRBtimeObject> whole) {
        int leftIndex = 0;
        int rightIndex = 0;
        int wholeIndex = 0;

        // As long as neither the left nor the right ArrayList has
        // been used up, keep taking the smaller of left.get(leftIndex)
        // or right.get(rightIndex) and adding it at both.get(bothIndex).
        while (leftIndex < left.size() && rightIndex < right.size()) {
            if ( (left.get(leftIndex).time.start<(right.get(rightIndex).time.start))) {
                whole.set(wholeIndex, left.get(leftIndex));
                leftIndex++;
            } else {
                whole.set(wholeIndex, right.get(rightIndex));
                rightIndex++;
            }
            wholeIndex++;
        }

        ArrayList<GRBtimeObject> rest;
        int restIndex;
        if (leftIndex >= left.size()) {
            // The left ArrayList has been use up...
            rest = right;
            restIndex = rightIndex;
        } else {
            // The right ArrayList has been used up...
            rest = left;
            restIndex = leftIndex;
        }

        // Copy the rest of whichever ArrayList (left or right) was not used up.
        for (int i=restIndex; i<rest.size(); i++) {
            whole.set(wholeIndex, rest.get(i));
            wholeIndex++;
        }
    }

}
