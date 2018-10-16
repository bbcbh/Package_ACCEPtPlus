package opt;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileFilter;
import java.io.FileReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Arrays;
import optimisation.AbstractParameterOptimiser;
import optimisation.AbstractResidualFunc;
import optimisation.GeneticAlgorithmOptimiser;

import transform.ParameterConstraintTransformSineCurve;
import random.MersenneTwisterRandomGenerator;
import random.RandomGenerator;

/**
 *
 * @author Ben Hui
 */
public class OptRun_Population_ACCEPtPlus_IntroInfection_GA_Optimisation {

    public static final int OPT_PARAM_INTRO_ADJ_MALE = 0;
    public static final int OPT_PARAM_INTRO_ADJ_FEMALE = OPT_PARAM_INTRO_ADJ_MALE + 1;
    public static final int OPT_PARAM_TRAN_MALE_TO_FEMALE_EXTRA = OPT_PARAM_INTRO_ADJ_FEMALE + 1;
    public static final int OPT_PARAM_TRAN_FEMALE_TO_MALE = OPT_PARAM_TRAN_MALE_TO_FEMALE_EXTRA + 1;
    public static final int OPT_PARAM_AVE_INF_DUR_FEMALE = OPT_PARAM_TRAN_FEMALE_TO_MALE + 1;
    public static final int OPT_PARAM_AVE_INF_DUR_MALE = OPT_PARAM_AVE_INF_DUR_FEMALE + 1;
    public static final int OPT_PARAM_PROPORTION_IN_ACCEPT_BEHAVIOR_MALE_16_19 = OPT_PARAM_AVE_INF_DUR_MALE + 1;
    public static final int OPT_PARAM_PROPORTION_IN_ACCEPT_BEHAVIOR_MALE_20_24 = OPT_PARAM_PROPORTION_IN_ACCEPT_BEHAVIOR_MALE_16_19 + 1;
    public static final int OPT_PARAM_PROPORTION_IN_ACCEPT_BEHAVIOR_MALE_25_29 = OPT_PARAM_PROPORTION_IN_ACCEPT_BEHAVIOR_MALE_20_24 + 1;
    public static final int OPT_PARAM_PROPORTION_IN_ACCEPT_BEHAVIOR_FEMALE_16_19 = OPT_PARAM_PROPORTION_IN_ACCEPT_BEHAVIOR_MALE_25_29 + 1;
    public static final int OPT_PARAM_PROPORTION_IN_ACCEPT_BEHAVIOR_FEMALE_20_24 = OPT_PARAM_PROPORTION_IN_ACCEPT_BEHAVIOR_FEMALE_16_19 + 1;
    public static final int OPT_PARAM_PROPORTION_IN_ACCEPT_BEHAVIOR_FEMALE_25_29 = OPT_PARAM_PROPORTION_IN_ACCEPT_BEHAVIOR_FEMALE_20_24 + 1;
    public static final int OPT_PARAM_GENDER_WEIGHT_16_19_M = OPT_PARAM_PROPORTION_IN_ACCEPT_BEHAVIOR_FEMALE_25_29 + 1;
    public static final int OPT_PARAM_GENDER_WEIGHT_20_24_M = OPT_PARAM_GENDER_WEIGHT_16_19_M + 1;
    public static final int OPT_PARAM_GENDER_WEIGHT_25_29_M = OPT_PARAM_GENDER_WEIGHT_20_24_M + 1;
    public static final int OPT_PARAM_GENDER_WEIGHT_30_39_M = OPT_PARAM_GENDER_WEIGHT_25_29_M + 1;
    public static final int OPT_PARAM_GENDER_WEIGHT_40_49_M = OPT_PARAM_GENDER_WEIGHT_30_39_M + 1;
    public static final int OPT_PARAM_GENDER_WEIGHT_50_59_M = OPT_PARAM_GENDER_WEIGHT_40_49_M + 1;
    public static final int OPT_PARAM_GENDER_WEIGHT_60_69_M = OPT_PARAM_GENDER_WEIGHT_50_59_M + 1;
    public static final int OPT_PARAM_GENDER_WEIGHT_16_19_F = OPT_PARAM_GENDER_WEIGHT_60_69_M + 1;
    public static final int OPT_PARAM_GENDER_WEIGHT_20_24_F = OPT_PARAM_GENDER_WEIGHT_16_19_F + 1;
    public static final int OPT_PARAM_GENDER_WEIGHT_25_29_F = OPT_PARAM_GENDER_WEIGHT_20_24_F + 1;
    public static final int OPT_PARAM_GENDER_WEIGHT_30_39_F = OPT_PARAM_GENDER_WEIGHT_25_29_F + 1;
    public static final int OPT_PARAM_GENDER_WEIGHT_40_49_F = OPT_PARAM_GENDER_WEIGHT_30_39_F + 1;
    public static final int OPT_PARAM_GENDER_WEIGHT_50_59_F = OPT_PARAM_GENDER_WEIGHT_40_49_F + 1;
    public static final int OPT_PARAM_GENDER_WEIGHT_60_69_F = OPT_PARAM_GENDER_WEIGHT_50_59_F + 1;
    public static final int OPT_PARAM_MIX_RANDOM = OPT_PARAM_GENDER_WEIGHT_60_69_F + 1;
    public static final int OPT_PARAM_TRAN_PROB_TO_16_19_M = OPT_PARAM_MIX_RANDOM + 1;
    public static final int OPT_PARAM_TRAN_PROB_TO_20_24_M = OPT_PARAM_TRAN_PROB_TO_16_19_M + 1;
    public static final int OPT_PARAM_TRAN_PROB_TO_25_29_M = OPT_PARAM_TRAN_PROB_TO_20_24_M + 1;
    public static final int OPT_PARAM_TRAN_PROB_TO_16_19_F_EXTRA = OPT_PARAM_TRAN_PROB_TO_25_29_M + 1;
    public static final int OPT_PARAM_TRAN_PROB_TO_20_24_F_EXTRA = OPT_PARAM_TRAN_PROB_TO_16_19_F_EXTRA + 1;
    public static final int OPT_PARAM_TRAN_PROB_TO_25_29_F_EXTRA = OPT_PARAM_TRAN_PROB_TO_20_24_F_EXTRA + 1;
    public static final int OPT_PARAM_INF_DUR_16_19_M = OPT_PARAM_TRAN_PROB_TO_25_29_F_EXTRA + 1;
    public static final int OPT_PARAM_INF_DUR_20_24_M = OPT_PARAM_INF_DUR_16_19_M + 1;
    public static final int OPT_PARAM_INF_DUR_25_29_M = OPT_PARAM_INF_DUR_20_24_M + 1;
    public static final int OPT_PARAM_INF_DUR_16_19_F = OPT_PARAM_INF_DUR_25_29_M + 1;
    public static final int OPT_PARAM_INF_DUR_20_24_F = OPT_PARAM_INF_DUR_16_19_F + 1;
    public static final int OPT_PARAM_INF_DUR_25_29_F = OPT_PARAM_INF_DUR_20_24_F + 1;

