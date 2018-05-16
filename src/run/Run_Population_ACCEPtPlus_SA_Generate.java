/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package run;

import availability.AbstractAvailability;
import availability.Availability_ACCEPtPlus_SelectiveMixing;
import infection.ChlamydiaInfection;
import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileFilter;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import person.AbstractIndividualInterface;
import person.Person_ACCEPtPlusSingleInflection;
import population.Population_ACCEPtPlus;
import simulation.Runnable_Population_ACCEPtPlus;
import simulation.Runnable_Population_ACCEPtPlus_Infection;
import util.FileZipper;
import util.PersonClassifier;

/**
 *
 * @author bhui
 */
public class Run_Population_ACCEPtPlus_SA_Generate {
    
    public static String IMPORT_PATH = "../ACCEPtPlusDirVM/ImportDir"; 
    public static File BATCH_BASE_PATH = new File("../ACCEPtPlusDirVM/SA_5000");   
    
    public static int[] ROW_LIMIT = new int[]{Integer.MIN_VALUE, Integer.MAX_VALUE};
    public static boolean COMBINE_THREADPOOL_DEFAULT = true;

    public static final int SENSITIVITY_ANALYSIS_INTRO_MALE = 0;
    public static final int SENSITIVITY_ANALYSIS_INTRO_FEMALE = SENSITIVITY_ANALYSIS_INTRO_MALE + 1;
    public static final int SENSITIVITY_ANALYSIS_TRANSIS_FEMALE_TO_MALE = SENSITIVITY_ANALYSIS_INTRO_FEMALE + 1;
    public static final int SENSITIVITY_ANALYSIS_TRANSIS_MALE_TO_FEMALE_EXTRA = SENSITIVITY_ANALYSIS_TRANSIS_FEMALE_TO_MALE + 1;
    public static final int SENSITIVITY_ANALYSIS_DURATION_SYM = SENSITIVITY_ANALYSIS_TRANSIS_MALE_TO_FEMALE_EXTRA + 1;
    public static final int SENSITIVITY_ANALYSIS_DURATION_ASM_EXTRA = SENSITIVITY_ANALYSIS_DURATION_SYM + 1;
    public static final int SENSITIVITY_ANALYSIS_TEST_RATE_MALE = SENSITIVITY_ANALYSIS_DURATION_ASM_EXTRA + 1;
    public static final int SENSITIVITY_ANALYSIS_TEST_RATE_FEMALE = SENSITIVITY_ANALYSIS_TEST_RATE_MALE + 1;
    public static final int SENSITIVITY_ANALYSIS_PARTNER_TREATMENT_RATE = SENSITIVITY_ANALYSIS_TEST_RATE_FEMALE + 1;
    public static final int SENSITIVITY_ANALYSIS_RETEST_RATE_ADJ = SENSITIVITY_ANALYSIS_PARTNER_TREATMENT_RATE + 1;
    public static final int SENSITIVITY_ANALYSIS_TEST_SENSITIVITY = SENSITIVITY_ANALYSIS_RETEST_RATE_ADJ + 1;

