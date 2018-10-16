package run;

import availability.AbstractAvailability;
import availability.Availability_ACCEPtPlus_SelectiveMixing_Rand;
import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.util.Arrays;
import person.AbstractIndividualInterface;
import person.Person_ACCEPtPlusSingleInflection;
import population.Population_ACCEPtPlus;
import sim.Runnable_Population_ACCEPtPlus;
import sim.Runnable_Population_ACCEPtPlus_Infection;
import infection.ChlamydiaInfection;
import infection.ChlamydiaInfectionClassSpecific;
import java.io.FileFilter;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.ObjectOutputStream;
import java.io.PrintStream;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import org.apache.commons.math3.distribution.AbstractRealDistribution;
import org.apache.commons.math3.distribution.BetaDistribution;
import org.apache.commons.math3.distribution.GammaDistribution;
import org.apache.commons.math3.exception.NotStrictlyPositiveException;
import org.apache.commons.math3.random.MersenneTwister;
import population.Population_ACCEPtPlus_MixedBehaviour;
import random.RandomGenerator;
import util.Classifier_ACCEPt;
import util.Classifier_Gender_Age_Specific_Infection;
import util.Classifier_Gender_Infection;
import util.FileZipper;
import util.PersonClassifier;

/**
 *
 * @author Ben
 * @version 20180626
 *
 * <pre>
 * History:
 *
 * 20180626 - Add parameter input for BEST_FIT_DUR_SD_MALE and BEST_FIT_DUR_SD_FEMALE
 * 20181011 - Add support for simulation run time
 *
 * </pre>
 */
public class Run_Population_ACCEPtPlus_InfectionIntro_Batch {

    public static final int NUM_THREADS = Runtime.getRuntime().availableProcessors();
    public static final PersonClassifier ACCEPT_CLASSIFIER = new Classifier_ACCEPt();
    public static final long BASE_SEED = 2251912970037127827l;

    public int NUM_SIM_TOTAL = 1000;
    public int SIM_DURATION = 30 * 12 * 30;
    public String IMPORT_PATH = "../ACCEPtPlusDirVM/ImportDir"; //;

    public File BATCH_BASE_PATH = new File("../ACCEPtPlusDirVM/1000_Runs");
    public int SKIP_DATA = 0;

    public double[] BEST_FIT_PARAMETER = new double[]{
        0,
        0,
        //BEST_FIT_PARAM_TRAN_MALE_TO_FEMALE_EXTRA
        //BEST_FIT_PARAM_TRAN_FEMALE_TO_MALE       
        // Default
        //0.04, //0.08058424309571056,
        //0.12,
        0.08,
        0.06005890880872483,
        //From C:\Users\Bhui\OneDrive - UNSW\ACCEPt\OptResult_AgeSpecTran_GA                
        //0.009054253860880417,
        //0.10005890880872483,
        // From HPC version   
        //0.21083079236305663,
        //0.4683239529032017,
        // From C:\Users\Bhui\OneDrive - UNSW\ACCEPt\OptResult_AgeSpecTran_GA - median          
        //0.11848589783392906,
        //0.29948000466137015,                                       
        433,
        433,
        0,
        0,
        0,
        0,
        0,
        0,
        // Male 16-19, 20-24, 25-29, 30-39, 40-49, 50-59, 60-69, (not used)
        // 1.4f, 1.4f, 1.2f, 1.1f, 1.0f, 0.9f, Float.NaN,  
        // 2.5f, 2.7f, 2.8f    
        0.85,
        1.4,
        1.4,
        1.2,
        1.1,
        1.0,
        0.9,
        // Female 16-19, 20-24, 25-29, 30-39, 40-49, 50-59, 60-69, (not used)
        // 1.0f, 1.1f, 1.0f, 0.9f, 0.8f, 0.6f, Float.NaN,
        // 2.0f, 1.9f, 1.8f 
        1.0,
        1.2,
        1.2,
        1.0,
        0.9,
        0.8,
        0.6,
        // Mixing - Random
        0.0,
        // ClassSpecific Transmission Prob - or set them as negative if not used

        -0.12,
        -0.12,
        -0.12,
        -0.04,
        -0.04,
        -0.04,
        // From C:\Users\Bhui\OneDrive - UNSW\ACCEPt\OptResult_AgeSpecTran_GA    	
        /*
        0.1430962072120926,
        0.12928530021587178,
        0.11342604999394956,
        0.3929934055032376 - 0.1430962072120926,
        0.38479320977846554 - 0.12928530021587178,
        0.353920282147386 - 0.11342604999394956,
         */
        // From HPC version   
        /*
        0.4919576888823929,
        0.4585467521205485,
        0.4244809849441269,
        0.1654047570061263,
        0.008608904532792434,
        0.07860823645434195, 
         */
        // From C:\Users\Bhui\OneDrive - UNSW\ACCEPt\OptResult_AgeSpecTran_GA - median  
        /*
        0.32140812823610787,
        0.31408198644220947,
        0.2874156545279897,
        0.12284093909265331,
        0.11992972705252944,
        0.12516298230014455,        
         */
        // ClassSpecific Infection duration - not used 
        433,
        433,
        433,
        433,
        433,
        433,
        //BEST_FIT_TRANS_SD_MF, BEST_FIT_TRANS_SD_FM
        // Set to 0 if not used
        0.10,
        0.04,
        //BEST_FIT_DUR_SD_MALE, BEST_FIT_DUR_SD_FEMALE
        7,
        7
    };

    public static final int INDEX_TEST_RATE_MALE = 0;
    public static final int INDEX_TEST_RATE_FEMALE = INDEX_TEST_RATE_MALE + 1;
    public static final int INDEX_RETEST_RATE = INDEX_TEST_RATE_FEMALE + 1;
    public static final int INDEX_PARTNER_TREATMENT_RATE = INDEX_RETEST_RATE + 1;
    public static final int INDEX_TEST_SENSITIVITY = INDEX_PARTNER_TREATMENT_RATE + 1;
    public static final int INDEX_CONT_TEST_30PLUS = INDEX_TEST_SENSITIVITY + 1;
    public static final int INDEX_INTRO_INFECTION = INDEX_CONT_TEST_30PLUS + 1;
    public static final int INDEX_MASS_SCREENING_SETTING = INDEX_INTRO_INFECTION + 1;
    public static final int INDEX_STORE_PREVAL_FREQ = INDEX_MASS_SCREENING_SETTING + 1;

    public static final int BEST_FIT_PARAM_INTRO_RATE_MALE = 0; // If negative, only for first time step
    public static final int BEST_FIT_PARAM_INTRO_RATE_FEMALE = BEST_FIT_PARAM_INTRO_RATE_MALE + 1;
    public static final int BEST_FIT_PARAM_TRAN_MALE_TO_FEMALE_EXTRA = BEST_FIT_PARAM_INTRO_RATE_FEMALE + 1;
    public static final int BEST_FIT_PARAM_TRAN_FEMALE_TO_MALE = BEST_FIT_PARAM_TRAN_MALE_TO_FEMALE_EXTRA + 1;
    public static final int BEST_FIT_PARAM_AVE_INF_DUR_FEMALE = BEST_FIT_PARAM_TRAN_FEMALE_TO_MALE + 1;
    public static final int BEST_FIT_PARAM_AVE_INF_DUR_MALE = BEST_FIT_PARAM_AVE_INF_DUR_FEMALE + 1;
    public static final int BEST_FIT_PARAM_PROPORTION_IN_ACCEPT_BEHAVIOR_MALE_16_19 = BEST_FIT_PARAM_AVE_INF_DUR_MALE + 1;
    public static final int BEST_FIT_PARAM_PROPORTION_IN_ACCEPT_BEHAVIOR_MALE_20_24 = BEST_FIT_PARAM_PROPORTION_IN_ACCEPT_BEHAVIOR_MALE_16_19 + 1;
    public static final int BEST_FIT_PARAM_PROPORTION_IN_ACCEPT_BEHAVIOR_MALE_25_29 = BEST_FIT_PARAM_PROPORTION_IN_ACCEPT_BEHAVIOR_MALE_20_24 + 1;
    public static final int BEST_FIT_PARAM_PROPORTION_IN_ACCEPT_BEHAVIOR_FEMALE_16_19 = BEST_FIT_PARAM_PROPORTION_IN_ACCEPT_BEHAVIOR_MALE_25_29 + 1;
    public static final int BEST_FIT_PARAM_PROPORTION_IN_ACCEPT_BEHAVIOR_FEMALE_20_24 = BEST_FIT_PARAM_PROPORTION_IN_ACCEPT_BEHAVIOR_FEMALE_16_19 + 1;
    public static final int BEST_FIT_PARAM_PROPORTION_IN_ACCEPT_BEHAVIOR_FEMALE_25_29 = BEST_FIT_PARAM_PROPORTION_IN_ACCEPT_BEHAVIOR_FEMALE_20_24 + 1;
    public static final int BEST_FIT_PARAM_GENDER_WEIGHT_16_19_M = BEST_FIT_PARAM_PROPORTION_IN_ACCEPT_BEHAVIOR_FEMALE_25_29 + 1;
    public static final int BEST_FIT_PARAM_GENDER_WEIGHT_20_24_M = BEST_FIT_PARAM_GENDER_WEIGHT_16_19_M + 1;
    public static final int BEST_FIT_PARAM_GENDER_WEIGHT_25_29_M = BEST_FIT_PARAM_GENDER_WEIGHT_20_24_M + 1;
    public static final int BEST_FIT_PARAM_GENDER_WEIGHT_30_39_M = BEST_FIT_PARAM_GENDER_WEIGHT_25_29_M + 1;
    public static final int BEST_FIT_PARAM_GENDER_WEIGHT_40_49_M = BEST_FIT_PARAM_GENDER_WEIGHT_30_39_M + 1;
    public static final int BEST_FIT_PARAM_GENDER_WEIGHT_50_59_M = BEST_FIT_PARAM_GENDER_WEIGHT_40_49_M + 1;
    public static final int BEST_FIT_PARAM_GENDER_WEIGHT_60_69_M = BEST_FIT_PARAM_GENDER_WEIGHT_50_59_M + 1;
    public static final int BEST_FIT_PARAM_GENDER_WEIGHT_16_19_F = BEST_FIT_PARAM_GENDER_WEIGHT_60_69_M + 1;
    public static final int BEST_FIT_PARAM_GENDER_WEIGHT_20_24_F = BEST_FIT_PARAM_GENDER_WEIGHT_16_19_F + 1;
    public static final int BEST_FIT_PARAM_GENDER_WEIGHT_25_29_F = BEST_FIT_PARAM_GENDER_WEIGHT_20_24_F + 1;
    public static final int BEST_FIT_PARAM_GENDER_WEIGHT_30_39_F = BEST_FIT_PARAM_GENDER_WEIGHT_25_29_F + 1;
    public static final int BEST_FIT_PARAM_GENDER_WEIGHT_40_49_F = BEST_FIT_PARAM_GENDER_WEIGHT_30_39_F + 1;
    public static final int BEST_FIT_PARAM_GENDER_WEIGHT_50_59_F = BEST_FIT_PARAM_GENDER_WEIGHT_40_49_F + 1;
    public static final int BEST_FIT_PARAM_GENDER_WEIGHT_60_69_F = BEST_FIT_PARAM_GENDER_WEIGHT_50_59_F + 1;
    public static final int BEST_FIT_PARAM_MIXING_RAND = BEST_FIT_PARAM_GENDER_WEIGHT_60_69_F + 1;
    public static final int BEST_FIT_TRAN_PROB_TO_16_19_M = BEST_FIT_PARAM_MIXING_RAND + 1;
    public static final int BEST_FIT_TRAN_PROB_TO_20_24_M = BEST_FIT_TRAN_PROB_TO_16_19_M + 1;
    public static final int BEST_FIT_TRAN_PROB_TO_25_29_M = BEST_FIT_TRAN_PROB_TO_20_24_M + 1;
    public static final int BEST_FIT_TRAN_PROB_TO_16_19_F_EXTRA = BEST_FIT_TRAN_PROB_TO_25_29_M + 1;
    public static final int BEST_FIT_TRAN_PROB_TO_20_24_F_EXTRA = BEST_FIT_TRAN_PROB_TO_16_19_F_EXTRA + 1;
    public static final int BEST_FIT_TRAN_PROB_TO_25_29_F_EXTRA = BEST_FIT_TRAN_PROB_TO_20_24_F_EXTRA + 1;
    public static final int BEST_FIT_INF_DUR_16_19_M = BEST_FIT_TRAN_PROB_TO_25_29_F_EXTRA + 1;
    public static final int BEST_FIT_INF_DUR_20_24_M = BEST_FIT_INF_DUR_16_19_M + 1;
    public static final int BEST_FIT_INF_DUR_25_29_M = BEST_FIT_INF_DUR_20_24_M + 1;
    public static final int BEST_FIT_INF_DUR_16_19_F = BEST_FIT_INF_DUR_25_29_M + 1;
    public static final int BEST_FIT_INF_DUR_20_24_F = BEST_FIT_INF_DUR_16_19_F + 1;
    public static final int BEST_FIT_INF_DUR_25_29_F = BEST_FIT_INF_DUR_20_24_F + 1;
    public static final int BEST_FIT_TRANS_SD_MF = BEST_FIT_INF_DUR_25_29_F + 1;
    public static final int BEST_FIT_TRANS_SD_FM = BEST_FIT_TRANS_SD_MF + 1;
    public static final int BEST_FIT_DUR_SD_MALE = BEST_FIT_TRANS_SD_FM + 1;
    public static final int BEST_FIT_DUR_SD_FEMALE = BEST_FIT_DUR_SD_MALE + 1;

