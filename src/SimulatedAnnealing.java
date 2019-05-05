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

    private Solution makeDeepCopy(Solution representation) {
        Solution deepcopy = new Solution(representation);
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

    private Solution getRandomNeighbor(Solution representation, int numChanges) {
        Solution neighbor = this.makeDeepCopy(representation);
        for (int numChange = 0; numChange < numChanges; numChange++){
            int indexSolution = ThreadLocalRandom.current().nextInt(0, neighbor.classes.size());
            int indexSolutionClass = ThreadLocalRandom.current().nextInt(0, 6);                     // change to (1, 6) if not to change class ids
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
        return neighbor;
    }

    private double cost(Solution representation) {
        for (Distribution distribution : this.instance.distributions) {
            distribution.validate(instance, representation);
            System.out.println(distribution.validate(instance, representation));
        }
        return 0.0;
    }

    private double getTemperature(double startTemperature, double endTemperature, int numIteration, int numIterations){
        // linear:
        //return startTemperature - numIteration * startTemperature / numIterations;

        // exponential:
        return startTemperature * Math.exp( (double) numIteration / (double) numIterations * Math.log(endTemperature / startTemperature));
    }


    private Solution optimize(Solution representation, double startTemperature, double endTemperature, int numIterations, int numChanges) {

        double representationCost = this.cost(representation);
        int numIteration = 0;

        while (numIteration < numIterations) {
            Solution neighbor = this.getRandomNeighbor(representation, numChanges);
            double neighborCost = this.cost(representation);
            double temperature = getTemperature(startTemperature, endTemperature, numIteration, numIterations);
            double prob = Math.min(1.0, Math.exp(-(neighborCost - representationCost) / temperature));
            if (this.getProbBool(prob)) {
                representation = this.makeDeepCopy(neighbor);
                representationCost = neighborCost;
            }

            System.out.println("numIteration:   " + numIteration + "\tCost: " + representationCost + "\tTemperature: " + temperature);
            numIteration++;
            // additional break condition !?
        }
        return representation;
    }

    // Main:
    public static void main(String[] args) {

        InstanceParser parser;
        Instance instance;
        SimulatedAnnealing S;
        Solution representation;
        Solution solution;

        try {
            String instanceFileName = "lums-sum17.xml";
            parser = new InstanceParser(instanceFileName);
            instance = parser.parse();
            S = new SimulatedAnnealing(instance);
            representation = S.initRepresentation(instance);
            solution = S.optimize(representation, 50.0, 0.01, 10000, 3);

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
