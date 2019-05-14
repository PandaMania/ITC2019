package entities.distribution;

import entities.Instance;
import entities.Solution;
import entities.SolutionClass;
import util.BitSets;

import java.util.BitSet;
import java.util.Collection;
import java.util.List;

public class Overlap extends Distribution {
    @Override
    public boolean validate(Instance instance, Solution solution) {
        List<SolutionClass> solutionClasses = getClassInDistribution(solution);

        return forAny(solutionClasses, (i,j)->
        {
            BitSet a = BitSets.and(i.days, j.days);

            BitSet b = BitSets.and(i.weeks, j.weeks);

            int i_end = i.start+i.length;
            int j_end = j.start+j.length;

            if(a.cardinality()==0){
                return false;
            }else if(b.cardinality()==0){
                return  false;
            }else if(i_end<= j.start){
                return false;
            }else if(j_end<= i.start){
                return false;
            }else return true;
        });
    }
}
