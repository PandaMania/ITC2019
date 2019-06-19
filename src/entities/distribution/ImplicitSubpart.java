package entities.distribution;

import entities.Instance;
import entities.Solution;
import entities.course.SubPart;

import java.util.List;
import java.util.stream.Collectors;

// At least one lecture of each subpart has to be scheduled
public class ImplicitSubpart extends ImplicitDistribution {

    public ImplicitSubpart() {
        super();
        type = "ImplicitSubPart";
    }

    private int mult = 5;

    @Override
    public boolean validate(Instance instance, Solution solution) {
        exceededBy = 0;
        List<SubPart> subParts = instance.courses.stream()
                .flatMap(course -> course.configs.stream()
                        .flatMap(config -> config.subparts.stream()))
                .collect(Collectors.toList());
        return forAll(subParts, s->{
            List<Integer> idsInSubpart = s.classes.stream().map(c -> Integer.parseInt(c.id)).collect(Collectors.toList());

            // Check if any of the classes in the subpart are scheduled
            if(solution.classes.stream().noneMatch(c->idsInSubpart.contains(c.classId))){
                exceededBy+= 1*mult;
                return false;
            }
            return true;
        }, true);
    }
}
