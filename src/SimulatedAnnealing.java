import Parsing.*;
import entities.*;
import entities.course.CourseClass;
import entities.distribution.Distribution;

import java.io.*;
import java.util.*;
import java.util.concurrent.ThreadLocalRandom;
import java.util.stream.Stream;
import java.util.Collections;

public class SimulatedAnnealing {

    private final Instance instance;
    private int numClasses;
    private int numRooms;
    private int numDays;
    private int numWeeks;
    private int numStudents;
    private int slotsPerDay;
    private int bitFlipsDays;
    private int bitFlipsWeeks;
    private int bitFlipsStudents;
    private String instanceFileName;
    private String instanceName;
    private double hardPenalty;
    private ArrayList<CourseClass> courseClasses;
    private ArrayList<ArrayList<ArrayList<String>>> courseTimes;

    // Constructor:
    public SimulatedAnnealing(Instance instance) {
        this.instance = instance;
        this.numRooms = this.instance.rooms.size();
        this.numDays = this.instance.days;
        this.numWeeks = this.instance.weeks;
        this.numStudents = this.instance.students.size();
        this.slotsPerDay = this.instance.slotsPerDay;
        this.bitFlipsStudents = (int) Math.max(1.0, 0.75 * this.numStudents);    // specify how many of the bits should be changed
        this.hardPenalty = 3.0 * this.getMaxPenalty(this.instance);
        this.courseClasses = this.getCourseClasses();
        this.courseTimes = this.getCourseTimes();
    }

    private ArrayList<ArrayList<ArrayList<String>>> getCourseTimes(){
        ArrayList<ArrayList<ArrayList<String>>> result = new ArrayList<>();
        for(int i=0;i<this.getCourseClasses().size();i++){
            result.add(new ArrayList<>());
        }
        for(int i=0;i<this.instance.courses.size();i++){
            for(int j=0;j<this.instance.courses.get(j).configs.size();j++){
                for(int k=0;k<this.instance.courses.get(i).configs.get(j).subparts.size();k++){
                    for(int l=0;l<this.instance.courses.get(i).configs.get(j).subparts.get(k).classes.size();l++){
                        for(int m=0;m<this.instance.courses.get(i).configs.get(j).subparts.get(k).classes.get(l).times.size();m++){
                            int idx = Integer.parseInt(this.instance.courses.get(i).configs.get(j).subparts.get(k).classes.get(l).id) - 1;
                            Boolean found = false;
                            for(int n=0; n<result.get(idx).size(); n++){
                                if (this.instance.courses.get(i).configs.get(j).subparts.get(k).classes.get(l).times.get(m).days == result.get(idx).get(n).get(0)
                                 && this.instance.courses.get(i).configs.get(j).subparts.get(k).classes.get(l).times.get(m).weeks == result.get(idx).get(n).get(1)
                                 && Integer.toString(this.instance.courses.get(i).configs.get(j).subparts.get(k).classes.get(l).times.get(m).start) == result.get(idx).get(n).get(2)){
                                    found = true;
                                    break;
                                }
                            }
                            if(!found){
                                ArrayList<String> item = new ArrayList<>();
                                item.add(this.instance.courses.get(i).configs.get(j).subparts.get(k).classes.get(l).times.get(m).days);
                                item.add(this.instance.courses.get(i).configs.get(j).subparts.get(k).classes.get(l).times.get(m).weeks);
                                item.add(Integer.toString(this.instance.courses.get(i).configs.get(j).subparts.get(k).classes.get(l).times.get(m).start));
                                result.get(idx).add(item);
                            }
                        }
                    }
                }
            }
        }
        return result;
    }

    private ArrayList<CourseClass> getCourseClasses(){
        ArrayList<CourseClass> result = new ArrayList<>();
        for(int i=0;i<this.instance.courses.size();i++){
            for(int j=0;j<this.instance.courses.get(j).configs.size();j++){
                for(int k=0;k<this.instance.courses.get(i).configs.get(j).subparts.size();k++){
                    for(int l=0;l<this.instance.courses.get(i).configs.get(j).subparts.get(k).classes.size();l++){
                        Boolean found = false;
                        for(CourseClass c:result){
                            String id = this.instance.courses.get(i).configs.get(j).subparts.get(k).classes.get(l).id;
                            if (id == c.id){
                                found = true;
                                break;
                            }
                        }
                        if(!found){
                            result.add(this.instance.courses.get(i).configs.get(j).subparts.get(k).classes.get(l));
                        }
                    }
                }
            }
        }
        Collections.sort(result, new Comparator<CourseClass>() {
            @Override public int compare(CourseClass x1, CourseClass x2) {
                return Integer.parseInt(x1.id) - Integer.parseInt(x2.id);
            }
        });
        return result;
    }

    //private long getNumClasses(Instance instance) {
    //    return instance.getClasses().count();
    //}

    public int getNumWeeks() {
        return this.numWeeks;
    }

