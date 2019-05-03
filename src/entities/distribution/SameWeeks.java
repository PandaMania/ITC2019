package entities.distribution;

import entities.Instance;
import entities.Solution;
import entities.SolutionClass;

import java.util.BitSet;
import java.util.Collection;
import java.util.List;

public class SameWeeks extends Distribution {
    @Override
    public boolean validate(Instance instance, Solution solution) {

        List<SolutionClass> solutionClasses = getClassInDistribution(solution);

        return forAny(solutionClasses, (i,j)->
        {
            BitSet a= i.weeks;

            a.or(j.weeks);

            if(a.equals(i.weeks)){
                return true;
            }else if(a.equals(j.weeks)){
                return true;
            }else return false;
        });
    }
}