    public static final String FILENAME_POP_SELECT = "pop_select.txt";
    public static final String FILENAME_PARAM_CONSTRIANTS = "ParamConstriants.csv";
    public static final String FILENAME_OPT_RESULTS_CSV = "ParamOpt.csv";
    public static final String FILENAME_OPT_RESULTS_OBJ = "ParamOpt.obj";
    public static final String FILENAME_OPT_GA_STORE = "GA_POP.obj";
    public static final String FILENAME_P0 = "Pre_P0.csv";

    // Target Prevalence, from ACCEPt     
    public final static float[] TARGET_PREVAL = new float[]{0.046f, 0.071f, 0.054f, 0.037f,
        0.090f, 0.081f, 0.038f, 0.013f};

    String optBaseDir = "Z:\\ACCEPT\\OptRes";
    String importPath = "Z:\\ACCEPT\\ImportDir";
    int numThreads = Runtime.getRuntime().availableProcessors();
    int[] simSelect = new int[64];
    boolean randSel = true;

    int NUM_OPT_TO_KEEP = 10;
    int GA_POP_SIZE = 1000;
    private final File[] OPT_RES_DIR_COLLECTION;
    private final double[] OPT_RES_SUM_SQS;

    private static final boolean DEBUG = !true;

    public static void main(String[] arg) throws IOException, ClassNotFoundException {
        OptRun_Population_ACCEPtPlus_IntroInfection_GA_Optimisation run
                = new OptRun_Population_ACCEPtPlus_IntroInfection_GA_Optimisation(arg);
        run.runOptimisation();
    }