    public static final Object[] DEFAULT_RATE = {
        //INDEX_TEST_RATE_MALE - Meeting note page 40
        0.072f,
        //INDEX_TEST_RATE_FEMALE - Meeting note page 40
        0.169f,
        //INDEX_RETEST_RATE        
        // Format: float[classId][][]{number of days up to one year,probability}
        // From email 20171019 TC Summary
        new float[][][]{
            new float[][]{
                new float[]{4 * 30}, new float[]{0.236f}
            },},
        //INDEX_PARTNER_TREATMENT_RATE        
        // float or float[]
        // From email 20171019 TC Summary
        0.3f, // From Jane's email 20160826       0.828f * (1 + 0.035f) / 2,
        //INDEX_TEST_SENSITIVITY
        // Default = 100%
        1f,
        //INDEX_CONT_TEST_30PLUS
        // Default = 100%
        new float[]{1f, 1f, 1f},
        // Optional extra
        // INDEX_INTRO_INFECTION
        // 0 = no intro, 1 = initial, 2 = periodic
        new Integer(1),
        // INDEX_MASS_SCREENING_SETTING        
        // Object[]{ PersonClassifier, float[] treatment_rate_by_classIndex, int[][] at_by_classIndex{[at, duration, ...]}   
        null,
        // INDEX_STORE_PREVAL_FREQ
        null,};

    public static void main(String[] arg) throws IOException, ClassNotFoundException, InterruptedException {
        Run_Population_ACCEPtPlus_InfectionIntro_Batch run = new Run_Population_ACCEPtPlus_InfectionIntro_Batch(arg);
        run.batchRun();
    }

    public Run_Population_ACCEPtPlus_InfectionIntro_Batch(String[] arg) {

        if (arg.length > 0) {
            if (!arg[0].isEmpty()) {
                System.out.println("Setting BATCH_BASE_PATH as " + arg[0]);
                BATCH_BASE_PATH = new File(arg[0]);
            }
        }
        if (arg.length > 1) {
            if (!arg[1].isEmpty()) {
                System.out.println("Setting IMPORT_PATH as " + arg[1]);
                IMPORT_PATH = arg[1];
            }
        }
        if (arg.length > 2) {
            if (!arg[2].isEmpty()) {
                System.out.println("Num of sim total = " + arg[2]);
                NUM_SIM_TOTAL = Integer.parseInt(arg[2]);
            }
        }

        if (arg.length > 3) {
            if (!arg[3].isEmpty()) {
                SKIP_DATA = Integer.parseInt(arg[3]);
                System.out.println("Skip dataset = 0b" + Integer.toBinaryString(SKIP_DATA));
            }
        }
        if (arg.length > 4) {
            if (!arg[4].isEmpty()) {
                DEFAULT_RATE[INDEX_INTRO_INFECTION] = Integer.parseInt(arg[4]);
                System.out.println("Infection intro type = " + DEFAULT_RATE[INDEX_INTRO_INFECTION].toString());
            }

        }

        if (arg.length > 5) {
            if (!arg[5].isEmpty()) {
                DEFAULT_RATE[INDEX_STORE_PREVAL_FREQ] = Integer.parseInt(arg[5]);
                System.out.println("Prevalence store freq = " + DEFAULT_RATE[INDEX_STORE_PREVAL_FREQ].toString());
            }
        }

        if (arg.length > 6) {
            if (!arg[6].isEmpty()) {
                int numSnap = Integer.parseInt(arg[6]);
                SIM_DURATION = (DEFAULT_RATE[INDEX_STORE_PREVAL_FREQ] == null)
                        ? (30 * numSnap) : (((Integer) DEFAULT_RATE[INDEX_STORE_PREVAL_FREQ]) * numSnap);
                System.out.println("Sim duration = " + SIM_DURATION);
            }
        }

    }

