package opt;

import availability.AbstractAvailability;
import availability.Availability_ACCEPtPlus_SelectiveMixing_Rand;
import infection.ChlamydiaInfection;
import infection.ChlamydiaInfectionClassSpecific;
import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.OutputStream;
import java.io.PrintStream;
import java.util.Arrays;
import java.util.concurrent.Callable;
import static opt.OptRun_Population_ACCEPtPlus_IntroInfection_GA_Optimisation.OPT_PARAM_INF_DUR_16_19_F;
import static opt.OptRun_Population_ACCEPtPlus_IntroInfection_GA_Optimisation.OPT_PARAM_INF_DUR_16_19_M;
import static opt.OptRun_Population_ACCEPtPlus_IntroInfection_GA_Optimisation.OPT_PARAM_INF_DUR_20_24_F;
import static opt.OptRun_Population_ACCEPtPlus_IntroInfection_GA_Optimisation.OPT_PARAM_INF_DUR_20_24_M;
import static opt.OptRun_Population_ACCEPtPlus_IntroInfection_GA_Optimisation.OPT_PARAM_INF_DUR_25_29_F;
import static opt.OptRun_Population_ACCEPtPlus_IntroInfection_GA_Optimisation.OPT_PARAM_INF_DUR_25_29_M;
import person.AbstractIndividualInterface;
import person.Person_ACCEPtPlusSingleInflection;
import population.Population_ACCEPtPlus;
import population.Population_ACCEPtPlus_MixedBehaviour;
import random.RandomGenerator;
import sim.Runnable_Population_ACCEPtPlus;
import sim.Runnable_Population_ACCEPtPlus_Infection;
import util.Classifier_ACCEPt;
import util.FileZipper;
import util.PersonClassifier;
import static opt.OptRun_Population_ACCEPtPlus_IntroInfection_Optimisation.OPT_PARAM_INTRO_ADJ_FEMALE;
import static opt.OptRun_Population_ACCEPtPlus_IntroInfection_Optimisation.OPT_PARAM_INTRO_ADJ_MALE;
import static opt.OptRun_Population_ACCEPtPlus_IntroInfection_Optimisation.OPT_PARAM_PROPORTION_IN_ACCEPT_BEHAVIOR_MALE_16_19;
import static opt.OptRun_Population_ACCEPtPlus_IntroInfection_Optimisation.OPT_PARAM_PROPORTION_IN_ACCEPT_BEHAVIOR_MALE_20_24;
import static opt.OptRun_Population_ACCEPtPlus_IntroInfection_Optimisation.OPT_PARAM_PROPORTION_IN_ACCEPT_BEHAVIOR_MALE_25_29;
import static opt.OptRun_Population_ACCEPtPlus_IntroInfection_Optimisation.OPT_PARAM_PROPORTION_IN_ACCEPT_BEHAVIOR_FEMALE_16_19;
import static opt.OptRun_Population_ACCEPtPlus_IntroInfection_Optimisation.OPT_PARAM_PROPORTION_IN_ACCEPT_BEHAVIOR_FEMALE_20_24;
import static opt.OptRun_Population_ACCEPtPlus_IntroInfection_Optimisation.OPT_PARAM_PROPORTION_IN_ACCEPT_BEHAVIOR_FEMALE_25_29;
import static opt.OptRun_Population_ACCEPtPlus_IntroInfection_Optimisation.OPT_PARAM_TRAN_MALE_TO_FEMALE_EXTRA;
import static opt.OptRun_Population_ACCEPtPlus_IntroInfection_Optimisation.OPT_PARAM_TRAN_FEMALE_TO_MALE;
import static opt.OptRun_Population_ACCEPtPlus_IntroInfection_Optimisation.OPT_PARAM_AVE_INF_DUR_FEMALE;
import static opt.OptRun_Population_ACCEPtPlus_IntroInfection_Optimisation.OPT_PARAM_AVE_INF_DUR_MALE;
import static opt.OptRun_Population_ACCEPtPlus_IntroInfection_Optimisation.OPT_PARAM_GENDER_WEIGHT_16_19_M;
import static opt.OptRun_Population_ACCEPtPlus_IntroInfection_Optimisation.OPT_PARAM_GENDER_WEIGHT_20_24_M;
import static opt.OptRun_Population_ACCEPtPlus_IntroInfection_Optimisation.OPT_PARAM_GENDER_WEIGHT_25_29_M;
import static opt.OptRun_Population_ACCEPtPlus_IntroInfection_Optimisation.OPT_PARAM_GENDER_WEIGHT_30_39_M;
import static opt.OptRun_Population_ACCEPtPlus_IntroInfection_Optimisation.OPT_PARAM_GENDER_WEIGHT_40_49_M;
import static opt.OptRun_Population_ACCEPtPlus_IntroInfection_Optimisation.OPT_PARAM_GENDER_WEIGHT_50_59_M;
import static opt.OptRun_Population_ACCEPtPlus_IntroInfection_Optimisation.OPT_PARAM_GENDER_WEIGHT_60_69_M;
import static opt.OptRun_Population_ACCEPtPlus_IntroInfection_Optimisation.OPT_PARAM_GENDER_WEIGHT_16_19_F;
import static opt.OptRun_Population_ACCEPtPlus_IntroInfection_Optimisation.OPT_PARAM_GENDER_WEIGHT_20_24_F;
import static opt.OptRun_Population_ACCEPtPlus_IntroInfection_Optimisation.OPT_PARAM_GENDER_WEIGHT_25_29_F;
import static opt.OptRun_Population_ACCEPtPlus_IntroInfection_Optimisation.OPT_PARAM_GENDER_WEIGHT_30_39_F;
import static opt.OptRun_Population_ACCEPtPlus_IntroInfection_Optimisation.OPT_PARAM_GENDER_WEIGHT_40_49_F;
import static opt.OptRun_Population_ACCEPtPlus_IntroInfection_Optimisation.OPT_PARAM_GENDER_WEIGHT_50_59_F;
import static opt.OptRun_Population_ACCEPtPlus_IntroInfection_Optimisation.OPT_PARAM_GENDER_WEIGHT_60_69_F;
import static opt.OptRun_Population_ACCEPtPlus_IntroInfection_Optimisation.OPT_PARAM_MIX_RANDOM;
import static opt.OptRun_Population_ACCEPtPlus_IntroInfection_Optimisation.OPT_PARAM_TRAN_FEMALE_TO_MALE_SD;
import static opt.OptRun_Population_ACCEPtPlus_IntroInfection_Optimisation.OPT_PARAM_TRAN_MALE_TO_FEMALE_SD;
import static opt.OptRun_Population_ACCEPtPlus_IntroInfection_Optimisation.OPT_PARAM_TRAN_PROB_TO_16_19_M;
import static opt.OptRun_Population_ACCEPtPlus_IntroInfection_Optimisation.OPT_PARAM_TRAN_PROB_TO_20_24_M;
import static opt.OptRun_Population_ACCEPtPlus_IntroInfection_Optimisation.OPT_PARAM_TRAN_PROB_TO_25_29_M;
import static opt.OptRun_Population_ACCEPtPlus_IntroInfection_Optimisation.OPT_PARAM_TRAN_PROB_TO_16_19_F_EXTRA;
import static opt.OptRun_Population_ACCEPtPlus_IntroInfection_Optimisation.OPT_PARAM_TRAN_PROB_TO_20_24_F_EXTRA;
import static opt.OptRun_Population_ACCEPtPlus_IntroInfection_Optimisation.OPT_PARAM_TRAN_PROB_TO_25_29_F_EXTRA;
import util.Classifier_Gender_Age_Specific_Infection;
import org.apache.commons.math3.distribution.AbstractRealDistribution;
import org.apache.commons.math3.distribution.BetaDistribution;
import org.apache.commons.math3.distribution.GammaDistribution;
import org.apache.commons.math3.exception.NotStrictlyPositiveException;
import random.MersenneTwisterRandomGenerator;
import static opt.OptRun_Population_ACCEPtPlus_IntroInfection_Optimisation.TARGET_PREVAL;

