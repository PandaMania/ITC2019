import Parsing.*;
import entities.*;
import entities.course.CourseClass;
import entities.distribution.Distribution;

import java.io.*;
import java.util.*;
import java.util.concurrent.ThreadLocalRandom;
import java.util.stream.Stream;

public class SimulatedAnnealing {

    private final Instance instance;
    private long numClasses;
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


    // Constructor:
    public SimulatedAnnealing(Instance instance) {
        this.instance = instance;
        this.numClasses = this.getNumClasses(this.instance);
        this.numRooms = this.instance.rooms.size();
        this.numDays = getNumDays(this.instance);                               // parser should read problem tag to instance class
        this.numWeeks = this.getNumWeeks(this.instance);                        // parser should read problem tag to instance class
        this.numStudents = this.instance.students.size();
        this.slotsPerDay = 288;                                                 // parser should read problem tag to instance class
        this.bitFlipsDays = (int) Math.max(1.0, 0.2 * this.numDays);            // specify how many of the bits should be changed
        this.bitFlipsWeeks = (int) Math.max(1.0, 0.2 * this.numWeeks);          // specify how many of the bits should be changed
        this.bitFlipsStudents = (int) Math.max(1.0, 0.2 * this.numStudents);    // specify how many of the bits should be changed
        //this.hardPenalty = 100.0;
        this.hardPenalty = 10.0 * this.getMaxPenalty(this.instance);
    }

    private long getNumClasses(Instance instance) {
        return instance.getClasses().count();
    }

    private int getNumWeeks(Instance instance) {
        // parser should read problem tag to instance class
        return instance.courses.get(0).configs.get(0).subparts.get(0).classes.get(0).times.get(0).weeks.length();
    }

    private int getNumDays(Instance instance) {
        // parser should read problem tag to instance class
        return instance.courses.get(0).configs.get(0).subparts.get(0).classes.get(0).times.get(0).days.length();
    }

    private double getMaxPenalty (Instance instance){
        int maxPenalty = 0;
        for (Distribution dist : instance.distributions){
            if (dist.getPenalty() > maxPenalty) {
                maxPenalty = dist.getPenalty();
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

    private Solution getRandomNeighbor(Solution representation, int numChanges) {
        Solution neighbor = this.makeDeepCopy(representation);
        for (int numChange = 0; numChange < numChanges; numChange++){
            int indexSolution = ThreadLocalRandom.current().nextInt(0, neighbor.classes.size());
            //int indexSolutionClass = ThreadLocalRandom.current().nextInt(0, 6);                     // change to (1, 6) if not to change class ids
            int indexSolutionClass = ThreadLocalRandom.current().nextInt(1, 6);
            switch (indexSolutionClass){
                case 0:
                    // change classId
                    neighbor.classes.get(indexSolution).classId = ThreadLocalRandom.current().nextInt(1, (int) this.numClasses + 1);
                    break;
                case 1:
                    // change roomId
                    neighbor.classes.get(indexSolution).roomId = ThreadLocalRandom.current().nextInt(1, this.numRooms + 1);
                    break;
                case 2:
                    // change start
                    neighbor.classes.get(indexSolution).start = ThreadLocalRandom.current().nextInt(1, this.slotsPerDay + 1);
                    break;
                case 3:
                    // change days
                    if (this.numDays != 0){
                        for (int bitFlip = 0; bitFlip < this.bitFlipsDays; bitFlip++) {
                            int flipIndex = ThreadLocalRandom.current().nextInt(0, this.numDays);
                            neighbor.classes.get(indexSolution).days.flip(flipIndex);
                        }
                    }
                    break;
                case 4:
                    // change weeks
                    if (this.numWeeks != 0){
                        for (int bitFlip = 0; bitFlip < this.bitFlipsWeeks; bitFlip++) {
                            int flipIndex = ThreadLocalRandom.current().nextInt(0, this.numWeeks);
                            neighbor.classes.get(indexSolution).weeks.flip(flipIndex);
                        }
                    }
                    break;
                case 5:
                    // change student
                    if (this.numStudents != 0){
                        for (int bitFlip = 0; bitFlip < this.bitFlipsStudents; bitFlip++) {
                            int flipIndex = ThreadLocalRandom.current().nextInt(0, this.numStudents);
                            neighbor.classes.get(indexSolution).students.flip(flipIndex);
                        }
                    }
                    break;
            }
        }

        // optional part: only considering feasible solutions
        // maybe add while loop or change numChanges, bitFlips
        // ...

        return neighbor;
    }

    private double cost(Solution repr) {
        double cost = 0.0;
        for (Distribution dist : this.instance.distributions) {
            Boolean reprValidate = dist.validate(this.instance, repr);
            if (dist.required && !reprValidate){
                cost += this.hardPenalty;
            } else if (!reprValidate) {
                cost += dist.getPenalty();
            }
        }
        return cost;
    }

    private double getTemperature(double startTemperature, double endTemperature, int numIteration, int numIterations){
        // linear:
        return startTemperature - numIteration * startTemperature / numIterations;

        // exponential:
        //return startTemperature * Math.exp( (double) numIteration / (double) numIterations * Math.log(endTemperature / startTemperature));
    }


    private Solution optimize(Solution repr, double startTemperature, double endTemperature, int numIterations, int numChanges) {

        double reprCost = this.cost(repr);
        int numIteration = 0;

        while (numIteration < numIterations) {
            Solution neighbor = this.getRandomNeighbor(repr, numChanges);
            double neighborCost = this.cost(neighbor);
            double temperature = getTemperature(startTemperature, endTemperature, numIteration, numIterations);
            double prob = Math.min(1.0, Math.exp(-(neighborCost - reprCost) / temperature));
            if (this.getProbBool(prob)) {
                repr = this.makeDeepCopy(neighbor);
                reprCost = neighborCost;
            }

            System.out.println("numIteration:   " + numIteration + "\tFeasible: " + this.isFeasible(repr) + "\t\tCost: " + reprCost + "\tTemperature: " + String.format("%.4f", temperature) + "\tProbability: " + String.format("%.4f", prob));
            numIteration++;
            // additional break condition !?
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
            String instanceFileName = "bet-sum18.xml";
            //String instanceFileName = "lums-sum17.xml";
            //String instanceFileName = "pu-c8-spr07.xml";
            parser = new InstanceParser(instanceFileName);
            instance = parser.parse();
            S = new SimulatedAnnealing(instance);
            repr = S.initRepresentation(instance);
            solution = S.optimize(repr, 10.0, 0.01, 1000000, 8);

            //for (Distribution dist : instance.distributions) {System.out.println(dist.type + "\t\t" + dist.required);}

        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }

    }

}

/*    private List<Solution> getNeighborhood(Solution representation_0) {
        // TO DO !!!
        // implement Neighborhood of element representation_0 ...
        List<Solution> neighborhood = new ArrayList<>();
        //neighborhood.add(representation_0);
        return neighborhood;
    }

    private Solution getRandomNeighbor(Solution representation_0) {
         get random neighbor of element representation_0 ...
         using uniform distribution ...
        List<Solution> neighborhood = this.getNeighborhood(representation_0);
        int size = neighborhood.size();
        int index = ThreadLocalRandom.current().nextInt(0, size);
        return neighborhood.get(index);
        //return representation_0;
    }
*/
