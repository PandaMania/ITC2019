package ILP;

import entities.Instance;
import entities.Unavailability;
import entities.course.CourseTime;
import gurobi.GRB;
import gurobi.GRBException;
import gurobi.GRBLinExpr;
import gurobi.GRBModel;
import util.BitSets;

import java.util.ArrayList;
import java.util.BitSet;

public class combiOverlap {
    int counter=0;
    public void OverlapInARoom(ArrayList<GRBcombi> givenroom, Instance instance,  GRBModel model){

        if(givenroom!=null){
            ArrayList<GRBcombi> orderedByStart= orderbystart(givenroom);
            ArrayList<GRBcombi> orderedByEnd= orderbyend(givenroom);

            runThroughGeneral(orderedByStart, orderedByEnd, instance, model);

        }
        System.out.println("counter= " + counter);



    }

    public void runThroughGeneral(ArrayList<GRBcombi> orderedByStart, ArrayList<GRBcombi> orderedByEnd, Instance instance, GRBModel model ){
        // we need to run this for every week and every day
        // first loop weeks

        for(int i=0; i< instance.weeks; i++){
            for(int j=0; j<instance.days; j++){
                runThroughInd(orderedByStart,orderedByEnd,instance, j, i, model );
              //  System.out.println(orderedByStart.size());
            }
        }
    }

    public void runThroughInd(ArrayList<GRBcombi> orderedByStart, ArrayList<GRBcombi> orderedByEnd, Instance instance, int day, int week, GRBModel model) {


        int timer = 0;
        int orderstart = 0;
        int orderend = 0;
        boolean start = true;
        boolean increment=false;
        ArrayList<GRBcombi> toremoveStart= new ArrayList<>();
        ArrayList<GRBcombi> toremoveend= new ArrayList<>();

        ArrayList<GRBcombi> current = new ArrayList<>();

        for(int i=0; i<orderedByStart.size(); i++){
            BitSet a= fromString(orderedByStart.get(i).courseTime.weeks);
            if(a.get(week)){
                BitSet b= fromString(orderedByStart.get(i).courseTime.days);
                if(b.get(day)){
                    toremoveStart.add(orderedByStart.get(i));
                }
                toremoveStart.add(orderedByStart.get(i));
            }

        }



        for(int i=0; i<orderedByEnd.size(); i++){
            BitSet a= fromString(orderedByEnd.get(i).courseTime.weeks);
            if(a.get(week)){
                BitSet b= fromString(orderedByEnd.get(i).courseTime.days);
                if(b.get(day)){
                    toremoveend.add(orderedByEnd.get(i));
                }

            }

        }


        if(orderedByStart.size()!=0 && orderedByEnd.size()!=0) {
            if(orderedByEnd.size()!=orderedByStart.size()){
                System.out.println("something is very wrong");
            }
           // System.out.println(orderedByEnd.get(0).courseTime.weeks);




            while (timer <= instance.slotsPerDay) {

                while (increment == false) {
                    if (orderend < toremoveend.size() && (toremoveend.get(orderend).courseTime.start + toremoveend.get(orderend).courseTime.length) == timer && !start) {
                        current.remove(toremoveend.get(orderend));
                        orderend++;
                    } else if (orderstart < toremoveStart.size() && toremoveStart.get(orderstart).courseTime.start == timer && start) {
                        current.add(toremoveStart.get(orderstart));
                        orderstart++;
                        //System.out.println("first");



                    } else    if (orderstart < toremoveStart.size() && toremoveStart.get(orderstart).courseTime.start == timer && !start) {

                       // addconstraint(current, model);
                        current.add(toremoveStart.get(orderstart));
                        orderstart++;

                        //System.out.println("third");
                        start = true;
                    } else if (orderend < toremoveend.size() && (toremoveend.get(orderend).courseTime.start + toremoveend.get(orderend).courseTime.length) == timer && start) {
                        addconstraint(current, model);

                        current.remove(toremoveend.get(orderend));
                        //    System.out.println("after removing" + current.size());
                        orderend++;
                        start = false;




                        //System.out.println("fourth");
                    } else {
                        increment = true;

                    }
                    if (timer == 168 && toremoveStart.get(0).room == 20) {
                        for (int i = 0; i < current.size(); i++) {
                          //     System.out.println( current.get(i).getName());
                        }
                    }
                }

                increment = false;

                //System.out.println(timer);
                timer++;


            }
            if (current.size() > 0) {
              //  System.out.println("end of the line and stuff left");
                addconstraint(current, model);
              //  current= new ArrayList<GRBcombi>();
            }

            //  System.out.println("current size" + current.size());

            // System.out.println("next");
            // System.out.println(counter);
        }
    }