/**
 *
 * @author Ben Hui
 * @version 20181025
 *
 * <pre>
 * History
 *
 * 20180619:
 *  - Remove reference to deprecated RNG
 *  - Change parameter to fit under 8 parameters setting
 *
 * 20181025:
 *  - Add OPT_TARGET_PREVAL_SEL option
 *
 *
 * </pre>
 */
public class Callable_Opt_Prevalence_IntroInfection implements Callable<float[]> {

    final int optRunId;
    final int simId;
    final File importDir;
    final File popFile;
    final File optRunDir;
    final double[] param = { // Default
        0,
        0,
        0.04,//0.08058424309571056,
        0.12,//0.18077640468159778,
        433,
        433,
        0,
        0,
        0,
        0,
        0,
        0,
        // Male 16-19, 20-24,25-29, 30-39, 40-49, 50-59, 60-69, (not used)
        // 1.4f, 1.4f, 1.4f 1.2f, 1.1f, 1.0f, 0.9f, Float.NaN,      
        // 2.5f, 2.7f, 2.8f    
        0.7, //1.1,
        1.1,
        1.3,
        1.2,
        1.1,
        1.0,
        0.9,
        // Female 16-19, 20-29, 30-39, 40-49, 50-59, 60-69, (not used)
        // 1.0f, 1.1f, 1.1f, 1.0f, 0.9f, 0.8f, 0.6f, Float.NaN,;
        // 2.0f, 1.9f, 1.8f   
        1.0,
        1.35,
        1.45,
        1.3,
        0.9,
        0.8,
        0.6,
        // Mix Random
        0.0,
        // ClassSpecific Transmission Prob 
        0.12,
        0.12,
        0.12,
        0.04,
        0.04,
        0.04,
        // ClassSpecific Infection duration
        433,
        433,
        433,
        433,
        433,
        433,
        // Transmission SD        
        0,
        0,
        // Duration SD
        7,
        7,};

    final PersonClassifier classifier = new Classifier_ACCEPt();
    private final boolean[] OPT_TARGET_PREVAL_SEL;
    float[] diffInPreval = null;

    protected int[] exportPopAt = null; //new int[]{startTime + (numYrToRun) * AbstractIndividualInterface.ONE_YEAR_INT} if null
    protected File[] exportPopPath = null; // new File[]{optRunDir} if null
    private boolean usingAgeSpec = false;

    private StringBuilder preOptStr = new StringBuilder();

    public float[] getDiffInPreval() {
        return diffInPreval;
    }

    public void setExportPopPath(int[] exportPopAt, File[] exportPopPath) {
        this.exportPopAt = exportPopAt;
        this.exportPopPath = exportPopPath;
    }

