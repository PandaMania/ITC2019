package entities.distribution;

import entities.Instance;
import entities.Solution;
import entities.SolutionClass;
import util.BitSets;

import java.util.BitSet;
import java.util.Collection;
import java.util.List;

public class SameWeeks extends Distribution {
    @Override
    public boolean validate(Instance instance, Solution solution) {

        List<SolutionClass> solutionClasses = getClassInDistribution(solution);

        return forAny(solutionClasses, (i,j)->
        {
            BitSet a = BitSets.or(i.weeks, j.weeks);
            if(a.equals(i.weeks)){
                return true;
            }else if(a.equals(j.weeks)){
                return true;
            }else return false;
        });
    }
}