    public void batchRun()
            throws ClassNotFoundException, IOException, InterruptedException {
        File targetDir;
        Object[] inputParam;
        long tic;
        int DURATION_MASS_SCR = 6 * 7;
        Run_Population_ACCEPtPlus_InfectionIntro_Batch batchRun = this;
        int datasetCount = 0;

        BATCH_BASE_PATH.mkdirs();

        // 0: Baseline rate
        if (((SKIP_DATA >> datasetCount) & 1) == 0) {
            targetDir = new File(BATCH_BASE_PATH, "Baseline");
            inputParam = new Object[]{
                DEFAULT_RATE[INDEX_TEST_RATE_MALE], DEFAULT_RATE[INDEX_TEST_RATE_FEMALE],
                DEFAULT_RATE[INDEX_RETEST_RATE], DEFAULT_RATE[INDEX_PARTNER_TREATMENT_RATE],
                DEFAULT_RATE[INDEX_TEST_SENSITIVITY], DEFAULT_RATE[INDEX_CONT_TEST_30PLUS],
                DEFAULT_RATE[INDEX_INTRO_INFECTION],
                DEFAULT_RATE[INDEX_MASS_SCREENING_SETTING], DEFAULT_RATE[INDEX_STORE_PREVAL_FREQ],};

            tic = System.currentTimeMillis();
            System.out.println("Generating data for " + targetDir.getAbsolutePath());
            batchRun.singleRun(targetDir, inputParam);
            tic = System.currentTimeMillis() - tic;
            System.out.println("Time required = " + (((float) tic) / 1000) + " s");
        }
        datasetCount++;
        // 1: Intervention rate 
        // Testing rate - Meeting note page 40
        // Retest and PT rate: From email 20171019 TC Summary
        if (((SKIP_DATA >> datasetCount) & 1) == 0) {
            targetDir = new File(BATCH_BASE_PATH, "Intervention_rate");
            inputParam = new Object[]{
                0.124f, 0.251f,
                new float[][][]{
                    new float[][]{
                        new float[]{4 * 30}, new float[]{0.281f}
                    },},
                DEFAULT_RATE[INDEX_PARTNER_TREATMENT_RATE],
                DEFAULT_RATE[INDEX_TEST_SENSITIVITY], DEFAULT_RATE[INDEX_CONT_TEST_30PLUS],
                DEFAULT_RATE[INDEX_INTRO_INFECTION],
                DEFAULT_RATE[INDEX_MASS_SCREENING_SETTING], DEFAULT_RATE[INDEX_STORE_PREVAL_FREQ],
                    
            };

            tic = System.currentTimeMillis();
            System.out.println("Generating data for " + targetDir.getAbsolutePath());
            batchRun.singleRun(targetDir, inputParam);
            tic = System.currentTimeMillis() - tic;
            System.out.println("Time required = " + (((float) tic) / 1000) + " s");
        }
        datasetCount++;
        // 2: 30 Plus testing
        if (((SKIP_DATA >> datasetCount) & 1) == 0) {
            targetDir = new File(BATCH_BASE_PATH, "Intervention_rate_30_Plus_Reduced");
            inputParam = new Object[]{
                0.124f, 0.251f,
                new float[][][]{
                    new float[][]{
                        new float[]{4 * 30}, new float[]{0.281f}
                    },},
                DEFAULT_RATE[INDEX_PARTNER_TREATMENT_RATE],
                DEFAULT_RATE[INDEX_TEST_SENSITIVITY],
                new float[]{0.5f, 0.25f, 0.0f},
                DEFAULT_RATE[INDEX_INTRO_INFECTION],
                DEFAULT_RATE[INDEX_MASS_SCREENING_SETTING], DEFAULT_RATE[INDEX_STORE_PREVAL_FREQ],
            };

            tic = System.currentTimeMillis();
            System.out.println("Generating data for " + targetDir.getAbsolutePath());
            batchRun.singleRun(targetDir, inputParam);
            tic = System.currentTimeMillis() - tic;
            System.out.println("Time required = " + (((float) tic) / 1000) + " s");
        }
        datasetCount++;
        // 3: No 30 Plus
        if (((SKIP_DATA >> datasetCount) & 1) == 0) {

            targetDir = new File(BATCH_BASE_PATH, "Intervention_rate_30_Plus_No");
            inputParam = new Object[]{
                0.124f, 0.251f,
                new float[][][]{
                    new float[][]{
                        new float[]{4 * 30}, new float[]{0.281f}
                    },},
                DEFAULT_RATE[INDEX_PARTNER_TREATMENT_RATE],
                DEFAULT_RATE[INDEX_TEST_SENSITIVITY],
                new float[]{0f, 0f, 0f},
                DEFAULT_RATE[INDEX_INTRO_INFECTION],
                DEFAULT_RATE[INDEX_MASS_SCREENING_SETTING], DEFAULT_RATE[INDEX_STORE_PREVAL_FREQ],
            };

            tic = System.currentTimeMillis();
            System.out.println("Generating data for " + targetDir.getAbsolutePath());
            batchRun.singleRun(targetDir, inputParam);
            tic = System.currentTimeMillis() - tic;
            System.out.println("Time required = " + (((float) tic) / 1000) + " s");
        }
        datasetCount++;
        // 4: Double Retesting
        if (((SKIP_DATA >> datasetCount) & 1) == 0) {
            targetDir = new File(BATCH_BASE_PATH, "Intervention_rate_Retesting_Double");
            inputParam = new Object[]{
                0.124f, 0.251f,
                new float[][][]{
                    new float[][]{new float[]{4 * 30}, new float[]{0.236f * 2}},}, // From email 20171019 TC Summary
                DEFAULT_RATE[INDEX_PARTNER_TREATMENT_RATE],
                DEFAULT_RATE[INDEX_TEST_SENSITIVITY], DEFAULT_RATE[INDEX_CONT_TEST_30PLUS],
                DEFAULT_RATE[INDEX_INTRO_INFECTION],
                DEFAULT_RATE[INDEX_MASS_SCREENING_SETTING], DEFAULT_RATE[INDEX_STORE_PREVAL_FREQ],
            };

            tic = System.currentTimeMillis();
            System.out.println("Generating data for " + targetDir.getAbsolutePath());
            batchRun.singleRun(targetDir, inputParam);
            tic = System.currentTimeMillis() - tic;
            System.out.println("Time required = " + (((float) tic) / 1000) + " s");
        }
        datasetCount++;
        // 5: Increase PT rate - 50        
        if (((SKIP_DATA >> datasetCount) & 1) == 0) {
            targetDir = new File(BATCH_BASE_PATH, "Intervention_rate_PT_50");
            inputParam = new Object[]{
                0.124f, 0.251f,
                new float[][][]{
                    new float[][]{
                        new float[]{4 * 30}, new float[]{0.281f}
                    },},
                0.5f,
                DEFAULT_RATE[INDEX_TEST_SENSITIVITY], DEFAULT_RATE[INDEX_CONT_TEST_30PLUS],
                DEFAULT_RATE[INDEX_INTRO_INFECTION],
                DEFAULT_RATE[INDEX_MASS_SCREENING_SETTING], DEFAULT_RATE[INDEX_STORE_PREVAL_FREQ],
            };

            tic = System.currentTimeMillis();
            System.out.println("Generating data for " + targetDir.getAbsolutePath());
            batchRun.singleRun(targetDir, inputParam);
            tic = System.currentTimeMillis() - tic;
            System.out.println("Time required = " + (((float) tic) / 1000) + " s");
        }
        datasetCount++;
        // 6: Increase PT rate - 80
        if (((SKIP_DATA >> datasetCount) & 1) == 0) {
            targetDir = new File(BATCH_BASE_PATH, "Intervention_rate_PT_80");
            inputParam = new Object[]{
                0.124f, 0.251f,
                new float[][][]{
                    new float[][]{
                        new float[]{4 * 30}, new float[]{0.281f}
                    },},
                0.8f,
                DEFAULT_RATE[INDEX_TEST_SENSITIVITY], DEFAULT_RATE[INDEX_CONT_TEST_30PLUS],
                DEFAULT_RATE[INDEX_INTRO_INFECTION],
                DEFAULT_RATE[INDEX_MASS_SCREENING_SETTING], DEFAULT_RATE[INDEX_STORE_PREVAL_FREQ],
            };

            tic = System.currentTimeMillis();
            System.out.println("Generating data for " + targetDir.getAbsolutePath());
            batchRun.singleRun(targetDir, inputParam);
            tic = System.currentTimeMillis() - tic;
            System.out.println("Time required = " + (((float) tic) / 1000) + " s");
        }
        datasetCount++;
        // 7: Double retest + Increase PT rate - 80
        if (((SKIP_DATA >> datasetCount) & 1) == 0) {
            targetDir = new File(BATCH_BASE_PATH, "Intervention_rate_Retesting_Double_PT_80");
            inputParam = new Object[]{
                0.124f, 0.251f,
                new float[][][]{
                    new float[][]{new float[]{4 * 30}, new float[]{0.236f * 2}},}, // From email 20171019 TC Summary
                0.8f,
                DEFAULT_RATE[INDEX_TEST_SENSITIVITY], DEFAULT_RATE[INDEX_CONT_TEST_30PLUS],
                DEFAULT_RATE[INDEX_INTRO_INFECTION],
                DEFAULT_RATE[INDEX_MASS_SCREENING_SETTING], DEFAULT_RATE[INDEX_STORE_PREVAL_FREQ],
            };

            tic = System.currentTimeMillis();
            System.out.println("Generating data for " + targetDir.getAbsolutePath());
            batchRun.singleRun(targetDir, inputParam);
            tic = System.currentTimeMillis() - tic;
            System.out.println("Time required = " + (((float) tic) / 1000) + " s");
        }
        datasetCount++;
        // 8: Mass screen - default rate
        if (((SKIP_DATA >> datasetCount) & 1) == 0) {
            targetDir = new File(BATCH_BASE_PATH, "MassScreen");
            inputParam = new Object[]{
                DEFAULT_RATE[INDEX_TEST_RATE_MALE], DEFAULT_RATE[INDEX_TEST_RATE_FEMALE],
                DEFAULT_RATE[INDEX_RETEST_RATE], DEFAULT_RATE[INDEX_PARTNER_TREATMENT_RATE],
                DEFAULT_RATE[INDEX_TEST_SENSITIVITY], DEFAULT_RATE[INDEX_CONT_TEST_30PLUS],
                DEFAULT_RATE[INDEX_INTRO_INFECTION],
                new Object[]{
                    new PersonClassifier() {
                        @Override
                        public int classifyPerson(AbstractIndividualInterface p) {
                            if (p.getAge() >= 16 * AbstractIndividualInterface.ONE_YEAR_INT
                                    && p.getAge() < 30 * AbstractIndividualInterface.ONE_YEAR_INT) {
                                return p.isMale() ? 0 : 1;
                            } else {
                                return -1;
                            }
                        }

                        @Override
                        public int numClass() {
                            return 2;
                        }

                    },
                    new float[]{0.69f * 0.7f, 0.85f * 0.7f},
                    new int[][]{
                        new int[]{5 * 360, DURATION_MASS_SCR},
                        new int[]{5 * 360, DURATION_MASS_SCR},}
                },
                DEFAULT_RATE[INDEX_STORE_PREVAL_FREQ]
            };

            tic = System.currentTimeMillis();
            System.out.println("Generating data for " + targetDir.getAbsolutePath());
            batchRun.singleRun(targetDir, inputParam);
            tic = System.currentTimeMillis() - tic;
            System.out.println("Time required = " + (((float) tic) / 1000) + " s");
        }
        datasetCount++;
        // 9:  Mass screen + 50 PT
        if (((SKIP_DATA >> datasetCount) & 1) == 0) {
            targetDir = new File(BATCH_BASE_PATH, "MassScreen_50_PT_During");

            inputParam = new Object[]{
                DEFAULT_RATE[INDEX_TEST_RATE_MALE], DEFAULT_RATE[INDEX_TEST_RATE_FEMALE],
                DEFAULT_RATE[INDEX_RETEST_RATE],
                new float[]{(float) DEFAULT_RATE[INDEX_PARTNER_TREATMENT_RATE],
                    5 * 360f, 0.5f, 5 * 360f + DURATION_MASS_SCR, (float) DEFAULT_RATE[INDEX_PARTNER_TREATMENT_RATE]},
                DEFAULT_RATE[INDEX_TEST_SENSITIVITY], DEFAULT_RATE[INDEX_CONT_TEST_30PLUS],
                DEFAULT_RATE[INDEX_INTRO_INFECTION],
                new Object[]{
                    new PersonClassifier() {
                        @Override
                        public int classifyPerson(AbstractIndividualInterface p) {
                            if (p.getAge() >= 16 * AbstractIndividualInterface.ONE_YEAR_INT
                                    && p.getAge() < 30 * AbstractIndividualInterface.ONE_YEAR_INT) {
                                return p.isMale() ? 0 : 1;
                            } else {
                                return -1;
                            }
                        }

                        @Override
                        public int numClass() {
                            return 2;
                        }

                    },
                    new float[]{0.69f * 0.7f, 0.85f * 0.7f},
                    new int[][]{
                        new int[]{5 * 360, DURATION_MASS_SCR},
                        new int[]{5 * 360, DURATION_MASS_SCR},}
                },
                DEFAULT_RATE[INDEX_STORE_PREVAL_FREQ]
            };

            tic = System.currentTimeMillis();
            System.out.println("Generating data for " + targetDir.getAbsolutePath());
            batchRun.singleRun(targetDir, inputParam);
            tic = System.currentTimeMillis() - tic;
            System.out.println("Time required = " + (((float) tic) / 1000) + " s");
        }
        datasetCount++;
        // 10: Mass screen + 60 PT
        if (((SKIP_DATA >> datasetCount) & 1) == 0) {
            targetDir = new File(BATCH_BASE_PATH, "MassScreen_60_PT_During");
            inputParam = new Object[]{
                DEFAULT_RATE[INDEX_TEST_RATE_MALE], DEFAULT_RATE[INDEX_TEST_RATE_FEMALE],
                DEFAULT_RATE[INDEX_RETEST_RATE],
                new float[]{(float) DEFAULT_RATE[INDEX_PARTNER_TREATMENT_RATE],
                    5 * 360f, 0.6f, 5 * 360f + DURATION_MASS_SCR, (float) DEFAULT_RATE[INDEX_PARTNER_TREATMENT_RATE]},
                DEFAULT_RATE[INDEX_TEST_SENSITIVITY], DEFAULT_RATE[INDEX_CONT_TEST_30PLUS],
                DEFAULT_RATE[INDEX_INTRO_INFECTION],
                new Object[]{
                    new PersonClassifier() {
                        @Override
                        public int classifyPerson(AbstractIndividualInterface p) {
                            if (p.getAge() >= 16 * AbstractIndividualInterface.ONE_YEAR_INT
                                    && p.getAge() < 30 * AbstractIndividualInterface.ONE_YEAR_INT) {
                                return p.isMale() ? 0 : 1;
                            } else {
                                return -1;
                            }
                        }

                        @Override
                        public int numClass() {
                            return 2;
                        }

                    },
                    new float[]{0.69f * 0.7f, 0.85f * 0.7f},
                    new int[][]{
                        new int[]{5 * 360, DURATION_MASS_SCR},
                        new int[]{5 * 360, DURATION_MASS_SCR},}
                },
                DEFAULT_RATE[INDEX_STORE_PREVAL_FREQ]
            };

            tic = System.currentTimeMillis();
            System.out.println("Generating data for " + targetDir.getAbsolutePath());
            batchRun.singleRun(targetDir, inputParam);
            tic = System.currentTimeMillis() - tic;
            System.out.println("Time required = " + (((float) tic) / 1000) + " s");
        }
        datasetCount++;
        // 11: Mass screen + 70 PT   
        if (((SKIP_DATA >> datasetCount) & 1) == 0) {
            targetDir = new File(BATCH_BASE_PATH, "MassScreen_70_PT_During");
            inputParam = new Object[]{
                DEFAULT_RATE[INDEX_TEST_RATE_MALE], DEFAULT_RATE[INDEX_TEST_RATE_FEMALE],
                DEFAULT_RATE[INDEX_RETEST_RATE],
                new float[]{(float) DEFAULT_RATE[INDEX_PARTNER_TREATMENT_RATE],
                    5 * 360f, 0.7f, 5 * 360f + DURATION_MASS_SCR, (float) DEFAULT_RATE[INDEX_PARTNER_TREATMENT_RATE]}, //0.7f,
                DEFAULT_RATE[INDEX_TEST_SENSITIVITY], DEFAULT_RATE[INDEX_CONT_TEST_30PLUS],
                DEFAULT_RATE[INDEX_INTRO_INFECTION],
                new Object[]{
                    new PersonClassifier() {
                        @Override
                        public int classifyPerson(AbstractIndividualInterface p) {
                            if (p.getAge() >= 16 * AbstractIndividualInterface.ONE_YEAR_INT
                                    && p.getAge() < 30 * AbstractIndividualInterface.ONE_YEAR_INT) {
                                return p.isMale() ? 0 : 1;
                            } else {
                                return -1;
                            }
                        }

                        @Override
                        public int numClass() {
                            return 2;
                        }

                    },
                    new float[]{0.69f * 0.7f, 0.85f * 0.7f},
                    new int[][]{
                        new int[]{5 * 360, DURATION_MASS_SCR},
                        new int[]{5 * 360, DURATION_MASS_SCR},}
                },
                DEFAULT_RATE[INDEX_STORE_PREVAL_FREQ]
            };

            tic = System.currentTimeMillis();
            System.out.println("Generating data for " + targetDir.getAbsolutePath());
            batchRun.singleRun(targetDir, inputParam);
            tic = System.currentTimeMillis() - tic;
            System.out.println("Time required = " + (((float) tic) / 1000) + " s");
        }
        datasetCount++;
        // 12: Mass screen + 50 PT for 3 months yr   
        if (((SKIP_DATA >> datasetCount) & 1) == 0) {
            targetDir = new File(BATCH_BASE_PATH, "MassScreen_50_PT_3_month");
            inputParam = new Object[]{
                DEFAULT_RATE[INDEX_TEST_RATE_MALE], DEFAULT_RATE[INDEX_TEST_RATE_FEMALE],
                DEFAULT_RATE[INDEX_RETEST_RATE],
                new float[]{(float) DEFAULT_RATE[INDEX_PARTNER_TREATMENT_RATE],
                    5 * 360f, 0.5f, 5 * 360f + DURATION_MASS_SCR + 30 * 3, (float) DEFAULT_RATE[INDEX_PARTNER_TREATMENT_RATE]}, //0.7f,
                DEFAULT_RATE[INDEX_TEST_SENSITIVITY], DEFAULT_RATE[INDEX_CONT_TEST_30PLUS],
                DEFAULT_RATE[INDEX_INTRO_INFECTION],
                new Object[]{
                    new PersonClassifier() {
                        @Override
                        public int classifyPerson(AbstractIndividualInterface p) {
                            if (p.getAge() >= 16 * AbstractIndividualInterface.ONE_YEAR_INT
                                    && p.getAge() < 30 * AbstractIndividualInterface.ONE_YEAR_INT) {
                                return p.isMale() ? 0 : 1;
                            } else {
                                return -1;
                            }
                        }

                        @Override
                        public int numClass() {
                            return 2;
                        }

                    },
                    new float[]{0.69f * 0.7f, 0.85f * 0.7f},
                    new int[][]{
                        new int[]{5 * 360, DURATION_MASS_SCR},
                        new int[]{5 * 360, DURATION_MASS_SCR},}
                },
                DEFAULT_RATE[INDEX_STORE_PREVAL_FREQ]
            };

            tic = System.currentTimeMillis();
            System.out.println("Generating data for " + targetDir.getAbsolutePath());
            batchRun.singleRun(targetDir, inputParam);
            tic = System.currentTimeMillis() - tic;
            System.out.println("Time required = " + (((float) tic) / 1000) + " s");
        }
        datasetCount++;
        // 13: Mass screen + 60 PT for 3 mths   
        if (((SKIP_DATA >> datasetCount) & 1) == 0) {
            targetDir = new File(BATCH_BASE_PATH, "MassScreen_60_PT_3_month");
            inputParam = new Object[]{
                DEFAULT_RATE[INDEX_TEST_RATE_MALE], DEFAULT_RATE[INDEX_TEST_RATE_FEMALE],
                DEFAULT_RATE[INDEX_RETEST_RATE],
                new float[]{(float) DEFAULT_RATE[INDEX_PARTNER_TREATMENT_RATE],
                    5 * 360f, 0.6f, 5 * 360f + DURATION_MASS_SCR + 30 * 3, (float) DEFAULT_RATE[INDEX_PARTNER_TREATMENT_RATE]}, //0.7f,
                DEFAULT_RATE[INDEX_TEST_SENSITIVITY], DEFAULT_RATE[INDEX_CONT_TEST_30PLUS],
                DEFAULT_RATE[INDEX_INTRO_INFECTION],
                new Object[]{
                    new PersonClassifier() {
                        @Override
                        public int classifyPerson(AbstractIndividualInterface p) {
                            if (p.getAge() >= 16 * AbstractIndividualInterface.ONE_YEAR_INT
                                    && p.getAge() < 30 * AbstractIndividualInterface.ONE_YEAR_INT) {
                                return p.isMale() ? 0 : 1;
                            } else {
                                return -1;
                            }
                        }

                        @Override
                        public int numClass() {
                            return 2;
                        }

                    },
                    new float[]{0.69f * 0.7f, 0.85f * 0.7f},
                    new int[][]{
                        new int[]{5 * 360, DURATION_MASS_SCR},
                        new int[]{5 * 360, DURATION_MASS_SCR},}
                },
                DEFAULT_RATE[INDEX_STORE_PREVAL_FREQ]
            };

            tic = System.currentTimeMillis();
            System.out.println("Generating data for " + targetDir.getAbsolutePath());
            batchRun.singleRun(targetDir, inputParam);
            tic = System.currentTimeMillis() - tic;
            System.out.println("Time required = " + (((float) tic) / 1000) + " s");
        }
        datasetCount++;
        // 14: Mass screen + 70 PT for 3 mths   
        if (((SKIP_DATA >> datasetCount) & 1) == 0) {
            targetDir = new File(BATCH_BASE_PATH, "MassScreen_70_PT_3_month");
            inputParam = new Object[]{
                DEFAULT_RATE[INDEX_TEST_RATE_MALE], DEFAULT_RATE[INDEX_TEST_RATE_FEMALE],
                DEFAULT_RATE[INDEX_RETEST_RATE],
                new float[]{(float) DEFAULT_RATE[INDEX_PARTNER_TREATMENT_RATE],
                    5 * 360f, 0.7f, 5 * 360f + DURATION_MASS_SCR + 30 * 3, (float) DEFAULT_RATE[INDEX_PARTNER_TREATMENT_RATE]}, //0.7f,
                DEFAULT_RATE[INDEX_TEST_SENSITIVITY], DEFAULT_RATE[INDEX_CONT_TEST_30PLUS],
                DEFAULT_RATE[INDEX_INTRO_INFECTION],
                new Object[]{
                    new PersonClassifier() {
                        @Override
                        public int classifyPerson(AbstractIndividualInterface p) {
                            if (p.getAge() >= 16 * AbstractIndividualInterface.ONE_YEAR_INT
                                    && p.getAge() < 30 * AbstractIndividualInterface.ONE_YEAR_INT) {
                                return p.isMale() ? 0 : 1;
                            } else {
                                return -1;
                            }
                        }

                        @Override
                        public int numClass() {
                            return 2;
                        }

                    },
                    new float[]{0.69f * 0.7f, 0.85f * 0.7f},
                    new int[][]{
                        new int[]{5 * 360, DURATION_MASS_SCR},
                        new int[]{5 * 360, DURATION_MASS_SCR},}
                },
                DEFAULT_RATE[INDEX_STORE_PREVAL_FREQ]
            };

            tic = System.currentTimeMillis();
            System.out.println("Generating data for " + targetDir.getAbsolutePath());
            batchRun.singleRun(targetDir, inputParam);
            tic = System.currentTimeMillis() - tic;
            System.out.println("Time required = " + (((float) tic) / 1000) + " s");
        }
        datasetCount++;
        // 15: Mass screen - 1 month 
        if (((SKIP_DATA >> datasetCount) & 1) == 0) {
            targetDir = new File(BATCH_BASE_PATH, "MassScreen_1_month");
            int massScr = 30;
            inputParam = new Object[]{
                DEFAULT_RATE[INDEX_TEST_RATE_MALE], DEFAULT_RATE[INDEX_TEST_RATE_FEMALE],
                DEFAULT_RATE[INDEX_RETEST_RATE],
                DEFAULT_RATE[INDEX_PARTNER_TREATMENT_RATE],
                DEFAULT_RATE[INDEX_TEST_SENSITIVITY], DEFAULT_RATE[INDEX_CONT_TEST_30PLUS],
                DEFAULT_RATE[INDEX_INTRO_INFECTION],
                new Object[]{
                    new PersonClassifier() {
                        @Override
                        public int classifyPerson(AbstractIndividualInterface p) {
                            if (p.getAge() >= 16 * AbstractIndividualInterface.ONE_YEAR_INT
                                    && p.getAge() < 30 * AbstractIndividualInterface.ONE_YEAR_INT) {
                                return p.isMale() ? 0 : 1;
                            } else {
                                return -1;
                            }
                        }

                        @Override
                        public int numClass() {
                            return 2;
                        }

                    },
                    new float[]{0.69f * 0.7f, 0.85f * 0.7f},
                    new int[][]{
                        new int[]{5 * 360, massScr},
                        new int[]{5 * 360, massScr},}
                },
                DEFAULT_RATE[INDEX_STORE_PREVAL_FREQ]
            };

            tic = System.currentTimeMillis();
            System.out.println("Generating data for " + targetDir.getAbsolutePath());
            batchRun.singleRun(targetDir, inputParam);
            tic = System.currentTimeMillis() - tic;
            System.out.println("Time required = " + (((float) tic) / 1000) + " s");
        }
        datasetCount++;
        // 16: Mass screen - 2 month 
        if (((SKIP_DATA >> datasetCount) & 1) == 0) {
            targetDir = new File(BATCH_BASE_PATH, "MassScreen_2_month");
            int massScr = 2 * 30;
            inputParam = new Object[]{
                DEFAULT_RATE[INDEX_TEST_RATE_MALE], DEFAULT_RATE[INDEX_TEST_RATE_FEMALE],
                DEFAULT_RATE[INDEX_RETEST_RATE],
                DEFAULT_RATE[INDEX_PARTNER_TREATMENT_RATE],
                DEFAULT_RATE[INDEX_TEST_SENSITIVITY], DEFAULT_RATE[INDEX_CONT_TEST_30PLUS],
                DEFAULT_RATE[INDEX_INTRO_INFECTION],
                new Object[]{
                    new PersonClassifier() {
                        @Override
                        public int classifyPerson(AbstractIndividualInterface p) {
                            if (p.getAge() >= 16 * AbstractIndividualInterface.ONE_YEAR_INT
                                    && p.getAge() < 30 * AbstractIndividualInterface.ONE_YEAR_INT) {
                                return p.isMale() ? 0 : 1;
                            } else {
                                return -1;
                            }
                        }

                        @Override
                        public int numClass() {
                            return 2;
                        }

                    },
                    new float[]{0.69f * 0.7f, 0.85f * 0.7f},
                    new int[][]{
                        new int[]{5 * 360, massScr},
                        new int[]{5 * 360, massScr},}
                },
                DEFAULT_RATE[INDEX_STORE_PREVAL_FREQ]
            };

            tic = System.currentTimeMillis();
            System.out.println("Generating data for " + targetDir.getAbsolutePath());
            batchRun.singleRun(targetDir, inputParam);
            tic = System.currentTimeMillis() - tic;
            System.out.println("Time required = " + (((float) tic) / 1000) + " s");
        }
        datasetCount++;
        // 17: Mass screen - 1 month PT 70 3 month
        if (((SKIP_DATA >> datasetCount) & 1) == 0) {
            targetDir = new File(BATCH_BASE_PATH, "MassScreen_1_month_70_PT_3_month");
            int massScr = 1 * 30;
            inputParam = new Object[]{
                DEFAULT_RATE[INDEX_TEST_RATE_MALE], DEFAULT_RATE[INDEX_TEST_RATE_FEMALE],
                DEFAULT_RATE[INDEX_RETEST_RATE],
                new float[]{(float) DEFAULT_RATE[INDEX_PARTNER_TREATMENT_RATE],
                    5 * 360f, 0.7f, 5 * 360f + massScr + 30 * 3, (float) DEFAULT_RATE[INDEX_PARTNER_TREATMENT_RATE]},
                DEFAULT_RATE[INDEX_TEST_SENSITIVITY], DEFAULT_RATE[INDEX_CONT_TEST_30PLUS],
                DEFAULT_RATE[INDEX_INTRO_INFECTION],
                new Object[]{
                    new PersonClassifier() {
                        @Override
                        public int classifyPerson(AbstractIndividualInterface p) {
                            if (p.getAge() >= 16 * AbstractIndividualInterface.ONE_YEAR_INT
                                    && p.getAge() < 30 * AbstractIndividualInterface.ONE_YEAR_INT) {
                                return p.isMale() ? 0 : 1;
                            } else {
                                return -1;
                            }
                        }

                        @Override
                        public int numClass() {
                            return 2;
                        }

                    },
                    new float[]{0.69f * 0.7f, 0.85f * 0.7f},
                    new int[][]{
                        new int[]{5 * 360, massScr},
                        new int[]{5 * 360, massScr},}
                },
                DEFAULT_RATE[INDEX_STORE_PREVAL_FREQ]
            };

            tic = System.currentTimeMillis();
            System.out.println("Generating data for " + targetDir.getAbsolutePath());
            batchRun.singleRun(targetDir, inputParam);
            tic = System.currentTimeMillis() - tic;
            System.out.println("Time required = " + (((float) tic) / 1000) + " s");
        }
        datasetCount++;
        // 18: Mass screen - 2 month PT 70 3 month
        if (((SKIP_DATA >> datasetCount) & 1) == 0) {
            targetDir = new File(BATCH_BASE_PATH, "MassScreen_2_month_70_PT_3_month");
            int massScr = 2 * 30;
            inputParam = new Object[]{
                DEFAULT_RATE[INDEX_TEST_RATE_MALE], DEFAULT_RATE[INDEX_TEST_RATE_FEMALE],
                DEFAULT_RATE[INDEX_RETEST_RATE],
                new float[]{(float) DEFAULT_RATE[INDEX_PARTNER_TREATMENT_RATE],
                    5 * 360f, 0.7f, 5 * 360f + massScr + 30 * 3, (float) DEFAULT_RATE[INDEX_PARTNER_TREATMENT_RATE]},
                DEFAULT_RATE[INDEX_TEST_SENSITIVITY], DEFAULT_RATE[INDEX_CONT_TEST_30PLUS],
                DEFAULT_RATE[INDEX_INTRO_INFECTION],
                new Object[]{
                    new PersonClassifier() {
                        @Override
                        public int classifyPerson(AbstractIndividualInterface p) {
                            if (p.getAge() >= 16 * AbstractIndividualInterface.ONE_YEAR_INT
                                    && p.getAge() < 30 * AbstractIndividualInterface.ONE_YEAR_INT) {
                                return p.isMale() ? 0 : 1;
                            } else {
                                return -1;
                            }
                        }

                        @Override
                        public int numClass() {
                            return 2;
                        }

                    },
                    new float[]{0.69f * 0.7f, 0.85f * 0.7f},
                    new int[][]{
                        new int[]{5 * 360, massScr},
                        new int[]{5 * 360, massScr},}
                },
                DEFAULT_RATE[INDEX_STORE_PREVAL_FREQ]
            };

            tic = System.currentTimeMillis();
            System.out.println("Generating data for " + targetDir.getAbsolutePath());
            batchRun.singleRun(targetDir, inputParam);
            tic = System.currentTimeMillis() - tic;
            System.out.println("Time required = " + (((float) tic) / 1000) + " s");
        }
        datasetCount++;
    }