    public Callable_Opt_Prevalence_IntroInfection(int optRunId, int simId, long timestamp,
            File optBaseDir, File importDir,
            double[] PRE_OPT_PARAM,
            boolean[] OPT_TARGET_PREVAL_SEL,
            double[] param) {

        this.optRunId = optRunId;
        this.simId = simId;
        this.importDir = importDir;
        popFile = new File(importDir, "pop_S" + simId + "_T0.zip");
        optRunDir = new File(optBaseDir, Long.toString(timestamp));
        this.OPT_TARGET_PREVAL_SEL = OPT_TARGET_PREVAL_SEL;

        // Setting pre-opt parameter 
        for (int i = 0; i < PRE_OPT_PARAM.length; i++) {
            if (!Double.isNaN(PRE_OPT_PARAM[i])) {
                this.param[i] = PRE_OPT_PARAM[i];
            }
        }

        preOptStr.append(this.getClass().getName()).append(": # Parameters = ").append(param.length);
        if (param.length == this.param.length) {
            preOptStr.append(this.getClass().getName()).append(" Parameter to optimise = All");
            System.arraycopy(param, 0, this.param, 0, param.length);
        } else if (param.length == 1) {
            preOptStr.append(this.getClass().getName()).append(" Parameter to optimise = Mixing only");
            this.param[OPT_PARAM_MIX_RANDOM] = param[0];
        } else if (param.length == 5) {
            preOptStr.append(this.getClass().getName()).append(" Parameter to optimise = Mixing, tranmission and duration");
            this.param[OPT_PARAM_MIX_RANDOM] = param[0];
            this.param[OPT_PARAM_TRAN_FEMALE_TO_MALE] = param[1];
            this.param[OPT_PARAM_TRAN_MALE_TO_FEMALE_EXTRA] = param[2];
            this.param[OPT_PARAM_AVE_INF_DUR_FEMALE] = param[3];
            this.param[OPT_PARAM_AVE_INF_DUR_MALE] = param[4];
        } else if (param.length == 10) {
            preOptStr.append(this.getClass().getName()).append(" Parameter to optimise = Tranmission and duration and gender weight for <30");
            this.param[OPT_PARAM_TRAN_FEMALE_TO_MALE] = param[0];
            this.param[OPT_PARAM_TRAN_MALE_TO_FEMALE_EXTRA] = param[1];
            this.param[OPT_PARAM_AVE_INF_DUR_FEMALE] = param[2];
            this.param[OPT_PARAM_AVE_INF_DUR_MALE] = param[3];
            this.param[OPT_PARAM_GENDER_WEIGHT_16_19_F] = param[4];
            this.param[OPT_PARAM_GENDER_WEIGHT_20_24_F] = param[5];
            this.param[OPT_PARAM_GENDER_WEIGHT_25_29_F] = param[6];
            this.param[OPT_PARAM_GENDER_WEIGHT_16_19_M] = param[7];
            this.param[OPT_PARAM_GENDER_WEIGHT_20_24_M] = param[8];
            this.param[OPT_PARAM_GENDER_WEIGHT_25_29_M] = param[9];
        } else if (param.length == 8) {
            preOptStr.append(this.getClass().getName()).append(" Parameter to optimise = tranmission and  gender weight for <30");
            this.param[OPT_PARAM_TRAN_MALE_TO_FEMALE_EXTRA] = param[0];
            this.param[OPT_PARAM_TRAN_FEMALE_TO_MALE] = param[1];
            this.param[OPT_PARAM_GENDER_WEIGHT_16_19_M] = param[2];
            this.param[OPT_PARAM_GENDER_WEIGHT_20_24_M] = param[3];
            this.param[OPT_PARAM_GENDER_WEIGHT_25_29_M] = param[4];
            this.param[OPT_PARAM_GENDER_WEIGHT_16_19_F] = param[5];
            this.param[OPT_PARAM_GENDER_WEIGHT_20_24_F] = param[6];
            this.param[OPT_PARAM_GENDER_WEIGHT_25_29_F] = param[7];
        } else if (param.length == 16) {
            this.usingAgeSpec = true;
            preOptStr.append(this.getClass().getName()).append(" Parameter to optimise = Age specific tranmission and duration");
            this.param[OPT_PARAM_TRAN_FEMALE_TO_MALE] = param[0];
            this.param[OPT_PARAM_TRAN_MALE_TO_FEMALE_EXTRA] = param[1];
            this.param[OPT_PARAM_TRAN_PROB_TO_16_19_M] = param[2];
            this.param[OPT_PARAM_TRAN_PROB_TO_20_24_M] = param[3];
            this.param[OPT_PARAM_TRAN_PROB_TO_25_29_M] = param[4];
            this.param[OPT_PARAM_TRAN_PROB_TO_16_19_F_EXTRA] = param[5];
            this.param[OPT_PARAM_TRAN_PROB_TO_20_24_F_EXTRA] = param[6];
            this.param[OPT_PARAM_TRAN_PROB_TO_25_29_F_EXTRA] = param[7];
            this.param[OPT_PARAM_AVE_INF_DUR_FEMALE] = param[8];
            this.param[OPT_PARAM_AVE_INF_DUR_MALE] = param[9];
            this.param[OPT_PARAM_INF_DUR_16_19_F] = param[10];
            this.param[OPT_PARAM_INF_DUR_20_24_F] = param[11];
            this.param[OPT_PARAM_INF_DUR_25_29_F] = param[12];
            this.param[OPT_PARAM_INF_DUR_16_19_M] = param[13];
            this.param[OPT_PARAM_INF_DUR_20_24_M] = param[14];
            this.param[OPT_PARAM_INF_DUR_25_29_M] = param[15];
        } else if (param.length == 4) {
            preOptStr.append(this.getClass().getName()).append(" Parameter to optimise = tranmission and variance");
            this.param[OPT_PARAM_TRAN_FEMALE_TO_MALE] = param[0];
            this.param[OPT_PARAM_TRAN_MALE_TO_FEMALE_EXTRA] = param[1];
            this.param[OPT_PARAM_TRAN_FEMALE_TO_MALE_SD] = param[2];
            this.param[OPT_PARAM_TRAN_MALE_TO_FEMALE_SD] = param[3];
        } else if (param.length == 2) {
            preOptStr.append(this.getClass().getName()).append(" Parameter to optimise = tranmission only");
            this.param[OPT_PARAM_TRAN_FEMALE_TO_MALE] = param[0];
            this.param[OPT_PARAM_TRAN_MALE_TO_FEMALE_EXTRA] = param[1];
        } else {
            System.err.println(this.getClass().getName() + ": Optimisation of " + param.length + " parameter(s) not defined.");
            System.exit(1);
        }

    }

