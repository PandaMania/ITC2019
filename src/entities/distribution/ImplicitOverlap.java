package entities.distribution;

import ILP.ILP;
import Parsing.InstanceParser;
import entities.Instance;
import entities.Solution;
import entities.SolutionClass;
import util.BitSets;
import util.Constants;

import java.io.IOException;

public class ImplicitOverlap extends ImplicitDistribution {

    public ImplicitOverlap() {
        super();
        type = "ImplicitOverlap";
        penalty = Constants.BIG_M;
    }


    /**
     * A class cannot be placed in a room when there is some other class placed in the room at an overlapping time
     *
     * @param instance
     * @param solution
     * @return
     */
    @Override
    public boolean validate(Instance instance, Solution solution) {
        exceededBy = 0;
        return forAny(solution.classes, (Ci, Cj) -> {

            if (!instance.getClassForId(Ci.classId).roomNeeded || !instance.getClassForId(Cj.classId).roomNeeded) {
                return true; // If either does not need a room, then no need to check
            }

            if (Ci.roomId == Cj.roomId &&                                // Same room
                    BitSets.and(Ci.weeks, Cj.weeks).cardinality() > 0 &&  // overlapping weeks
                    BitSets.and(Ci.days, Cj.weeks).cardinality() > 0) {   // Overapping days
                if ((Ci.start + Ci.length <= Cj.start)) {
                    return true;
                } else if ((Cj.start + Cj.length <= Ci.start)) {
                    return true;
                } else if ((Cj.start >= Ci.start && Cj.start + Cj.length <= Ci.start + Ci.length) ||
                        (Ci.start >= Cj.start && Ci.start + Ci.length <= Cj.start + Cj.length)) {
                    exceededBy++;
                    return false;
                }
                exceededBy++;
                return false;
            }
            return true;
        }, true);
    }

    public static boolean test(SolutionClass Ci, SolutionClass Cj) {
        if (Ci.roomId == Cj.roomId &&                               // Same room
                BitSets.and(Ci.weeks, Cj.weeks).cardinality() > 0 &&  // overlapping weeks
                BitSets.and(Ci.days, Cj.weeks).cardinality() > 0) {   // Overapping days
            if ((Ci.start + Ci.length <= Cj.start)) {
                return true;
            } else if ((Cj.start + Cj.length <= Ci.start)) {
                return true;
            } else if (Cj.start >= Ci.start && Cj.start + Cj.length <= Ci.start + Ci.length ||
                    Ci.start >= Cj.start && Ci.start + Ci.length <= Cj.start + Cj.length) {
                return false;
            }
        }
        return false;
    }

    @Override
    public int getPenalty() {
        if (exceededBy == null) {
            return Constants.BIG_M;
//            throw new IllegalStateException("Constraint not checked yet");
        } else {
            return (penalty * exceededBy);
        }
    }

    public static void testCase() {
        SolutionClass Ci = new SolutionClass(0, 6, 6);
        SolutionClass Cj = new SolutionClass(0, 6, 6);
        Ci.weeks.set(3);
        Cj.weeks.set(3);
        Ci.days.set(3);
        Cj.days.set(3);
        Ci.start = 25;
        Ci.length = 10;
        Cj.start = 10;
        Cj.length = 10;
        System.out.println(test(Ci, Cj));
    }


    public static void main(String[] args) {
        IlpTest();
    }

    private static void IlpTest() {
        ILP.main(null);
//        Solution sol = ILP.sol;
        Instance parse;
        try {
            parse = new InstanceParser("lums-sum17.xml").parse();
            Distribution distribution = parse.distributions.get(parse.distributions.size() - 1);
//            System.out.println(distribution.validate(parse, sol));

        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}