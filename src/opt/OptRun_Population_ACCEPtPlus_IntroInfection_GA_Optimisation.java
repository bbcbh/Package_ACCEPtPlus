package opt;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import optimisation.AbstractParameterOptimiser;
import optimisation.AbstractResidualFunc;
import optimisation.GeneticAlgorithmOptimiser;

import transform.ParameterConstraintTransformSineCurve;

/**
 * Perform parameter optimisation using GA Optimisiser
 * 
 * @author Ben Hui
 * @version 20181024
 * 
 */
public class OptRun_Population_ACCEPtPlus_IntroInfection_GA_Optimisation extends Abstract_Population_ACCEPtPlus_IntroInfection_Optimisation{

    public static final String FILENAME_OPT_GA_STORE = "GA_POP.obj";
    int GA_POP_SIZE = 1000;
    private static final boolean DEBUG = !true;

    public static void main(String[] arg) throws IOException, ClassNotFoundException {
        Abstract_Population_ACCEPtPlus_IntroInfection_Optimisation run
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
                popSelectIndex = new int[Integer.parseInt(arg[3])];
            }
        }
        if (arg.length > 4) {
            if (!arg[4].isEmpty()) {
                simSelCSVPath = arg[4];
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
        System.out.println("numSimPerStep = " + popSelectIndex.length + (simSelCSVPath.length() == 0 ? " (Rand)" : " (From " +  simSelCSVPath + ")"));
        System.out.println("numResultToKeep = " + NUM_OPT_TO_KEEP);

    }

    public void runOptimisation() throws IOException, ClassNotFoundException {

        File parentDir = new File(optBaseDir);
        parentDir.mkdirs();

        ParameterConstraintTransformSineCurve[] constraints;

        constraints = initContraints(parentDir);
        setPopSelIndex();
        
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
            popOptFunc = new Opt_ResFunc_IntroInfection(numThreads, popSelectIndex,
                    optBaseDir, importPath, 
                    this.getTargetPrevalSel(), 
                    this.getPreOptParameter(),
                    OPT_RES_DIR_COLLECTION, OPT_RES_SUM_SQS);
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
