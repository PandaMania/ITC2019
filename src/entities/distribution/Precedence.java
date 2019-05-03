package entities.distribution;

import entities.Instance;
import entities.Solution;
import entities.SolutionClass;

import java.util.BitSet;
import java.util.Collection;
import java.util.List;

public class Precedence extends Distribution {
    @Override
    public boolean validate(Instance instance, Solution solution) {
        List<SolutionClass> solutionClasses = getClassInDistribution(solution);

        for(int i=0; i<solutionClasses.size();i++){
            if(!ordered(solutionClasses.get(i).weeks, solutionClasses.get(i+1).weeks)){
                return false;
            }

        }
        return true;
    }

    public boolean ordered(BitSet first, BitSet second){
        int a= (int) convert(first);
        int b= (int) convert(second);

        if(a<b) {
            return true;
        }else
            return false;

    }





}
