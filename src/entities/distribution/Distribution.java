package entities.distribution;

import entities.Instance;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

public abstract class Distribution {
    public String type;
    public boolean required;
    public int penalty;
    public ArrayList<Integer> idInDistribution= new ArrayList<>();

    private static Map<String, Class<? extends Distribution>> classes = new HashMap<>();

    static{
        classes.put("SameStart",        SameStart.class);
        classes.put("SameTime",         SameTime.class);
        classes.put("DifferentTime",    DifferentTime.class);
        classes.put("SameDays",         SameDays.class);
        classes.put("DifferentDays",    DifferentDays.class);
        classes.put("SameWeeks",    SameWeeks.class);
        classes.put("DifferentWeeks",    DifferentWeeks.class);
        classes.put("Overlap",    Overlap.class);
        classes.put("NotOverlap",       NotOverlap.class);
        classes.put("SameRoom",         SameRoom.class);
        classes.put("DifferentRoom",         DifferentRoom.class);
        classes.put("SameAttendees",    SameAttendees.class);
        classes.put("Precedence",    Precedence.class);
        classes.put("WorkDay",          WorkDay.class);
        classes.put("MinGap",    MinGap.class);
        classes.put("MaxDays",    MaxDays.class);
        classes.put("MaxDayLoad",    MaxDayLoad.class);
        classes.put("MaxBreaks",    MaxBreaks.class);
        classes.put("MaxBlock",    MaxBlock.class);
    }

    public static Class<? extends Distribution> getClassForType(String type){
        Class<? extends Distribution> aClass = classes.get(type);
        if(aClass != null){
            return aClass;
        }
        throw new IllegalArgumentException("No Class for:" + type);
    }

    public static Distribution get(String type){
        String[] split = type.split("(\\()|(\\))");
        String typeString = split[0];

        // TODO: Optimize by caching the constructor per type
        try {
            Class<? extends Distribution> distribution = getClassForType(typeString);
            if (split.length == 2){
                return distribution.getConstructor(String.class).newInstance(split[1]);
            }
            return distribution.getConstructor().newInstance();
        } catch (ReflectiveOperationException | IllegalArgumentException e) {
            throw new IllegalArgumentException("Could not create class for type: " + type + "\n" + e.getMessage());
        }
    }

    public abstract boolean validate(Instance instance, Collection<int[]> solution);
}
