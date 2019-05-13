

package ILP;

import Parsing.InstanceParser;
import entities.Instance;
import gurobi.*;

import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.Set;

public class ILP {
    public static void main(String[] args){
        try{

            GRBEnv env = new GRBEnv(true);
            env.set("logFile", "mip1.log");
            env.start();

            // Create empty model
            GRBModel  model = new GRBModel(env);


            InstanceParser p;
            try {
                p = new InstanceParser(//"lums-sum17.xml");
                        //            p.parse("pu-cs-fal07.xml");
                        "lums-sum17.xml");
                Instance x = p.parse();
               // System.out.println(x);
                System.out.println("Courses= " +x.courses.size());
                System.out.println("Distributions= " +x.distributions.size());
                System.out.println("Students= " +x.students.size());
                System.out.println("rooms= " +x.rooms.size());
                int curr;
            // we have this horrible object so we can look into the different variables.
                ArrayList< ArrayList<ArrayList<ArrayList<ArrayList<GRBVar>>>>> allVariables= new ArrayList<>();
                GRBLinExpr objectiveFunc = new GRBLinExpr();
                GRBLinExpr scheduledConstraint;
                ArrayList<ArrayList<GRBVar>> roomsgrouped= new ArrayList<>();

                    for(int j=0; j<x.courses.size(); j++){
                        // being able to look down into specific courses.
                        ArrayList<  ArrayList<ArrayList<ArrayList<GRBVar>>>> outsideLoop= new ArrayList<>();
                       for(int k=0; k<x.courses.get(j).configs.size();k++){
                           // need now to look at the different configs
                           ArrayList<    ArrayList<ArrayList<GRBVar>>> middleLoop= new ArrayList<>();
                           for(int l=0; l<x.courses.get(j).configs.get(k).subparts.size(); l++){
                               //inside there are subparts

                               ArrayList<  ArrayList<GRBVar>> insideLoop= new ArrayList<>();
                               for(int m=0; m<x.courses.get(j).configs.get(k).subparts.get(l).classes.size();m++){
                                   // now to the actual part we have classes that have multiple options
                                   ArrayList<GRBVar> timeLoop= new ArrayList<>();
                                   scheduledConstraint = new GRBLinExpr();
                                   for(int n=0; n<x.courses.get(j).configs.get(k).subparts.get(l).classes.get(m).times.size(); n++){
                                       // here we have the various options, we only can select one of these
                                       // i wanna replace the future lines with an id but for now i do it like this
                                       timeLoop.add(  model.addVar(0.0, 1.0, 0.0, GRB.BINARY,
                                               "time-- " +
                                                       "course " + x.courses.get(j).id + "," +
                                                       " config " + x.courses.get(j).configs.get(k).id + "," +
                                                       " subpart " + x.courses.get(j).configs.get(k).subparts.get(l).id + "," +
                                                       " class " + x.courses.get(j).configs.get(k).subparts.get(l).classes.get(m).id + "," +
                                                       //from here i wanna change this last part to an id but for now we do it like this.
                                                       " week " + x.courses.get(j).configs.get(k).subparts.get(l).classes.get(m).times.get(n).weeks+ "," +
                                                       " day " + x.courses.get(j).configs.get(k).subparts.get(l).classes.get(m).times.get(n).days+ "," +
                                                       " time " + x.courses.get(j).configs.get(k).subparts.get(l).classes.get(m).times.get(n).start+ "," +
                                                       " length " +  x.courses.get(j).configs.get(k).subparts.get(l).classes.get(m).times.get(n).length + " value= "
                                       ));
                                       objectiveFunc.addTerm(x.courses.get(j).configs.get(k).subparts.get(l).classes.get(m).times.get(n).penalty,timeLoop.get(n));
                                       scheduledConstraint.addTerm(1,timeLoop.get(n));

                                   }
                                   model.addConstr(scheduledConstraint, GRB.EQUAL, 1.0, "constraint" + x.courses.get(j).id + "," +
                                           x.courses.get(j).configs.get(k).id + "," +
                                           x.courses.get(j).configs.get(k).subparts.get(l).id + "," +
                                           x.courses.get(j).configs.get(k).subparts.get(l).classes.get(m).id);

                                   insideLoop.add(timeLoop);
                                   ArrayList<GRBVar> roomLoop= new ArrayList<>();
                                  Set<Integer> list= x.courses.get(j).configs.get(k).subparts.get(l).classes.get(m).roomPenalties.keySet();
                                   Object[] finallist= list.toArray();
                                   scheduledConstraint = new GRBLinExpr();
                                   for(int n=0; n<list.size(); n++){
                                       roomLoop.add(  model.addVar(0.0, 1.0, 0.0, GRB.BINARY,
                                               "room" +
                                                       "course " +x.courses.get(j).id + "," +
                                                       " config " +x.courses.get(j).configs.get(k).id + "," +
                                                       " subpart " + x.courses.get(j).configs.get(k).subparts.get(l).id + "," +
                                                       " class " +x.courses.get(j).configs.get(k).subparts.get(l).classes.get(m).id + "," +
                                                       finallist[n]));
                                               //from here i wanna change this last part to an id but for now we do it like this.
                                       objectiveFunc.addTerm(x.courses.get(j).configs.get(k).subparts.get(l).classes.get(m).roomPenalties.get(finallist[n]),roomLoop.get(n));
                                       scheduledConstraint.addTerm(1,roomLoop.get(n));
                                   }
                                   model.addConstr(scheduledConstraint, GRB.EQUAL, 1.0, "constraint" + x.courses.get(j).id + "," +
                                           x.courses.get(j).configs.get(k).id + "," +
                                           x.courses.get(j).configs.get(k).subparts.get(l).id + "," +
                                           x.courses.get(j).configs.get(k).subparts.get(l).classes.get(m).id);
                                   insideLoop.add(roomLoop);



                               }
                               middleLoop.add(insideLoop);
                           }
                           outsideLoop.add(middleLoop);
                             // insideLoop.add(now);
                       }
                      allVariables.add(outsideLoop);
                    }

                //for all rooms, check that no times are the same that equal 1, we need subset of rooms, and inside that to check values of 1, and then check if they have overlapping times.



                model.setObjective(objectiveFunc, GRB.MINIMIZE);
                model.optimize();


                for(int j=0; j<allVariables.size(); j++) {
                    for (int k = 0; k < allVariables.get(j).size(); k++) {
                        for (int l = 0; l < allVariables.get(j).get(k).size(); l++) {
                            for (int m = 0; m < allVariables.get(j).get(k).get(l).size(); m++) {
                                for (int n = 0; n <allVariables.get(j).get(k).get(l).get(m).size(); n++) {
                                    System.out.println( allVariables.get(j).get(k).get(l).get(m).get(n).get(GRB.StringAttr.VarName) + " " + allVariables.get(j).get(k).get(l).get(m).get(n).get(GRB.DoubleAttr.X));
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