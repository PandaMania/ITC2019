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
    // Parameters:
    private double temperature;

    // Constructor:
    public SimulatedAnnealing(double start_temperature, Instance instance) {
        this.temperature = start_temperature;
        this.instance = instance;
    }

    private long getNumClasses(Instance instance) {
        return instance.getClasses().count();
        //System.out.println(instance.courses);
//        return counter.size();
    }

    private int getNumWeeks(Instance instance) {
        // uses assumption that room 0 is unavaiable at some time ...
        String s = instance.rooms.get(0).unaivailableweeks.get(0);
        int start = s.indexOf("weeks= ") + "weeks= ".length();
        int end = s.indexOf(" ,length");
        //System.out.println(s);
        //System.out.println(end - start);
        return end - start;
    }

    private int getNumDays(Instance instance) {
        // uses assumption that room 0 is unavaiable at some time ...
        String s = instance.rooms.get(0).unaivailableweeks.get(0);
        int start = s.indexOf("days= ") + "days= ".length();
        int end = s.length();
        //System.out.println(s);
        //System.out.println(end - start);
        return end - start;
    }

    private List<int[]> makeDeepCopy(List<int[]> representation) {
        List<int[]> deepcopy = new ArrayList<>();
        for (int[] item : representation) {
            deepcopy.add(item.clone());
        }
        return deepcopy;
    }

    private Boolean getProbBool(double prob) {
        return Math.random() <= prob;
    }

    private List<int[]> initRepresentation(Instance instance) {
        // TO DO !!!

        long numClasses = this.getNumClasses(instance);
        int numRooms = instance.rooms.size();
        int numDays = getNumDays(instance);
        int numWeeks = this.getNumWeeks(instance);
        int numStudents = instance.students.size();

        // [class_id, room_id, start, end, days, weeks, students]
        // I don't think and end is necessary as we know the length of a class soit would become;
        // [class_id, room_id, start, days, weeks, students]
        // representation.add(classAssignment_1)
        // representation.add(classAssignment_2)
        // ...

//        int[] classAssignment = new int[4 + numDays + numWeeks + numStudents];

        Stream<CourseClass> allCourses = instance.getClasses();

        List<int[]> representation = new ArrayList<>();
        for (Iterator<CourseClass> it = allCourses.iterator(); it.hasNext(); ) {
            CourseClass Class = it.next();
            int[] classAssignment = new int[3 + numDays + numWeeks + numStudents];
            classAssignment[0] = Integer.parseInt(Class.id);
            representation.add(classAssignment);

        }
        return representation;
    }

    private List<List<int[]>> getNeighborhood(List<int[]> representation_0) {
        // TO DO !!!
        // implement Neighborhood of element representation_0 ...
        List<List<int[]>> neighborhood = new ArrayList<>();
        //neighborhood.add(representation_0);
        return neighborhood;
    }

    private List<int[]> getRandomNeighbor(List<int[]> representation_0) {
        // get random neighbor of element representation_0 ...
        // using uniform distribution ...
        List<List<int[]>> neighborhood = this.getNeighborhood(representation_0);
        int size = neighborhood.size();
        int index = ThreadLocalRandom.current().nextInt(0, size);
        return neighborhood.get(index);
    }

    private double cost(List<int[]> representation) {
        // TO DO !!!
        // implement cost of a representation ...
        for (Distribution distribution : this.instance.distributions) {
            distribution.validate(instance, representation);
        }
        return 0.0;
    }

    private List<int[]> optimize(List<int[]> representation) {
        // TO DO !!!
        // implementation of simulated annealing ...
        // searching for minimum of cost function ...

        double representation_cost = this.cost(representation);

        while (true) {
            List<int[]> neighbor = this.getRandomNeighbor(representation);
            double neighbor_cost = this.cost(representation);
            double prob = Math.min(1.0, Math.exp(-(neighbor_cost - representation_cost) / this.temperature));
            if (this.getProbBool(prob)) {
                representation = this.makeDeepCopy(neighbor);
                representation_cost = neighbor_cost;
            }

            // adjustment of temperature:

            // break condition:
            if (true) {
                break;
            }
        }
        return representation;
    }

    // Main:
    public static void main(String[] args) {

        InstanceParser parser;
        Instance instance;
        SimulatedAnnealing S;
        List<int[]> representation;
        List<int[]> solution;

        try {
            parser = new InstanceParser("lums-sum17.xml");
            instance = parser.parse();
            S = new SimulatedAnnealing(0.9, instance);
            representation = S.initRepresentation(instance);
            solution = S.optimize(representation);

        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }

    }

}
