
package ILP;

import Parsing.InstanceParser;
import entities.Instance;
import entities.course.Course;
import entities.course.CourseTime;
import entities.distribution.Overlap;
import gurobi.*;

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
            GRBModel  model = new GRBModel(env);


            InstanceParser p;
            addingConstraints O= new addingConstraints();
            combiOverlap combi = new combiOverlap();
            try {
                p = new InstanceParser(//"lums-sum17.xml");
                        //            p.parse("pu-cs-fal07.xml");
                        "tg-fal17.xml");
                Instance x = p.parse();
                // System.out.println(x);
                System.out.println("Courses= " + x.courses.size());
                System.out.println("Distributions= " + x.distributions.size());
                System.out.println("Students= " + x.students.size());
                System.out.println("rooms= " + x.rooms.size());

                int curr = 0;

                // time to make the variables

                ArrayList<ArrayList<ArrayList<ArrayList<ArrayList<ArrayList<GRBcombi>>>>>> allvariables= new ArrayList<>();
                GRBLinExpr objectiveFunc = new GRBLinExpr();
                GRBLinExpr scheduledConstraint;

                HashMap<Integer, ArrayList<GRBcombi>> overlapCheck= new HashMap<>();



                for(int j=0; j<x.courses.size(); j++) {
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

                                for (int o = 0; o < list.size(); o++) {
                                   ArrayList<GRBcombi> timeLoop = new ArrayList<>();

                                    for (int n = 0; n < x.courses.get(j).configs.get(k).subparts.get(l).classes.get(m).times.size(); n++) {
                                    //into the number of times
                                    //TODO need to ensure that the unavailable weeks is accounted for here. If its unavailable we just increment n.

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
                                                "room " + finallist[o] + " value= "
                                        ), (Integer) finallist[o], x.courses.get(j).configs.get(k).subparts.get(l).classes.get(m).times.get(n), x.courses.get(j).configs.get(k).subparts.get(l).classes.get(m)));
                                        objectiveFunc.addTerm(x.courses.get(j).configs.get(k).subparts.get(l).classes.get(m).times.get(n).penalty +x.courses.get(j).configs.get(k).subparts.get(l).classes.get(m).roomPenalties.get(finallist[o]) ,timeLoop.get(n).getGrbVar());
                                        scheduledConstraint.addTerm(1,timeLoop.get(n).getGrbVar());

                                        if(overlapCheck.containsKey( finallist[o])){
                                            ArrayList<GRBcombi> testing= new ArrayList<>();

                                            testing=overlapCheck.get(finallist[o]);
                                            testing.add(timeLoop.get(n));
                                            overlapCheck.replace((Integer) finallist[o],testing);
                                        }else{

                                            ArrayList<GRBcombi> currentroom =new ArrayList<>();
                                            currentroom.add(timeLoop.get(n));

                                            overlapCheck.put((Integer) finallist[o], currentroom);

                                        }

                                    }

                                    roomLoop.add(timeLoop);
                                }
                                model.addConstr(scheduledConstraint,GRB.EQUAL,1, "c");
                                insideLoop.add(roomLoop);
                            }
                            middleLoop.add(insideLoop);
                        }
                        outsideLoop.add(middleLoop);
                    }
                    allvariables.add(outsideLoop);
                }

                for(int i=1; i<=x.rooms.size(); i++){

                    System.out.println("overlapcheck room " + i);
                    combi.OverlapInARoom(overlapCheck.get(i), x, model);
                    //O.computeAllOverlaps(overlapCheck.get(i), model, sameAttendance, x);
                }


                                     /*  for(int y=0; y<list.size(); y++){

                                           if(overlapCheck.containsKey( finallist[y])){
                                               ArrayList<GRBtimeObject> current=  overlapCheck.get(finallist[y]);
                                               current.add(timeLoop.get(n));
                                               overlapCheck.replace((Integer) finallist[y],current);
                                           }else{
                                               ArrayList<GRBtimeObject> first= new ArrayList<>();
                                               first.add(timeLoop.get(n));

                                               overlapCheck.put((Integer) finallist[y], first);

                                           }

                                       }*/













                System.out.println("model size " +model.getConstrs().length);


                model.setObjective(objectiveFunc, GRB.MINIMIZE);
                Long timer= System.currentTimeMillis();
                model.optimize();
                Long endtime= System.currentTimeMillis();
                System.out. println("time to get solution= " + (endtime-timer));







                for(int j=0; j<allvariables.size(); j++) {
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


              /*  System.out.println(x.get(GRB.StringAttr.VarName)
                        + " " +x.get(GRB.DoubleAttr.X));
                System.out.println(y.get(GRB.StringAttr.VarName)
                        + " " +y.get(GRB.DoubleAttr.X));
                System.out.println(z.get(GRB.StringAttr.VarName)
                        + " " +z.get(GRB.DoubleAttr.X));
*/
                System.out.println("Obj: " + model.get(GRB.DoubleAttr.ObjVal));
                System.out.println("num constraints= " + model.getConstrs().length);

                // Dispose of model and environment
                model.dispose();
                env.dispose();





            } catch (FileNotFoundException e) {
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


        }catch (GRBException e) {
            System.out.println("Error code: " + e.getErrorCode() + ". " +
                    e.getMessage());
        }








    }
}
