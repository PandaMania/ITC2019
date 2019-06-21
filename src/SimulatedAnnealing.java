//TODO:
// Optimize:    multiple calls to validation. Not needed!!!
//              Random neighbor selection has to be optimized
//              Debug validation


import Parsing.*;
import entities.*;
import entities.course.CourseTime;
import entities.distribution.ImplicitAvailability;
import util.*;
import entities.course.CourseClass;
import entities.distribution.Distribution;

import java.io.*;
import java.util.*;
import java.util.concurrent.*;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
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
    private ExecutorService pool = Executors.newFixedThreadPool(4);
    private ArrayList<ArrayList<Integer>> classesSameDays;
    private ArrayList<ArrayList<Integer>> classesSameWeeks;
    private ArrayList<ArrayList<Integer>> classesSameRoom;
    private ArrayList<ArrayList<Integer>> classesSameStart;
    private ArrayList<ArrayList<Integer>> classesSameTime;
    private ArrayList<ArrayList<Integer>> classesSameAttendees;

    // Constructor:
    public SimulatedAnnealing(Instance instance) {
        this.instance = instance;
        this.numRooms = this.instance.rooms.size();
        this.numDays = this.instance.days;
        this.numWeeks = this.instance.weeks;
        this.numStudents = this.instance.students.size();
        this.slotsPerDay = this.instance.slotsPerDay;
        this.bitFlipsStudents = (int) Math.max(1.0, 0.75 * this.numStudents);    // specify how many of the bits should be changed
        this.hardPenalty = 1.0 * this.getMaxPenalty(this.instance);
        this.courseClasses = this.getCourseClasses();
//        this.courseTimes = this.getCourseTimes();
        System.out.println("soft constraints max penalty: " + this.getMaxPenalty(this.instance) + "\thard constraints penalty: " + this.hardPenalty);
        this.classesSameDays = this.getClassesSameAttribute("SameDays");
        this.classesSameWeeks = this.getClassesSameAttribute("SameWeeks");
        this.classesSameRoom = this.getClassesSameAttribute("SameRoom");
        this.classesSameStart = this.getClassesSameAttribute("SameStart");
        this.classesSameTime = this.getClassesSameAttribute("SameTime");
        this.classesSameAttendees = this.getClassesSameAttribute("SameAttendees");
    }

    private ArrayList<ArrayList<Integer>> getClassesSameAttribute(String attribute){
        ArrayList<ArrayList<Integer>> result = new ArrayList<>();
        for (Distribution dist : this.instance.distributions){
            if (dist.type.equals(attribute) && dist.required){
                if (result.size() == 0){
                    result.add(dist.idInDistribution);
                } else {
                    boolean addNewRow = true;
                    for (int i=0;i<result.size();i++){
                        if (result.get(i) == dist.idInDistribution){
                            addNewRow = false;
                            break;
                        }
                    }
                    if (addNewRow){
                        result.add(dist.idInDistribution);
                    }
                }
            }
        }
        return result;
    }

    private class boolIntArray{
        public boolean boolVal = false;
        public ArrayList<Integer> intArray = new ArrayList<>();
    }

    private boolIntArray classIdInRequiredSameArrayForAttribute(int id, String attribute){
        boolIntArray result = new boolIntArray();
        ArrayList<Integer> emptyArray = new ArrayList<>();
        if (attribute.equals("SameDays")){
            for (int i=0;i<this.classesSameDays.size();i++){
                if (this.classesSameDays.get(i).contains(id)){
                    result.boolVal = true;
                    result.intArray = this.classesSameDays.get(i);
                    break;
                }
            }
        } else if (attribute.equals("SameWeeks")){
            for (int i=0;i<this.classesSameWeeks.size();i++){
                if (this.classesSameWeeks.get(i).contains(id)){
                    result.boolVal = true;
                    result.intArray = this.classesSameWeeks.get(i);
                    break;
                }
            }
        } else if (attribute.equals("SameRoom")){
            for (int i=0;i<this.classesSameRoom.size();i++){
                if (this.classesSameRoom.get(i).contains(id)){
                    result.boolVal = true;
                    result.intArray = this.classesSameRoom.get(i);
                    break;
                }
            }
        } else if (attribute.equals("SameStart")){
            for (int i=0;i<this.classesSameStart.size();i++){
                if (this.classesSameStart.get(i).contains(id)){
                    result.boolVal = true;
                    result.intArray = this.classesSameStart.get(i);
                    break;
                }
            }
        } else if (attribute.equals("SameTime")){
            for (int i=0;i<this.classesSameTime.size();i++){
                if (this.classesSameTime.get(i).contains(id)){
                    result.boolVal = true;
                    result.intArray = this.classesSameTime.get(i);
                    break;
                }
            }
        } else if (attribute.equals("SameAttendees")){
            for (int i=0;i<this.classesSameAttendees.size();i++){
                if (this.classesSameAttendees.get(i).contains(id)){
                    result.boolVal = true;
                    result.intArray = this.classesSameAttendees.get(i);
                    break;
                }
            }
        }
        return result;
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

    private class roomCourseTime{
        public int roomId;
        public CourseTime ct;
    }

    private Solution getRandomNeighbor(Solution repr, int numChanges) {
        Solution neighbor = this.makeDeepCopy(repr);
        removed = new ArrayList<>(reprRemoved);
        for (int numChange = 0; numChange < numChanges; numChange++) {
            // Determines the class to change
            int indexSolution = ThreadLocalRandom.current().nextInt(0, neighbor.classes.size());
            // Determines what part of the solution to change
            int indexSolutionPart = ThreadLocalRandom.current().nextInt(0, 3);
            boolean repeat;
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
                    repeat = true;
                    while (repeat){
                        repeat = false;
                        SolutionClass changeClass = neighbor.classes.get(indexSolution);
                        CourseClass courseClass = instance.getClassForId(changeClass.classId);
                        if(courseClass.roomNeeded) {
                            ArrayList<Integer> rooms = new ArrayList<>(courseClass.roomPenalties.keySet());
                            int randIdx = ThreadLocalRandom.current().nextInt(rooms.size());
                            changeClass.roomId = rooms.get(randIdx);

                            if (this.classIdInRequiredSameArrayForAttribute(neighbor.classes.get(indexSolution).classId, "SameRoom").boolVal) {
                                for (int id2:this.classIdInRequiredSameArrayForAttribute(neighbor.classes.get(indexSolution).classId, "SameRoom").intArray){
                                    if (id2 != neighbor.classes.get(indexSolution).classId){
                                        int indexSolution2 = -1;
                                        for (int index2=0;index2<neighbor.classes.size();index2++){
                                            if (neighbor.classes.get(index2).classId == id2){
                                                indexSolution2 = index2;
                                                break;
                                            }
                                        }
                                        if(!this.instance.getClassForId(neighbor.classes.get(indexSolution2).classId).roomPenalties.containsKey(changeClass.roomId)){
                                            repeat = true;
                                            continue;
                                        }
                                        neighbor.classes.get(indexSolution2).roomId = changeClass.roomId;
                                    }
                                }
                            }

                        }
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
                    repeat = true;
                    while (repeat){
                        repeat = false;
                        ArrayList<CourseTime> times = instance.getClassForId(neighbor.classes.get(indexSolution).classId).times;
                        int idxNewTime = ThreadLocalRandom.current().nextInt(0, times.size());
                        CourseTime newTime = times.get(idxNewTime);
                        neighbor.classes.get(indexSolution).days = newTime.days;
                        neighbor.classes.get(indexSolution).weeks = newTime.weeks;
                        neighbor.classes.get(indexSolution).start = newTime.start;
                        neighbor.classes.get(indexSolution).length = newTime.length;
                        if (this.classIdInRequiredSameArrayForAttribute(neighbor.classes.get(indexSolution).classId, "SameDays").boolVal) {
                            for (int id2:this.classIdInRequiredSameArrayForAttribute(neighbor.classes.get(indexSolution).classId, "SameDays").intArray){
                                if (id2 != neighbor.classes.get(indexSolution).classId){
                                    int indexSolution2 = -1;
                                    for (int index2=0;index2<neighbor.classes.size();index2++){
                                        if (neighbor.classes.get(index2).classId == id2){
                                            indexSolution2 = index2;
                                            break;
                                        }
                                    }
                                    ArrayList<CourseTime> times2 = new ArrayList<>();
                                    for (CourseTime time2 : instance.getClassForId(id2).times){
                                        if ((BitSets.or(time2.days, neighbor.classes.get(indexSolution).days).equals(neighbor.classes.get(indexSolution).days)) ||
                                                (BitSets.or(time2.days, neighbor.classes.get(indexSolution).days).equals(time2.days))){
                                            times2.add(time2);
                                        }
                                    }
                                    if(times2.size() == 0){
                                        repeat = true;
                                        continue;
                                    }
                                    int idxNewTime2 = ThreadLocalRandom.current().nextInt(0, times2.size());
                                    CourseTime newTime2 = times2.get(idxNewTime2);
                                    neighbor.classes.get(indexSolution2).days = newTime2.days;
                                    neighbor.classes.get(indexSolution2).weeks = newTime2.weeks;
                                    neighbor.classes.get(indexSolution2).start = newTime2.start;
                                    neighbor.classes.get(indexSolution2).length = newTime2.length;
                                }
                            }
                        }

                        if (this.classIdInRequiredSameArrayForAttribute(neighbor.classes.get(indexSolution).classId, "SameWeeks").boolVal) {
                            for (int id2:this.classIdInRequiredSameArrayForAttribute(neighbor.classes.get(indexSolution).classId, "SameWeeks").intArray){
                                if (id2 != neighbor.classes.get(indexSolution).classId){
                                    int indexSolution2 = -1;
                                    for (int index2=0;index2<neighbor.classes.size();index2++){
                                        if (neighbor.classes.get(index2).classId == id2){
                                            indexSolution2 = index2;
                                            break;
                                        }
                                    }
                                    ArrayList<CourseTime> times2 = new ArrayList<>();
                                    for (CourseTime time2 : instance.getClassForId(id2).times){
                                        if ((BitSets.or(time2.weeks, neighbor.classes.get(indexSolution).weeks).equals(neighbor.classes.get(indexSolution).weeks)) ||
                                                (BitSets.or(time2.weeks, neighbor.classes.get(indexSolution).weeks).equals(time2.weeks))){
                                            times2.add(time2);
                                        }
                                    }
                                    if(times2.size() == 0){
                                        repeat = true;
                                        continue;
                                    }
                                    int idxNewTime2 = ThreadLocalRandom.current().nextInt(0, times2.size());
                                    CourseTime newTime2 = times2.get(idxNewTime2);
                                    neighbor.classes.get(indexSolution2).days = newTime2.days;
                                    neighbor.classes.get(indexSolution2).weeks = newTime2.weeks;
                                    neighbor.classes.get(indexSolution2).start = newTime2.start;
                                    neighbor.classes.get(indexSolution2).length = newTime2.length;
                                }
                            }
                        }

                        if (this.classIdInRequiredSameArrayForAttribute(neighbor.classes.get(indexSolution).classId, "SameStart").boolVal) {
                            for (int id2:this.classIdInRequiredSameArrayForAttribute(neighbor.classes.get(indexSolution).classId, "SameStart").intArray){
                                if (id2 != neighbor.classes.get(indexSolution).classId){
                                    int indexSolution2 = -1;
                                    for (int index2=0;index2<neighbor.classes.size();index2++){
                                        if (neighbor.classes.get(index2).classId == id2){
                                            indexSolution2 = index2;
                                            break;
                                        }
                                    }
                                    ArrayList<CourseTime> times2 = new ArrayList<>();
                                    for (CourseTime time2 : instance.getClassForId(id2).times){
                                        if (time2.start == neighbor.classes.get(indexSolution).start){
                                            times2.add(time2);
                                        }
                                    }
                                    if(times2.size() == 0){
                                        repeat = true;
                                        continue;
                                    }
                                    int idxNewTime2 = ThreadLocalRandom.current().nextInt(0, times2.size());
                                    CourseTime newTime2 = times2.get(idxNewTime2);
                                    neighbor.classes.get(indexSolution2).days = newTime2.days;
                                    neighbor.classes.get(indexSolution2).weeks = newTime2.weeks;
                                    neighbor.classes.get(indexSolution2).start = newTime2.start;
                                    neighbor.classes.get(indexSolution2).length = newTime2.length;
                                }
                            }
                        }

                        if (this.classIdInRequiredSameArrayForAttribute(neighbor.classes.get(indexSolution).classId, "SameTime").boolVal) {
                            for (int id2:this.classIdInRequiredSameArrayForAttribute(neighbor.classes.get(indexSolution).classId, "SameTime").intArray){
                                if (id2 != neighbor.classes.get(indexSolution).classId){
                                    int indexSolution2 = -1;
                                    for (int index2=0;index2<neighbor.classes.size();index2++){
                                        if (neighbor.classes.get(index2).classId == id2){
                                            indexSolution2 = index2;
                                            break;
                                        }
                                    }
                                    ArrayList<CourseTime> times2 = new ArrayList<>();
                                    for (CourseTime time2 : instance.getClassForId(id2).times){
                                        if ((time2.start <= neighbor.classes.get(indexSolution).start &&
                                                neighbor.classes.get(indexSolution).start + neighbor.classes.get(indexSolution).length <= time2.start + time2.length) ||
                                                (neighbor.classes.get(indexSolution).start <= time2.start  &&
                                                        time2.start + time2.length <= neighbor.classes.get(indexSolution).start + neighbor.classes.get(indexSolution).length)){
                                            times2.add(time2);
                                        }
                                    }
                                    if(times2.size() == 0){
                                        repeat = true;
                                        continue;
                                    }
                                    int idxNewTime2 = ThreadLocalRandom.current().nextInt(0, times2.size());
                                    CourseTime newTime2 = times2.get(idxNewTime2);
                                    neighbor.classes.get(indexSolution2).days = newTime2.days;
                                    neighbor.classes.get(indexSolution2).weeks = newTime2.weeks;
                                    neighbor.classes.get(indexSolution2).start = newTime2.start;
                                    neighbor.classes.get(indexSolution2).length = newTime2.length;
                                }
                            }
                        }

                        /*if (this.classIdInRequiredSameArrayForAttribute(neighbor.classes.get(indexSolution).classId, "SameAttendees").boolVal) {
                            for (int id2:this.classIdInRequiredSameArrayForAttribute(neighbor.classes.get(indexSolution).classId, "SameAttendees").intArray){
                                if (id2 != neighbor.classes.get(indexSolution).classId){
                                    int indexSolution2 = -1;
                                    for (int index2=0;index2<neighbor.classes.size();index2++){
                                        if (neighbor.classes.get(index2).classId == id2){
                                            indexSolution2 = index2;
                                            break;
                                        }
                                    }
                                    ArrayList<roomCourseTime> roomtimes2 = new ArrayList<>();
                                    for (CourseTime time2 : instance.getClassForId(id2).times){
                                        for (int roomId : this.instance.getClassForId(id2).roomPenalties.keySet()){
                                        //for (int roomId : this.instance.getRoom(neighbor.classes.get(indexSolution).roomId).distanceToRooms.keySet() ){
                                            if (!this.instance.getRoom(roomId).distanceToRooms.keySet().contains(neighbor.classes.get(indexSolution).roomId)){
                                                continue;
                                            }

                                            if ((neighbor.classes.get(indexSolution).start + neighbor.classes.get(indexSolution).length + this.instance.getRoom(neighbor.classes.get(indexSolution).roomId).distanceToRooms.get(roomId) <= time2.start) ||
                                                    (time2.start + time2.length + this.instance.getRoom(roomId).distanceToRooms.get(neighbor.classes.get(indexSolution).roomId) <= neighbor.classes.get(indexSolution).start) ||
                                                    (BitSets.and(neighbor.classes.get(indexSolution).days, time2.days).isEmpty()) ||
                                                    (BitSets.and(neighbor.classes.get(indexSolution).weeks, time2.weeks).isEmpty())){
                                                roomCourseTime roomtime2 = new roomCourseTime();
                                                roomtime2.roomId = roomId;
                                                roomtime2.ct = time2;
                                                roomtimes2.add(roomtime2);
                                            }
                                        }
                                    }
                                    if(roomtimes2.size() == 0){
                                        repeat = true;
                                        continue;
                                    }

                                    int idxNewRoomTime2 = ThreadLocalRandom.current().nextInt(0, roomtimes2.size());
                                    int roomId2 = roomtimes2.get(idxNewRoomTime2).roomId;
                                    CourseTime newTime2 = roomtimes2.get(idxNewRoomTime2).ct;
                                    neighbor.classes.get(indexSolution2).roomId = roomId2;
                                    neighbor.classes.get(indexSolution2).days = newTime2.days;
                                    neighbor.classes.get(indexSolution2).weeks = newTime2.weeks;
                                    neighbor.classes.get(indexSolution2).start = newTime2.start;
                                    neighbor.classes.get(indexSolution2).length = newTime2.length;
                                }
                            }
                        }*/

                    }

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
                    cost += this.instance.optDist * dist.getPenalty();      //this.hardPenalty;
                } else {
                    cost += this.instance.optDist *  dist.getPenalty();
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
        cost += this.instance.optRoom * roomPenalty;

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
        cost+= this.instance.optTime * timePenalty;
        return new ValidationResult(cost, infeasibleViolated, infeasibleViolated == 0);
    }

    private double getTemperature(double startTemperature, double endTemperature, int numIteration, int numIterations) {
        // linear:
        //return startTemperature - numIteration * startTemperature / numIterations;

        // exponential:
        return startTemperature * Math.exp((double) numIteration / (double) numIterations * Math.log(endTemperature / startTemperature));
    }

    private int getNumChanges(int numChangesStart, int numIteration, int numIterations) {
        double result;
        // linear:
        //result = (double) numChangesStart - (double) numIteration * (double) numChangesStart / (double) numIterations;

        // exponential:
        result =  (double) numChangesStart * Math.exp((double) numIteration / (double) numIterations * Math.log(1.0 / (double) numChangesStart));
        return (int) Math.round(result);
    }
    //private Boolean hasValidStructure(Solution repr){
    //    return false;
    //}

    private Solution optimize(Solution repr, double startTemperature, double endTemperature, int numIterations, int numChangesStart, boolean useShrinking) {
        ValidationResult resRepr = this.cost(repr);
        double reprCost = resRepr.cost;
        int numIteration = 0;
        boolean reachedFeasibility = resRepr.isFeasible;
        //this.isFeasible(repr);
        //Boolean reachedValidStructure = this.hasValidStructure(repr);

        while (numIteration < numIterations) {
            Solution neighbor;
            int numChanges = numChangesStart;
            if (useShrinking){
                numChanges = this.getNumChanges(numChangesStart, numIteration, numIterations);
                neighbor = this.getRandomNeighbor(repr, numChanges);
            } else {
                neighbor = this.getRandomNeighbor(repr, numChanges);
            }

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
                        "\tCost: " + reprCost + "\tTemperature: " + String.format("%.4f", temperature) + "\tnumChanges: " + numChanges + "\tProbability: " + String.format("%.4f", prob) +
                        "\tnumInfeasible: " + /*this.getNumInfeasible(repr)*/ numInfeasible
                        /*+"   " + String.format("%.2f", (100 * */ /*this.getNumInfeasible(repr)*/ /*numInfeasible * this.hardPenalty) / reprCost) + "%"*/);
            }
            else{
                System.out.print("\r        \r");
                System.out.print(numIteration);
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

    // Main:
    public static void main(String[] args) {

        InstanceParser parser;
        Instance instance;
        SimulatedAnnealing S;
        Solution init;
        Solution solution;

        try {
//            String instanceFileName = "bet-sum18.xml";
//            String instanceFileName = "pu-llr-spr07.xml";
//            ILP.ILP.main(null);
//            String instanceFileName = "lums-sum17.xml";
            String instanceFileName = "tg-fal17.xml";
//            String instanceFileName = "pu-c8-spr07.xml";

            parser = new InstanceParser(instanceFileName);
            instance = parser.parse();
            System.out.println(instance.toString());
            S = new SimulatedAnnealing(instance);
//            init = ILP.ILP.sol;
            init = S.initRepresentation(instance);
            solution = S.optimize(init, 50.0, 0.1, 100000, 1, false);
            //solution = S.optimize(init, 0.2, 0.1, 4000000, 1);
            String solutionText = solution.serialize();
            System.out.println(solutionText);
            System.out.print("Violated Constraints: ");
            for (Distribution dist : instance.distributions) {
                if (!dist.validate(instance, solution)) {
                    System.out.print(dist.type);
                    if (dist.required) {
                        System.out.print("(req)");
                    }
                    System.out.print(",");
                }
            }
            System.out.println();
            S.cost(solution);
            solution.saveToFile(String.format("solution-%d.xml", System.currentTimeMillis()));

        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }

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

}




/*

for (SolutionClass solClass : repr.classes) {
    int classId = solClass.classId;
    int roomId = solClass.roomId;
    Boolean foundRoomPenalty = false;
    Boolean foundTimePenalty = false;
    for(int i=0;i<this.instance.courses.size();i++){
        for(int j=0;j<this.instance.courses.get(i).configs.size();j++){
            for(int k=0;k<this.instance.courses.get(i).configs.get(j).subparts.size();k++){
                for(int l=0;l<this.instance.courses.get(i).configs.get(j).subparts.get(k).classes.size();l++){
                    // room penalty:
                    if (Integer.parseInt(this.instance.courses.get(i).configs.get(j).subparts.get(k).classes.get(l).id) == classId){
                        try {
                            if (!foundRoomPenalty){
                                cost += this.instance.courses.get(i).configs.get(j).subparts.get(k).classes.get(l).roomPenalties.get(roomId);
                                foundRoomPenalty = true;
                            }
                        } catch (NullPointerException e) {continue;}
                    }
                    // time penalty:

                    // break:
                    if (foundRoomPenalty && foundTimePenalty){break;}
                }
                if (foundRoomPenalty && foundTimePenalty){break;}
            }
            if (foundRoomPenalty && foundTimePenalty){break;}
        }
        if (foundRoomPenalty && foundTimePenalty){break;}
    }
}

*/
