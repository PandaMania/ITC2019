import Parsing.InstanceParser;
import entities.Instance;
import entities.Solution;
import entities.distribution.Distribution;
import entities.distribution.ImplicitDistribution;

import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.util.Arrays;
import java.util.concurrent.ThreadLocalRandom;

public class SAExperiments {

    static String file = "wbg-fal10.xml";
    public static void main(String[] args) {
        neighbourExp();
    }

    static void neighbourExp(){
        InstanceParser parser;
        Instance instance;
        SimulatedAnnealing S;
        Solution init;
        Solution solution;
        try {
            String instanceFileName = file;
            parser = new InstanceParser(instanceFileName);
            instance = parser.parse();
            int[] neighbourSizes = new int[]{1,3,5,10,50};
            for (int x : neighbourSizes) {

                S = new SimulatedAnnealing(instance);
                S.rand.setSeed(197841684351L);
                init = S.initRepresentation(instance);

                solution = S.optimize(init, 70.0, 0.1, 1_000, x, false);

                FileOutputStream f = new FileOutputStream("neighboursize" + x + ".txt");
                OutputStreamWriter out = new OutputStreamWriter(f);
                out.append(Arrays.toString(S.costs));
                out.append("\n");
                for (Distribution dist : instance.distributions) {
                    if (!dist.validate(instance, solution)) {

                        out.append(dist.type+"(");
                        if (dist.required)
                            out.append("req ");
                        out.append("penalty: "+dist.getPenalty());
                        if (ImplicitDistribution.class.isAssignableFrom(dist.getClass())) {
                            ImplicitDistribution d = (ImplicitDistribution) dist;
                            out.append("exceededBy: " + d.getExceededBy());
                        }
                        out.append("),");
                    }
                }
                out.append("\n");
                out.close();
                f.close();
                S.pool.shutdown();
            }

        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