    public int getNumDays() {
        return this.numDays;
    }

    private double getMaxPenalty (Instance instance){
        int maxPenalty = 0;
        for (Distribution dist : instance.distributions){
            if (dist.getPenalty() > maxPenalty) {
                maxPenalty = dist.getPenalty();
            }
        }
        for(int i=0;i<instance.courses.size();i++){
            for(int j=0;j<instance.courses.get(i).configs.size();j++){
                for(int k=0;k<instance.courses.get(i).configs.get(j).subparts.size();k++){
                    for(int l=0;l<instance.courses.get(i).configs.get(j).subparts.get(k).classes.size();l++){
                        for(int m=0;m<instance.courses.get(i).configs.get(j).subparts.get(k).classes.get(l).roomPenalties.size();m++){
                            try {
                                int p = instance.courses.get(i).configs.get(j).subparts.get(k).classes.get(l).roomPenalties.get(m);
                                if (p > maxPenalty) {
                                    maxPenalty = p;
                                }
                            } catch (NullPointerException e){}
                        }
                        for(int m=0;m<instance.courses.get(i).configs.get(j).subparts.get(k).classes.get(l).times.size();m++){
                            int p = instance.courses.get(i).configs.get(j).subparts.get(k).classes.get(l).times.get(m).penalty;
                            if (p > maxPenalty) {
                                maxPenalty = p;
                            }
                        }
                    }
                }
            }
        }
        return (double) maxPenalty;
    }

    private Solution makeDeepCopy(Solution repr) {
        Solution deepcopy = new Solution(repr);
        return deepcopy;
    }

    private Boolean getProbBool(double prob) {
        return Math.random() <= prob;
    }

    private Solution initRepresentation(Instance instance) {

        Stream<CourseClass> allCourses = instance.getClasses();

        Solution representation = new Solution();
        for (Iterator<CourseClass> it = allCourses.iterator(); it.hasNext(); ) {

            CourseClass Class = it.next();
            SolutionClass classAssignment = new SolutionClass(this.numStudents, this.numWeeks, this.numDays);
            // TODO Set fields
            classAssignment.classId = Integer.parseInt(Class.id);
            representation.classes.add(classAssignment);

        }
        return representation;
    }

    private Boolean isFeasible (Solution representation){
        for (Distribution distribution : this.instance.distributions) {
            if (distribution.required && !distribution.validate(this.instance, representation)){
                return false;
            }
        }
        return true;
    }

    private int getNumInfeasible (Solution representation){
        int i = 0;
        for (Distribution distribution : this.instance.distributions) {
            if (distribution.required && !distribution.validate(this.instance, representation)){
                i++;
            }
        }
        return i;
    }

    private BitSet getBitSetFromString (String binary){
        BitSet result = new BitSet();
        for (int i=0;i<binary.length();i++){
            if(binary.charAt(i) == '1'){
                result.set(i);
            }
        }
        return result;
    }

    private Solution getRandomNeighbor(Solution repr, int numChanges) {
        Solution neighbor = this.makeDeepCopy(repr);
        for (int numChange = 0; numChange < numChanges; numChange++){
            int indexSolution = ThreadLocalRandom.current().nextInt(0, neighbor.classes.size());
            int indexSolutionPart = ThreadLocalRandom.current().nextInt(0, 3);
            switch (indexSolutionPart){
                case 0:
                    // change room (done):
                    while (true){
                        int selectedCourseIdx = neighbor.classes.get(indexSolution).classId - 1;
                        int newRoomIdx = ThreadLocalRandom.current().nextInt(1, this.numRooms + 1);
                        if (this.courseClasses.get(selectedCourseIdx).roomPenalties.get(newRoomIdx) != null){
                            neighbor.classes.get(indexSolution).roomId = newRoomIdx;
                            break;
                        }
                    }
                    break;
                case 1:
                    // change time (done):
                    int selectedCourseIdx = neighbor.classes.get(indexSolution).classId - 1;
                    int idxNewTime = ThreadLocalRandom.current().nextInt(0, this.courseTimes.get(selectedCourseIdx).size());
                    neighbor.classes.get(indexSolution).days = this.getBitSetFromString(this.courseTimes.get(selectedCourseIdx).get(idxNewTime).get(0));
                    neighbor.classes.get(indexSolution).weeks = this.getBitSetFromString(this.courseTimes.get(selectedCourseIdx).get(idxNewTime).get(1));
                    neighbor.classes.get(indexSolution).start = Integer.parseInt(this.courseTimes.get(selectedCourseIdx).get(idxNewTime).get(2));
                    break;
                case 2:
                    // change students:
                    if (this.numStudents != 0){
                        for (int bitFlip = 0; bitFlip < this.bitFlipsStudents; bitFlip++) {
                            int flipIndex = ThreadLocalRandom.current().nextInt(0, this.numStudents);
                            neighbor.classes.get(indexSolution).students.flip(flipIndex);
                        }
                    }
                    break;
            }
        }
        return neighbor;
    }

