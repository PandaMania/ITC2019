
package ILP;

import Parsing.InstanceParser;
import entities.Instance;
import entities.Solution;
import entities.SolutionClass;
import entities.course.Course;
import entities.course.CourseTime;
import entities.distribution.Overlap;
import gurobi.*;
import util.BitSets;

import java.io.FileNotFoundException;
import java.util.*;

public class Mip1 {


    public static void main(String[] args) {
        try {

            // Create empty environment, set options, and start
            GRBEnv env = new GRBEnv(true);
            env.set("logFile", "mip1.log");
            env.start();

            // Create empty model
            GRBModel model = new GRBModel(env);


            InstanceParser p;
            addingConstraints O = new addingConstraints();
            combiOverlap combi = new combiOverlap();
            try {
                p = new InstanceParser(//"bet-sum18.xml");
                        //            p.parse("pu-cs-fal07.xml");
                        "tg-fal17.xml");
               // "output4.xml");
                Instance x = p.parse();
                // System.out.println(x);
                System.out.println("Courses= " + x.courses.size());
                System.out.println("Distributions= " + x.distributions.size());
                System.out.println("Students= " + x.students.size());
                System.out.println("rooms= " + x.rooms.size());
                System.out.println(x.weeks);

                int curr = 0;

                // time to make the variables

                ArrayList<ArrayList<ArrayList<ArrayList<ArrayList<ArrayList<GRBcombi>>>>>> allvariables = new ArrayList<>();
                GRBLinExpr objectiveFunc = new GRBLinExpr();

                GRBLinExpr scheduledConstraint;
                HashMap<Integer, ArrayList<GRBcombi>> overlapCheck = new HashMap<>();

                int week= 5;
                    BitSet a= BitSet.valueOf(x.courses.get(0).configs.get(0).subparts.get(0).classes.get(0).times.get(0).days.getBytes());
                    if(!a.get(week)){
                        System.out.println("fuck yeah");
                    }




                for (int j = 0; j < x.courses.size(); j++) {
                    // being able to look down into specific courses.
                    ArrayList<ArrayList<ArrayList<ArrayList<ArrayList<GRBcombi>>>>> outsideLoop = new ArrayList<>();
                    for (int k = 0; k < x.courses.get(j).configs.size(); k++) {
                        // need now to look at the different configs
                        ArrayList<ArrayList<ArrayList<ArrayList<GRBcombi>>>> middleLoop = new ArrayList<>();
                        for (int l = 0; l < x.courses.get(j).configs.get(k).subparts.size(); l++) {
                            //inside there are subparts

                            ArrayList<ArrayList<ArrayList<GRBcombi>>> insideLoop = new ArrayList<>();
                            for (int m = 0; m < x.courses.get(j).configs.get(k).subparts.get(l).classes.size(); m++) {
                                // now to the actual part we have classes that have multiple options
                                scheduledConstraint = new GRBLinExpr();
                                Set<Integer> list = x.courses.get(j).configs.get(k).subparts.get(l).classes.get(m).roomPenalties.keySet();
                                Object[] finallist = list.toArray();
                                ArrayList<ArrayList<GRBcombi>> roomLoop = new ArrayList<>();

                                if (finallist.length == 0 || x.courses.get(j).configs.get(k).subparts.get(l).classes.get(m).roomNeeded==false) {
                                    int fakelist = -1;
                                    ArrayList<GRBcombi> faketimeLoop = new ArrayList<>();

                                    for (int n = 0; n < x.courses.get(j).configs.get(k).subparts.get(l).classes.get(m).times.size(); n++) {

                                        //into the number of times
                                        //TODO need to ensure that the unavailable weeks is accounted for here. If its unavailable we just increment n.

                                        // System.out.println(x.rooms.get((Integer) finallist[o]).id + " ==== " + (Integer) finallist[o] );


                                        // we're now into the rooms too
                                        faketimeLoop.add(new GRBcombi(model.addVar(0, 1, 0, GRB.BINARY, "course " + x.courses.get(j).id + "," +
                                                " config " + x.courses.get(j).configs.get(k).id + "," +
                                                " subpart " + x.courses.get(j).configs.get(k).subparts.get(l).id + "," +
                                                " class " + x.courses.get(j).configs.get(k).subparts.get(l).classes.get(m).id + "," +
                                                //from here i wanna change this last part to an id but for now we do it like this.
                                                " week " + x.courses.get(j).configs.get(k).subparts.get(l).classes.get(m).times.get(n).weeks + "," +
                                                " day " + x.courses.get(j).configs.get(k).subparts.get(l).classes.get(m).times.get(n).days + "," +
                                                " time " + x.courses.get(j).configs.get(k).subparts.get(l).classes.get(m).times.get(n).start + "," +
                                                " length " + x.courses.get(j).configs.get(k).subparts.get(l).classes.get(m).times.get(n).length +
                                                "room " + fakelist + " value= "
                                        ), fakelist, x.courses.get(j).configs.get(k).subparts.get(l).classes.get(m).times.get(n), x.courses.get(j).configs.get(k).subparts.get(l).classes.get(m)));

                                        objectiveFunc.addTerm((x.courses.get(j).configs.get(k).subparts.get(l).classes.get(m).times.get(n).penalty), faketimeLoop.get(faketimeLoop.size() - 1).grbVar);
                                        scheduledConstraint.addTerm(1, faketimeLoop.get(faketimeLoop.size() - 1).grbVar);


                                    }
                                    model.addConstr(scheduledConstraint, GRB.EQUAL, 1, "fake CONSTRAINT= j=  " + x.courses.get(j).id + " k= " + k +" l= "+ l+ " m= " + m);
                                    roomLoop.add(faketimeLoop);
                                }else {



                                    for (int o = 0; o < finallist.length; o++) {
                                        int currRoom= (Integer) finallist[o];

                                        ArrayList<GRBcombi> timeLoop = new ArrayList<>();
                                        int numvariables = 0;

                                        for (int n = 0; n < x.courses.get(j).configs.get(k).subparts.get(l).classes.get(m).times.size(); n++) {

                                            //into the number of times
                                            //TODO need to ensure that the unavailable weeks is accounted for here. If its unavailable we just increment n.

                                            // System.out.println(x.rooms.get((Integer) finallist[o]).id + " ==== " + (Integer) finallist[o] );

                                            while (n < x.courses.get(j).configs.get(k).subparts.get(l).classes.get(m).times.size() && combi.singlecheck(x.rooms.get((Integer) finallist[o] - 1).unaivailableweeks, x.courses.get(j).configs.get(k).subparts.get(l).classes.get(m).times.get(n))) {
                                                n++;
                                                if(x.courses.get(j).configs.get(k).subparts.get(l).classes.get(m).times.size()==1){
                                                    System.out.println("size is 1 and class is = " + x.courses.get(j).configs.get(k).subparts.get(l).classes.get(m).id);
                                                    System.out.println("number of rooms = " + x.courses.get(j).configs.get(k).subparts.get(l).classes.get(m).roomPenalties.size());
                                                }

                                            }

                                            if (n >= x.courses.get(j).configs.get(k).subparts.get(l).classes.get(m).times.size() ) {
                                               //|| ( x.courses.get(j).configs.get(k).subparts.get(l).classes.get(m).times.size()==1 && x.courses.get(j).configs.get(k).subparts.get(l).classes.get(m).roomPenalties.size()==1 )
                                                break;
                                            }
                                            // we're now into the rooms too
                                            timeLoop.add(new GRBcombi(model.addVar(0, 1, 0, GRB.BINARY, "course " + x.courses.get(j).id + "," +
                                                    " config " + x.courses.get(j).configs.get(k).id + "," +
                                                    " subpart " + x.courses.get(j).configs.get(k).subparts.get(l).id + "," +
                                                    " class " + x.courses.get(j).configs.get(k).subparts.get(l).classes.get(m).id + "," +
                                                    //from here i wanna change this last part to an id but for now we do it like this.
                                                    " week " + x.courses.get(j).configs.get(k).subparts.get(l).classes.get(m).times.get(n).weeks + "," +
                                                    " day " + x.courses.get(j).configs.get(k).subparts.get(l).classes.get(m).times.get(n).days + "," +
                                                    " time " + x.courses.get(j).configs.get(k).subparts.get(l).classes.get(m).times.get(n).start + "," +
                                                    " length " + x.courses.get(j).configs.get(k).subparts.get(l).classes.get(m).times.get(n).length +
                                                    "room " + currRoom + " value= "
                                            ), currRoom, x.courses.get(j).configs.get(k).subparts.get(l).classes.get(m).times.get(n), x.courses.get(j).configs.get(k).subparts.get(l).classes.get(m)));

                                            objectiveFunc.addTerm((x.courses.get(j).configs.get(k).subparts.get(l).classes.get(m).times.get(n).penalty + x.courses.get(j).configs.get(k).subparts.get(l).classes.get(m).roomPenalties.get(currRoom)), timeLoop.get(timeLoop.size() - 1).grbVar);
                                            scheduledConstraint.addTerm(1, timeLoop.get(timeLoop.size() - 1).grbVar);


                                            if (overlapCheck.containsKey(currRoom)) {
                                                ArrayList<GRBcombi> testing;

                                                testing = overlapCheck.get(currRoom);
                                                testing.add(timeLoop.get(timeLoop.size()-1));
                                                overlapCheck.replace(currRoom, testing);
                                            } else {

                                                ArrayList<GRBcombi> currentroom = new ArrayList<>();
                                                currentroom.add(timeLoop.get(timeLoop.size()-1));

                                                overlapCheck.put(currRoom, currentroom);

                                            }
                                            numvariables++;

                                        }


                                        roomLoop.add(timeLoop);
                                    }
                                    model.addConstr(scheduledConstraint, GRB.EQUAL, 1, "real CONSTRAINT j=  " +  x.courses.get(j).id + " k= " + k +" l= "+ l+ " m= " + m);
                                }

                                 //     model.addConstr(scheduledConstraint,GRB.EQUAL,1, "b");
                                    insideLoop.add(roomLoop);
                                }
                                middleLoop.add(insideLoop);
                            }
                            outsideLoop.add(middleLoop);
                        }
                        allvariables.add(outsideLoop);
                    }



                    for (int i = 1; i <= x.rooms.size(); i++) {

                        System.out.println("overlapcheck room " + i);
                        combi.OverlapInARoom(overlapCheck.get(i), x, model);
                        //O.computeAllOverlaps(overlapCheck.get(i), model, sameAttendance, x);
                    }

                    System.out.println("model size " + model.getConstrs().length);


                    model.setObjective(objectiveFunc, GRB.MINIMIZE);
                    //model.feasRelax(GRB.FEASRELAX_LINEAR, true, false, true);
                    Long timer = System.currentTimeMillis();

                    model.optimize();
                    Long endtime = System.currentTimeMillis();

                    System.out.println("time to get solution= " + (endtime - timer));

                int status = model.get(GRB.IntAttr.Status);
                if (status == GRB.Status.UNBOUNDED) {
                    System.out.println("The model cannot be solved "
                            + "because it is unbounded");
                    return;
                }
                if (status == GRB.Status.OPTIMAL) {
                    System.out.println("The optimal objective is " +
                            model.get(GRB.DoubleAttr.ObjVal));
                    return;
                }
                if (status != GRB.Status.INF_OR_UNBD &&
                        status != GRB.Status.INFEASIBLE    ) {
                    System.out.println("Optimization was stopped with status " + status);
                    return;
                }

                // Do IIS
                System.out.println("The model is infeasible; computing IIS");
                LinkedList<String> removed = new LinkedList<String>();

                // Loop until we reduce to a model that can be solved
                while (true) {
                    model.computeIIS();
                    System.out.println("\nThe following constraint cannot be satisfied:");
                    for (GRBConstr c : model.getConstrs()) {
                        if (c.get(GRB.IntAttr.IISConstr) == 1) {
                            System.out.println(c.get(GRB.StringAttr.ConstrName));
                            // Remove a single constraint from the model
                            removed.add(c.get(GRB.StringAttr.ConstrName));
                            model.remove(c);
                            break;
                        }
                    }

                    System.out.println();
                    model.optimize();
                    status = model.get(GRB.IntAttr.Status);

                    if (status == GRB.Status.UNBOUNDED) {
                        System.out.println("The model cannot be solved "
                                + "because it is unbounded");
                        return;
                    }
                    if (status == GRB.Status.OPTIMAL) {
                        break;
                    }
                    if (status != GRB.Status.INF_OR_UNBD &&
                            status != GRB.Status.INFEASIBLE    ) {
                        System.out.println("Optimization was stopped with status " +
                                status);
                        return;
                    }
                }

                System.out.println("\nThe following constraints were removed "
                        + "to get a feasible LP:");
                for (String s : removed) {
                    System.out.print(s + " ");
                }
                System.out.println();


                for (int j = 0; j < allvariables.size(); j++) {
                        for (int k = 0; k < allvariables.get(j).size(); k++) {
                            for (int l = 0; l < allvariables.get(j).get(k).size(); l++) {
                                for (int m = 0; m < allvariables.get(j).get(k).get(l).size(); m++) {
                                    for (int n = 0; n < allvariables.get(j).get(k).get(l).get(m).size(); n++) {
                                        for (int o = 0; o < allvariables.get(j).get(k).get(l).get(m).get(n).size(); o++) {
                                            if (allvariables.get(j).get(k).get(l).get(m).get(n).get(o).getGrbVar().get(GRB.DoubleAttr.X) == 1) {
                                                System.out.println(allvariables.get(j).get(k).get(l).get(m).get(n).get(o).getGrbVar().get(GRB.StringAttr.VarName) +
                                                        " " + allvariables.get(j).get(k).get(l).get(m).get(n).get(o).getGrbVar().get(GRB.DoubleAttr.X));

                                            }
                                        }

                                    }
                                }
                            }
                        }
                    }


                    System.out.println("Obj: " + model.get(GRB.DoubleAttr.ObjVal));
                    System.out.println("num constraints= " + model.getConstrs().length);


                    // Dispose of model and environment
                    model.dispose();
                    env.dispose();


                } catch(FileNotFoundException e){
                    e.printStackTrace();
                }

                // Create variables
          /* GRBVar x = model.addVar(0.0, 1.0, 0.0, GRB.BINARY, "x");
            GRBVar y = model.addVar(0.0, 1.0, 0.0, GRB.BINARY, "y");
            GRBVar z = model.addVar(0.0, 1.0, 0.0, GRB.BINARY, "z");

            // Set objective: maximize x + y + 2 z
            GRBLinExpr expr = new GRBLinExpr();
            expr.addTerm(1.0, x); expr.addTerm(1.0, y); expr.addTerm(2.0, z);
            model.setObjective(expr, GRB.MAXIMIZE);

            // Add constraint: x + 2 y + 3 z <= 4
            expr = new GRBLinExpr();
            expr.addTerm(1.0, x);
            expr.addTerm(2.0, y);
            expr.addTerm(3.0, z);
            model.addConstr(expr, GRB.LESS_EQUAL, 4.0, "c0");

            // Add constraint: x + y >= 1
            expr = new GRBLinExpr();
            expr.addTerm(1.0, x); expr.addTerm(1.0, y);
            model.addConstr(expr, GRB.GREATER_EQUAL, 1.0, "c1");

            // Optimize model
            model.optimize();

            System.out.println(x.get(GRB.StringAttr.VarName)
                    + " " +x.get(GRB.DoubleAttr.X));
            System.out.println(y.get(GRB.StringAttr.VarName)
                    + " " +y.get(GRB.DoubleAttr.X));
            System.out.println(z.get(GRB.StringAttr.VarName)
                    + " " +z.get(GRB.DoubleAttr.X));

            System.out.println("Obj: " + model.get(GRB.DoubleAttr.ObjVal));

            // Dispose of model and environment
            model.dispose();
            env.dispose();

*/


            } catch (GRBException e) {
                System.out.println("Error code: " + e.getErrorCode() + ". " +
                        e.getMessage());
            }


        }
    }