    public static void main(String[] arg) throws IOException, ClassNotFoundException, InterruptedException {
        
        int numThread = Runtime.getRuntime().availableProcessors();
        int numSimPerSet = Runtime.getRuntime().availableProcessors();

        if (arg.length > 0) {            
            if(Integer.parseInt(arg[0]) > 0){            
                numSimPerSet = Integer.parseInt(arg[0]);
            }
        }
        if (arg.length > 1) {
            BATCH_BASE_PATH = new File(arg[1]);
        }
        if (arg.length > 2) {
            IMPORT_PATH = arg[2];
        }
        if (arg.length > 3) {
            numThread = Integer.parseInt(arg[3]);
        }
        if (arg.length > 5) {
            ROW_LIMIT[0] = Integer.parseInt(arg[4]);
            ROW_LIMIT[1] = Integer.parseInt(arg[5]);
        }

        BATCH_BASE_PATH.mkdirs();
        File[] paramFiles = BATCH_BASE_PATH.listFiles(new FileFilter() {
            @Override
            public boolean accept(File file) {
                return file.getName().endsWith(".csv");
            }
        });

        if (paramFiles.length != 1) {
            System.err.println("Parameter file not found, please put parameter file *.csv at " + BATCH_BASE_PATH.getAbsolutePath());

        } else {
            BufferedReader reader = new BufferedReader(new FileReader(paramFiles[0]));
            String[] header = reader.readLine().split(",");
            System.out.println("Number of parameters = " + header.length);
            System.out.println("Number of threads = " + numThread);
            System.out.println("Number of sim for each row = " + numSimPerSet);
            System.out.println("BATCH_BASE_PATH = " + BATCH_BASE_PATH.getAbsolutePath());
            System.out.println("IMPORT_PATH = " + new File(IMPORT_PATH).getAbsolutePath());
            System.out.println("ROW LIMIT = " + Arrays.toString(ROW_LIMIT));

            ExecutorService threadpool = null;
            
            boolean combine_threadpool = COMBINE_THREADPOOL_DEFAULT;

            if (!combine_threadpool) {
                System.out.println("Indivudal threadpool");
            }

            Run_Population_ACCEPtPlus_SA_Generate genSAResult = new Run_Population_ACCEPtPlus_SA_Generate();

            int counter = 0;
            String readLine;
            String[] lines;
            File targetDir;
            long tic;
            int numInExe = 0;
            int numRow = 0;

            java.util.ArrayList<String> linesCollection = new ArrayList<>();

            while ((readLine = reader.readLine()) != null) {
                linesCollection.add(readLine);
                numRow++;
            }

            reader.close();

            System.out.println("Num of row for analysis = " + numRow);
            lines = linesCollection.toArray(new String[linesCollection.size()]);

            for (String line : lines) {
                if (combine_threadpool) {
                    if (numInExe == numThread && threadpool != null) {
                        tic = System.currentTimeMillis();
                        threadpool.shutdown();
                        if (!threadpool.awaitTermination(72, TimeUnit.DAYS)) {
                            System.out.println("Inf Thread time-out!");
                        }
                        tic = System.currentTimeMillis() - tic;
                        System.out.println("Time required for pool of "
                                + numInExe + " thread(s) = " + (((float) tic) / 1000) + " s");
                        threadpool = null;
                    }

                    if (threadpool == null) {
                        threadpool = Executors.newFixedThreadPool(numThread);
                        numInExe = 0;
                    }

                }

                if (counter >= ROW_LIMIT[0] && counter <= ROW_LIMIT[1]) {
                    tic = System.currentTimeMillis();
                    if (threadpool == null) {
                        System.out.println("Generating data for parameter Set_" + counter);
                    } else {
                        System.out.println("Adding data for parameter Set_" + counter);
                    }

                    targetDir = new File(BATCH_BASE_PATH, "Set_" + counter);
                    targetDir.mkdirs();
                    numInExe += genSAResult.genSAResult(targetDir, line.split(","), threadpool, 
                            numThread, numSimPerSet);
                    tic = System.currentTimeMillis() - tic;
                    System.out.println("Time required = " + (((float) tic) / 1000) + " s");                    
                }
                counter++;
            }

            if (threadpool != null && numInExe > 0) {
                tic = System.currentTimeMillis();
                threadpool.shutdown();
                if (!threadpool.awaitTermination(72, TimeUnit.DAYS)) {
                    System.out.println("Inf Thread time-out!");
                }
                tic = System.currentTimeMillis() - tic;
                System.out.println("Time required for pool of "
                        + numInExe + " thread(s) = " + (((float) tic) / 1000) + " s");
                threadpool = null;
            }

        }

    }

    public int genSAResult(File targetDir, String[] parameters, 
            ExecutorService combineThreadPool,
            int NUM_THREADS, int NUM_SIM_PER_SET)            
            throws IOException, ClassNotFoundException, InterruptedException {

        File importDir = new File(IMPORT_PATH);
        File[] existedPop = importDir.exists() ? importDir.listFiles(new FileFilter() {
            @Override
            public boolean accept(File file) {
                return file.getName().endsWith(".zip");
            }
        }) : new File[0];

        boolean importingPopExist = existedPop.length >= NUM_SIM_PER_SET;
        boolean useParallel = NUM_THREADS > 0;
        boolean useCombineThreadPool = combineThreadPool != null;
        int numThreadAdded = 0;

        if (!importingPopExist) {
            System.err.println("Imported population doesn't exist at " + importDir.getAbsolutePath());
        } else {
            long ticInf = System.currentTimeMillis();
            ExecutorService executor = combineThreadPool;
            int numInExe = 0;

            for (int s = 0; s < NUM_SIM_PER_SET; s++) {
                File popFile = new File(importDir, "pop_S" + s + "_T0.zip");
                SA_Generate_Runnable runnable
                        = new SA_Generate_Runnable(s, popFile, targetDir, parameters);

                if (!useParallel) {
                    runnable.run();
                } else {
                    if (executor == null) {
                        executor = Executors.newFixedThreadPool(NUM_THREADS);
                        numInExe = 0;
                    }

                    executor.submit(runnable);
                    numInExe++;
                    numThreadAdded++;

                    if (numInExe == NUM_THREADS && !useCombineThreadPool) {
                        executor.shutdown();
                        if (!executor.awaitTermination(72, TimeUnit.DAYS)) {
                            System.out.println("Inf Thread time-out!");
                        }
                        executor = null;
                    }
                }

            }

            if (useParallel && executor != null && !useCombineThreadPool) {
                executor.shutdown();
                if (!executor.awaitTermination(72, TimeUnit.DAYS)) {
                    System.out.println("Inf Thread time-out!");
                }

            }
            if (!useCombineThreadPool) {
                System.out.println("Time required for parameter set at " + targetDir.getName()
                        + " = " + (System.currentTimeMillis() - ticInf) / 1000f);
            }

        }
        return numThreadAdded;

    }