    public void singleRun(File testDir, Object[] parameters)
            throws IOException, ClassNotFoundException, InterruptedException {

        Population_ACCEPtPlus pop;
        testDir.mkdirs(); // Create directory if not exists
        File importDir = new File(IMPORT_PATH);

        MersenneTwister rng = new MersenneTwister(BASE_SEED);

        File[] existedPop = importDir.exists() ? importDir.listFiles(new FileFilter() {
            @Override
            public boolean accept(File file) {
                return file.getName().endsWith(".zip");
            }
        }) : new File[0];

        boolean importingPopExist = existedPop.length >= NUM_SIM_TOTAL;

        boolean useParallel = NUM_SIM_TOTAL > 1;

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

                sim.getRunnableParam()[Runnable_Population_ACCEPtPlus.RUNNABLE_SIM_DURATION] = new int[]{SIM_DURATION, 30};
                sim.getRunnableParam()[Runnable_Population_ACCEPtPlus.RUNNABLE_EXPORT_AT] = new int[]{startTime + SIM_DURATION};
                sim.getRunnableParam()[Runnable_Population_ACCEPtPlus.RUNNABLE_EXPORT_PATH]
                        = new File[]{importDir};

                System.out.println("Sim #" + s + " seed = " + seed + " created");

                setPartnerAccquistionBehaviour(sim, null);
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
            System.out.println("Time required for generating all population (without infection) = " + (System.currentTimeMillis() - tic) / 1000f + "s");
        }

