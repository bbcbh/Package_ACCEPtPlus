package test.util;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.apache.commons.math3.distribution.BetaDistribution;
import org.apache.commons.math3.exception.MathIllegalArgumentException;
import org.apache.commons.math3.stat.StatUtils;
import random.MersenneTwisterRandomGenerator;

public class Util_Population_ACCEPtPlus_Extract_TranmissionProb {

    static final File basedir = new File("C:\\Users\\Bhui\\OneDrive - UNSW\\ACCEPt\\SingleRun\\SingleRun_Control");
    static final int NUM_SIM_TOTAL = 1000;

    //static final Pattern PATTERN_OUTPUT_FILE = Pattern.compile("output_sim_(\\d+).txt");
    static final Pattern[] PARTTERN_TRANSMISSION_INFO = {
        Pattern.compile("Tranmission MF from existing value of \\[(.+)\\]"),
        Pattern.compile("Tranmission FM from existing value of \\[(.+)\\]")};
    static final String FILENAME_EXTRACTED = "tranmission_prob.csv";
    static final String FILENAME_FILLED = "tranmission_prob_sim.csv";

    public static void main(String[] arg) throws FileNotFoundException, IOException {
        extract_transmission_prob();
        resample_tranmission_prob();
    }

    protected static void resample_tranmission_prob() throws FileNotFoundException, MathIllegalArgumentException, NumberFormatException, IOException {
        random.RandomGenerator rng = new MersenneTwisterRandomGenerator(2251912970037127827l);
        File extractFile = new File(basedir, FILENAME_EXTRACTED);
        if (!extractFile.exists()) {
            extract_transmission_prob();
        }

        ArrayList<Double>[] valArrays = new ArrayList[PARTTERN_TRANSMISSION_INFO.length];
        double[][] valFinal = new double[NUM_SIM_TOTAL][PARTTERN_TRANSMISSION_INFO.length];
        for (int a = 0; a < valArrays.length; a++) {
            valArrays[a] = new ArrayList();
        }
        BufferedReader reader = new BufferedReader(new FileReader(extractFile));
        String line;
        int numEnt = 0;
        while ((line = reader.readLine()) != null) {
            String[] ent = line.split(",");
            int simNum = Integer.parseInt(ent[0]);
            if (ent.length == 1) {
                // Need to generated 
                Arrays.fill(valFinal[simNum], Double.NaN);
            } else {
                numEnt++;
                for (int i = 0; i < PARTTERN_TRANSMISSION_INFO.length; i++) {
                    valFinal[simNum][i] = Double.parseDouble(ent[i + 1]);
                    valArrays[i].add(valFinal[simNum][i]);
                }
            }
        }

        System.out.println("# pre-filled entries = " + numEnt);

        // Fill the rest with sample from beta dist
        BetaDistribution[] dist = new BetaDistribution[valArrays.length];
        for (int a = 0; a < valArrays.length; a++) {
            double[] vals = toPrimitive(valArrays[a].toArray(new Double[valArrays[a].size()]));
            double[] mean_sd = new double[]{StatUtils.mean(vals), Math.sqrt(StatUtils.variance(vals))};

            double[] betaParam = generatedBetaParam(mean_sd);

            System.out.println("Param #" + a + " from a BetaDistribution of mean "
                    + mean_sd[0] + " and SD of " + mean_sd[1]
                    + " (alpha, beta = " + Arrays.toString(betaParam) + ")");
            dist[a] = new BetaDistribution(rng, betaParam[0], betaParam[1]);

        }

        for (double[] valFinalPerSim : valFinal) {
            for (int paramNum = 0; paramNum < valFinalPerSim.length; paramNum++) {
                if (Double.isNaN(valFinalPerSim[paramNum])) {
                    valFinalPerSim[paramNum] = dist[paramNum].sample();
                }
            }
        }

        // Print output
        PrintWriter pWri = new PrintWriter(new File(basedir, FILENAME_FILLED));

        for (int simNum = 0; simNum < valFinal.length; simNum++) {
            pWri.print(simNum);
            for (double ent : valFinal[simNum]) {
                pWri.print(',');
                pWri.print(ent);
            }
            pWri.println();
        }

        pWri.close();
    }

    private static double[] toPrimitive(Double[] arr) {
        double[] res = new double[arr.length];
        int i = 0;
        for (Double d : arr) {
            res[i] = d.doubleValue();
            i++;
        }
        return res;

    }

    private static double[] generatedBetaParam(double[] input) {
        // For Beta distribution, 
        // alpha = mean*(mean*(1-mean)/variance - 1)
        // beta = (1-mean)*(mean*(1-mean)/variance - 1)
        double[] res = new double[2];
        double var = input[1] * input[1];
        double rP = input[0] * (1 - input[0]) / var - 1;
        //alpha
        res[0] = rP * input[0];
        //beta
        res[1] = rP * (1 - input[0]);
        return res;

    }

    protected static void extract_transmission_prob() throws FileNotFoundException, IOException, NumberFormatException {
        PrintWriter pWri = new PrintWriter(new File(basedir, FILENAME_EXTRACTED));
        for (int s = 0; s < NUM_SIM_TOTAL; s++) {
            pWri.print(s);

            File outputFile = new File(basedir, "output_sim_" + Integer.toString(s) + ".txt");

            if (outputFile.exists()) {
                int num_tranSel = 0;
                Matcher m;
                double[] tranValue;
                try (BufferedReader reader = new BufferedReader(new FileReader(outputFile))) {
                    tranValue = new double[PARTTERN_TRANSMISSION_INFO.length];
                    while (num_tranSel < PARTTERN_TRANSMISSION_INFO.length) {
                        String line = reader.readLine();
                        if (line == null) {
                            break;
                        }

                        for (int i = 0; i < PARTTERN_TRANSMISSION_INFO.length; i++) {
                            m = PARTTERN_TRANSMISSION_INFO[i].matcher(line);
                            if (m.matches()) {
                                String[] ent = m.group(1).split(",");
                                tranValue[i] = Double.parseDouble(ent[0]);
                                num_tranSel++;
                            }
                        }

                    }
                }

                if (num_tranSel == PARTTERN_TRANSMISSION_INFO.length) {
                    for (int i = 0; i < PARTTERN_TRANSMISSION_INFO.length; i++) {
                        pWri.print(',');
                        pWri.print(tranValue[i]);
                    }

                    System.out.println("Tranmission prob for Sim #" + s + " = " + Arrays.toString(tranValue));

                }

            }

            pWri.println();
        }
        pWri.close();
    }

}