    public OptRun_Population_ACCEPtPlus_IntroInfection_GA_Optimisation(String[] arg) {
        if (arg.length > 0) {
            if (!arg[0].isEmpty()) {
                optBaseDir = arg[0];
            }
        }
        if (arg.length > 1) {
            if (!arg[1].isEmpty()) {
                importPath = arg[1];
            }
        }
        if (arg.length > 2) {
            if (!arg[2].isEmpty()) {
                numThreads = Integer.parseInt(arg[2]);
            }
        }
        if (arg.length > 3) {
            if (!arg[3].isEmpty()) {
                simSelect = new int[Integer.parseInt(arg[3])];
            }
        }
        if (arg.length > 4) {
            if (!arg[4].isEmpty()) {
                randSel = Boolean.parseBoolean(arg[4]);
            }
        }

        if (arg.length > 5) {
            if (!arg[5].isEmpty()) {
                NUM_OPT_TO_KEEP = Integer.parseInt(arg[5]);
            }
        }
        
        if(arg.length > 6){
            if(!arg[6].isEmpty()){
                GA_POP_SIZE = Integer.parseInt(arg[6]);
            }
            
        }

        OPT_RES_DIR_COLLECTION = new File[NUM_OPT_TO_KEEP];
        OPT_RES_SUM_SQS = new double[NUM_OPT_TO_KEEP];
        Arrays.fill(OPT_RES_SUM_SQS, Double.POSITIVE_INFINITY);

        System.out.println("optBaseDir = " + optBaseDir);
        System.out.println("importPath = " + importPath);
        System.out.println("numThreads = " + numThreads);
        System.out.println("numSimPerStep = " + simSelect.length + (randSel ? " (rand)" : ""));
        System.out.println("numResultToKeep = " + NUM_OPT_TO_KEEP);

    }

