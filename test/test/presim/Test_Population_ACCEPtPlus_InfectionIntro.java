package test.presim;

import availability.AbstractAvailability;
import availability.Availability_ACCEPtPlus_SelectiveMixing;
import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.util.Arrays;
import person.AbstractIndividualInterface;
import person.Person_ACCEPtPlusSingleInflection;
import population.Population_ACCEPtPlus;
import random.MersenneTwisterFastRandomGenerator;
import sim.Runnable_Population_ACCEPtPlus;
import sim.Runnable_Population_ACCEPtPlus_Infection;
import infection.ChlamydiaInfection;
import java.io.FileFilter;
import java.io.PrintStream;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import util.Classifier_ACCEPt;
import util.FileZipper;
import util.PersonClassifier;
import static run.Run_Population_ACCEPtPlus_InfectionIntro_Batch.BEST_FIT_PARAM_TRAN_MALE_TO_FEMALE_EXTRA;
import static run.Run_Population_ACCEPtPlus_InfectionIntro_Batch.BEST_FIT_PARAM_TRAN_FEMALE_TO_MALE;
import static run.Run_Population_ACCEPtPlus_InfectionIntro_Batch.BEST_FIT_PARAM_INTRO_RATE_MALE;
import static run.Run_Population_ACCEPtPlus_InfectionIntro_Batch.BEST_FIT_PARAM_INTRO_RATE_FEMALE;
import static run.Run_Population_ACCEPtPlus_InfectionIntro_Batch.BEST_FIT_PARAM_AVE_INF_DUR_FEMALE;
import static run.Run_Population_ACCEPtPlus_InfectionIntro_Batch.BEST_FIT_PARAM_AVE_INF_DUR_MALE;

/**
 *
 * @author Ben Hui
 * @deprecated  if possible use Test_Population_ACCEPtPlus_Simulation_Batch instead
 */
public class Test_Population_ACCEPtPlus_InfectionIntro {

    public static final long BASE_SEED = 2251912970037127827l;

    public static final int NUM_SIM_TOTAL = 1000;

    public static final String DIR_PATH = "Z:\\ACCEPT\\Baseline"; 

    public static final String IMPORT_PATH = "Z:\\ACCEPT\\ImportDir"; 

    public static final int NUM_THREADS = Runtime.getRuntime().availableProcessors();

    public static final PersonClassifier ACCEPT_CLASSIFIER = new Classifier_ACCEPt();

    public static double[] BEST_FIT_PARAMETER = new double[]{
        0.0049703264848381095,
        0.009999964904922165,
        0.005111174844307004,
        0.015142304722707228,
        432.99262444104454,
        5.174928269626601E-4
    };