        System.out.println("Running simulations....");

        if (NUM_SIM_TOTAL == 0) { // For deduging
            return;
        }

        long ticInf = System.currentTimeMillis();

        ExecutorService executor = null;
        int numInExe = 0;

        for (int s = 0; s < NUM_SIM_TOTAL; s++) {
            File popFile = new File(importDir, "pop_S" + s + "_T0.zip");
            Importation_Simulation_SingleBatch_Runnable runnable
                    = new Importation_Simulation_SingleBatch_Runnable(s, popFile, testDir, parameters);

            File existPop = new File(testDir, "pop_S" + s + "_T0.zip");

            if (existPop.exists()) {
                System.out.println("Result population, " + existPop.getAbsolutePath() + " already exist. Simulation NOT Run");
            } else {

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
        }
        if (useParallel && executor != null) {
            executor.shutdown();
            if (!executor.awaitTermination(2, TimeUnit.DAYS)) {
                System.out.println("Inf Thread time-out!");
            }

        }

        System.out.println("Time required for simulation (s) = " + (System.currentTimeMillis() - ticInf) / 1000f);

    }

    public double[] getParameter() {
        return BEST_FIT_PARAMETER;
    }

    private void setPartnerAccquistionBehaviour(Runnable_Population_ACCEPtPlus sim, PrintStream textOutput) {

        Population_ACCEPtPlus pop = sim.getPopulation();

        AbstractAvailability[] avail = pop.getAvailability();

        if (avail != null) {
            Integer mixType = 0;
            for (int i = 0; i < avail.length; i++) {
                avail[i] = new Availability_ACCEPtPlus_SelectiveMixing_Rand(pop.getRNG());
                avail[i].setRelationshipMap(pop.getRelMap()[i]);
                ((Availability_ACCEPtPlus_SelectiveMixing_Rand) avail[i]).setParameter("KEY_MATCH_TYPE", mixType);
            }

            if (textOutput != null) {
                textOutput.println("Mixing type = " + mixType.toString());
            }
        }

        // 3: ACCEPT_FIELDS_PARTNER_IN_12_MONTHS_TARGET_WEIGHTING
        // Replacement instead of weighting if entry is 0 or -ive
        /*
         // Defined from ASHR2 (Rissel C 2014)
         protected final float[] PARTNER_IN_12_MONTHS_TARGET = new float[]{
         // Male 16-19, 20-24, 25-29, 30-39, 40-49, 50-59, 60-69, (not used)
         1.4f, 1.4f, 1.4f, 1.2f, 1.1f, 1.0f, 0.9f, Float.NaN,
         // Female 16-19, 20-24, 25-29, 30-39, 40-49, 50-59, 60-69, (not used)
         1.0f, 1.1f, 1.1f, 1.0f, 0.9f, 0.8f, 0.6f, Float.NaN,};
         */
        //float gender_weight_male = 0.005f; //0.125f;
        ((float[]) pop.getFields()[Population_ACCEPtPlus.ACCEPT_FIELDS_PARTNER_IN_12_MONTHS_TARGET_WEIGHTING])[0]
                = -((float) BEST_FIT_PARAMETER[BEST_FIT_PARAM_GENDER_WEIGHT_16_19_M]);
        ((float[]) pop.getFields()[Population_ACCEPtPlus.ACCEPT_FIELDS_PARTNER_IN_12_MONTHS_TARGET_WEIGHTING])[1]
                = -((float) BEST_FIT_PARAMETER[BEST_FIT_PARAM_GENDER_WEIGHT_20_24_M]);
        ((float[]) pop.getFields()[Population_ACCEPtPlus.ACCEPT_FIELDS_PARTNER_IN_12_MONTHS_TARGET_WEIGHTING])[2]
                = -((float) BEST_FIT_PARAMETER[BEST_FIT_PARAM_GENDER_WEIGHT_25_29_M]);
        ((float[]) pop.getFields()[Population_ACCEPtPlus.ACCEPT_FIELDS_PARTNER_IN_12_MONTHS_TARGET_WEIGHTING])[3]
                = -((float) BEST_FIT_PARAMETER[BEST_FIT_PARAM_GENDER_WEIGHT_30_39_M]);
        ((float[]) pop.getFields()[Population_ACCEPtPlus.ACCEPT_FIELDS_PARTNER_IN_12_MONTHS_TARGET_WEIGHTING])[4]
                = -((float) BEST_FIT_PARAMETER[BEST_FIT_PARAM_GENDER_WEIGHT_40_49_M]);
        ((float[]) pop.getFields()[Population_ACCEPtPlus.ACCEPT_FIELDS_PARTNER_IN_12_MONTHS_TARGET_WEIGHTING])[5]
                = -((float) BEST_FIT_PARAMETER[BEST_FIT_PARAM_GENDER_WEIGHT_50_59_M]);
        ((float[]) pop.getFields()[Population_ACCEPtPlus.ACCEPT_FIELDS_PARTNER_IN_12_MONTHS_TARGET_WEIGHTING])[6]
                = -((float) BEST_FIT_PARAMETER[BEST_FIT_PARAM_GENDER_WEIGHT_60_69_M]);

        ((float[]) pop.getFields()[Population_ACCEPtPlus.ACCEPT_FIELDS_PARTNER_IN_12_MONTHS_TARGET_WEIGHTING])[8]
                = -((float) BEST_FIT_PARAMETER[BEST_FIT_PARAM_GENDER_WEIGHT_16_19_F]);
        ((float[]) pop.getFields()[Population_ACCEPtPlus.ACCEPT_FIELDS_PARTNER_IN_12_MONTHS_TARGET_WEIGHTING])[9]
                = -((float) BEST_FIT_PARAMETER[BEST_FIT_PARAM_GENDER_WEIGHT_20_24_F]);
        ((float[]) pop.getFields()[Population_ACCEPtPlus.ACCEPT_FIELDS_PARTNER_IN_12_MONTHS_TARGET_WEIGHTING])[10]
                = -((float) BEST_FIT_PARAMETER[BEST_FIT_PARAM_GENDER_WEIGHT_25_29_F]);
        ((float[]) pop.getFields()[Population_ACCEPtPlus.ACCEPT_FIELDS_PARTNER_IN_12_MONTHS_TARGET_WEIGHTING])[11]
                = -((float) BEST_FIT_PARAMETER[BEST_FIT_PARAM_GENDER_WEIGHT_30_39_F]);
        ((float[]) pop.getFields()[Population_ACCEPtPlus.ACCEPT_FIELDS_PARTNER_IN_12_MONTHS_TARGET_WEIGHTING])[12]
                = -((float) BEST_FIT_PARAMETER[BEST_FIT_PARAM_GENDER_WEIGHT_40_49_F]);
        ((float[]) pop.getFields()[Population_ACCEPtPlus.ACCEPT_FIELDS_PARTNER_IN_12_MONTHS_TARGET_WEIGHTING])[13]
                = -((float) BEST_FIT_PARAMETER[BEST_FIT_PARAM_GENDER_WEIGHT_50_59_F]);
        ((float[]) pop.getFields()[Population_ACCEPtPlus.ACCEPT_FIELDS_PARTNER_IN_12_MONTHS_TARGET_WEIGHTING])[14]
                = -((float) BEST_FIT_PARAMETER[BEST_FIT_PARAM_GENDER_WEIGHT_60_69_F]);

        if (textOutput != null) {
            textOutput.println("Partner Weighting = "
                    + Arrays.toString((float[]) pop.getFields()[Population_ACCEPtPlus.ACCEPT_FIELDS_PARTNER_IN_12_MONTHS_TARGET_WEIGHTING]));
        }
    }