    @Override
    public float[] call() throws Exception {
        PrintStream textOutput = new PrintStream(new OutputStream() {
            @Override
            public void write(int i) throws IOException {
                // Do nothing
            }
        });

        try {
            Population_ACCEPtPlus pop;

            if (optRunDir != null) {
                optRunDir.mkdirs();
                textOutput = new PrintStream(new File(optRunDir, "opt_run_" + simId + ".txt"));
            }

            textOutput.println("Continue using pop from " + popFile.getAbsolutePath());

            File tempFile = FileZipper.unzipFile(popFile, popFile.getParentFile());
            ObjectInputStream oIStream = new ObjectInputStream(new BufferedInputStream(new FileInputStream(tempFile)));
            pop = Population_ACCEPtPlus_MixedBehaviour.decodeFromStream(oIStream);
            oIStream.close();
            if (pop != null) {
                tempFile.delete();
            }

            if (pop != null) {
                Runnable_Population_ACCEPtPlus_Infection sim = new Runnable_Population_ACCEPtPlus_Infection(simId);

                final long seed = pop.getSeed();
                RandomGenerator rng = new MersenneTwisterRandomGenerator(seed);

                pop.setRNG(rng);
                sim.setPopulation(pop);

                int numYrToRun = 50;
                sim.getRunnableParam()[Runnable_Population_ACCEPtPlus.RUNNABLE_SIM_DURATION] = new int[]{numYrToRun, AbstractIndividualInterface.ONE_YEAR_INT};

                // Export pop time
                int startTime = pop.getGlobalTime();
                sim.getRunnableParam()[Runnable_Population_ACCEPtPlus.RUNNABLE_EXPORT_AT]
                        = this.exportPopAt == null ? new int[]{startTime + (numYrToRun) * AbstractIndividualInterface.ONE_YEAR_INT} : this.exportPopAt;
                sim.getRunnableParam()[Runnable_Population_ACCEPtPlus.RUNNABLE_EXPORT_PATH]
                        = this.exportPopPath == null ? new File[]{optRunDir} : this.exportPopPath;

                textOutput.println("Years to run (with infection) = " + numYrToRun);

                setPartnerAccquistionBehaviour(sim, param, textOutput);

                // Seed infection, from ACCEPt     
                float introRateMale = (float) param[OPT_PARAM_INTRO_ADJ_MALE];
                float introRateFemale = (float) param[OPT_PARAM_INTRO_ADJ_FEMALE];

                StringBuilder introOutput = new StringBuilder("Infect intro rate  = ");
                ((float[]) sim.getRunnableParam()[Runnable_Population_ACCEPtPlus_Infection.RUNNABLE_INFECTION_INTRO_RATE])[0] = Math.max(introRateMale, 0); //0.02f * introAdj;
                ((float[]) sim.getRunnableParam()[Runnable_Population_ACCEPtPlus_Infection.RUNNABLE_INFECTION_INTRO_RATE])[1] = Math.max(introRateFemale, 0); //0.04f * introAdj;
                introOutput.append(Arrays.toString((float[]) sim.getRunnableParam()[Runnable_Population_ACCEPtPlus_Infection.RUNNABLE_INFECTION_INTRO_RATE]));

                PersonClassifier introClassifier; // = (PersonClassifier) sim.getRunnableParam()[Runnable_Population_ACCEPtPlus_Infection.RUNNABLE_INFECTION_INTRO_CLASSIFIER];
                float[] prevalByClass;

                if (introRateMale < 0 || introRateFemale < 0) {

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
                        Math.max((float) -introRateMale, 0),
                        Math.max((float) -introRateFemale, 0)
                    };

                } else {
                    introClassifier = new Classifier_ACCEPt();
                    prevalByClass = opt.OptRun_Population_ACCEPtPlus_IntroInfection_Optimisation.TARGET_PREVAL;

                }

                sim.getPopulation().setInstantInfection(0, introClassifier, prevalByClass, 296);
                introOutput.append(" Prevalence at start = ");
                introOutput.append(Arrays.toString(prevalByClass));

                if (introRateMale <= 0 && introRateFemale <= 0) {
                    sim.getRunnableParam()[Runnable_Population_ACCEPtPlus_Infection.RUNNABLE_INFECTION_INTRO_CLASSIFIER] = null;
                }

                textOutput.println(introOutput);

                // From Jane's email 20160826
                ((float[]) sim.getRunnableParam()[Runnable_Population_ACCEPtPlus_Infection.RUNNABLE_INFECTION_PARTNER_TREATMENT_RATE])[0]
                        //      = 0.828f * 0.035f; // * (1 + 0.035f)/2;
                        = 0.3f;

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
                        = new float[]{0.072f, 0.169f};

                textOutput.println("Testing coverage = " + Arrays.toString(
                        (float[]) sim.getRunnableParam()[Runnable_Population_ACCEPtPlus_Infection.RUNNABLE_INFECTION_TESTING_COVERAGE]));

                textOutput.println("Retest rate = " + Arrays.deepToString(
                        (float[][][]) sim.getRunnableParam()[Runnable_Population_ACCEPtPlus_Infection.RUNNABLE_INFECTION_RETEST_RATE]));

                ///*
                ChlamydiaInfection ct_inf = (ChlamydiaInfection) pop.getInfList()[0];

                if (this.usingAgeSpec) {

                    PersonClassifier inf_classifier = new Classifier_Gender_Age_Specific_Infection();
                    AbstractRealDistribution[][] dist
                            = new AbstractRealDistribution[inf_classifier.numClass()][ChlamydiaInfection.DIST_TOTAL];
                    double[][][] distVar = new double[inf_classifier.numClass()][ChlamydiaInfection.DIST_TOTAL][];

                    // Infection duration
                    setupClassSpecificInfectionParam(distVar, dist, ct_inf,
                            new double[]{param[OPT_PARAM_INF_DUR_16_19_M], 7}, 0, ChlamydiaInfectionClassSpecific.DIST_INFECT_ASY_DUR_INDEX);
                    setupClassSpecificInfectionParam(distVar, dist, ct_inf,
                            new double[]{param[OPT_PARAM_INF_DUR_16_19_M], 7}, 0, ChlamydiaInfectionClassSpecific.DIST_INFECT_SYM_DUR_INDEX);
                    setupClassSpecificInfectionParam(distVar, dist, ct_inf,
                            new double[]{param[OPT_PARAM_INF_DUR_20_24_M], 7}, 1, ChlamydiaInfectionClassSpecific.DIST_INFECT_ASY_DUR_INDEX);
                    setupClassSpecificInfectionParam(distVar, dist, ct_inf,
                            new double[]{param[OPT_PARAM_INF_DUR_20_24_M], 7}, 1, ChlamydiaInfectionClassSpecific.DIST_INFECT_SYM_DUR_INDEX);
                    setupClassSpecificInfectionParam(distVar, dist, ct_inf,
                            new double[]{param[OPT_PARAM_INF_DUR_25_29_M], 7}, 2, ChlamydiaInfectionClassSpecific.DIST_INFECT_ASY_DUR_INDEX);
                    setupClassSpecificInfectionParam(distVar, dist, ct_inf,
                            new double[]{param[OPT_PARAM_INF_DUR_25_29_M], 7}, 2, ChlamydiaInfectionClassSpecific.DIST_INFECT_SYM_DUR_INDEX);

                    setupClassSpecificInfectionParam(distVar, dist, ct_inf,
                            new double[]{param[OPT_PARAM_INF_DUR_16_19_F], 7}, 3, ChlamydiaInfectionClassSpecific.DIST_INFECT_ASY_DUR_INDEX);
                    setupClassSpecificInfectionParam(distVar, dist, ct_inf,
                            new double[]{param[OPT_PARAM_INF_DUR_16_19_F], 7}, 3, ChlamydiaInfectionClassSpecific.DIST_INFECT_SYM_DUR_INDEX);
                    setupClassSpecificInfectionParam(distVar, dist, ct_inf,
                            new double[]{param[OPT_PARAM_INF_DUR_20_24_F], 7}, 4, ChlamydiaInfectionClassSpecific.DIST_INFECT_ASY_DUR_INDEX);
                    setupClassSpecificInfectionParam(distVar, dist, ct_inf,
                            new double[]{param[OPT_PARAM_INF_DUR_20_24_F], 7}, 4, ChlamydiaInfectionClassSpecific.DIST_INFECT_SYM_DUR_INDEX);
                    setupClassSpecificInfectionParam(distVar, dist, ct_inf,
                            new double[]{param[OPT_PARAM_INF_DUR_25_29_F], 7}, 5, ChlamydiaInfectionClassSpecific.DIST_INFECT_ASY_DUR_INDEX);
                    setupClassSpecificInfectionParam(distVar, dist, ct_inf,
                            new double[]{param[OPT_PARAM_INF_DUR_25_29_F], 7}, 5, ChlamydiaInfectionClassSpecific.DIST_INFECT_SYM_DUR_INDEX);

                    // Transmission
                    setupClassSpecificInfectionParam(distVar, dist, ct_inf,
                            new double[]{param[OPT_PARAM_TRAN_PROB_TO_16_19_M], 0}, 0, ChlamydiaInfection.DIST_TRANS_FM_INDEX);
                    setupClassSpecificInfectionParam(distVar, dist, ct_inf,
                            new double[]{param[OPT_PARAM_TRAN_PROB_TO_20_24_M], 0}, 1, ChlamydiaInfection.DIST_TRANS_FM_INDEX);
                    setupClassSpecificInfectionParam(distVar, dist, ct_inf,
                            new double[]{param[OPT_PARAM_TRAN_PROB_TO_25_29_M], 0}, 2, ChlamydiaInfection.DIST_TRANS_FM_INDEX);

                    setupClassSpecificInfectionParam(distVar, dist, ct_inf,
                            new double[]{param[OPT_PARAM_TRAN_PROB_TO_16_19_M]
                                + param[OPT_PARAM_TRAN_PROB_TO_16_19_F_EXTRA], 0}, 3, ChlamydiaInfection.DIST_TRANS_MF_INDEX);
                    setupClassSpecificInfectionParam(distVar, dist, ct_inf,
                            new double[]{param[OPT_PARAM_TRAN_PROB_TO_20_24_M]
                                + param[OPT_PARAM_TRAN_PROB_TO_20_24_F_EXTRA], 0}, 4, ChlamydiaInfection.DIST_TRANS_MF_INDEX);
                    setupClassSpecificInfectionParam(distVar, dist, ct_inf,
                            new double[]{param[OPT_PARAM_TRAN_PROB_TO_25_29_M]
                                + param[OPT_PARAM_TRAN_PROB_TO_25_29_F_EXTRA], 0}, 5, ChlamydiaInfection.DIST_TRANS_MF_INDEX);

                    ct_inf = new ChlamydiaInfectionClassSpecific(ct_inf.getRNG(),
                            inf_classifier,
                            dist,
                            distVar);

                    pop.getInfList()[0] = ct_inf;

                }

                String key;

                double[] trans = new double[]{0.16, 0.12}; // M->F, F->M

                if (param.length > OPT_PARAM_TRAN_FEMALE_TO_MALE) {
                    trans[1] = param[OPT_PARAM_TRAN_FEMALE_TO_MALE];
                    trans[0] = param[OPT_PARAM_TRAN_FEMALE_TO_MALE] + param[OPT_PARAM_TRAN_MALE_TO_FEMALE_EXTRA];
                }

                if (param[OPT_PARAM_TRAN_FEMALE_TO_MALE_SD] > 0 || param[OPT_PARAM_TRAN_MALE_TO_FEMALE_SD] > 0) {
                    RandomGenerator popRNG = pop.getInfList()[0].getRNG();
                    double[] betaParam;
                    BetaDistribution beta;

                    if (param[OPT_PARAM_TRAN_FEMALE_TO_MALE_SD] > 0) {
                        betaParam = generatedBetaParam(new double[]{param[OPT_PARAM_TRAN_FEMALE_TO_MALE],
                            param[OPT_PARAM_TRAN_FEMALE_TO_MALE_SD]});
                        textOutput.println("Tranmission FM sample from Beta " + Arrays.toString(betaParam)
                                + " Mean = " + (betaParam[0] / (betaParam[0] + betaParam[1]))
                                + " sd = "
                                + Math.sqrt((betaParam[0] * betaParam[1])
                                        / (Math.pow(betaParam[0] + betaParam[1], 2) * (betaParam[0] + betaParam[1] + 1))));
                        beta = new BetaDistribution(popRNG, betaParam[0], betaParam[1]);
                        trans[1] = beta.sample();
                    }

                    if (param[OPT_PARAM_TRAN_MALE_TO_FEMALE_SD] > 0) {
                        betaParam = generatedBetaParam(new double[]{
                            param[OPT_PARAM_TRAN_FEMALE_TO_MALE] + param[OPT_PARAM_TRAN_MALE_TO_FEMALE_EXTRA],
                            param[OPT_PARAM_TRAN_MALE_TO_FEMALE_SD]});
                        textOutput.println("Tranmission MF sample from Beta " + Arrays.toString(betaParam)
                                + " Mean = " + (betaParam[0] / (betaParam[0] + betaParam[1]))
                                + " sd = "
                                + Math.sqrt((betaParam[0] * betaParam[1])
                                        / (Math.pow(betaParam[0] + betaParam[1], 2) * (betaParam[0] + betaParam[1] + 1))));
                        beta = new BetaDistribution(popRNG, betaParam[0], betaParam[1]);
                        trans[0] = beta.sample();
                    }
                }
                key = ChlamydiaInfection.PARAM_DIST_PARAM_INDEX_REGEX.replaceAll("999", "" + ChlamydiaInfection.DIST_TRANS_MF_INDEX);
                ct_inf.setParameter(key, new double[]{trans[0], 0});

                textOutput.println("Trans MF = " + Arrays.toString((double[]) ct_inf.getParameter(key)));

                key = ChlamydiaInfection.PARAM_DIST_PARAM_INDEX_REGEX.replaceAll("999", "" + ChlamydiaInfection.DIST_TRANS_FM_INDEX);
                ct_inf.setParameter(key, new double[]{trans[1], 0});

                textOutput.println("Trans FM = " + Arrays.toString((double[]) ct_inf.getParameter(key)));

                // Gender specific infection
                double[] dur;
                key = ChlamydiaInfection.PARAM_DIST_PARAM_INDEX_REGEX.replaceAll("999", "" + ChlamydiaInfectionClassSpecific.DIST_INFECT_ASY_DUR_INDEX);
                dur = (double[]) ct_inf.getParameter(key);

                dur[0] = param[OPT_PARAM_AVE_INF_DUR_FEMALE];
                dur[1] = 7;
                ct_inf.setParameter(key, dur);

                textOutput.println("Duration Asy (Female) = " + Arrays.toString((double[]) ct_inf.getParameter(key)));

                key = ChlamydiaInfection.PARAM_DIST_PARAM_INDEX_REGEX.replaceAll("999", "" + ChlamydiaInfectionClassSpecific.DIST_INFECT_ASY_DUR_INDEX);
                dur = (double[]) ct_inf.getParameter(key);

                dur[0] = param[OPT_PARAM_AVE_INF_DUR_FEMALE];
                dur[1] = 7;
                ct_inf.setParameter(key, dur);

                textOutput.println("Duration Sym (Female) = " + Arrays.toString((double[]) ct_inf.getParameter(key)));

                key = ChlamydiaInfection.PARAM_DIST_PARAM_INDEX_REGEX.replaceAll("999", "" + ChlamydiaInfectionClassSpecific.DIST_INFECT_ASY_DUR_INDEX);
                dur = (double[]) ct_inf.getParameter(key);

                dur[0] = param[OPT_PARAM_AVE_INF_DUR_MALE];
                dur[1] = 7;
                ct_inf.setParameter(key, dur);

                textOutput.println("Duration Asy (Male) = " + Arrays.toString((double[]) ct_inf.getParameter(key)));

                key = ChlamydiaInfection.PARAM_DIST_PARAM_INDEX_REGEX.replaceAll("999", "" + ChlamydiaInfectionClassSpecific.DIST_INFECT_ASY_DUR_INDEX);
                dur = (double[]) ct_inf.getParameter(key);

                dur[0] = param[OPT_PARAM_AVE_INF_DUR_MALE];
                dur[1] = 7;
                ct_inf.setParameter(key, dur);

                textOutput.println("Duration Sym (Male) = " + Arrays.toString((double[]) ct_inf.getParameter(key)));

                if (ct_inf instanceof ChlamydiaInfectionClassSpecific) {
                    textOutput.println("ClassSpecific parameter set:");

                    double[][][] distVar = ((ChlamydiaInfectionClassSpecific) ct_inf).getClassSpecificDistVar();
                    AbstractRealDistribution[][] dist = ((ChlamydiaInfectionClassSpecific) ct_inf).getClassSpecificDist();

                    for (int cI = 0; cI < dist.length; cI++) {
                        StringBuilder output = new StringBuilder();
                        output.append("Class #").append(cI);
                        for (int distId = 0; distId < distVar[cI].length; distId++) {
                            if (distVar[cI][distId] != null) {
                                String distName = dist[cI][distId] != null ? dist[cI][distId].getClass().getName() : "None";
                                output.append(System.lineSeparator()).append("distId #").append(distId).append(": ").append(distName).append(" [m,sd] = ").append(Arrays.toString(distVar[cI][distId]));
                            } else if (distVar[cI][distId] != null) {
                                output.append(System.lineSeparator()).append("distId #").append(distId).append(": ")
                                        .append("Fixed Value = ").append(Double.toString(distVar[cI][distId][0]));

                            }
                        }

                        textOutput.println(output.toString());
                    }

                }

                textOutput.println("Inf Sim #" + simId + " seed = " + seed + " created");

                if (pop instanceof Population_ACCEPtPlus_MixedBehaviour) {
                    float[] mixBehavProp
                            = (float[]) pop.getFields()[Population_ACCEPtPlus_MixedBehaviour.ACCEPT_MIXED_BEHAVIOUR_FIELDS_PROPORTION_IN_ACCEPT_BEHAVIOUR];

                    mixBehavProp = new float[6];
                    mixBehavProp[0] = (float) param[OPT_PARAM_PROPORTION_IN_ACCEPT_BEHAVIOR_MALE_16_19];
                    mixBehavProp[1] = (float) param[OPT_PARAM_PROPORTION_IN_ACCEPT_BEHAVIOR_MALE_20_24];
                    mixBehavProp[2] = (float) param[OPT_PARAM_PROPORTION_IN_ACCEPT_BEHAVIOR_MALE_25_29];
                    mixBehavProp[3] = (float) param[OPT_PARAM_PROPORTION_IN_ACCEPT_BEHAVIOR_FEMALE_16_19];
                    mixBehavProp[4] = (float) param[OPT_PARAM_PROPORTION_IN_ACCEPT_BEHAVIOR_FEMALE_20_24];
                    mixBehavProp[5] = (float) param[OPT_PARAM_PROPORTION_IN_ACCEPT_BEHAVIOR_FEMALE_25_29];

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
                Float randoProb = (float) param[OPT_PARAM_MIX_RANDOM];
                avail.setParameter(availK, randoProb);
                textOutput.println(avail.getClass().getName() + "." + availK + " = " + randoProb.toString());

                sim.setOutputPrintStream(textOutput);
                sim.run();
                textOutput.close();

                // Check prevalence against ACCEPt results
                float[] num = new float[TARGET_PREVAL.length];
                float[] inf = new float[TARGET_PREVAL.length];
                for (AbstractIndividualInterface person : pop.getPop()) {
                    int aIndex = classifier.classifyPerson(person);
                    if (aIndex >= 0) {
                        num[aIndex]++;
                        if (person.getInfectionStatus()[0] != AbstractIndividualInterface.INFECT_S) {
                            inf[aIndex]++;
                        }
                    }
                }

                diffInPreval = new float[TARGET_PREVAL.length];
                for (int i = 0; i < diffInPreval.length; i++) {
                    diffInPreval[i] = (inf[i] / num[i]) - TARGET_PREVAL[i];
                }

                if (OPT_TARGET_PREVAL_SEL != null) {
                    int selPt = 0;
                    float[] diffInPrevalSel = new float[diffInPreval.length];
                    for (int i = 0; i < diffInPrevalSel.length; i++) {
                        if (OPT_TARGET_PREVAL_SEL[i]) {
                            diffInPrevalSel[selPt] = diffInPreval[i];
                            selPt++;
                        }
                    }
                    diffInPreval = Arrays.copyOf(diffInPrevalSel, selPt);
                }

            }

        } catch (ClassNotFoundException | IOException ex) {
            ex.printStackTrace(textOutput);
        }

        return diffInPreval;

    }

