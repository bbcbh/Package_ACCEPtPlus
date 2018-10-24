package opt;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileFilter;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Arrays;
import optimisation.AbstractParameterOptimiser;
import optimisation.AbstractResidualFunc;
import optimisation.NelderMeadOptimiser;
import transform.ParameterConstraintTransformSineCurve;
import random.MersenneTwisterRandomGenerator;
import random.RandomGenerator;

/**
 * Perform parameter optimisation using Nelder-Mead Optimisiser
 *
 * @author Ben Hui
 * @version 20181024
 */
public class OptRun_Population_ACCEPtPlus_IntroInfection_Optimisation extends Abstract_Population_ACCEPtPlus_IntroInfection_Optimisation {

    public static void main(String[] arg) throws IOException, ClassNotFoundException {
        OptRun_Population_ACCEPtPlus_IntroInfection_Optimisation run = new OptRun_Population_ACCEPtPlus_IntroInfection_Optimisation(arg);
        run.runOptimisation();
    }

// Target Prevalence, from ACCEPt
    public OptRun_Population_ACCEPtPlus_IntroInfection_Optimisation(String[] arg) {
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

        OPT_RES_DIR_COLLECTION = new File[NUM_OPT_TO_KEEP];
        OPT_RES_SUM_SQS = new double[NUM_OPT_TO_KEEP];
        Arrays.fill(OPT_RES_SUM_SQS, Double.POSITIVE_INFINITY);

        System.out.println("optBaseDir = " + optBaseDir);
        System.out.println("importPath = " + importPath);
        System.out.println("numThreads = " + numThreads);
        System.out.println("numSimPerStep = " + popSelectIndex.length + (simSelCSVPath.length() == 0 ? " (rand)" : " (From " + simSelCSVPath + ")"));
        System.out.println("numResultToKeep = " + NUM_OPT_TO_KEEP);

    }

    @Override
    public void runOptimisation() throws IOException, ClassNotFoundException {

        File parentDir = new File(optBaseDir);
        parentDir.mkdirs();

        ParameterConstraintTransformSineCurve[] constraints;

        constraints = initContraints(parentDir);
        setPopSelIndex();

        AbstractResidualFunc popOptFunc;

        popOptFunc = new Opt_ResFunc_IntroInfection(numThreads, popSelectIndex,
                optBaseDir, importPath, 
                this.getTargetPrevalSel(), 
                this.getPreOptParameter(),
                OPT_RES_DIR_COLLECTION, OPT_RES_SUM_SQS);

        AbstractParameterOptimiser opt = new NelderMeadOptimiser(popOptFunc);

        //<editor-fold defaultstate="collapsed" desc="Optimisation process">    
        // Initial value              
        double[] p0 = null, r0 = null;

        // Default p0, to be replace by imported if necessary
        p0 = new double[]{0.000060147124293455, 0.000119995570730198,
            0.0398023921822977, 0.120858843913148, //(0.16 - 0.12), 0.12,
            432.996452115454, 0.000223571986788022, //0.125,  0.125,  0.125,  0.125,  0.125, 
    };

        // Default p0, to be replace by imported if necessary
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
        }

        opt.setResOptions(
                false, AbstractParameterOptimiser.RES_OPTIONS_PRINT);

        opt.setFilename(parentDir.getAbsolutePath() + File.separator + FILENAME_OPT_RESULTS_CSV, AbstractParameterOptimiser.FILEPATH_CSV);
        opt.setFilename(parentDir.getAbsolutePath() + File.separator + FILENAME_OPT_RESULTS_OBJ, AbstractParameterOptimiser.FILEPATH_OBJ);
        opt.setFilename(parentDir.getAbsolutePath() + File.separator + FILENAME_OPT_SIMPLEX, NelderMeadOptimiser.FILEPATH_SIMPLEX);

        File preSimplexFile = new File(parentDir, FILENAME_OPT_SIMPLEX);
        File preX0CSV = new File(parentDir, FILENAME_PRE_SIMPLEX_CSV);

        double[][] sX = null;
        double[][] sR = null;

        if (preSimplexFile.exists()) {
            System.out.print("Reading previous simplex....");
            try (ObjectInputStream objStr = new ObjectInputStream(new FileInputStream(preSimplexFile))) {
                sX = (double[][]) objStr.readObject();
                sR = (double[][]) objStr.readObject();
            }
            preSimplexFile.renameTo(new File(parentDir, preSimplexFile.getName() + "_" + System.currentTimeMillis()));
            System.out.println(" done");
        } else if (preX0CSV.exists()) {
            System.out.println("Reading previous simplex CSV ....");
            BufferedReader lines = new BufferedReader(new FileReader(preX0CSV));
            BufferedReader rLines = null;
            File preR0CSV = new File(parentDir, "Pre_Residue.csv");
            if (preR0CSV.exists()) {
                rLines = new BufferedReader(new FileReader(preR0CSV));
            }
            String line, rLine;
            int pt = 0;
            while ((line = lines.readLine()) != null) {
                String[] entries = line.split(",");
                if (sX == null) {
                    sX = new double[entries.length + 1][];
                    sR = new double[entries.length + 1][];
                }
                sX[pt] = new double[entries.length];
                for (int i = 0; i < entries.length; i++) {
                    sX[pt][i] = Double.parseDouble(entries[i]);
                }
                if (rLines != null) {
                    rLine = rLines.readLine();
                    if (rLine != null && !rLine.isEmpty()) {
                        entries = rLine.split(",");
                        sR[pt] = new double[entries.length];
                        for (int i = 0; i < entries.length; i++) {
                            sR[pt][i] = Double.parseDouble(entries[i]);
                        }
                    }
                }
                pt++;
            }
        }

        if (sX
                != null) {
            r0 = sR[0];
            if (p0 == null) {
                p0 = new double[sX[0].length];
            }
            for (int i = 0; i < p0.length; i++) {
                p0[i] = sX[0][i];
                if (constraints[i] != null) {
                    p0[i] = constraints[i].toContrainted(p0[i]);
                }
            }
        }

        System.out.println(
                "P0 = " + Arrays.toString(p0));
        if (r0
                != null) {
            System.out.println("R0 = " + Arrays.toString(r0));
        }

        opt.setP0(p0, constraints);

        opt.setR0(r0);

        if (sX
                != null) {
            for (int i = 0; i < sX.length; i++) {
                if (sX[i] != null) {
                    System.out.println("Loading simplex vertex #" + i);
                    System.out.println(i + ": X = " + Arrays.toString(sX[i]));
                    if (sR[i] == null) {
                        System.out.println(i + ": R to be generated");
                    }
                    sR[i] = ((NelderMeadOptimiser) opt).setPreDefineSimplex(sX[i], sR[i], i);
                    System.out.println(i + ": R = " + Arrays.toString(sR[i]));
                }
            }
        }

        //< /editor-fold>
        opt.initialise();

        opt.optimise();

    }


}
