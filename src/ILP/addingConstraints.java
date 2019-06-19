package ILP;

import Parsing.InstanceParser;
import entities.Instance;
import entities.course.Course;
import entities.course.CourseTime;
import entities.distribution.Distribution;
import gurobi.*;
import util.BitSets;


import java.util.ArrayList;
import java.util.BitSet;
import java.util.HashMap;
import java.util.List;

public class addingConstraints {


    FindSameAttendees findSameAttendees= new FindSameAttendees();



    public void computeAllOverlaps(ArrayList<roomTimePairs> times, GRBModel model,ArrayList<ArrayList<Integer>> sameAttendance, Instance instance ){
    if(times!=null) {

        for(int j=0; j<times.size();j++) {
            for(int k=0; k<times.size(); k++){
                if(times.get(j)!=null){
                    orderbystart(times.get(j).grBtimeObject);
                    for (int i = 0; i < times.get(j).grBtimeObject.size(); i++) {
                        OverlapwithX(times.get(k).grBtimeObject, times.get(j).grBtimeObject.get(i), i+1, times.get(k).grBroomObject, times.get(j).grBroomObject.get(0), model, sameAttendance, instance);
                     //   getforX(times.get(j).grBtimeObject, times.get(j).grBtimeObject.get(i), i + 1);

                    }
                }
            }


        }

    }



    }





    public void OverlapwithX(ArrayList<GRBtimeObject> times, GRBtimeObject X, int positionY, ArrayList<GRBroomObject> grBroomObject, GRBroomObject grbVar, GRBModel model, ArrayList<ArrayList<Integer>> sameAttendance, Instance instance){
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
//            if(times.get(positionY).courseClass!=X.courseClass){
//            BitSet a= BitSet.valueOf(i.days.getBytes());
//            a.and(BitSet.valueOf(j.days.getBytes()));
            BitSet a = BitSets.and(i.days,j.days);

//            BitSet b= BitSet.valueOf(i.weeks.getBytes());
//            b.and(BitSet.valueOf(j.weeks.getBytes()));
            BitSet b = BitSets.and(i.weeks,j.weeks);
            int i_end = i.start+i.length;
            int j_end = j.start+j.length;

//            if(i.start == j.start && i.start == 96){
//                System.out.println("reached test case");
//            }
            if(a.cardinality()!=0) {
                if (b.cardinality() != 0) {
                    if (i_end >= j.start) {
                        if (j_end >= i.start) {
//                            if(i.weeks== "111111111" && j.weeks=="111111111" && i.days =="1111000" && i.start==192 && i.length== 22 && j.days =="1111000" && j.start==192 && j.length== 22){
//                                System.out.println(" look here you piece of shit " + grbVar.room + " " + grBroomObject.get(0).room);
//                            }
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
                    if(sameAttendance!=null){
                        for(int x=0; x<sameAttendance.size(); x++){
                            for(int y=0; y<sameAttendance.get(x).size(); y++){
                                for(int k=0; k<sameAttendance.get(x).size(); k++){
                                    if(times.get(positionY).courseClass.id == (sameAttendance.get(x).get(y)) && X.courseClass.id==(sameAttendance.get(x).get(y)) ){
                                        // now do the checkif
                                        if(i_end+ instance.distances[times.get(positionY).courseClass.id][X.courseClass.id]<=X.time.start ||
                                                j_end + instance.distances[X.courseClass.id][times.get(positionY).courseClass.id]<= times.get(positionY).time.start
                                                ){
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
                                            }}

                                    }
                                }
                            }
                    }

                    }
                    if(i.start== j.start ) {
//                        if(i.weeks== "111111111" && j.weeks=="111111111" && i.days =="1111000" && i.start==192 && i.length== 22 && j.days =="1111000" && j.start==192 && j.length== 22){
//                            System.out.println(" look here you piece of shit " + grbVar.room + " " + grBroomObject.get(0).room);
//                        }
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

                }
                                //ListX.add(new OverlappingPair(X, times.get(positionY)));
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


    public HashMap<String, ArrayList<ArrayList<Integer>>> dealwithDistributions(List<Distribution> distributions, Instance instance, String name){
       HashMap<String, ArrayList<ArrayList<Integer>>> toreturn= new HashMap<>();
        ArrayList<ArrayList<Integer>> workingwith= new ArrayList<>();
        for(int i=0; i<instance.distributions.size();i++){
            String current= instance.distributions.get(i).type;
            if(current== name){
                if(toreturn.containsKey(current)){
                   workingwith= toreturn.get(current);
                   workingwith.add(instance.distributions.get(i).idInDistribution);
                   toreturn.replace(current,workingwith);
                }else{
                    workingwith.add( instance.distributions.get(i).idInDistribution);
                    toreturn.put(current, workingwith);
                }



            }else if(current== "DifferentDays"){

            }





        }
        return toreturn;
    }

}