    private class Importation_Simulation_SingleBatch_Runnable implements Runnable {

        final File popFile;
        final int simId;
        final File OUTPUT_PATH;
        final Object[] param;

        public Importation_Simulation_SingleBatch_Runnable(
                int s, File popFile, File dirPath, Object[] param) {
            this.simId = s;
            this.popFile = popFile;
            this.OUTPUT_PATH = dirPath;
            this.param = param;
        }

        @Override
        public void run() {

            PrintStream textOutput;
            ObjectOutputStream testHist;
            File prevalStorePath;

            try {
                Population_ACCEPtPlus pop;

                textOutput = new PrintStream(new File(OUTPUT_PATH, "output_sim_" + simId + ".txt"));
                testHist = new ObjectOutputStream(new FileOutputStream(new File(OUTPUT_PATH, "testing_history_" + simId + ".obj")));
                prevalStorePath = new File(OUTPUT_PATH, "preval_store_" + simId + ".obj");

                textOutput.println("Continue using pop from " + popFile.getAbsolutePath());
                File tempFile = FileZipper.unzipFile(popFile, popFile.getParentFile());
                ObjectInputStream oIStream = new ObjectInputStream(new BufferedInputStream(new FileInputStream(tempFile)));
                if (BEST_FIT_PARAMETER.length > BEST_FIT_PARAM_PROPORTION_IN_ACCEPT_BEHAVIOR_MALE_16_19) {
                    pop = Population_ACCEPtPlus_MixedBehaviour.decodeFromStream(oIStream);
                } else {
                    pop = Population_ACCEPtPlus.decodeFromStream(oIStream);
                }
                oIStream.close();
                if (pop != null) {
                    tempFile.delete();
                }

                if (pop != null) {

                    Runnable_Population_ACCEPtPlus_Infection sim = new Runnable_Population_ACCEPtPlus_Infection(simId);

                    long seed = pop.getSeed();
                    sim.setPopulation(pop);
                    int startTime = pop.getGlobalTime();
                    int numYrToRun = 30;

                    sim.getRunnableParam()[Runnable_Population_ACCEPtPlus.RUNNABLE_SIM_DURATION]
                            = new int[]{numYrToRun, AbstractIndividualInterface.ONE_YEAR_INT};
                    sim.getRunnableParam()[Runnable_Population_ACCEPtPlus.RUNNABLE_EXPORT_AT]
                            = new int[]{startTime + (numYrToRun) * AbstractIndividualInterface.ONE_YEAR_INT};
                    sim.getRunnableParam()[Runnable_Population_ACCEPtPlus.RUNNABLE_EXPORT_PATH]
                            = new File[]{OUTPUT_PATH};

                    textOutput.println("Start time = " + startTime);
                    textOutput.println("Years to run (with infection) = " + numYrToRun);

                    setPartnerAccquistionBehaviour(sim, textOutput);

                    // Infection intro
                    if (param.length < INDEX_INTRO_INFECTION || ((Integer) param[INDEX_INTRO_INFECTION]) != 0) {

                        ((float[]) sim.getRunnableParam()[Runnable_Population_ACCEPtPlus_Infection.RUNNABLE_INFECTION_INTRO_RATE])[0]
                                = Math.max((float) BEST_FIT_PARAMETER[BEST_FIT_PARAM_INTRO_RATE_MALE], 0);
                        ((float[]) sim.getRunnableParam()[Runnable_Population_ACCEPtPlus_Infection.RUNNABLE_INFECTION_INTRO_RATE])[1]
                                = Math.max((float) BEST_FIT_PARAMETER[BEST_FIT_PARAM_INTRO_RATE_FEMALE], 0);

                        StringBuilder introOutput = new StringBuilder("Infect intro rate  = ");
                        introOutput.append(Arrays.toString((float[]) sim.getRunnableParam()[Runnable_Population_ACCEPtPlus_Infection.RUNNABLE_INFECTION_INTRO_RATE]));

                        float[] prevalByClass;
                        PersonClassifier introClassifier; // = (PersonClassifier) sim.getRunnableParam()[Runnable_Population_ACCEPtPlus_Infection.RUNNABLE_INFECTION_INTRO_CLASSIFIER];

                        if (BEST_FIT_PARAMETER[BEST_FIT_PARAM_INTRO_RATE_MALE] < 0 || BEST_FIT_PARAMETER[BEST_FIT_PARAM_INTRO_RATE_FEMALE] < 0) {
                            introClassifier = new PersonClassifier() {
                                @Override
                                public int classifyPerson(AbstractIndividualInterface p) {
                                    if (p.getAge() >= 16 * AbstractIndividualInterface.ONE_YEAR_INT
                                            && p.getAge() < 20 * AbstractIndividualInterface.ONE_YEAR_INT) {
                                        return p.isMale() ? 0 : 1;
                                    } else {
                                        return -1;
                                    }
                                }

                                @Override
                                public int numClass() {
                                    return 2;
                                }
                            };

                            prevalByClass = new float[]{
                                Math.max((float) -BEST_FIT_PARAMETER[BEST_FIT_PARAM_INTRO_RATE_MALE], 0),
                                Math.max((float) -BEST_FIT_PARAMETER[BEST_FIT_PARAM_INTRO_RATE_FEMALE], 0)
                            };
                        } else {
                            introClassifier = new Classifier_ACCEPt();

                            prevalByClass = new float[]{0.046f, 0.071f, 0.054f, 0.037f, 0.090f, 0.081f, 0.038f, 0.013f};
                            //opt.OptRun_Population_ACCEPtPlus_IntroInfection_Optimisation.TARGET_PREVAL 
                            //= new float[]{0.046f, 0.071f, 0.054f, 0.037f, 0.090f, 0.081f, 0.038f, 0.013f};
                        }

                        sim.getPopulation().setInstantInfection(0, introClassifier, prevalByClass, 296);

                        introOutput.append(" Prevalence at start = ");
                        introOutput.append(Arrays.toString(prevalByClass));

                        // Set to no intro
                        if (BEST_FIT_PARAMETER[BEST_FIT_PARAM_INTRO_RATE_MALE] <= 0 && BEST_FIT_PARAMETER[BEST_FIT_PARAM_INTRO_RATE_FEMALE] <= 0) {
                            sim.getRunnableParam()[Runnable_Population_ACCEPtPlus_Infection.RUNNABLE_INFECTION_INTRO_CLASSIFIER] = null;
                        }

                        textOutput.println(introOutput.toString());
                    }
                    if (param[INDEX_PARTNER_TREATMENT_RATE] instanceof float[]) {
                        float[][] timeVarPT = new float[][]{
                            (float[]) param[INDEX_PARTNER_TREATMENT_RATE]
                        };
                        sim.getRunnableParam()[Runnable_Population_ACCEPtPlus_Infection.RUNNABLE_INFECTION_PARTNER_TREATMENT_RATE] = timeVarPT;
                        textOutput.println("PT rate = " + Arrays.deepToString(
                                (float[][]) sim.getRunnableParam()[Runnable_Population_ACCEPtPlus_Infection.RUNNABLE_INFECTION_PARTNER_TREATMENT_RATE]));

                    } else {
                        ((float[]) sim.getRunnableParam()[Runnable_Population_ACCEPtPlus_Infection.RUNNABLE_INFECTION_PARTNER_TREATMENT_RATE])[0]
                                = (float) param[INDEX_PARTNER_TREATMENT_RATE];
                        textOutput.println("PT rate = " + Arrays.toString(
                                (float[]) sim.getRunnableParam()[Runnable_Population_ACCEPtPlus_Infection.RUNNABLE_INFECTION_PARTNER_TREATMENT_RATE]));
                    }

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
                            = new float[]{(float) param[INDEX_TEST_RATE_MALE], (float) param[INDEX_TEST_RATE_FEMALE]};

                    textOutput.println("Testing coverage = " + Arrays.toString(
                            (float[]) sim.getRunnableParam()[Runnable_Population_ACCEPtPlus_Infection.RUNNABLE_INFECTION_TESTING_COVERAGE]));

                    sim.getRunnableParam()[Runnable_Population_ACCEPtPlus_Infection.RUNNABLE_INFECTION_RETEST_RATE]
                            = param[INDEX_RETEST_RATE];

                    if (param[INDEX_RETEST_RATE] != null) {

                        textOutput.println("Retest rate  = "
                                + Arrays.deepToString(
                                        (float[][][]) sim.getRunnableParam()[Runnable_Population_ACCEPtPlus_Infection.RUNNABLE_INFECTION_RETEST_RATE]));
                    } else {
                        sim.getRunnableParam()[Runnable_Population_ACCEPtPlus_Infection.RUNNABLE_INFECTION_RETEST_CLASSIFIER] = null;
                        textOutput.println("No retesting");

                    }

                    if (param.length > INDEX_TEST_SENSITIVITY) {
                        sim.getRunnableParam()[Runnable_Population_ACCEPtPlus_Infection.RUNNABLE_INFECTION_TEST_SENSITIVITY]
                                = param[INDEX_TEST_SENSITIVITY];
                        textOutput.println("Test sensitivity = "
                                + Float.toString((float) sim.getRunnableParam()[Runnable_Population_ACCEPtPlus_Infection.RUNNABLE_INFECTION_TEST_SENSITIVITY]));
                    }

                    if (param.length > INDEX_CONT_TEST_30PLUS) {
                        sim.getRunnableParam()[Runnable_Population_ACCEPtPlus_Infection.RUNNABLE_INFECTION_CONTINUE_TEST_30_PLUS_RATE]
                                = param[INDEX_CONT_TEST_30PLUS];
                        textOutput.println("Contiunue retest rate for 30+ = "
                                + Arrays.toString((float[]) sim.getRunnableParam()[Runnable_Population_ACCEPtPlus_Infection.RUNNABLE_INFECTION_CONTINUE_TEST_30_PLUS_RATE]));
                    }

                    // Mass screening
                    if (param.length > INDEX_MASS_SCREENING_SETTING && param[INDEX_MASS_SCREENING_SETTING] != null) {
                        sim.getRunnableParam()[Runnable_Population_ACCEPtPlus_Infection.RUNNABLE_INFECTION_MASS_SCREENING_SETTING]
                                = param[INDEX_MASS_SCREENING_SETTING];

                        PersonClassifier msc = (PersonClassifier) ((Object[]) param[INDEX_MASS_SCREENING_SETTING])[0];
                        float[] rateByClass = (float[]) ((Object[]) param[INDEX_MASS_SCREENING_SETTING])[1];
                        int[][] startAt = (int[][]) ((Object[]) param[INDEX_MASS_SCREENING_SETTING])[2];

                        StringBuilder output = new StringBuilder("Mass screening setting");
                        for (int c = 0; c < msc.numClass(); c++) {
                            output.append(System.lineSeparator());
                            output.append("Mass screening class #").append(c);
                            output.append(System.lineSeparator());
                            output.append(" Rate = ").append(rateByClass[c]);
                            output.append(System.lineSeparator());
                            output.append(" At = ").append(Arrays.toString(startAt[c]));
                        }
                        textOutput.println(output.toString());
                    }

                    ChlamydiaInfection ct_inf = (ChlamydiaInfection) pop.getInfList()[0];

                    if (!(ct_inf instanceof ChlamydiaInfectionClassSpecific)) {
                        PersonClassifier inf_classifier;
                        AbstractRealDistribution[][] dist;
                        double[][][] distVar;

                        if (BEST_FIT_PARAMETER[BEST_FIT_TRAN_PROB_TO_16_19_M] < 0) {
                            inf_classifier = new Classifier_Gender_Infection();
                            dist = new AbstractRealDistribution[inf_classifier.numClass()][ChlamydiaInfection.DIST_TOTAL];
                            distVar = new double[inf_classifier.numClass()][ChlamydiaInfection.DIST_TOTAL][];
                            // Male  
                            setupClassSpecificInfectionParam(distVar, dist, ct_inf,
                                    new double[]{BEST_FIT_PARAMETER[BEST_FIT_PARAM_AVE_INF_DUR_MALE],
                                        BEST_FIT_PARAMETER[BEST_FIT_DUR_SD_MALE]}, 0, ChlamydiaInfectionClassSpecific.DIST_INFECT_ASY_DUR_INDEX, textOutput);

                            setupClassSpecificInfectionParam(distVar, dist, ct_inf,
                                    new double[]{BEST_FIT_PARAMETER[BEST_FIT_PARAM_AVE_INF_DUR_MALE], BEST_FIT_PARAMETER[BEST_FIT_DUR_SD_MALE]}, 0, ChlamydiaInfectionClassSpecific.DIST_INFECT_SYM_DUR_INDEX, textOutput);

                            // Female
                            setupClassSpecificInfectionParam(distVar, dist, ct_inf,
                                    new double[]{BEST_FIT_PARAMETER[BEST_FIT_PARAM_AVE_INF_DUR_FEMALE], BEST_FIT_PARAMETER[BEST_FIT_DUR_SD_FEMALE]}, 1, ChlamydiaInfectionClassSpecific.DIST_INFECT_ASY_DUR_INDEX, textOutput);

                            setupClassSpecificInfectionParam(distVar, dist, ct_inf,
                                    new double[]{BEST_FIT_PARAMETER[BEST_FIT_PARAM_AVE_INF_DUR_FEMALE], BEST_FIT_PARAMETER[BEST_FIT_DUR_SD_FEMALE]}, 1, ChlamydiaInfectionClassSpecific.DIST_INFECT_SYM_DUR_INDEX, textOutput);

                        } else {
                            inf_classifier = new Classifier_Gender_Age_Specific_Infection();
                            dist = new AbstractRealDistribution[inf_classifier.numClass()][ChlamydiaInfection.DIST_TOTAL];
                            distVar = new double[inf_classifier.numClass()][ChlamydiaInfection.DIST_TOTAL][];

                            // Transmission
                            setupClassSpecificInfectionParam(distVar, dist, ct_inf,
                                    new double[]{BEST_FIT_PARAMETER[BEST_FIT_TRAN_PROB_TO_16_19_M], 0}, 0, ChlamydiaInfection.DIST_TRANS_FM_INDEX, textOutput);
                            setupClassSpecificInfectionParam(distVar, dist, ct_inf,
                                    new double[]{BEST_FIT_PARAMETER[BEST_FIT_TRAN_PROB_TO_20_24_M], 0}, 1, ChlamydiaInfection.DIST_TRANS_FM_INDEX, textOutput);
                            setupClassSpecificInfectionParam(distVar, dist, ct_inf,
                                    new double[]{BEST_FIT_PARAMETER[BEST_FIT_TRAN_PROB_TO_25_29_M], 0}, 2, ChlamydiaInfection.DIST_TRANS_FM_INDEX, textOutput);

                            setupClassSpecificInfectionParam(distVar, dist, ct_inf,
                                    new double[]{BEST_FIT_PARAMETER[BEST_FIT_TRAN_PROB_TO_16_19_M]
                                        + BEST_FIT_PARAMETER[BEST_FIT_TRAN_PROB_TO_16_19_F_EXTRA], 0}, 3, ChlamydiaInfection.DIST_TRANS_MF_INDEX, textOutput);
                            setupClassSpecificInfectionParam(distVar, dist, ct_inf,
                                    new double[]{BEST_FIT_PARAMETER[BEST_FIT_TRAN_PROB_TO_20_24_M]
                                        + BEST_FIT_PARAMETER[BEST_FIT_TRAN_PROB_TO_20_24_F_EXTRA], 0}, 4, ChlamydiaInfection.DIST_TRANS_MF_INDEX, textOutput);
                            setupClassSpecificInfectionParam(distVar, dist, ct_inf,
                                    new double[]{BEST_FIT_PARAMETER[BEST_FIT_TRAN_PROB_TO_25_29_M]
                                        + BEST_FIT_PARAMETER[BEST_FIT_TRAN_PROB_TO_25_29_F_EXTRA], 0}, 5, ChlamydiaInfection.DIST_TRANS_MF_INDEX, textOutput);

                        }

                        ct_inf = new ChlamydiaInfectionClassSpecific(ct_inf.getRNG(), inf_classifier,
                                dist,
                                distVar);

                        pop.getInfList()[0] = ct_inf;
                    }

                    String key;

                    double[] trans = new double[]{
                        BEST_FIT_PARAMETER[BEST_FIT_PARAM_TRAN_FEMALE_TO_MALE] + BEST_FIT_PARAMETER[BEST_FIT_PARAM_TRAN_MALE_TO_FEMALE_EXTRA],
                        BEST_FIT_PARAMETER[BEST_FIT_PARAM_TRAN_FEMALE_TO_MALE],}; // M->F, F->M

                    if (BEST_FIT_PARAMETER[BEST_FIT_TRANS_SD_MF] > 0 || BEST_FIT_PARAMETER[BEST_FIT_TRANS_SD_FM] > 0) {
                        RandomGenerator popRNG = pop.getInfList()[0].getRNG();
                        double[] betaParam;
                        BetaDistribution beta;

                        if (BEST_FIT_PARAMETER[BEST_FIT_TRANS_SD_MF] > 0) {
                            betaParam = generatedBetaParam(new double[]{trans[0], BEST_FIT_PARAMETER[BEST_FIT_TRANS_SD_MF]});
                            textOutput.println("Tranmission MF sample from Beta " + Arrays.toString(betaParam)
                                    + " Mean = " + (betaParam[0] / (betaParam[0] + betaParam[1]))
                                    + " sd = "
                                    + Math.sqrt((betaParam[0] * betaParam[1])
                                            / (Math.pow(betaParam[0] + betaParam[1], 2) * (betaParam[0] + betaParam[1] + 1))));
                            beta = new BetaDistribution(popRNG, betaParam[0], betaParam[1]);
                            trans[0] = beta.sample();
                        }

                        if (BEST_FIT_PARAMETER[BEST_FIT_TRANS_SD_FM] > 0) {
                            betaParam = generatedBetaParam(new double[]{trans[1], BEST_FIT_PARAMETER[BEST_FIT_TRANS_SD_FM]});
                            textOutput.println("Tranmission FM sample from Beta " + Arrays.toString(betaParam)
                                    + " Mean = " + (betaParam[0] / (betaParam[0] + betaParam[1]))
                                    + " sd = "
                                    + Math.sqrt((betaParam[0] * betaParam[1])
                                            / (Math.pow(betaParam[0] + betaParam[1], 2) * (betaParam[0] + betaParam[1] + 1))));
                            beta = new BetaDistribution(popRNG, betaParam[0], betaParam[1]);
                            trans[1] = beta.sample();
                        }
                    }

                    key = ChlamydiaInfection.PARAM_DIST_PARAM_INDEX_REGEX.replaceAll("999", "" + ChlamydiaInfection.DIST_TRANS_MF_INDEX);
                    ct_inf.setParameter(key, new double[]{trans[0], 0});

                    textOutput.println("Trans MF = " + Arrays.toString((double[]) ct_inf.getParameter(key)));

                    key = ChlamydiaInfection.PARAM_DIST_PARAM_INDEX_REGEX.replaceAll("999", "" + ChlamydiaInfection.DIST_TRANS_FM_INDEX);
                    ct_inf.setParameter(key, new double[]{trans[1], 0});

                    textOutput.println("Trans FM = " + Arrays.toString((double[]) ct_inf.getParameter(key)));

                    textOutput.println("Inf Sim #" + simId + " seed = " + seed + " created");

                    if (ct_inf instanceof ChlamydiaInfectionClassSpecific) {
                        textOutput.println("ClassSpecific parameter set:");

                        double[][][] distVar = ((ChlamydiaInfectionClassSpecific) ct_inf).getClassSpecificDistVar();
                        AbstractRealDistribution[][] dist = ((ChlamydiaInfectionClassSpecific) ct_inf).getClassSpecificDist();

                        for (int cI = 0; cI < dist.length; cI++) {

                            StringBuilder output = new StringBuilder();

                            output.append("Class #").append(cI);

                            for (int distId = 0; distId < distVar[cI].length; distId++) {
                                if (dist[cI][distId] != null) {
                                    output.append(System.lineSeparator()).append("distId #").append(distId).append(": ")
                                            .append(dist[cI][distId].getClass().getName()).append(" [m,sd] = ").append(Arrays.toString(distVar[cI][distId]));
                                } else if (distVar[cI][distId] != null) {
                                    output.append(System.lineSeparator()).append("distId #").append(distId).append(": ")
                                            .append("Fixed Value = ").append(Double.toString(distVar[cI][distId][0]));

                                }
                            }

                            textOutput.println(output.toString());

                        }

                    }

                    if (pop instanceof Population_ACCEPtPlus_MixedBehaviour) {
                        float[] mixBehavProp
                                = (float[]) pop.getFields()[Population_ACCEPtPlus_MixedBehaviour.ACCEPT_MIXED_BEHAVIOUR_FIELDS_PROPORTION_IN_ACCEPT_BEHAVIOUR];

                        mixBehavProp = new float[6];
                        mixBehavProp[0] = (float) BEST_FIT_PARAMETER[BEST_FIT_PARAM_PROPORTION_IN_ACCEPT_BEHAVIOR_MALE_16_19];
                        mixBehavProp[1] = (float) BEST_FIT_PARAMETER[BEST_FIT_PARAM_PROPORTION_IN_ACCEPT_BEHAVIOR_MALE_20_24];
                        mixBehavProp[2] = (float) BEST_FIT_PARAMETER[BEST_FIT_PARAM_PROPORTION_IN_ACCEPT_BEHAVIOR_MALE_25_29];
                        mixBehavProp[3] = (float) BEST_FIT_PARAMETER[BEST_FIT_PARAM_PROPORTION_IN_ACCEPT_BEHAVIOR_FEMALE_16_19];
                        mixBehavProp[4] = (float) BEST_FIT_PARAMETER[BEST_FIT_PARAM_PROPORTION_IN_ACCEPT_BEHAVIOR_FEMALE_20_24];
                        mixBehavProp[5] = (float) BEST_FIT_PARAMETER[BEST_FIT_PARAM_PROPORTION_IN_ACCEPT_BEHAVIOR_FEMALE_25_29];

                        pop.getFields()[Population_ACCEPtPlus_MixedBehaviour.ACCEPT_MIXED_BEHAVIOUR_FIELDS_PROPORTION_IN_ACCEPT_BEHAVIOUR]
                                = mixBehavProp;

                        textOutput.println("% in ACCEPt behaviour = " + Arrays.toString(
                                (float[]) pop.getFields()[Population_ACCEPtPlus_MixedBehaviour.ACCEPT_MIXED_BEHAVIOUR_FIELDS_PROPORTION_IN_ACCEPT_BEHAVIOUR]));
                    }

                    Availability_ACCEPtPlus_SelectiveMixing_Rand avail = (Availability_ACCEPtPlus_SelectiveMixing_Rand) pop.getAvailability()[0];
                    String availK = Availability_ACCEPtPlus_SelectiveMixing_Rand.KEY_STRING[Availability_ACCEPtPlus_SelectiveMixing_Rand.KEY_MATCH_TYPE_ID];
                    Integer KEY_MATCH_TYPE = 0;
                    avail.setParameter(availK, KEY_MATCH_TYPE);
                    textOutput.println(avail.getClass().getName() + "." + availK + " = " + KEY_MATCH_TYPE.toString());
                    availK = Availability_ACCEPtPlus_SelectiveMixing_Rand.KEY_STRING[Availability_ACCEPtPlus_SelectiveMixing_Rand.KEY_MATCH_RANDOM_PROB];
                    Float randoProb = (float) BEST_FIT_PARAMETER[BEST_FIT_PARAM_MIXING_RAND];
                    avail.setParameter(availK, randoProb);
                    textOutput.println(avail.getClass().getName() + "." + availK + " = " + randoProb.toString());
                    sim.setOutputPrintStream(textOutput);

                    sim.setTestingHistory(new ConcurrentHashMap<Integer, int[]>());

                    if (param.length > INDEX_STORE_PREVAL_FREQ && param[INDEX_STORE_PREVAL_FREQ] != null) {
                        int prevalenceStoreFreq = ((Integer) param[INDEX_STORE_PREVAL_FREQ]);
                        sim.setPrevalStoreFreq(prevalenceStoreFreq, prevalStorePath);
                        textOutput.println("Prevalence stored at " + prevalStorePath.getAbsolutePath() + " with frequency of " + prevalenceStoreFreq);
                    }

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

        private double[] generatedGammaParam(double[] input) {
            // For Gamma distribution
            // shape = alpha = mean*mean / variance
            // scale = 1/rate = lambda/beta = (variance / mean);
            double[] res = new double[2];
            double var = input[1] * input[1];
            // beta/lambda
            res[1] = var / input[0];
            //alpha
            res[0] = input[0] * res[1];
            return res;
        }

        private double[] generatedBetaParam(double[] input) {
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

        private void setupClassSpecificInfectionParam(
                double[][][] distVar,
                AbstractRealDistribution[][] dist,
                ChlamydiaInfection ct_inf,
                double[] paramEnt, int classIndex, int distIndex,
                PrintStream textOutput) throws NotStrictlyPositiveException {

            double[] distParam;
            distVar[classIndex][distIndex] = paramEnt;
            switch (distIndex) {
                case ChlamydiaInfection.DIST_INFECT_ASY_DUR_INDEX:
                case ChlamydiaInfection.DIST_INFECT_SYM_DUR_INDEX:
                case ChlamydiaInfection.DIST_EXPOSED_DUR_INDEX:
                case ChlamydiaInfection.DIST_IMMUNE_DUR_INDEX:
                    distParam = generatedGammaParam(paramEnt);

                    if (paramEnt[1] != 0) {
                        dist[classIndex][distIndex]
                                = new GammaDistribution(ct_inf.getRNG(), distParam[0], 1 / distParam[1]);
                    }
                    break;

                case ChlamydiaInfection.DIST_TRANS_MF_INDEX:
                case ChlamydiaInfection.DIST_TRANS_FM_INDEX:
                case ChlamydiaInfection.DIST_SYM_MALE_INDEX:
                case ChlamydiaInfection.DIST_SYM_FEMALE_INDEX:
                    distParam = generatedBetaParam(paramEnt);
                    if (paramEnt[1] != 0) {
                        dist[classIndex][distIndex]
                                = new BetaDistribution(ct_inf.getRNG(), distParam[0], distParam[1]);
                    }

                    double[] trans = new double[]{
                        BEST_FIT_PARAMETER[BEST_FIT_PARAM_TRAN_FEMALE_TO_MALE] + BEST_FIT_PARAMETER[BEST_FIT_PARAM_TRAN_MALE_TO_FEMALE_EXTRA],
                        BEST_FIT_PARAMETER[BEST_FIT_PARAM_TRAN_FEMALE_TO_MALE],}; // M->F, F->M

                    if (BEST_FIT_PARAMETER[BEST_FIT_TRANS_SD_MF] > 0 || BEST_FIT_PARAMETER[BEST_FIT_TRANS_SD_FM] > 0) {
                        RandomGenerator popRNG = ct_inf.getRNG();
                        double[] betaParam;
                        BetaDistribution beta;
                        double sd;

                        if (distIndex == ChlamydiaInfection.DIST_TRANS_MF_INDEX && BEST_FIT_PARAMETER[BEST_FIT_TRANS_SD_MF] > 0) {
                            sd = BEST_FIT_PARAMETER[BEST_FIT_TRANS_SD_MF];
                            betaParam = generatedBetaParam(new double[]{paramEnt[0], sd});
                            textOutput.println("Class #" + classIndex + " distId #" + distIndex
                                    + ": Tranmission MF sample from Beta " + Arrays.toString(betaParam)
                                    + " Mean = " + (betaParam[0] / (betaParam[0] + betaParam[1]))
                                    + " sd = "
                                    + Math.sqrt((betaParam[0] * betaParam[1])
                                            / (Math.pow(betaParam[0] + betaParam[1], 2) * (betaParam[0] + betaParam[1] + 1))));

                            beta = new BetaDistribution(popRNG, betaParam[0], betaParam[1]);
                            paramEnt[0] = beta.sample();

                        }
                        if (distIndex == ChlamydiaInfection.DIST_TRANS_FM_INDEX && BEST_FIT_PARAMETER[BEST_FIT_TRANS_SD_FM] > 0) {
                            sd = BEST_FIT_PARAMETER[BEST_FIT_TRANS_SD_FM];
                            betaParam = generatedBetaParam(new double[]{paramEnt[0], sd});
                            textOutput.println("Class #" + classIndex + " distId #" + distIndex
                                    + ": Tranmission FM sample from Beta " + Arrays.toString(betaParam)
                                    + " Mean = " + (betaParam[0] / (betaParam[0] + betaParam[1]))
                                    + " sd = "
                                    + Math.sqrt((betaParam[0] * betaParam[1])
                                            / (Math.pow(betaParam[0] + betaParam[1], 2) * (betaParam[0] + betaParam[1] + 1))));
                            beta = new BetaDistribution(popRNG, betaParam[0], betaParam[1]);
                            paramEnt[0] = beta.sample();

                        }
                    }

                    break;

                default:
                    System.err.println(getClass().getName() + ".setupClassSpecificInfectionParam: distIndex " + distIndex + " not defined!");
                    System.exit(-1);

            }

        }

    }

}