    private class SA_Generate_Runnable implements Runnable {

        final File popFile;
        final int simId;
        final File OUTPUT_PATH;
        final float[] param;

        private SA_Generate_Runnable(int s, File popFile, File targetDir, String[] parameters) {
            this.simId = s;
            this.popFile = popFile;
            this.OUTPUT_PATH = targetDir;
            this.param = new float[parameters.length];

            for (int i = 0; i < parameters.length; i++) {
                param[i] = Float.parseFloat(parameters[i]);
            }

        }

        @Override
        public void run() {
            PrintStream textOutput;
            ObjectOutputStream testHist;
            Population_ACCEPtPlus pop;

            try {
                textOutput = new PrintStream(new File(OUTPUT_PATH, "output_sim_" + simId + ".txt"));
                testHist = new ObjectOutputStream(new FileOutputStream(new File(OUTPUT_PATH, "testing_history_" + simId + ".obj")));

                textOutput.println("Continue using pop from " + popFile.getAbsolutePath());
                File tempFile = FileZipper.unzipFile(popFile, popFile.getParentFile());
                try (ObjectInputStream oIStream = new ObjectInputStream(new BufferedInputStream(new FileInputStream(tempFile)))) {
                    pop = Population_ACCEPtPlus.decodeFromStream(oIStream);
                }
                if (pop != null) {
                    tempFile.delete();
                }

                if (pop != null) {
                    Runnable_Population_ACCEPtPlus_Infection sim = new Runnable_Population_ACCEPtPlus_Infection(simId);
                    long seed = pop.getSeed();
                    sim.setPopulation(pop);
                    int startTime = pop.getGlobalTime();
                    int numYrToRun = 50;
                    sim.getRunnableParam()[Runnable_Population_ACCEPtPlus.RUNNABLE_SIM_DURATION]
                            = new int[]{numYrToRun, AbstractIndividualInterface.ONE_YEAR_INT};
                    sim.getRunnableParam()[Runnable_Population_ACCEPtPlus.RUNNABLE_EXPORT_AT]
                            = new int[]{startTime + (numYrToRun) * AbstractIndividualInterface.ONE_YEAR_INT};
                    sim.getRunnableParam()[Runnable_Population_ACCEPtPlus.RUNNABLE_EXPORT_PATH]
                            = new File[]{OUTPUT_PATH};

                    textOutput.println("Start time = " + startTime);
                    textOutput.println("Years to run (with infection) = " + numYrToRun);

                    setPartnerAccquistionBehaviour(sim);

                    ((float[]) sim.getRunnableParam()[Runnable_Population_ACCEPtPlus_Infection.RUNNABLE_INFECTION_INTRO_RATE])[0]
                            = param[SENSITIVITY_ANALYSIS_INTRO_MALE];
                    ((float[]) sim.getRunnableParam()[Runnable_Population_ACCEPtPlus_Infection.RUNNABLE_INFECTION_INTRO_RATE])[1]
                            = param[SENSITIVITY_ANALYSIS_INTRO_FEMALE];

                    textOutput.println("Infect intro rate  = "
                            + Arrays.toString((float[]) sim.getRunnableParam()[Runnable_Population_ACCEPtPlus_Infection.RUNNABLE_INFECTION_INTRO_RATE]));

                    ((float[]) sim.getRunnableParam()[Runnable_Population_ACCEPtPlus_Infection.RUNNABLE_INFECTION_PARTNER_TREATMENT_RATE])[0]
                            = (float) param[SENSITIVITY_ANALYSIS_PARTNER_TREATMENT_RATE];

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
                            = new float[]{(float) param[SENSITIVITY_ANALYSIS_TEST_RATE_MALE], (float) param[SENSITIVITY_ANALYSIS_TEST_RATE_FEMALE]};

                    textOutput.println("Testing coverage = " + Arrays.toString(
                            (float[]) sim.getRunnableParam()[Runnable_Population_ACCEPtPlus_Infection.RUNNABLE_INFECTION_TESTING_COVERAGE]));

                    float[][][] retest_rate_org 
                            = (float[][][]) sim.getRunnableParam()[Runnable_Population_ACCEPtPlus_Infection.RUNNABLE_INFECTION_RETEST_RATE];
                    
                    
                    float[][][] retest_rate = new float[retest_rate_org.length][][];
                    
                    for(int c = 0; c < retest_rate.length; c++){
                        retest_rate[c] = new float[retest_rate_org[c].length][];
                        retest_rate[c][0] = retest_rate_org[c][0];
                        retest_rate[c][1] = new float[retest_rate_org[c][1].length];
                        for (int r = 0; r < retest_rate[c][1].length; r++) {
                            retest_rate[c][1][r] =  retest_rate_org[c][1][r] * param[SENSITIVITY_ANALYSIS_RETEST_RATE_ADJ];
                        }
                        
                    }

                    sim.getRunnableParam()[Runnable_Population_ACCEPtPlus_Infection.RUNNABLE_INFECTION_RETEST_RATE]
                            = retest_rate;

                    textOutput.println("Retest rate  = "
                            + Arrays.deepToString(
                                    (float[][][]) sim.getRunnableParam()[Runnable_Population_ACCEPtPlus_Infection.RUNNABLE_INFECTION_RETEST_RATE]));

                    sim.getRunnableParam()[Runnable_Population_ACCEPtPlus_Infection.RUNNABLE_INFECTION_TEST_SENSITIVITY]
                            = param[SENSITIVITY_ANALYSIS_TEST_SENSITIVITY];

                    textOutput.println("Test sensitivity = "
                            + Float.toString((float) sim.getRunnableParam()[Runnable_Population_ACCEPtPlus_Infection.RUNNABLE_INFECTION_TEST_SENSITIVITY]));

                    ChlamydiaInfection ct_inf = (ChlamydiaInfection) pop.getInfList()[0];
                    String key;

                    double[] trans = new double[]{
                        param[SENSITIVITY_ANALYSIS_TRANSIS_FEMALE_TO_MALE] + param[SENSITIVITY_ANALYSIS_TRANSIS_MALE_TO_FEMALE_EXTRA],
                        param[SENSITIVITY_ANALYSIS_TRANSIS_FEMALE_TO_MALE],}; // M->F, F->M

                    key = ChlamydiaInfection.PARAM_DIST_PARAM_INDEX_REGEX.replaceAll("999", "" + ChlamydiaInfection.DIST_TRANS_MF_INDEX);
                    ct_inf.setParameter(key, new double[]{trans[0], 0});

                    textOutput.println("Trans MF = " + Arrays.toString((double[]) ct_inf.getParameter(key)));

                    key = ChlamydiaInfection.PARAM_DIST_PARAM_INDEX_REGEX.replaceAll("999", "" + ChlamydiaInfection.DIST_TRANS_FM_INDEX);
                    ct_inf.setParameter(key, new double[]{trans[1], 0});

                    textOutput.println("Trans FM = " + Arrays.toString((double[]) ct_inf.getParameter(key)));

                    double[] dur;
                    key = ChlamydiaInfection.PARAM_DIST_PARAM_INDEX_REGEX.replaceAll("999", "" + ChlamydiaInfection.DIST_INFECT_ASY_DUR_INDEX);
                    dur = (double[]) ct_inf.getParameter(key);

                    dur[0] = param[SENSITIVITY_ANALYSIS_DURATION_SYM]
                            + param[SENSITIVITY_ANALYSIS_DURATION_ASM_EXTRA];
                    dur[1] = 0;
                    ct_inf.setParameter(key, dur);

                    textOutput.println("Duration Asy = " + Arrays.toString((double[]) ct_inf.getParameter(key)));

                    key = ChlamydiaInfection.PARAM_DIST_PARAM_INDEX_REGEX.replaceAll("999", "" + ChlamydiaInfection.DIST_INFECT_SYM_DUR_INDEX);
                    dur = (double[]) ct_inf.getParameter(key);

                    dur[0] = param[SENSITIVITY_ANALYSIS_DURATION_SYM];
                    dur[1] = 0;
                    ct_inf.setParameter(key, dur);

                    textOutput.println("Duration Sym = " + Arrays.toString((double[]) ct_inf.getParameter(key)));

                    textOutput.println("Inf Sim #" + simId + " seed = " + seed + " created");

                    sim.setOutputPrintStream(textOutput);

                    sim.setTestingHistory(new ConcurrentHashMap<Integer, int[]>());
                    sim.run();
                    textOutput.close();

                    testHist.writeObject(sim.getTestingHistory());
                    testHist.close();

                }

            } catch (IOException | ClassNotFoundException ex) {
                try {
                    textOutput = new PrintStream(new File(OUTPUT_PATH, "output_sim_" + simId + ".txt"));
                    ex.printStackTrace(textOutput);
                } catch (FileNotFoundException ex1) {
                    ex.printStackTrace(System.err);

                }

            }

        }

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

}
