import Parsing.*;
import entities.*;
import util.*;
import entities.course.CourseClass;
import entities.course.CourseTime;
import entities.distribution.Distribution;

import java.io.*;
import java.util.*;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.ThreadLocalRandom;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class GeneticAlgorithm {

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
    private ExecutorService pool = Executors.newFixedThreadPool(4);
	private int numGenerations;
	private int numPopulation;
	private ValidationResult[][] fitnessOverTime;
	private int[] diversityOverTime;
	private Solution[] population;
	private int genePoolSize;


	public GeneticAlgorithm(int numGenerations, int numPopulation, Instance instance) {

		this.instance = instance;
        this.numRooms = this.instance.rooms.size();
        this.numDays = this.instance.days;
        this.numWeeks = this.instance.weeks;
        this.numStudents = this.instance.students.size();
        this.slotsPerDay = this.instance.slotsPerDay;
        this.bitFlipsStudents = (int) Math.max(1.0, 0.75 * this.numStudents);    // specify how many of the bits should be changed
        this.hardPenalty = 1.0 * this.getMaxPenalty(this.instance);
        this.courseClasses = this.getCourseClasses();
		this.numGenerations = numGenerations;
		this.numPopulation = numPopulation;
		this.genePoolSize = numPopulation;
		this.fitnessOverTime = new ValidationResult[numGenerations][numPopulation];
		this.diversityOverTime = new int[numGenerations];
		this.population = new Solution[numPopulation];
	}

	public int geneDistance(Genome x, Genome y) {
		return 0;
	}

	public Solution simulateEvolution(Solution[] init) {
		this.population = init;
		//LinkedList<Double> fitnessgen = new LinkedList<Double>();
		for (int i=0; i<this.numGenerations; i++) {
			ValidationResult[] fitnessGeneration = new ValidationResult[this.numPopulation];
			for (int j=0; j<this.numPopulation; j++) {
				fitnessGeneration[j] = cost(this.population[j]);
				this.fitnessOverTime[i][j] = fitnessGeneration[j];
			}
			Solution[] genePool = new Solution[this.genePoolSize];
			for (int j=0; j<this.numPopulation; j++) {
				int[] tournament = new Random().ints(0, numPopulation).distinct().limit(5).toArray();
				Solution best = this.population[tournament[0]];
				double bestCost = cost(best).cost;
				for (int k=1; k<5; k++) {
					double c = cost(this.population[tournament[k]]).cost;
					if (bestCost < c) {
						best = this.population[tournament[k]];
						bestCost = c;
					}
			//		fitnessgen.add(bestCost);
				}
				int[] tournament2 = new Random().ints(0, numPopulation).distinct().limit(5).toArray();
				Solution best2 = this.population[tournament2[0]];
				double bestCost2 = cost(best2).cost;
				for (int k=1; k<5; k++) {
					double c2 = cost(this.population[tournament2[k]]).cost;
					if (bestCost2 < c2) {
						best2 = this.population[tournament2[k]];
						bestCost2 = c2;
					}
				//	fitnessgen.add(bestCost);
				}
				genePool[j] = crossover(best, best2);
				//mutation
				int mutations = ThreadLocalRandom.current().nextInt(1, 10);
				genePool[j] = getRandomNeighbor(genePool[j], mutations);
				genePool[j] = repair(genePool[j], 7000.0, 0.1, 200, 1);
			}
			for (int j=0; j<this.numPopulation; j++) {
				this.population[j] = makeDeepCopy(genePool[j]);
			}
		}
		Solution best = repair(this.population[0], 7000, 0.1, 100_000, 1);
		double bestCost = cost(best).cost;
		for (int k=1; k<this.numPopulation; k++) {
			double c = cost(this.population[k]).cost;
			if (bestCost < c) {
				best = repair(this.population[k], 7000, 0.1, 100_000, 1);
				bestCost = c;
			}
		}
		//System.out.println(fitnessgen);
		return best;
	}

	private Solution crossover(Solution p1, Solution p2) {
		Solution child = this.makeDeepCopy(p1);
        removed = new ArrayList<>(reprRemoved);
        for (int i = 0; i < child.classes.size(); i++) {
            int indexSolutionPart = ThreadLocalRandom.current().nextInt(0, 1);
            switch (indexSolutionPart) {
                case 0:
                    SolutionClass changeClass = child.classes.get(i);
                    CourseClass courseClass = instance.getClassForId(changeClass.classId);
                    SolutionClass altClass = p2.classes.get(i);
                    if(courseClass.roomNeeded) {
                    	//System.out.println("id " + altClass.getRoomId().substring(6, altClass.getRoomId().length() - 1));
                    	changeClass.roomId = Integer.parseInt(altClass.getRoomId().substring(6, altClass.getRoomId().length() - 1));
                    }
                    child.classes.get(i).days = p2.classes.get(i).days;
                    child.classes.get(i).weeks = p2.classes.get(i).weeks;
                    child.classes.get(i).start = p2.classes.get(i).start;
                    child.classes.get(i).length = p2.classes.get(i).length;
                    break;
                case 1:
                    break;
            }
        }
        return child;
	}

    private ArrayList<ArrayList<ArrayList<String>>> getCourseTimes() {
        ArrayList<ArrayList<ArrayList<String>>> result = new ArrayList<>();
        for (int i = 0; i < this.getCourseClasses().size(); i++) {
            result.add(new ArrayList<>());
        }
        for (int i = 0; i < this.instance.courses.size(); i++) {
            for (int j = 0; j < this.instance.courses.get(j).configs.size(); j++) {
                for (int k = 0; k < this.instance.courses.get(i).configs.get(j).subparts.size(); k++) {
                    for (int l = 0; l < this.instance.courses.get(i).configs.get(j).subparts.get(k).classes.size(); l++) {
                        for (int m = 0; m < this.instance.courses.get(i).configs.get(j).subparts.get(k).classes.get(l).times.size(); m++) {
                            int idx = this.instance.courses.get(i).configs.get(j).subparts.get(k).classes.get(l).id - 1;
                            Boolean found = false;
                            for (int n = 0; n < result.get(idx).size(); n++) {
                                if (this.instance.courses.get(i).configs.get(j).subparts.get(k).classes.get(l).times.get(m).days == BitSets.fromString(result.get(idx).get(n).get(0))
                                        && this.instance.courses.get(i).configs.get(j).subparts.get(k).classes.get(l).times.get(m).weeks == BitSets.fromString(result.get(idx).get(n).get(1))
                                        && Integer.toString(this.instance.courses.get(i).configs.get(j).subparts.get(k).classes.get(l).times.get(m).start) == result.get(idx).get(n).get(2)) {
                                    found = true;
                                    break;
                                }
                            }
                            if (!found) {
                                ArrayList<String> item = new ArrayList<>();
                                item.add(BitSets.toBitString(this.instance.courses.get(i).configs.get(j).subparts.get(k).classes.get(l).times.get(m).days, numDays));
                                item.add(BitSets.toBitString(this.instance.courses.get(i).configs.get(j).subparts.get(k).classes.get(l).times.get(m).weeks, numWeeks));
                                item.add(Integer.toString(this.instance.courses.get(i).configs.get(j).subparts.get(k).classes.get(l).times.get(m).start));
                                item.add(Integer.toString(this.instance.courses.get(i).configs.get(j).subparts.get(k).classes.get(l).times.get(m).penalty));
                                result.get(idx).add(item);
                            }
                        }
                    }
                }
            }
        }
        return result;
    }

    private ArrayList<CourseClass> getCourseClasses() {
        ArrayList<CourseClass> result = new ArrayList<>();
        for (int i = 0; i < this.instance.courses.size(); i++) {
            for (int j = 0; j < this.instance.courses.get(j).configs.size(); j++) {
                for (int k = 0; k < this.instance.courses.get(i).configs.get(j).subparts.size(); k++) {
                    for (int l = 0; l < this.instance.courses.get(i).configs.get(j).subparts.get(k).classes.size(); l++) {
                        boolean found = false;
                        for (CourseClass c : result) {
                            Integer id = this.instance.courses.get(i).configs.get(j).subparts.get(k).classes.get(l).id;
                            if (id.equals(c.id)) {
                                found = true;
                                break;
                            }
                        }
                        if (!found) {
                            result.add(this.instance.courses.get(i).configs.get(j).subparts.get(k).classes.get(l));
                        }
                    }
                }
            }
        }
        Collections.sort(result, Comparator.comparingInt(x -> x.id));
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

    private double getMaxPenalty(Instance instance) {
        int maxPenalty = 0;
        for (Distribution dist : instance.distributions) {
            if (dist.getPenalty() > maxPenalty && !dist.required) {
                maxPenalty = dist.getPenalty();
            }
        }
        for (int i = 0; i < instance.courses.size(); i++) {
            for (int j = 0; j < instance.courses.get(i).configs.size(); j++) {
                for (int k = 0; k < instance.courses.get(i).configs.get(j).subparts.size(); k++) {
                    for (int l = 0; l < instance.courses.get(i).configs.get(j).subparts.get(k).classes.size(); l++) {
                        for (int m = 0; m < instance.courses.get(i).configs.get(j).subparts.get(k).classes.get(l).roomPenalties.size(); m++) {
                            try {
                                int p = instance.courses.get(i).configs.get(j).subparts.get(k).classes.get(l).roomPenalties.get(m);
                                if (p > maxPenalty) {
                                    maxPenalty = p;
                                }
                            } catch (NullPointerException e) {
                            }
                        }
                        for (int m = 0; m < instance.courses.get(i).configs.get(j).subparts.get(k).classes.get(l).times.size(); m++) {
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
        Solution deepcopy = new Solution(repr, instance);
        return deepcopy;
    }

    private Boolean getProbBool(double prob) {
        return Math.random() <= prob;
    }

    private Solution initRepresentation(Instance instance) {

        Stream<CourseClass> allCourses = instance.getClasses();

        Solution representation = new Solution(instance);
        for (Iterator<CourseClass> it = allCourses.iterator(); it.hasNext(); ) {

            CourseClass C = it.next();
            SolutionClass classAssignment = new SolutionClass(this.numStudents, this.numWeeks, this.numDays);
            if (!C.roomNeeded) {
                classAssignment.roomId = -1;
            } else if(C.roomPenalties.size() == 1){
                classAssignment.roomId = C.roomPenalties.keySet().iterator().next();
            }else{
                //set random room
                int roomIdx = ThreadLocalRandom.current().nextInt(C.roomPenalties.size());
                Integer roomId = new ArrayList<>(C.roomPenalties.keySet()).get(roomIdx);
                classAssignment.roomId = roomId;
            }
            int timeIndex;
            if(C.times.size()==1){
                timeIndex=0;
            }
            else {
                timeIndex = ThreadLocalRandom.current().nextInt(C.times.size());
            }

            classAssignment.start = C.times.get(timeIndex).start;
            classAssignment.length = C.times.get(timeIndex).length;
            classAssignment.days = C.times.get(timeIndex).days;
            classAssignment.weeks = C.times.get(timeIndex).weeks;

            classAssignment.classId = C.id;
            representation.classes.add(classAssignment);

        }
        return representation;
    }

    private Boolean isFeasible(Solution representation) {
        for (Distribution distribution : this.instance.distributions) {
            if (distribution.required && !distribution.validate(this.instance, representation)) {
                return false;
            }
        }
        return true;
    }

    private int getNumInfeasible(Solution representation) {
        int i = 0;
        for (Distribution distribution : this.instance.distributions) {
            if (distribution.required && !distribution.validate(this.instance, representation)) {
                i++;
            }
        }
        return i;
    }

    private BitSet getBitSetFromString(String binary) {
        BitSet result = new BitSet();
        for (int i = 0; i < binary.length(); i++) {
            if (binary.charAt(i) == '1') {
                result.set(i);
            }
        }
        return result;
    }

    private Boolean anyInBitSet(BitSet bitset) {
        return !bitset.isEmpty();
    }

    private Boolean isAvailableWeeks(ArrayList<Unavailability> unaivailableweeks, int newRoomIdx, Solution repr, int indexSolution) {
        for (Unavailability item : unaivailableweeks) {
//            String days = item.substring(item.indexOf("days= ") + "days= ".length(),item.indexOf("days= ") + "days= ".length() + this.numDays);
//            String weeks = item.substring(item.indexOf("weeks= ") + "weeks= ".length(),item.indexOf("weeks= ") + "weeks= ".length() + this.numWeeks);
            int length = item.length; //Integer.parseInt(item.substring(item.indexOf("length= ") + "length= ".length(), item.indexOf(" ", item.indexOf("length= ") + "length= ".length())));
            int start = item.start;//Integer.parseInt(item.substring(item.indexOf("start= ") + "start= ".length(),item.indexOf(" ", item.indexOf("start= ") + "start= ".length())));
            //System.out.println(start);
            SolutionClass solClass = repr.classes.get(indexSolution);
            BitSet daysBitSet = item.days;
            BitSet weeksBitSet = item.weeks;
            BitSet overlapDaysBitSet = BitSets.and(solClass.days, daysBitSet);
            BitSet overlapWeeksBitSet = BitSets.and(solClass.weeks, weeksBitSet);
            if (!overlapDaysBitSet.isEmpty() && !overlapWeeksBitSet.isEmpty()) {
                if (((start <= solClass.start) && (solClass.start < start + length))
                        || ((start <= solClass.start + solClass.length) && (solClass.start + solClass.length < start + length))
                        || ((solClass.start <= start) && (solClass.start + solClass.length >= start + length))) {
                    return false;
                }
            }
        }
        return true;
    }

    private Solution repair(Solution repr, double startTemperature, double endTemperature, int numIterations, int numChanges) {
        ValidationResult resRepr = this.cost(repr);
        double reprCost = resRepr.cost;
        int numIteration = 0;
        boolean reachedFeasibility = resRepr.isFeasible;
        //this.isFeasible(repr);
        //Boolean reachedValidStructure = this.hasValidStructure(repr);

        while (numIteration < numIterations && !reachedFeasibility) {
            Solution neighbor = this.getRandomNeighbor(repr, numChanges);
            ValidationResult resNeighbour = this.cost(neighbor);

            // optional: don't give up feasibility or valid structure once reached
            if (reachedFeasibility && !resNeighbour.isFeasible) {
                continue;
            }
            //if (reachedValidStructure && !this.hasValidStructure(neighbor)){continue;}


            double neighborCost = resNeighbour.cost;
            double temperature = getTemperature(startTemperature, endTemperature, numIteration, numIterations);
            double prob = Math.min(1.0, Math.exp(-(neighborCost - reprCost) / temperature));

            int numInfeasible;
            boolean feasible;

//            if(resNeighbour.cost == resRepr.cost && resNeighbour.numInfeasible != resRepr.numInfeasible){
//                System.out.println("\nsomething wrong");
//                System.out.println(resNeighbour.cost + " " + resRepr.cost + " " + this.hardPenalty);
//                System.out.println(resNeighbour.numInfeasible * this.hardPenalty + " " + resRepr.numInfeasible * this.hardPenalty);
//                System.out.println();
//            }

            //if (resNeighbour.numInfeasible < resRepr.numInfeasible
            //// this.getNumInfeasible(neighbor) < this.getNumInfeasible(repr)
            //) {
            //    repr = this.makeDeepCopy(neighbor);
            //    reprCost = neighborCost;
            //    numInfeasible = resNeighbour.numInfeasible;
            //    feasible = resNeighbour.isFeasible;
            //}
            //else
            if (this.getProbBool(prob)) {
                repr = this.makeDeepCopy(neighbor);
                resRepr = resNeighbour;
                reprCost = neighborCost;
                numInfeasible = resNeighbour.numInfeasible;
                feasible = resNeighbour.isFeasible;
                reprRemoved = new ArrayList<>(removed);
            }
            else{
                numInfeasible = resRepr.numInfeasible;
                feasible = resRepr.isFeasible;
            }

            if (numIteration % 100 == 0 || numIteration + 1 == numIterations) {
                System.out.print("\r        \r");
                System.out.println("numIteration: " + numIteration + "\tFeasible: " + /*this.isFeasible(repr)*/ feasible +
                        "\tCost: " + reprCost + "\tTemperature: " + String.format("%.4f", temperature) + "\tProbability: " + String.format("%.4f", prob) +
                        "\tnumInfeasible: " + /*this.getNumInfeasible(repr)*/ numInfeasible
                        /*+"   " + String.format("%.2f", (100 * */ /*this.getNumInfeasible(repr)*/ /*numInfeasible * this.hardPenalty) / reprCost) + "%"*/);
            }
            else{
                //System.out.print("\r        \r");
                //System.out.print(numIteration);
            }
            numIteration++;

            // optional: don't give up feasibility or valid structure once reached
            if (!reachedFeasibility && /*this.isFeasible(repr)*/ feasible) {
                reachedFeasibility = true;
                System.out.println("Reached feasibility in iteration: " + numIteration);
            }
            //if (!reachedValidStructure && this.hasValidStructure(repr)){reachedValidStructure = true;}

            //if (reprCost == 0.0 && this.isFeasible(repr)) {break;}
            //if (reprCost == 0.0 && this.isFeasible(repr) && this.hasValidStructure(repr)) {break;}
            if (resRepr.cost == 0.0){break;}
        }
        return repr;
    }

    private Solution getRandomNeighbor(Solution repr, int numChanges) {
        Solution neighbor = this.makeDeepCopy(repr);
        removed = new ArrayList<>(reprRemoved);
        for (int numChange = 0; numChange < numChanges; numChange++) {
            // Determines the class to change
            int indexSolution = ThreadLocalRandom.current().nextInt(0, neighbor.classes.size());
            // Determines what part of the solution to change
            int indexSolutionPart = ThreadLocalRandom.current().nextInt(0, 3);
            switch (indexSolutionPart) {
                case 0:
                    // change room (done) - add check for unaivailableweeks:
//                    int i = 0;
//                    int classId = neighbor.classes.get(indexSolution).classId;
//                    CourseClass courseClass = instance.getClassForId(classId);
//                    if(courseClass.roomNeeded && courseClass.roomPenalties.size() > 1){
//                        while (true) {
//                            i++;
//                            //int selectedCourseIdx = neighbor.classes.get(indexSolution).classId - 1;
//                            int selectedCourseId = neighbor.classes.get(indexSolution).classId;
//                            int newRoomId = ThreadLocalRandom.current().nextInt(1, this.numRooms + 1);
//
//                            //System.out.println(selectedCourseIdx + " " + newRoomId + " " + indexSolution);
//                            //if (this.courseClasses.get(selectedCourseIdx).roomPenalties.get(newRoomId) != null
//                            //System.out.println(selectedCourseIdx);
//                            if (this.instance.getClassForId(selectedCourseId).roomPenalties.get(newRoomId) != null
//                                    && isAvailableWeeks(this.instance.rooms.get(newRoomId - 1).unaivailableweeks, newRoomId - 1, neighbor, indexSolution)) {
//                                neighbor.classes.get(indexSolution).roomId = newRoomId;
//                                break;
//                            }
//                            if(i > 10000){
//                                break;
//                            }
//
//                        }
//                    }
//                  ###### Changed to randomly pick an available room instead of picking any room and checking whether it is available
//                    SolutionClass changeClass = neighbor.classes.get(indexSolution);
//                    CourseClass courseClass = instance.getClassForId(changeClass.classId);
//                    // Loop over rooms for this class
//                    ArrayList<Integer> availableRooms = new ArrayList<>();
//                    for (Integer roomId : courseClass.roomPenalties.keySet()) {
//                        changeClass.roomId = roomId;
//                        for (Unavailability U : instance.getRoom(roomId).unaivailableweeks) {
//                            if (!ImplicitAvailability.overlaps(changeClass, U)) {
//                                availableRooms.add(roomId);
//                                break;
//                            }
//                        }
//                    }
//                    if (availableRooms.size() == 0) {
//                        System.out.println("No available rooms!!!");
//                    }
//                    int newRoomId = availableRooms.get(ThreadLocalRandom.current().nextInt(availableRooms.size()));
//                    changeClass.roomId = newRoomId;

                    // ##### random room #####
                    // TODO: choose with a probability proportional to the penalty
                    SolutionClass changeClass = neighbor.classes.get(indexSolution);
                    CourseClass courseClass = instance.getClassForId(changeClass.classId);
                    if(courseClass.roomNeeded) {
                        ArrayList<Integer> rooms = new ArrayList<>(courseClass.roomPenalties.keySet());
                        int randIdx = ThreadLocalRandom.current().nextInt(rooms.size());
                        changeClass.roomId = rooms.get(randIdx);
                    }
                    // #### penalty probability ####
//                    SolutionClass changeClass = neighbor.classes.get(indexSolution);
//                    CourseClass courseClass = instance.getClassForId(changeClass.classId);
//                    if(courseClass.roomNeeded) {
//                        int newRoom = selectRandomRoom(changeClass);
//                        changeClass.roomId = newRoom;
//                    }
                    break;
            case 1:
                // change time (done):
                // TODO: maybe change this to choose times that are available (like in the rooms)
//                int selectedCourseIdx = neighbor.classes.get(indexSolution).classId - 1;
//                int idxNewTime = ThreadLocalRandom.current().nextInt(0, this.courseTimes.get(selectedCourseIdx).size());
//                neighbor.classes.get(indexSolution).days = this.getBitSetFromString(this.courseTimes.get(selectedCourseIdx).get(idxNewTime).get(0));
//                neighbor.classes.get(indexSolution).weeks = this.getBitSetFromString(this.courseTimes.get(selectedCourseIdx).get(idxNewTime).get(1));
//                neighbor.classes.get(indexSolution).start = Integer.parseInt(this.courseTimes.get(selectedCourseIdx).get(idxNewTime).get(2));
                    ArrayList<CourseTime> times = instance.getClassForId(neighbor.classes.get(indexSolution).classId).times;
                    int idxNewTime = ThreadLocalRandom.current().nextInt(0, times.size());
                    CourseTime newTime = times.get(idxNewTime);
                    neighbor.classes.get(indexSolution).days = newTime.days;
                    neighbor.classes.get(indexSolution).weeks = newTime.weeks;
                    neighbor.classes.get(indexSolution).start = newTime.start;
                    neighbor.classes.get(indexSolution).length = newTime.length;
                    break;
                case 2:
                    // change students:
                    // TODO: update this to work on ArrayLists instead of BitSet
                    if (this.numStudents != 0) {
                        for (int bitFlip = 0; bitFlip < this.bitFlipsStudents; bitFlip++) {
                            int flipIndex = ThreadLocalRandom.current().nextInt(0, this.numStudents);
//                            neighbor.classes.get(indexSolution).students.flip(flipIndex);
                        }
                    }
                    break;
                case 3:
                    // add or remove classes
                    int origin;
                    int bound;
                    //only remove when there are more than 3 classes
                    if(neighbor.classes.size() > 3){
                        origin=0;
                    }else origin=1;
                    // only add when there are classes to add
                    if(removed.size()>0){
                        bound = 2;
                    }else bound=1;
                    int addOrRemove = ThreadLocalRandom.current().nextInt(origin, bound);
                    if(addOrRemove == 0){
                        // removing a random class
//                        System.out.println("removing");
                        int removeIdx = ThreadLocalRandom.current().nextInt(neighbor.classes.size());
                        SolutionClass removeClass = neighbor.classes.get(removeIdx);
                        // To make sure that we only add courses once to the list
                        if(removed.stream().noneMatch(r->r.classId == removeClass.classId)){
                            removed.add(removeClass);
                        }
                        neighbor.classes.remove(removeIdx);
                    }else{
                        boolean added = false;
                        int tries = 0;
                        while(!added) {
                            // adding any class that has been removed before
//                        System.out.println("adding");
                            int addIdx = ThreadLocalRandom.current().nextInt(removed.size());
                            SolutionClass addClass = removed.get(addIdx);
                            if (neighbor.classes.stream().noneMatch(c -> c.classId == addClass.classId)) {
                                neighbor.classes.add(addClass);
                                removed.remove(addIdx);
                                added=true;
                            }
                            if(tries > 10000){
                                added=true;
                            }
                            tries++;
                        }
                    }
                    break;
            }
        }
        return neighbor;
    }


ArrayList<SolutionClass> reprRemoved = new ArrayList<>();
ArrayList<SolutionClass> removed;


    private int selectRandomRoom(SolutionClass changeClass) {
        CourseClass courseClass = instance.getClassForId(changeClass.classId);

        ArrayList<Integer> rooms = new ArrayList<>(courseClass.roomPenalties.keySet());
        List<Integer> penalties = rooms.stream().mapToInt(r -> courseClass.roomPenalties.get(r)).boxed().collect(Collectors.toList());
        final double sumPenalty = penalties.stream().mapToInt(r->r).sum();
        final double maxPenalty = penalties.stream().mapToInt(r->r).max().getAsInt();
        int k= 1;
        int T = 1;
        double boltzmannNormalization = penalties.stream()
//                .mapToDouble(r -> (1 - (r/maxPenalty) ))
                .mapToDouble(penalty -> Math.exp(-penalty / k * T)).sum();
        List<Double> probs = penalties.stream()
//                .mapToDouble(r -> (1 - (r/maxPenalty) ))
                .mapToDouble(penalty->Math.exp(-penalty/k*T)/boltzmannNormalization)
                .boxed().collect(Collectors.toList());
        // invert and normalize

        double p = Math.random();
        double cumulativeProbability = 0.0;
        for (int roomIdx = 0; roomIdx < rooms.size(); roomIdx++) {
            cumulativeProbability += probs.get(roomIdx);
            if (p <= cumulativeProbability) {
                return rooms.get(roomIdx);
            }
        }
        throw new IllegalStateException("Invalid probability distribution");

    }


    private ValidationResult cost(Solution repr) {

        double cost = 0.0;
        int infeasibleViolated = 0;

        Future<Boolean>[] futures = new Future[instance.distributions.size()];
        List<Distribution> distributions = instance.distributions;
        // Validating in reverse order as the implicit constraints are in the back of the array
        for (int i = distributions.size() - 1; i >= 0; i--) {
            Distribution distribution = distributions.get(i);
            Future<Boolean> submit = pool.submit(new ValidationRunnable(distribution, instance, repr));
            futures[i] = submit;
        }

        // constraint penalty:
        // Get the results in opposite order. This gives the Implicit constraints the most time
        List<Distribution> distributionList = this.instance.distributions;
        for (int i = 0; i < distributionList.size(); i++) {
            Distribution dist = distributionList.get(i);
//            boolean valid = dist.validate(this.instance, repr);
            boolean valid = false;
            try {
                valid = futures[i].get();
            } catch (InterruptedException e) {
                e.printStackTrace();
            } catch (ExecutionException e) {
                e.printStackTrace();
            }
            if (!valid) {
                //System.out.println(this.getMaxPenalty(this.instance));

                if (dist.required) {
                    infeasibleViolated++;
                    cost += dist.getPenalty();//this.hardPenalty;
                } else {
                    cost += dist.getPenalty();
                }
            }
        }
        // room penalty:nt i = 0;
//                    int classId = neighbor.classes.get(indexSolution).classId;
//                    CourseClass courseClass = instance.getClassForId(classId);
//                    if(courseClass.roomNeeded && courseClass.roomPenalties.size() > 1){
//                        while (true) {
//                            i++;
//                            int selectedCourseIdx = neighbor.classes.get(indexSolution).classId - 1;
//                            int newRoomId = ThreadLocalRandom.current().nextInt(1, this.numRooms + 1);
//                            if (this.courseClasses.get(selectedCourseIdx).roomPenalties.get(newRoomId) != null
//                                    && isAvailableWeeks(this.instance.rooms.get(newRoomId - 1).unaivailableweeks, newRoomId - 1, neighbor, indexSolution)) {
//                                neighbor.classes.get(indexSolution).roomId = newRoomId;
//                                break;
//                            }
//                            if(i > 10000){
//                                break;
//                            }
//
//                        }
//                    }
        int roomPenalty = 0;
        for (SolutionClass solClass : repr.classes) {
            int classId = solClass.classId;
            int roomId = solClass.roomId;
            CourseClass courseClass = instance.getClassForId(classId);
            if(courseClass.roomNeeded){
                Integer penalty = courseClass.roomPenalties.get(roomId);
                if(penalty!=null) roomPenalty+=penalty;
            }
//            Boolean foundRoomPenalty = false;
//            for (int i = 0; i < this.instance.courses.size(); i++) {
//                for (int j = 0; j < this.instance.courses.get(i).configs.size(); j++) {
//                    for (int k = 0; k < this.instance.courses.get(i).configs.get(j).subparts.size(); k++) {
//                        for (int l = 0; l < this.instance.courses.get(i).configs.get(j).subparts.get(k).classes.size(); l++) {
//                            // room penalty:
//                            if (Integer.parseInt(this.instance.courses.get(i).configs.get(j).subparts.get(k).classes.get(l).id) == classId) {
//                                try {
//                                    cost += this.instance.courses.get(i).configs.get(j).subparts.get(k).classes.get(l).roomPenalties.get(roomId);
//                                    foundRoomPenalty = true;
//                                    break;
//                                } catch (NullPointerException e) {
//                                    continue;
//                                }
//                            }
//                        }
//                        if (foundRoomPenalty) {
//                            break;
//                        }
//                    }
//                    if (foundRoomPenalty) {
//                        break;
//                    }
//                }
//                if (foundRoomPenalty) {
//                    break;
//                }
//            }
        }
        cost += roomPenalty;

        // time penalty:
        int timePenalty = 0;
        for (SolutionClass solClass : repr.classes) {
            int classId = solClass.classId;
            CourseClass courseClass = instance.getClassForId(classId);
            //TODO: rewrite
            BitSet weeks = solClass.weeks;
            BitSet days = solClass.days;
            int start = solClass.start;
            Boolean foundTimePenalty = false;
//            ArrayList<ArrayList<String>> item = this.courseTimes.get(solClass.classId - 1);
//            for (int i = 0; i < item.size(); i++) {
//                //System.out.println(item.get(i).get(0) +" "+ BitSets.toBitString(solClass.days, this.numDays) + " " + item.get(i).get(0).equals(BitSets.toBitString(solClass.days, this.numDays)));
//                if (item.get(i).get(0).equals(BitSets.toBitString(solClass.days, this.numDays))
//                        && item.get(i).get(1).equals(BitSets.toBitString(solClass.weeks, this.numWeeks))
//                        && Integer.parseInt(item.get(i).get(2)) == solClass.start) {
//                    timePenalty += Integer.parseInt(item.get(i).get(3));
//                    break;
//                }
//            }
            ArrayList<CourseTime> items = instance.getClassForId(classId).times;
            for (CourseTime item : items) {
                //System.out.println(item.get(i).get(0) +" "+ BitSets.toBitString(solClass.days, this.numDays) + " " + item.get(i).get(0).equals(BitSets.toBitString(solClass.days, this.numDays)));
                if (item.days == solClass.days
                        && item.weeks == solClass.weeks
                        && item.start == solClass.start) {
                    timePenalty += item.penalty;
                    break;
                }
            }
        }
        cost+= timePenalty;
        return new ValidationResult(cost, infeasibleViolated, infeasibleViolated == 0);
    }

    private double getTemperature(double startTemperature, double endTemperature, int numIteration, int numIterations) {
        // linear:
        //return startTemperature - numIteration * startTemperature / numIterations;

        // exponential:
        return startTemperature * Math.exp((double) numIteration / (double) numIterations * Math.log(endTemperature / startTemperature));
    }

    class ValidationResult{

        ValidationResult(double cost, int numInfeasible, boolean isFeasible) {
            this.cost = cost;
            this.numInfeasible = numInfeasible;
            this.isFeasible = isFeasible;
        }

        double cost;
        int numInfeasible;
        boolean isFeasible;
    }

    class ValidationRunnable implements Callable<Boolean> {

        private Distribution distribution;
        private Instance instance;
        private Solution repr;

        public ValidationRunnable(Distribution distribution, Instance instance, Solution repr) {
            this.distribution = distribution;
            this.instance = instance;
            this.repr = repr;
        }

        @Override
        public Boolean call() {
            return distribution.validate(instance, repr);
        }
    }

 // Main:
    public static void main(String[] args) {

        InstanceParser parser;
        Instance instance;
        GeneticAlgorithm S;
        Solution[] init;
        Solution solution;

        try {
        	String instanceFileName = "wbg-fal10.xml";
//            String instanceFileName = "bet-sum18.xml";
//            ILP.ILP.main(null);
//            String instanceFileName = "lums-sum17.xml";
//            String instanceFileName = "tg-fal17.xml";
//            String instanceFileName = "pu-c8-spr07.xml";

            parser = new InstanceParser(instanceFileName);
            instance = parser.parse();
            System.out.println(instance.toString());
            S = new GeneticAlgorithm(400, 100, instance);
//            init = ILP.ILP.sol;
            init = new Solution[100];
            for (int i=0; i<100;i++) {
            	init[i] = S.initRepresentation(instance);
            }
            solution = S.simulateEvolution(init);
            //solution = S.repair(solution);
            //solution = S.optimize(init, 0.2, 0.1, 4000000, 1);
            String solutionText = solution.serialize();
            System.out.println(solutionText);
            System.out.print("Violated Constraints: ");
            for (Distribution dist : instance.distributions) {
                if (!dist.validate(instance, solution)) {
                    System.out.print(dist.type + ",");
                    if (dist.required) {
                        System.out.print("(req)");
                    }
                    System.out.print(",");
                }
            }
            System.out.println();
            S.cost(solution);
            solution.saveToFile(String.format("solution-%d.xml", System.currentTimeMillis()));
            System.out.println(S.cost(solution).cost);
            System.out.println(S.isFeasible(solution));

        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }

    }

}

//class GeneticAlgorithm:
//    def __init__(self, numGenerations=100, numPopulation=100, max_time_steps=1000):
//        self.numGenerations = numGenerations
//        self.numPopulation = numPopulation
//        self.max_time_steps = max_time_steps
//        self.fitnessOverTime = np.zeros((numGenerations, numPopulation))
//        self.diversityOverTime = np.zeros(numGenerations)
//        # population = "genes"(ANNs) of the robots
//        self.population = []
//        for i in range(numPopulation):
//            # robot
//            # get real number of genes for ANN --> 18*4+4*2
//            self.population.append(Genome(amount_genes=80))
//            # rosenbrock/rastrigin
//            # self.population.append(Genome(amount_genes=2))
//        # self.population = initPopulation()
//
//    def calcDiversity(self):
//        # calc diversity of the gene
//        # diveristy = uniqueness of gene
//        diversity = 0
//        for i in range(self.numPopulation):
//            for j in range(self.numPopulation):
//                diversity += np.linalg.norm(self.population[i].getGenes() - self.population[j].getGenes())
//        return diversity
//
//    def simulateEvolution(self):
//        # init robots with population and let it run until time runs out/done
//        # save fitness for every population-entry
//        # save n-best agents and do crossover, mutation and repopulation
//        sameFitness = 0
//        for i in trange(self.numGenerations, desc="Generation"):
//            with open(f"genomes/generation_{i}.pkl", "wb") as f:
//                pkl.dump(self.population, f)
//            fitnessGeneration = np.zeros(len(self.population))
//            # not sure about axis
//            diversityGeneration = self.calcDiversity()
//
//            # Do simulation here
//            multiprocessing = True
//            if multiprocessing:
//                # MULTIPROCESSING
//                pool = ThreadPool(4)
//                fitnessGeneration = pool.map(self.parallelSim, self.population)
//                pool.close()
//                pool.join()
//            else:
//                # SINGLE PROCESSING
//                for j, genome in enumerate(tqdm(self.population, desc="Population")):
//                    # Change the roomNumber to a specific room to train it only on one
//                    sim = Simulation(itlimit=self.max_time_steps, update=None, roomNumber=np.random.randint(0, 6))
//                    fitnessGeneration[j] = sim.run(genome)
//
//            # at the end add the average fitness of this generation
//            fitnessGeneration = np.array(fitnessGeneration)
//            self.diversityOverTime[i] = np.mean(diversityGeneration)
//            self.fitnessOverTime[i] = fitnessGeneration
//            print((f'Mean fitness: {np.mean(fitnessGeneration):2.4f} max: {np.max(fitnessGeneration):2.4f}'
//                   f' diversity: {diversityGeneration:8.1f} zero fitness: {np.sum(fitnessGeneration == 0)}'))
//            # rank the best 20% of the population and breed them
//            bestGenomesInd = np.argsort(fitnessGeneration)[-int(self.numPopulation*0.2):]
//            # do crossover and mutation stuff --> change population
//            self.population = self.createOffspring(bestGenomesInd)
//
//            # Save the diversity and fitness
//            with open(f'diversity.pkl', 'wb') as f:
//                pkl.dump(self.diversityOverTime, f)
//            with open(f'fitness.pkl', 'wb') as f:
//                pkl.dump(self.fitnessOverTime, f)
//
//            # other Stoppingcriteria:
//            if diversityGeneration == 0:
//                break
//            # if mean fitness is the same as max fitness for few generations
//            if np.round(np.mean(fitnessGeneration), 5) == np.round(np.max(fitnessGeneration), 5):
//                sameFitness += 1
//                if sameFitness == 5:
//                    break
//
//    def parallelSim(self, genome):
//        # NOTE: room without obstacles is not used
//        sim = Simulation(itlimit=self.max_time_steps, update=None, roomNumber=np.random.randint(1, 6))
//        return sim.run(genome)
//
//    def createOffspring(self, bestGenomes):
//        newPops = []
//        i = 0
//        while len(newPops) < self.numPopulation:
//            # RIGHT NOW ARITHMETIC IS NOT USED
//            child1, child2 = self.population[bestGenomes[i]].crossover(
//                                self.population[bestGenomes[np.random.randint(0, len(bestGenomes))]], np.random.randint(2))
//            newPops.append(child1)
//            if child2 is not None:
//                newPops.append(child2)
//            i = (i+1) % len(bestGenomes)
//        newPops = newPops[:self.numPopulation]
//        # after: do mutation
//        for p in newPops:
//            p.mutate()
//
//        return newPops
//
//    def rosenbrockGenome(self, genome):
//        return self.rosenbrock(genome.genes[0], genome.genes[1])
//
//    def rastriginGenome(self, genome):
//        return self.rastrigin(genome.genes[0], genome.genes[1])
//
//    def rosenbrock(self, x, y):
//        return (0 - x) ** 2 + 100 * (y - x ** 2) ** 2
//
//    def rastrigin(self, x, y):
//        return 10 * 2 * np.sum(x ** 2.0 - 10 * np.cos(2 * np.pi * x) + y ** 2.0 - 10 * np.cos(2 * np.pi * y))
//
//
//def main():
//    numGenerations = 50
//    numPopulation = 512
//    max_time_steps = 1000
//    ga = GeneticAlgorithm(numGenerations, numPopulation, max_time_steps)
//    ga.simulateEvolution()