    public void runOptimisation() throws IOException, ClassNotFoundException {

        File parentDir = new File(optBaseDir);
        parentDir.mkdirs();

        ParameterConstraintTransformSineCurve[] constraints;

        //<editor-fold defaultstate="collapsed" desc="Intialise constraints">   
        File costrainFile = new File(parentDir, FILENAME_PARAM_CONSTRIANTS);
        try (BufferedReader constraintReader = new BufferedReader(new FileReader(costrainFile))) {
            int lnNum = 0;
            String line;
            while (constraintReader.readLine() != null) {
                lnNum++;
            }
            constraints = new ParameterConstraintTransformSineCurve[lnNum];
            lnNum = 0;
            BufferedReader constraintReader2 = new BufferedReader(new FileReader(costrainFile));

            while ((line = constraintReader2.readLine()) != null) {
                String[] ent = line.split(",");
                constraints[lnNum] = new ParameterConstraintTransformSineCurve(new double[]{
                    Double.parseDouble(ent[0]), Double.parseDouble(ent[1])});
                lnNum++;
            }
        }
        //</editor-fold>    
        //</editor-fold>    

        if (randSel) {

            RandomGenerator rng = new MersenneTwisterRandomGenerator(2251912456367477945l);
            PrintWriter wri = new PrintWriter(new File(optBaseDir, FILENAME_POP_SELECT));
            File[] importDir = (new File(importPath)).listFiles(new FileFilter() {
                @Override
                public boolean accept(File file) {
                    return file.getName().endsWith(".zip");
                }
            });

            int pt = 0;
            int num = 0;

            while (num < importDir.length && pt < simSelect.length) {
                if (rng.nextInt(importDir.length - num) < (simSelect.length - pt)) {
                    simSelect[pt] = num;
                    wri.println(num);
                    pt++;
                }
                num++;
            }

            wri.close();

        }

        AbstractResidualFunc popOptFunc;

        if (DEBUG) {
            System.out.println("Debugging using Rosenbrock's function");
            // Rosenbrock's function
            popOptFunc = new AbstractResidualFunc() {
                @Override
                public double[] generateResidual(double[] param) {
                    try {
                        Thread.sleep(1000);
                    } catch (InterruptedException ex) {
                        ex.printStackTrace(System.err);
                    }
                    return new double[]{
                        100 * Math.pow(param[1] - Math.pow(param[0], 2), 2) + Math.pow(1 - param[0], 2)
                    };
                }
            };

        } else {

            popOptFunc = new Opt_ResFunc_IntroInfection(numThreads, simSelect,
                    optBaseDir, importPath, OPT_RES_DIR_COLLECTION, OPT_RES_SUM_SQS);
        }

        AbstractParameterOptimiser opt = new GeneticAlgorithmOptimiser(popOptFunc);

        opt.setFilename(parentDir.getAbsolutePath() + File.separator + FILENAME_OPT_RESULTS_CSV, AbstractParameterOptimiser.FILEPATH_CSV);
        opt.setFilename(parentDir.getAbsolutePath() + File.separator + FILENAME_OPT_RESULTS_OBJ, AbstractParameterOptimiser.FILEPATH_OBJ);
        opt.setParameter(GeneticAlgorithmOptimiser.PARAM_GA_OPT_POP_FILE, new File(parentDir, FILENAME_OPT_GA_STORE));
        opt.setParameter(GeneticAlgorithmOptimiser.PARAM_GA_OPT_USE_PARALLEL, numThreads);        
        opt.setParameter(GeneticAlgorithmOptimiser.PARAM_GA_OPT_POP_SIZE, GA_POP_SIZE);
        opt.setResOptions(false, AbstractParameterOptimiser.RES_OPTIONS_PRINT);
        
       System.out.println("# Parameters = " + constraints.length);                
        

        //<editor-fold defaultstate="collapsed" desc="Optimisation process">    
        // Initial value             
        double[] p0 = null, r0 = null;

        if (DEBUG) {
            p0 = new double[]{0.01, 0.01};
            r0 = new double[1];           
            opt.setResOptions(true, AbstractParameterOptimiser.RES_OPTIONS_PRINT);

        } else {
            // Default p0, to be replace by imported if necessary
            p0 = new double[constraints.length];
            r0 = new double[TARGET_PREVAL.length];
        }

        Arrays.fill(r0, Double.NaN);

        // Default p0, to be replace by imported if necessary
        File preGAPop = new File(parentDir, FILENAME_OPT_GA_STORE);
        if (!preGAPop.exists()) {

            File preP0 = new File(optBaseDir, FILENAME_P0);

            if (preP0.exists()) {
                ArrayList<String> p0_Arr = new ArrayList<>();
                try (BufferedReader p0Reader = new BufferedReader(new FileReader(preP0))) {
                    String line;
                    while ((line = p0Reader.readLine()) != null) {
                        p0_Arr.add(line);
                    }
                }
                p0 = new double[p0_Arr.size()];
                int index = 0;
                for (String ent : p0_Arr) {
                    p0[index] = Double.parseDouble(ent);
                    index++;
                }
                
                System.out.println("P0 from " + preP0.getAbsolutePath() + " imported");
                //System.out.println("R0 to be generated within GA.");

            }

        }

        opt.setP0(p0, constraints);
        opt.setR0(r0);

        //< /editor-fold>
        opt.initialise();

        opt.optimise();

    }

}
