package entities.distribution;

import entities.Instance;
import entities.Solution;
import entities.SolutionClass;
import entities.course.CourseClass;
import util.Constants;

import java.util.*;
import java.util.function.BiFunction;
import java.util.function.Function;
import java.util.stream.Collectors;

public abstract class Distribution {
    public String type;
    public boolean required;
    protected int penalty;
    public ArrayList<Integer> idInDistribution = new ArrayList<>();

    private static Map<String, Class<? extends Distribution>> classes = new HashMap<>();

    static {
        classes.put("SameStart", SameStart.class);
        classes.put("SameTime", SameTime.class);
        classes.put("DifferentTime", DifferentTime.class);
        classes.put("SameDays", SameDays.class);
        classes.put("DifferentDays", DifferentDays.class);
        classes.put("SameWeeks", SameWeeks.class);
        classes.put("DifferentWeeks", DifferentWeeks.class);
        classes.put("Overlap", Overlap.class);
        classes.put("NotOverlap", NotOverlap.class);
        classes.put("SameRoom", SameRoom.class);
        classes.put("DifferentRoom", DifferentRoom.class);
        classes.put("SameAttendees", SameAttendees.class);
        classes.put("Precedence", Precedence.class);
        classes.put("WorkDay", WorkDay.class);
        classes.put("MinGap", MinGap.class);
        classes.put("MaxDays", MaxDays.class);
        classes.put("MaxDayLoad", MaxDayLoad.class);
        classes.put("MaxBreaks", MaxBreaks.class);
        classes.put("MaxBlock", MaxBlock.class);
    }

    public static Class<? extends Distribution> getClassForType(String type) {
        Class<? extends Distribution> aClass = classes.get(type);
        if (aClass != null) {
            return aClass;
        }
        throw new IllegalArgumentException("No Class for:" + type);
    }


    /**
     * Returns the correct Distribution subtype
     *
     * @param type String that maps to a Distribution subclass
     * @return Instantiated Instance of the subclass
     */
    public static Distribution get(String type) {
        String[] split = type.split("(\\()|(\\))");
        String typeString = split[0];

        // TODO: Optimize by caching the constructor per type
        try {
            Class<? extends Distribution> distribution = getClassForType(typeString);
            if (split.length == 2) {
                return distribution.getConstructor(String.class).newInstance(split[1]);
            }
            return distribution.getConstructor().newInstance();
        } catch (ReflectiveOperationException | IllegalArgumentException e) {
            throw new IllegalArgumentException("Could not create class for type: " + type + "\n" + e.getMessage());
        }
    }

    public abstract boolean validate(Instance instance, Solution solution);

    protected boolean inDistribution(CourseClass c) {
        return idInDistribution.contains(c.id);
    }

    public boolean inDistribution(SolutionClass solutionClass) {
        return idInDistribution.contains(solutionClass.classId);
    }

    /**
     * Checks a given constraint for all pairs of classes in the distribution
     *
     * @param items      Classes for which the constraints should hold
     * @param constraint Function that takes a pair of classes and returns a boolean indicating if the constraint holds
     * @param <T>        Type of the class
     * @return
     */
    protected <T> boolean forAny(List<T> items, BiFunction<T, T, Boolean> constraint, boolean checkAll) {
        boolean result = true;
        for (int i = 0; i < items.size() - 1; i++) {
            T C_i = items.get(i);
            for (int j = i + 1; j < items.size(); j++) {
                T C_j = items.get(j);
                boolean holds = constraint.apply(C_i, C_j);
                // We only have to do something if the constraint does not hold
                if (!holds) {
                    if (checkAll) {
                        result = false;
                    } else {
                        return false;
                    }
                }
            }
        }
        return result;
    }

    protected <T> boolean forAny(List<T> items, BiFunction<T, T, Boolean> constraint) {
        return forAny(items, constraint, false);
    }

    protected <T> boolean forAll(List<T> classes, Function<T, Boolean> constraint, boolean checkAll) {
        boolean result = true;
        for (T item : classes) {
            boolean holds = constraint.apply(item);
            if (!holds) {
                if (checkAll) {
                    result = false;
                } else {
                    return false;
                }
            }
        }
        return result;
    }

    protected <T> boolean forAll(List<T> classes, Function<T, Boolean> constraint) {
        return forAll(classes, constraint, false);
    }

    protected List<SolutionClass> getClassInDistribution(Solution solution) {
        return solution.classes.stream().filter(this::inDistribution).collect(Collectors.toList());
    }

    public static long convert(BitSet bits) {
        long value = 0L;
        for (int i = 0; i < bits.length(); ++i) {
            value += bits.get(i) ? (1L << i) : 0L;
        }
        return value;
    }

    public int getPenalty(){
        if (required && penalty == 0){
            return Constants.BIG_M;
        }
        return penalty;
    }

    public void setPenalty(int penalty) {
        this.penalty = penalty;
    }
}
