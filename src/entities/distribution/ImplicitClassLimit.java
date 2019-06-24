package entities.distribution;

import entities.Instance;
import entities.Solution;

public class ImplicitClassLimit extends ImplicitDistribution {

    public ImplicitClassLimit() {
        super();
        type = "ImplicitClassLimit";
    }

    @Override
    public boolean validate(Instance instance, Solution solution) {
        exceededBy = 0;
        return forAll(solution.classes, c -> {
            int excessStudents = c.students.size() - c.limit;
            if (excessStudents > 0) {
                exceededBy += excessStudents;
                return false;
            }
            return true;
        }, true);
    }
}
