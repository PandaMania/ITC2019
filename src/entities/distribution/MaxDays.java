package entities.distribution;

import entities.Instance;
import entities.Solution;
import entities.SolutionClass;

import java.util.BitSet;
import java.util.Collection;
import java.util.List;

public class MaxDays extends Distribution {
// by jonty, therefore it might not work, take a look if you want to double check.
    int D;

    public MaxDays(String d) {
        D = Integer.parseInt(d);
    }
//
   @Override
    public boolean validate(Instance instance, Solution solution) {
        List<SolutionClass> solutionClasses = getClassInDistribution(solution);
       BitSet span= new BitSet();
         for(int i=0; i<solutionClasses.size(); i++){
            span.or(solutionClasses.get(i).days);
            }
       if(span.cardinality()>D) {
           return false;
       }else
           return true;
    }

}