    private double cost(Solution repr) {
        double cost = 0.0;
        // constraint penalty:
        for (Distribution dist : this.instance.distributions) {
            Boolean reprValidate = dist.validate(this.instance, repr);
            if (dist.required && !reprValidate){
                cost += this.hardPenalty;
            } else if (!reprValidate) {
                cost += dist.getPenalty();
            }
        }
        // room penalty:
        for (SolutionClass solClass : repr.classes) {
            int classId = solClass.classId;
            int roomId = solClass.roomId;
            Boolean found = false;
            for(int i=0;i<this.instance.courses.size();i++){
                for(int j=0;j<this.instance.courses.get(i).configs.size();j++){
                    for(int k=0;k<this.instance.courses.get(i).configs.get(j).subparts.size();k++){
                        for(int l=0;l<this.instance.courses.get(i).configs.get(j).subparts.get(k).classes.size();l++){
                            if (Integer.parseInt(this.instance.courses.get(i).configs.get(j).subparts.get(k).classes.get(l).id) == classId){
                                try {
                                    cost += this.instance.courses.get(i).configs.get(j).subparts.get(k).classes.get(l).roomPenalties.get(roomId);
                                    found = true;
                                    break;
                                } catch (NullPointerException e) {continue;}
                            }
                        }
                        if (found){break;}
                    }
                    if (found){break;}
                }
                if (found){break;}
            }
        }
        return cost;
    }

    private double getTemperature(double startTemperature, double endTemperature, int numIteration, int numIterations){
        // linear:
        //return startTemperature - numIteration * startTemperature / numIterations;

        // exponential:
        return startTemperature * Math.exp( (double) numIteration / (double) numIterations * Math.log(endTemperature / startTemperature));
    }

    //private Boolean hasValidStructure(Solution repr){
    //    return false;
    //}

    private Solution optimize(Solution repr, double startTemperature, double endTemperature, int numIterations, int numChanges) {

        double reprCost = this.cost(repr);
        int numIteration = 0;
        Boolean reachedFeasibility = this.isFeasible(repr);
        //Boolean reachedValidStructure = this.hasValidStructure(repr);

        while (numIteration < numIterations) {
            Solution neighbor = this.getRandomNeighbor(repr, numChanges);

            // optional: don't give up feasibility or valid structure once reached
            if (reachedFeasibility && !this.isFeasible(neighbor)){continue;}
            //if (reachedValidStructure && !this.hasValidStructure(neighbor)){continue;}

            double neighborCost = this.cost(neighbor);
            double temperature = getTemperature(startTemperature, endTemperature, numIteration, numIterations);
            double prob = Math.min(1.0, Math.exp(-(neighborCost - reprCost) / temperature));
            if (this.getNumInfeasible(neighbor) < this.getNumInfeasible(repr)) {
                repr = this.makeDeepCopy(neighbor);
                reprCost = neighborCost;
            } else if (this.getProbBool(prob)) {
                repr = this.makeDeepCopy(neighbor);
                reprCost = neighborCost;
            }

            if (numIteration % 1000 == 0 || numIteration + 1 == numIterations){System.out.println("numIteration: " + numIteration + "\tFeasible: " + this.isFeasible(repr) +
            "\tCost: " + reprCost + "\tTemperature: " + String.format("%.4f", temperature) + "\tProbability: " + String.format("%.4f", prob) +
            "\tnumInfeasible: " + this.getNumInfeasible(repr) +
            "   " + String.format("%.2f", (100 * this.getNumInfeasible(repr) * this.hardPenalty) / reprCost)  + "%");}
            numIteration++;

            // optional: don't give up feasibility or valid structure once reached
            if (!reachedFeasibility && this.isFeasible(repr)){
                reachedFeasibility = true;
                System.out.println("Reached feasibility in iteration: " + numIteration);
            }
            //if (!reachedValidStructure && this.hasValidStructure(repr)){reachedValidStructure = true;}

            //if (reprCost == 0.0 && this.isFeasible(repr)) {break;}
            //if (reprCost == 0.0 && this.isFeasible(repr) && this.hasValidStructure(repr)) {break;}

        }
        return repr;
    }

    // Main:
    public static void main(String[] args) {

        InstanceParser parser;
        Instance instance;
        SimulatedAnnealing S;
        Solution repr;
        Solution solution;

        try {
            //String instanceFileName = "bet-sum18.xml";
            String instanceFileName = "lums-sum17.xml";
            parser = new InstanceParser(instanceFileName);
            instance = parser.parse();
            S = new SimulatedAnnealing(instance);
            repr = S.initRepresentation(instance);
            solution = S.optimize(repr, 2.0, 0.1, 10000, 1);
            String solutionText = solution.serialize(S.getNumDays(), S.getNumWeeks());
            System.out.println(solutionText);

        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }

    }

}
