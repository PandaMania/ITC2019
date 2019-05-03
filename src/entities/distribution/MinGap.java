package entities.distribution;

import entities.Instance;
import entities.Solution;
import entities.SolutionClass;

import java.util.BitSet;
import java.util.Collection;
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

            BitSet a= i.days;
            a.and(j.days);

            BitSet b= i.weeks;
            b.and(j.weeks);
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