     public void addconstraint(ArrayList<GRBcombi> list,  GRBModel model){
        counter++;

         GRBLinExpr overlapConstraint = new GRBLinExpr();
         for(int i=0; i<list.size(); i++){

             overlapConstraint.addTerm(1, list.get(i).grbVar);

         } try {
             model.addConstr(overlapConstraint, GRB.LESS_EQUAL, 1, "c" + counter );

         } catch (GRBException e) {
             System.out.println("Error code: " + e.getErrorCode() + ". " +
                     e.getMessage());
         }



     }




    public ArrayList<GRBcombi> orderbyend(ArrayList<GRBcombi> whole){
        //   ArrayList<GRBtimeObject> order= new ArrayList<GRBtimeObject>();
        ArrayList<GRBcombi> left = new ArrayList<GRBcombi>();
        ArrayList<GRBcombi> right = new ArrayList<GRBcombi>();
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
            mergebyend(left, right, whole);
        }
        return whole;

    }
    private void mergebyend(ArrayList<GRBcombi> left, ArrayList<GRBcombi> right, ArrayList<GRBcombi> whole) {
        int leftIndex = 0;
        int rightIndex = 0;
        int wholeIndex = 0;

        // As long as neither the left nor the right ArrayList has
        // been used up, keep taking the smaller of left.get(leftIndex)
        // or right.get(rightIndex) and adding it at both.get(bothIndex).
        while (leftIndex < left.size() && rightIndex < right.size()) {
            if ( ((left.get(leftIndex).courseTime.start+left.get(leftIndex).courseTime.length)<(right.get(rightIndex).courseTime.start+ right.get(rightIndex).courseTime.length))) {
                whole.set(wholeIndex, left.get(leftIndex));
                leftIndex++;
            } else {
                whole.set(wholeIndex, right.get(rightIndex));
                rightIndex++;
            }
            wholeIndex++;
        }

        ArrayList<GRBcombi> rest;
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


    public ArrayList<GRBcombi> orderbystart(ArrayList<GRBcombi> whole){
        //   ArrayList<GRBtimeObject> order= new ArrayList<GRBtimeObject>();
        ArrayList<GRBcombi> left = new ArrayList<GRBcombi>();
        ArrayList<GRBcombi> right = new ArrayList<GRBcombi>();
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
            mergebystart(left, right, whole);
        }
        return whole;

    }
    private void mergebystart(ArrayList<GRBcombi> left, ArrayList<GRBcombi> right, ArrayList<GRBcombi> whole) {
        int leftIndex = 0;
        int rightIndex = 0;
        int wholeIndex = 0;

        // As long as neither the left nor the right ArrayList has
        // been used up, keep taking the smaller of left.get(leftIndex)
        // or right.get(rightIndex) and adding it at both.get(bothIndex).
        while (leftIndex < left.size() && rightIndex < right.size()) {
            if ( (left.get(leftIndex).courseTime.start<(right.get(rightIndex).courseTime.start))) {
                whole.set(wholeIndex, left.get(leftIndex));
                leftIndex++;
            } else {
                whole.set(wholeIndex, right.get(rightIndex));
                rightIndex++;
            }
            wholeIndex++;
        }

        ArrayList<GRBcombi> rest;
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
    public boolean singlecheck(ArrayList<Unavailability> first, CourseTime second) {
     boolean overlaps= false;
        if(first.size()==0){
            return overlaps;
        }




        for(int i=0; i<first.size(); i++){

            BitSet j_weeks;
            j_weeks= fromString(second.weeks);


            BitSet j_days;
            j_days= fromString(second.days);

            BitSet a = BitSets.and(first.get(i).days, j_days);

            BitSet b = BitSets.and(first.get(i).weeks, j_weeks);

            int i_end = first.get(i).start+first.get(i).length;
            int j_end = second.start+second.length;

            if(!(j_end<first.get(i).start || i_end< second.start || b.cardinality()==0 || a.cardinality()==0)){
                overlaps=true;
            }


        }
        return overlaps;


    }
    private static BitSet fromString(String binary) {
        BitSet bitset = new BitSet(binary.length());
        for (int i = 0; i < binary.length(); i++) {
            if (binary.charAt(i) == '1') {
                bitset.set(i);
            }
        }
        return bitset;
    }

}
