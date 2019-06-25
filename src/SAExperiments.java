import Parsing.InstanceParser;
import entities.Instance;
import entities.Solution;
import entities.distribution.Distribution;
import entities.distribution.ImplicitDistribution;

import java.io.*;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.concurrent.ThreadLocalRandom;

public class SAExperiments {

    private static final int numIterations = 10_000;
    private static final long seed = 197841684351L;
    static String file = "wbg-fal10.xml";
    static int n = 10;

    public static void main(String[] args) {
        tempExperiment();
        neighbourExp();
//        tempDecayExp();
    }

    static void tempExperiment() {
        InstanceParser parser;
        Instance instance;
        SimulatedAnnealing S;
        Solution init;
        Solution solution;
        try {
            String instanceFileName = file;
            parser = new InstanceParser(instanceFileName);
            instance = parser.parse();
            double[] temperatures = new double[]{1, 10, 50, 70, 200, 500};
            for (double x : temperatures) {
                double[][] costs = new double[n][numIterations];
                ArrayList<String> infeasibles = new ArrayList<>();
                for (int i = 0; i < n; i++) {
                    StringWriter w = new StringWriter();
                    S = new SimulatedAnnealing(instance);
//                    S.rand.setSeed(seed);
                    init = S.initRepresentation(instance);
                    solution = S.optimize(init, x, 0.1, numIterations, 3, false);
                    costs[i] = S.costs;
                    writeInfeasible(instance, solution, w);
                    S.pool.shutdown();
                    infeasibles.add(w.toString());
                }
                writeToFile(infeasibles, costs, "temperature" + x + ".txt");
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    static void neighbourExp() {
        InstanceParser parser;
        Instance instance;
        SimulatedAnnealing S;
        Solution init;
        Solution solution;
        try {
            String instanceFileName = file;
            parser = new InstanceParser(instanceFileName);
            instance = parser.parse();
            int[] neighbourSizes = new int[]{1, 3, 5, 10, 50};
            for (int x : neighbourSizes) {
                double[][] costs = new double[n][numIterations];
                ArrayList<String> infeasibles = new ArrayList<>();
                for (int i = 0; i < n; i++) {
                    StringWriter w = new StringWriter();
                    S = new SimulatedAnnealing(instance);
//                    S.rand.setSeed(seed);
                    init = S.initRepresentation(instance);
                    solution = S.optimize(init, 70.0, 0.1, numIterations, x, false);
                    costs[i] = S.costs;
                    writeInfeasible(instance, solution, w);
                    S.pool.shutdown();
                    infeasibles.add(w.toString());
                }
                writeToFile(infeasibles, costs, "neighboursize" + x + ".txt");
            }

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private static void writeInfeasible(Instance instance, Solution solution, Writer out) throws IOException {
        for (Distribution dist : instance.distributions) {
            if (!dist.validate(instance, solution)) {

                out.append(dist.type + "(");
                if (dist.required)
                    out.append("req ");
                out.append("penalty: " + dist.getPenalty());
                if (ImplicitDistribution.class.isAssignableFrom(dist.getClass())) {
                    ImplicitDistribution d = (ImplicitDistribution) dist;
                    out.append("exceededBy: " + d.getExceededBy());
                }
                out.append("),");
            }
        }
    }

    static void tempDecayExp() {
        InstanceParser parser;
        Instance instance;
        SimulatedAnnealing S = null;
        Solution init;
        Solution solution = null;
        try {
            String instanceFileName = file;
            parser = new InstanceParser(instanceFileName);
            instance = parser.parse();
            boolean[] neighbourSizes = new boolean[]{true, false};
            for (boolean x : neighbourSizes) {
                ArrayList<String> infeasibles = new ArrayList<>();
                double[][] costs = new double[n][numIterations];
                for (int i = 0; i < n; i++) {
                    StringWriter w = new StringWriter();
                    S = new SimulatedAnnealing(instance);
//                    S.rand.setSeed(seed);
                    init = S.initRepresentation(instance);
                    S.exp = x;
                    solution = S.optimize(init, 200.0, 0.1, numIterations, 1, false);
                    costs[i] = S.costs;
                    writeInfeasible(instance, solution, w);
                    S.pool.shutdown();
                    infeasibles.add(w.toString());
                }
                writeToFile(infeasibles, costs, "exp" + x + ".txt");
            }

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private static void writeToFile(ArrayList<String> infeasibles, double[][] costs, String fileName) throws IOException {
        double[] avgCosts = average(costs);
        FileOutputStream f = new FileOutputStream(fileName);
        OutputStreamWriter out = new OutputStreamWriter(f);
        out.append(Arrays.toString(avgCosts));
        out.append("\n");
        for (String infeasible : infeasibles) {
            out.append(infeasible);
            out.append("\n");
        }
        out.close();
        f.close();
    }

    public static double[] average(double[][] costs) {
        int iters = costs[0].length;
        int runs = costs.length;
        double[] averages = new double[iters];
        // for every iteration
        for (int i = 0; i < iters; i++) {
            //average over all runs
            double s = 0;
            double n = 0;
            for (int j = 0; j < runs; j++) {
                s += costs[j][i];
                n++;
            }
            averages[i] = s / n;
        }
        return averages;
    }


}