    private void setupClassSpecificInfectionParam(
            double[][][] distVar,
            AbstractRealDistribution[][] dist,
            ChlamydiaInfection ct_inf,
            double[] paramEnt, int classIndex, int distIndex) throws NotStrictlyPositiveException {

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
                break;

            default:
                System.err.println(getClass().getName() + ".setupClassSpecificInfectionParam: distIndex " + distIndex + " not defined!");
                System.exit(-1);

        }

    }

    private static void setPartnerAccquistionBehaviour(Runnable_Population_ACCEPtPlus sim, double[] param, PrintStream textOutput) {

        Population_ACCEPtPlus pop = sim.getPopulation();

        AbstractAvailability[] avail = pop.getAvailability();

        if (avail != null) {
            for (int i = 0; i < avail.length; i++) {
                avail[i] = new Availability_ACCEPtPlus_SelectiveMixing_Rand(pop.getRNG());
                avail[i].setRelationshipMap(pop.getRelMap()[i]);
            }
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
        //float gender_weight_male = 0.125f;
        //Arrays.fill(gender_weight_male_by_age, gender_weight_male);
        ((float[]) pop.getFields()[Population_ACCEPtPlus.ACCEPT_FIELDS_PARTNER_IN_12_MONTHS_TARGET_WEIGHTING])[0]
                = -((float) param[OPT_PARAM_GENDER_WEIGHT_16_19_M]);
        ((float[]) pop.getFields()[Population_ACCEPtPlus.ACCEPT_FIELDS_PARTNER_IN_12_MONTHS_TARGET_WEIGHTING])[1]
                = -((float) param[OPT_PARAM_GENDER_WEIGHT_20_24_M]);
        ((float[]) pop.getFields()[Population_ACCEPtPlus.ACCEPT_FIELDS_PARTNER_IN_12_MONTHS_TARGET_WEIGHTING])[2]
                = -((float) param[OPT_PARAM_GENDER_WEIGHT_25_29_M]);
        ((float[]) pop.getFields()[Population_ACCEPtPlus.ACCEPT_FIELDS_PARTNER_IN_12_MONTHS_TARGET_WEIGHTING])[3]
                = -((float) param[OPT_PARAM_GENDER_WEIGHT_30_39_M]);
        ((float[]) pop.getFields()[Population_ACCEPtPlus.ACCEPT_FIELDS_PARTNER_IN_12_MONTHS_TARGET_WEIGHTING])[4]
                = -((float) param[OPT_PARAM_GENDER_WEIGHT_40_49_M]);
        ((float[]) pop.getFields()[Population_ACCEPtPlus.ACCEPT_FIELDS_PARTNER_IN_12_MONTHS_TARGET_WEIGHTING])[5]
                = -((float) param[OPT_PARAM_GENDER_WEIGHT_50_59_M]);
        ((float[]) pop.getFields()[Population_ACCEPtPlus.ACCEPT_FIELDS_PARTNER_IN_12_MONTHS_TARGET_WEIGHTING])[6]
                = -((float) param[OPT_PARAM_GENDER_WEIGHT_60_69_M]);

        ((float[]) pop.getFields()[Population_ACCEPtPlus.ACCEPT_FIELDS_PARTNER_IN_12_MONTHS_TARGET_WEIGHTING])[8]
                = -((float) param[OPT_PARAM_GENDER_WEIGHT_16_19_F]);
        ((float[]) pop.getFields()[Population_ACCEPtPlus.ACCEPT_FIELDS_PARTNER_IN_12_MONTHS_TARGET_WEIGHTING])[9]
                = -((float) param[OPT_PARAM_GENDER_WEIGHT_20_24_F]);
        ((float[]) pop.getFields()[Population_ACCEPtPlus.ACCEPT_FIELDS_PARTNER_IN_12_MONTHS_TARGET_WEIGHTING])[10]
                = -((float) param[OPT_PARAM_GENDER_WEIGHT_25_29_F]);
        ((float[]) pop.getFields()[Population_ACCEPtPlus.ACCEPT_FIELDS_PARTNER_IN_12_MONTHS_TARGET_WEIGHTING])[11]
                = -((float) param[OPT_PARAM_GENDER_WEIGHT_30_39_F]);
        ((float[]) pop.getFields()[Population_ACCEPtPlus.ACCEPT_FIELDS_PARTNER_IN_12_MONTHS_TARGET_WEIGHTING])[12]
                = -((float) param[OPT_PARAM_GENDER_WEIGHT_40_49_F]);
        ((float[]) pop.getFields()[Population_ACCEPtPlus.ACCEPT_FIELDS_PARTNER_IN_12_MONTHS_TARGET_WEIGHTING])[13]
                = -((float) param[OPT_PARAM_GENDER_WEIGHT_50_59_F]);
        ((float[]) pop.getFields()[Population_ACCEPtPlus.ACCEPT_FIELDS_PARTNER_IN_12_MONTHS_TARGET_WEIGHTING])[14]
                = -((float) param[OPT_PARAM_GENDER_WEIGHT_60_69_F]);

        if (textOutput != null) {
            textOutput.println("Partner Weighting = "
                    + Arrays.toString((float[]) pop.getFields()[Population_ACCEPtPlus.ACCEPT_FIELDS_PARTNER_IN_12_MONTHS_TARGET_WEIGHTING]));
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

}
