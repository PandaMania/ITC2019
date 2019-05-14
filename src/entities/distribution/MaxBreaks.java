package entities.distribution;

import entities.Instance;
import entities.Solution;
import entities.SolutionClass;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

public class MaxBreaks extends ExceedableDistribution {
    int R;
    int S;

    public MaxBreaks(String params) {
        String[] split = params.split(",");
        R = Integer.parseInt(split[0]);
        S = Integer.parseInt(split[1]);
    }

    @Override
    public boolean validate(Instance instance, Solution solution) {
        exceededBy = 0;
        List<SolutionClass> classes = getClassInDistribution(solution);
        for (int w = 0; w < instance.weeks; w++) {
            for (int d = 0; d < instance.days; d++) {
                ArrayList<int[]> blocks = new ArrayList<>();
                for (SolutionClass C : classes) {
                    if(C.days.get(d) && C.weeks.get(w)){
                        blocks.add(new int[]{C.start, C.start + C.length});
                    }
                }
                ArrayList<int[]> merged = MergeBlocks(blocks);
                Boolean holds = merged.size()<=R+1;
                int excess = Math.max(merged.size() - R+1,0);
                exceededBy += excess;
            }
        }
        //|MergeBlocks{(C.start, C.end) | (C.days and 2d) ≠ 0 ∧ (C.weeks and 2w) ≠ 0})| ≤ R + 1
        return exceededBy == 0;
    }

    public ArrayList<int[]> MergeBlocks(ArrayList<int[]> blocks) {
        //(Ba.end + S ≥ Bb.start) ∧ (Bb.end + S ≥ Ba.start) ⇒ (B.start = min(Ba.start, Bb.start)) ∧ (B.end = max(Ba.end, Bb.end))
        // Sort blocks by their starting time
        // If a block is not in the current block then all subsequent blocks cannot be either.
        blocks.sort(Comparator.comparingInt(e -> e[0]));
        ArrayList<int[]> mergedBlocks = new ArrayList<>();
        int[] B = blocks.get(0);
        for (int[] b : blocks) {
            Boolean mergeable = (B[1] + S >= b[0]) && (b[1] + S >= B[0]);
            if (mergeable) {
                B[0] = Math.min(B[0],b[0]);
                B[1] = Math.max(B[1], b[1]);
            }
            else{
                mergedBlocks.add(B);
                B = b;
            }
        }
        // Dirty check if the last element of the loop was added already or not
        if(!mergedBlocks.contains(B))
        {
            mergedBlocks.add(B);
        }
        return mergedBlocks;
    }
}