    public static void main(String[] arg) throws IOException, ClassNotFoundException, InterruptedException {

        Population_ACCEPtPlus pop;

        File testDir = new File(DIR_PATH);
        testDir.mkdirs(); // Create directory if not exists
        File importDir = new File(IMPORT_PATH);

        MersenneTwisterFastRandomGenerator rng = new MersenneTwisterFastRandomGenerator(BASE_SEED);

        File[] existedPop = importDir.exists() ? importDir.listFiles(new FileFilter() {
            @Override
            public boolean accept(File file) {
                return file.getName().endsWith(".zip");
            }
        }) : new File[0];

        boolean importingPopExist = existedPop.length == NUM_SIM_TOTAL;

        boolean useParallel = NUM_THREADS > 0;

        // Generate new population
        if (!importingPopExist) {
            importDir.mkdirs();

            String[] existPopName = new String[existedPop.length];

            for (int i = 0; i < existPopName.length; i++) {
                existPopName[i] = existedPop[i].getName();
            }

            Arrays.sort(existPopName);

            System.out.println("Generating " + NUM_SIM_TOTAL + " population(s) without infections at " + importDir.toString());

            if (useParallel) {
                System.out.println("Using " + NUM_THREADS + " threads");
            }

            long tic = System.currentTimeMillis();

            Runnable_Population_ACCEPtPlus[] threads = new Runnable_Population_ACCEPtPlus[NUM_SIM_TOTAL];

            for (int s = 0; s < NUM_SIM_TOTAL; s++) {
                pop = new Population_ACCEPtPlus(rng.nextLong());

                int[] numPerGrp = new int[46 * 2];

                Arrays.fill(numPerGrp, 1000);
                Arrays.fill(numPerGrp, numPerGrp.length / 2, numPerGrp.length, 1000);
                pop.getFields()[Population_ACCEPtPlus.ACCEPT_FIELDS_NUM_INDIV_PER_GRP] = numPerGrp;

                //pop.initialise();
                // Initalised partnership
                //pop.advanceTimeStep(1);
                // Set simulation                
                Runnable_Population_ACCEPtPlus sim = new Runnable_Population_ACCEPtPlus(s);
                long seed = pop.getSeed();
                sim.setPopulation(pop);
                int startTime = pop.getGlobalTime();
                int numYrToRun = 100;

                sim.getRunnableParam()[Runnable_Population_ACCEPtPlus.RUNNABLE_SIM_DURATION] = new int[]{12 * numYrToRun, 30};
                sim.getRunnableParam()[Runnable_Population_ACCEPtPlus.RUNNABLE_EXPORT_AT] = new int[]{startTime + (numYrToRun) * 360};
                sim.getRunnableParam()[Runnable_Population_ACCEPtPlus.RUNNABLE_EXPORT_PATH]
                        = new File[]{importDir};

                System.out.println("Sim #" + s + " seed = " + seed + " created");

                setPartnerAccquistionBehaviour(sim);
                threads[s] = sim;
            }

            if (!useParallel) {
                for (Runnable_Population_ACCEPtPlus thread : threads) {

                    if (Arrays.binarySearch(existPopName, "pop_S" + thread.getRunnableId() + "_T0.zip") >= 0) {

                        System.out.println("Sim #" + thread.getRunnableId() + " with seed = "
                                + thread.getPopulation().getSeed() + " skipped.");

                    } else {

                        System.out.println("Sim #" + thread.getRunnableId() + " with seed = " + thread.getPopulation().getSeed() + " running");
                        thread.run();
                        System.out.println("Sim #" + thread.getRunnableId() + "completed");
                    }
                }
            } else {
                int numSubmitted = 0;
                int numInExe = 0;
                //int threadPt = 0;
                //int numThreads = Math.min(NUM_THREADS, threads.length - threadPt);                

                ExecutorService executor = null;

                for (Runnable_Population_ACCEPtPlus thread : threads) {
                    if (executor == null) {
                        //int numThreads = Math.min(NUM_THREADS, threads.length - threadPt);
                        executor = Executors.newFixedThreadPool(NUM_THREADS);
                        numInExe = 0;
                    }
                    if (Arrays.binarySearch(existPopName, "pop_S" + thread.getRunnableId() + "_T0.zip") >= 0) {
                    } else {
                        System.out.println("Sim #" + thread.getRunnableId() + " with seed = " + thread.getPopulation().getSeed() + " submited");
                        executor.submit(thread);
                        numSubmitted++;
                        numInExe++;
                        if (numInExe == NUM_THREADS) {
                            executor.shutdown();
                            if (!executor.awaitTermination(2, TimeUnit.DAYS)) {
                                System.out.println("Thread time-out!");
                            }

                            System.out.println("Time required for generating up to "
                                    + numSubmitted + " population (s) = " + (System.currentTimeMillis() - tic) / 1000f);

                            executor = null;
                            numInExe = 0;

                        }
                    }
                }

                if (numInExe != 0 && executor != null) {

                    executor.shutdown();
                    if (!executor.awaitTermination(2, TimeUnit.DAYS)) {
                        System.out.println("Thread time-out!");
                    }
                    System.out.println("Time required for generating up to "
                            + numSubmitted + " population (s) = " + (System.currentTimeMillis() - tic) / 1000f);

                }

            }
            System.out.println("Time required for generating all population (s) = " + (System.currentTimeMillis() - tic) / 1000f);
        }

        System.out.println("Introducing infections....");

        long ticInf = System.currentTimeMillis();

        ExecutorService executor = null;
        int numInExe = 0;

        for (int s = 0; s < NUM_SIM_TOTAL; s++) {
            File popFile = new File(importDir, "pop_S" + s + "_T0.zip");
            Importation_Simulation_Runnable runnable = new Importation_Simulation_Runnable(s, popFile);

            if (!useParallel) {
                runnable.run();
            } else {
                if (executor == null) {
                    executor = Executors.newFixedThreadPool(NUM_THREADS);
                    numInExe = 0;
                }

                executor.submit(runnable);
                numInExe++;

                if (numInExe == NUM_THREADS) {
                    executor.shutdown();
                    if (!executor.awaitTermination(2, TimeUnit.DAYS)) {
                        System.out.println("Inf Thread time-out!");
                    }
                    executor = null;
                }
            }
        }
        if (useParallel && executor != null) {
            executor.shutdown();
            if (!executor.awaitTermination(2, TimeUnit.DAYS)) {
                System.out.println("Inf Thread time-out!");
            }

        }

        System.out.println("Time required for intro simulation (s) = " + (System.currentTimeMillis() - ticInf) / 1000f);
    }

