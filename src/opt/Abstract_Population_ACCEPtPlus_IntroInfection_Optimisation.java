package opt;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileFilter;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.Arrays;
import random.MersenneTwisterRandomGenerator;
import random.RandomGenerator;
import transform.ParameterConstraintTransformSineCurve;

/**
 * Abstract class defining common code for parameter optimisation.
 *
 * @author Ben Hui
 * @version 20181024
 *
 */
public abstract class Abstract_Population_ACCEPtPlus_IntroInfection_Optimisation {

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
    public static final int OPT_PARAM_TRAN_FEMALE_TO_MALE_SD = OPT_PARAM_INF_DUR_25_29_F + 1;
    public static final int OPT_PARAM_TRAN_MALE_TO_FEMALE_SD = OPT_PARAM_TRAN_FEMALE_TO_MALE_SD + 1;
    public static final int OPT_PARAM_DUR_SD_MALE = OPT_PARAM_TRAN_MALE_TO_FEMALE_SD + 1;
    public static final int OPT_PARAM_DUR_SD_FEMALE = OPT_PARAM_TRAN_MALE_TO_FEMALE_SD + 1;

    
    public static final String FILENAME_POP_SELECT = "pop_select.txt";
    public static final String FILENAME_PARAM_CONSTRIANTS = "ParamConstriants.csv";
    public static final String FILENAME_OPT_RESULTS_CSV = "ParamOpt.csv";
    public static final String FILENAME_OPT_RESULTS_OBJ = "ParamOpt.obj";
    public static final String FILENAME_OPT_SIMPLEX = "ParamSimplex.obj";
    public static final String FILENAME_PRE_SIMPLEX_CSV = "Pre_Simplex.csv";
    public static final String FILENAME_PRE_RESIDUE = "Pre_Residue.csv";
    public static final String FILENAME_P0 = "Pre_P0.csv";
    // Target Prevalence, from ACCEPt - Survey 1
    public static final float[] TARGET_PREVAL = new float[]{0.050f,0.074f, 0.037f, 0.083f, 0.053f, 0.012f};
    public boolean[] targetPrevalSel = new boolean[]{true,true,true,true,true,true};

    
    String optBaseDir = "Z:\\ACCEPT\\OptRes";
    String importPath = "Z:\\ACCEPT\\ImportDir";
    int numThreads = Runtime.getRuntime().availableProcessors();
    int[] popSelectIndex = new int[64];
    String simSelCSVPath = "";
    int NUM_OPT_TO_KEEP = 10;
    protected File[] OPT_RES_DIR_COLLECTION;
    protected double[] OPT_RES_SUM_SQS;
    
    
    protected double[] preOptParameter = null;

    public double[] getPreOptParameter() {
        return preOptParameter;
    }

    public void setPreOptParameter(double[] preOptParameter) {
        this.preOptParameter = preOptParameter;
    }                

    public boolean[] getTargetPrevalSel() {
        return targetPrevalSel;
    }

    public void setTargetPrevalSel(boolean[] targetPrevalSel) {
        this.targetPrevalSel = targetPrevalSel;
    }            
    
    public abstract void runOptimisation() throws IOException, ClassNotFoundException;

    protected ParameterConstraintTransformSineCurve[] initContraints(File parentDir) throws NumberFormatException, IOException {
        ParameterConstraintTransformSineCurve[] constraints;
        //<editor-fold defaultstate="collapsed" desc="Intialise constraints">
        File costrainFile = new File(parentDir, FILENAME_PARAM_CONSTRIANTS);
        try (final BufferedReader constraintReader = new BufferedReader(new FileReader(costrainFile))) {
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
                constraints[lnNum] = new ParameterConstraintTransformSineCurve(new double[]{Double.parseDouble(ent[0]), Double.parseDouble(ent[1])});
                lnNum++;
            }
        }
        //< /editor-fold>
        //< /editor-fold>
        return constraints;
    }

    protected void setPopSelIndex() throws IOException, NumberFormatException, FileNotFoundException {
        try (final PrintWriter wri = new PrintWriter(new File(optBaseDir, FILENAME_POP_SELECT))) {
            File[] importDir = (new File(importPath)).listFiles(new FileFilter() {
                @Override
                public boolean accept(File file) {
                    return file.getName().endsWith(".zip");
                }
            });
            if (simSelCSVPath.length() == 0) {
                RandomGenerator rng = new MersenneTwisterRandomGenerator(2251912456367477945L);
                int pt = 0;
                int num = 0;
                while (num < importDir.length && pt < popSelectIndex.length) {
                    if (rng.nextInt(importDir.length - num) < (popSelectIndex.length - pt)) {
                        popSelectIndex[pt] = num;
                        wri.println(num);
                        pt++;
                    }
                    num++;
                }
            } else {
                File popSelCSV = new File(simSelCSVPath);
                int simSelectMaxLength;
                try (final BufferedReader reader = new BufferedReader(new FileReader(popSelCSV))) {
                    simSelectMaxLength = 0;
                    for (int i = 0; i < popSelectIndex.length; i++) {
                        String line = reader.readLine();
                        if (line != null) {
                            int num = Integer.parseInt(line);
                            popSelectIndex[i] = num;
                            wri.println(num);
                            simSelectMaxLength++;
                        } else {
                            System.err.println("Not enough in pop in " + popSelCSV + ". Use the first " + simSelectMaxLength + " sim for optimisation.");
                        }
                    }
                }
                popSelectIndex = Arrays.copyOf(popSelectIndex, simSelectMaxLength);
            }
        }
    }

}
