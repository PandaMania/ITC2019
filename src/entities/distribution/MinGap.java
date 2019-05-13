package entities.distribution;

import entities.Instance;
import entities.Solution;
import entities.SolutionClass;
import util.BitSets;

import java.util.BitSet;
import java.util.List;

public class MinGap extends Distribution {

    int G;

    public MinGap(String g) {
        G = Integer.parseInt(g);
    }

    @Override
    public boolean validate(Instance instance, Solution solution) {
        List<SolutionClass> solutionClasses = getClassInDistribution(solution);

        return forAny(solutionClasses, (i,j)->
        {
            // This would change the bitsets in i and j as well. You sadly ahve to create a new bitmap for the result.
//            BitSet a= i.days;
//            a.and(j.days);
//            BitSet b= i.weeks;
//            b.and(j.weeks);
            BitSet a = BitSets.and(i.days, j.days);
            BitSet b = BitSets.and(i.weeks, j.weeks);
            int i_end = i.start+i.length;
            int j_end = j.start+j.length;

            if(a.cardinality()==0){
                return true;
            }else if(b.cardinality()==0){
                return  true;
            }else if(i_end+G <= j.start){
                return true;
            }else if(j_end +G<= i.start){
                return true;
            }else return false;





        });
    }
}