    private static void setPartnerAccquistionBehaviour(Runnable_Population_ACCEPtPlus sim) {

        Population_ACCEPtPlus pop = sim.getPopulation();

        AbstractAvailability[] avail = pop.getAvailability();

        if (avail != null) {
            Integer mixType = 1;
            for (int i = 0; i < avail.length; i++) {
                avail[i] = new Availability_ACCEPtPlus_SelectiveMixing(pop.getRNG());
                avail[i].setRelationshipMap(pop.getRelMap()[i]);
                ((Availability_ACCEPtPlus_SelectiveMixing) avail[i]).setParameter("KEY_MATCH_TYPE", mixType);
            }
            //System.out.println("Mixing type = " + mixType.toString());
        }

        // 3: ACCEPT_FIELDS_PARTNER_IN_12_MONTHS_TARGET_WEIGHTING
        // Replacement instead of weighting if entry is 0 or -ive
        /*
         // Defined from ASHR2 (Rissel C 2014)
         protected final float[] PARTNER_IN_12_MONTHS_TARGET = new float[]{
         // Male 16-19, 20-29, 30-39, 40-49, 50-59, 60-69, (not used)
         1.4f, 1.4f, 1.2f, 1.1f, 1.0f, 0.9f, Float.NaN,
         // Female 16-19, 20-29, 30-39, 40-49, 50-59, 60-69, (not used)
         1.0f, 1.1f, 1.0f, 0.9f, 0.8f, 0.6f, Float.NaN,};
         */
        float gender_weight_male = 0.125f;
        ((float[]) pop.getFields()[Population_ACCEPtPlus.ACCEPT_FIELDS_PARTNER_IN_12_MONTHS_TARGET_WEIGHTING])[7] = -(1.4f * gender_weight_male + 1.0f * (1 - gender_weight_male));
        ((float[]) pop.getFields()[Population_ACCEPtPlus.ACCEPT_FIELDS_PARTNER_IN_12_MONTHS_TARGET_WEIGHTING])[8] = -(1.4f * gender_weight_male + 1.1f * (1 - gender_weight_male));
        ((float[]) pop.getFields()[Population_ACCEPtPlus.ACCEPT_FIELDS_PARTNER_IN_12_MONTHS_TARGET_WEIGHTING])[9] = -(1.2f * gender_weight_male + 1.0f * (1 - gender_weight_male));
        ((float[]) pop.getFields()[Population_ACCEPtPlus.ACCEPT_FIELDS_PARTNER_IN_12_MONTHS_TARGET_WEIGHTING])[10] = -(1.1f * gender_weight_male + 0.9f * (1 - gender_weight_male));
        ((float[]) pop.getFields()[Population_ACCEPtPlus.ACCEPT_FIELDS_PARTNER_IN_12_MONTHS_TARGET_WEIGHTING])[11] = -(1.0f * gender_weight_male + 0.8f * (1 - gender_weight_male));
        ((float[]) pop.getFields()[Population_ACCEPtPlus.ACCEPT_FIELDS_PARTNER_IN_12_MONTHS_TARGET_WEIGHTING])[12] = -(0.9f * gender_weight_male + 0.6f * (1 - gender_weight_male));

        //System.out.println("Gender Weight (Male)  = " + Float.toString(gender_weight_male));
        //System.out.println("Partner Weighting = "
        //        + Arrays.toString((float[]) pop.getFields()[Population_ACCEPtPlus.ACCEPT_FIELDS_PARTNER_IN_12_MONTHS_TARGET_WEIGHTING]));
    }

