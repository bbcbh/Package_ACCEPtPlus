package test;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.util.Arrays;
import population.Population_ACCEPtPlus;
import random.MersenneTwisterFastRandomGenerator;
import simulation.Runnable_Population_ACCEPtPlus;
import util.FileZipper;

/**
 *
 * @author Ben Hui
 */
public class Test_Population_ACCEPtPlus_Generate {

    public static final long BASE_SEED = 2251912970037127827l;

    public static final int NUM_SIM_TOTAL = 100;

    public static final String DIR_PATH = "C:\\Users\\bhui\\Desktop\\ACCEPtPlusDir\\";

    public static void main(String[] arg) throws IOException, ClassNotFoundException {

        Population_ACCEPtPlus pop;

        File testDir = new File(DIR_PATH);
        testDir.mkdirs(); // Create directory if not exists
        File importDir = new File(testDir, "ImportDir");

        MersenneTwisterFastRandomGenerator rng = new MersenneTwisterFastRandomGenerator(BASE_SEED);

        for (int s = 0; s < NUM_SIM_TOTAL; s++) {

            boolean importingPop = importDir.exists() && importDir.listFiles().length > 0;

            if (!importingPop) {

                pop = new Population_ACCEPtPlus(rng.nextLong());

                // Unlimited for female. i.e. partnership totally dicated by male
                /*
                float[] weights = (float[]) pop.getFields()[Population_ACCEPtPlus.ACCEPT_FIELDS_PARTNER_IN_12_MONTHS_TARGET_WEIGHTING];

                for (int i = 7; i < weights.length; i++) {
                    if (!Float.isNaN(weights[i])) {
                        weights[i] = weights[i] * 3;
                    }
                }
                */
                
                /*

                pop.getFields()[Population_ACCEPtPlus.ACCEPT_FIELDS_PARTNER_IN_12_MONTHS_TARGET_WEIGHTING]
                        = new float[]{
                            Float.NEGATIVE_INFINITY, Float.NEGATIVE_INFINITY, 
                            Float.NEGATIVE_INFINITY, Float.NEGATIVE_INFINITY,  
                            Float.NEGATIVE_INFINITY, Float.NEGATIVE_INFINITY, Float.NaN,
                            -1.4f, -1.4f, 
                            -1.2f, -1.1f, 
                            -1.0f, -0.9f, Float.NaN};

                System.out.println(
                        Arrays.toString(
                                (float[]) pop.getFields()[Population_ACCEPtPlus.ACCEPT_FIELDS_PARTNER_IN_12_MONTHS_TARGET_WEIGHTING]));
                */

                int[] numPerGrp = new int[46 * 2];

                Arrays.fill(numPerGrp, 1000);
                Arrays.fill(numPerGrp, numPerGrp.length / 2, numPerGrp.length, 1000);

                pop.getFields()[Population_ACCEPtPlus.ACCEPT_FIELDS_NUM_INDIV_PER_GRP] = numPerGrp;

                pop.initialise();
                // Initalised partnership
                pop.advanceTimeStep(1);
            } else {

                File popFile = importDir.listFiles()[s];
                popFile = FileZipper.unzipFile(popFile, popFile.getParentFile());
                ObjectInputStream oIStream = new ObjectInputStream(new BufferedInputStream(new FileInputStream(popFile)));
                pop = Population_ACCEPtPlus.decodeFromStream(oIStream);
                oIStream.close();
                if (pop != null) {
                    popFile.delete();
                }

            }

            if (pop != null) {
                Runnable_Population_ACCEPtPlus sim = new Runnable_Population_ACCEPtPlus(s);
                long seed = pop.getSeed();
                sim.setPopulation(pop);
                int startTime = pop.getGlobalTime();
                int numYrToRun = 100;
                sim.getRunnableParam()[Runnable_Population_ACCEPtPlus.RUNNABLE_SIM_DURATION] = new int[]{12 * numYrToRun, 30};
                sim.getRunnableParam()[Runnable_Population_ACCEPtPlus.RUNNABLE_EXPORT_AT] = new int[]{startTime + (numYrToRun) * 360};
                sim.getRunnableParam()[Runnable_Population_ACCEPtPlus.RUNNABLE_EXPORT_PATH]
                        = new File[]{new File(DIR_PATH)};

                System.out.println("Sim #" + s + " seed = " + seed);
                long tic = System.currentTimeMillis();
                sim.run();
                System.out.println("Time required (s) = " + (System.currentTimeMillis() - tic) / 1000f);
            }
        }

    }

}