    private static class Importation_Simulation_Runnable implements Runnable {

        final File popFile;
        final int simId;

        public Importation_Simulation_Runnable(int s, File popFile) {
            this.simId = s;
            this.popFile = popFile;
        }

        @Override
        public void run() {

            PrintStream textOutput;
            try {
                Population_ACCEPtPlus pop;

                textOutput = new PrintStream(new File(DIR_PATH, "output_sim_" + simId + ".txt"));

                textOutput.println("Continue using pop from " + popFile.getAbsolutePath());
                File tempFile = FileZipper.unzipFile(popFile, popFile.getParentFile());
                ObjectInputStream oIStream = new ObjectInputStream(new BufferedInputStream(new FileInputStream(tempFile)));
                pop = Population_ACCEPtPlus.decodeFromStream(oIStream);
                oIStream.close();
                if (pop != null) {
                    tempFile.delete();
                }

                if (pop != null) {
                    Runnable_Population_ACCEPtPlus_Infection sim = new Runnable_Population_ACCEPtPlus_Infection(simId);

                    long seed = pop.getSeed();
                    sim.setPopulation(pop);
                    int startTime = pop.getGlobalTime();
                    int numYrToRun = 50;
                    sim.getRunnableParam()[Runnable_Population_ACCEPtPlus.RUNNABLE_SIM_DURATION] = new int[]{numYrToRun, AbstractIndividualInterface.ONE_YEAR_INT};
                    sim.getRunnableParam()[Runnable_Population_ACCEPtPlus.RUNNABLE_EXPORT_AT] = new int[]{startTime + (numYrToRun) * AbstractIndividualInterface.ONE_YEAR_INT};
                    sim.getRunnableParam()[Runnable_Population_ACCEPtPlus.RUNNABLE_EXPORT_PATH]
                            = new File[]{new File(DIR_PATH)};

                    textOutput.println("Years to run (with infection) = " + numYrToRun);

                    setPartnerAccquistionBehaviour(sim);

                    ((float[]) sim.getRunnableParam()[Runnable_Population_ACCEPtPlus_Infection.RUNNABLE_INFECTION_INTRO_RATE])[0]
                            = (float) BEST_FIT_PARAMETER[BEST_FIT_PARAM_INTRO_RATE_MALE];
                    ((float[]) sim.getRunnableParam()[Runnable_Population_ACCEPtPlus_Infection.RUNNABLE_INFECTION_INTRO_RATE])[1]
                            = (float) BEST_FIT_PARAMETER[BEST_FIT_PARAM_INTRO_RATE_FEMALE];

                    textOutput.println("Infect intro rate  = "
                            + Arrays.toString((float[]) sim.getRunnableParam()[Runnable_Population_ACCEPtPlus_Infection.RUNNABLE_INFECTION_INTRO_RATE]));

                    // Seed infection, from ACCEPt     
                    /*
                     float[] prevalByClass = new float[]{0.046f, 0.071f, 0.054f, 0.037f,
                     0.090f, 0.081f, 0.038f, 0.013f};
                     pop.setInstantInfection(0, ACCEPT_CLASSIFIER, prevalByClass, 296);                
                     */
                    
                    // From Jane's email 20160826
                    ((float[]) sim.getRunnableParam()[Runnable_Population_ACCEPtPlus_Infection.RUNNABLE_INFECTION_PARTNER_TREATMENT_RATE])[0]
                            //      = 0.828f * 0.035f; // * (1 + 0.035f)/2;
                            = 0.828f * (1 + 0.035f) / 2;

                    textOutput.println("PT rate = " + Arrays.toString(
                            (float[]) sim.getRunnableParam()[Runnable_Population_ACCEPtPlus_Infection.RUNNABLE_INFECTION_PARTNER_TREATMENT_RATE]));

                    PersonClassifier testCoverageClassifier = new PersonClassifier() {

                        @Override
                        public int classifyPerson(AbstractIndividualInterface p) {
                            Person_ACCEPtPlusSingleInflection person = (Person_ACCEPtPlusSingleInflection) p;

                            if ((p.getAge() >= 16 * AbstractIndividualInterface.ONE_YEAR_INT
                                    && p.getAge() < 35 * AbstractIndividualInterface.ONE_YEAR_INT)) {
                                return p.isMale() ? 1 : 2;
                            } else if (p.getAge() >= 16 * AbstractIndividualInterface.ONE_YEAR_INT
                                    && p.getAge() < 35 * AbstractIndividualInterface.ONE_YEAR_INT
                                    && person.getPartnerHistoryLifetimePt() > 0) {
                                // Has at least one partner
                                return -(p.isMale() ? 1 : 2);
                            } else {

                                return 0;
                            }
                        }

                        @Override
                        public int numClass() {
                            return 2; // Male, female
                        }
                    };

                    sim.getRunnableParam()[Runnable_Population_ACCEPtPlus_Infection.RUNNABLE_INFECTION_TESTING_CLASSIFIER] = testCoverageClassifier;
                    sim.getRunnableParam()[Runnable_Population_ACCEPtPlus_Infection.RUNNABLE_INFECTION_TESTING_COVERAGE]
                            = new float[]{0.08f, 0.15f};

                    textOutput.println("Testing coverage = " + Arrays.toString(
                            (float[]) sim.getRunnableParam()[Runnable_Population_ACCEPtPlus_Infection.RUNNABLE_INFECTION_TESTING_COVERAGE]));

                    ///*
                    ChlamydiaInfection ct_inf = (ChlamydiaInfection) pop.getInfList()[0];
                    String key;

                    double[] trans = new double[]{
                        BEST_FIT_PARAMETER[BEST_FIT_PARAM_TRAN_FEMALE_TO_MALE] + BEST_FIT_PARAMETER[BEST_FIT_PARAM_TRAN_MALE_TO_FEMALE_EXTRA],
                        BEST_FIT_PARAMETER[BEST_FIT_PARAM_TRAN_FEMALE_TO_MALE],}; // M->F, F->M

                    key = ChlamydiaInfection.PARAM_DIST_PARAM_INDEX_REGEX.replaceAll("999", "" + ChlamydiaInfection.DIST_TRANS_MF_INDEX);
                    ct_inf.setParameter(key, new double[]{trans[0], 0});

                    textOutput.println("Trans MF = " + Arrays.toString((double[]) ct_inf.getParameter(key)));

                    key = ChlamydiaInfection.PARAM_DIST_PARAM_INDEX_REGEX.replaceAll("999", "" + ChlamydiaInfection.DIST_TRANS_FM_INDEX);
                    ct_inf.setParameter(key, new double[]{trans[1], 0});

                    textOutput.println("Trans FM = " + Arrays.toString((double[]) ct_inf.getParameter(key)));

                    double[] dur;
                    key = ChlamydiaInfection.PARAM_DIST_PARAM_INDEX_REGEX.replaceAll("999", "" + ChlamydiaInfection.DIST_INFECT_ASY_DUR_INDEX);
                    dur = (double[]) ct_inf.getParameter(key);

                    dur[0] = BEST_FIT_PARAMETER[BEST_FIT_PARAM_AVE_INF_DUR_FEMALE]
                            + BEST_FIT_PARAMETER[BEST_FIT_PARAM_AVE_INF_DUR_MALE];
                    ct_inf.setParameter(key, dur);

                    textOutput.println("Duration Asy = " + Arrays.toString((double[]) ct_inf.getParameter(key)));

                    key = ChlamydiaInfection.PARAM_DIST_PARAM_INDEX_REGEX.replaceAll("999", "" + ChlamydiaInfection.DIST_INFECT_SYM_DUR_INDEX);
                    dur = (double[]) ct_inf.getParameter(key);
                    
                    dur[0] = BEST_FIT_PARAMETER[BEST_FIT_PARAM_AVE_INF_DUR_FEMALE];
                    ct_inf.setParameter(key, dur);

                    textOutput.println("Duration Sym = " + Arrays.toString((double[]) ct_inf.getParameter(key)));

                    textOutput.println("Inf Sim #" + simId + " seed = " + seed + " created");

                    sim.setOutputPrintStream(textOutput);
                    sim.run();

                    textOutput.close();
                }

            } catch (IOException | ClassNotFoundException ex) {
                ex.printStackTrace();
            }

        }

    }

}
