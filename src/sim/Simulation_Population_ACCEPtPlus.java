package sim;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Properties;
import opt.Abstract_Population_ACCEPtPlus_IntroInfection_Optimisation;
import opt.OptRun_Population_ACCEPtPlus_IntroInfection_GA_Optimisation;
import opt.OptRun_Population_ACCEPtPlus_IntroInfection_Optimisation;
import run.Run_Population_ACCEPtPlus_InfectionIntro_Batch;
import static sim.SimulationInterface.PROP_CLASS;
import static sim.SimulationInterface.PROP_NAME;
import util.PersonClassifier;
import util.PropValUtils;

/**
 *
 * @author Ben Hui
 * @version 20190107
 *
 * 20190107 : Addition a comment in code in relation to intro type.
 *
 *
 *
 */
public class Simulation_Population_ACCEPtPlus implements SimulationInterface {

    public static final String[] PROP_NAME_ACCEPT = {
        "PROP_SKIP_DATA_SET", // For simulation batch: Binary number indices to skip which scenario, or -1 if not used, For optimistion - Binary number indices for targetPrevalSel
        "PROP_INTRO_TYPE", // Intro type:  0 = no intro, 1 = initial, 2 = periodic
        "PROP_ACCEPT_SIM_TYPE", // 0 = simulation batch (default), 1 = optimistion (NM) 2 = optimisation (GA) 
        "PROP_MASS_SRN_SETTING" // Default mass screen setting - int[] { introAt, duration } 
    };
    public static final Class[] PROP_CLASS_ACCEPT = {
        Integer.class, Integer.class, Integer.class, float[].class};

    public static final int PROP_SKIP_DATA_SET = PROP_NAME.length;
    public static final int PROP_INTRO_TYPE = PROP_SKIP_DATA_SET + 1;
    public static final int PROP_ACCEPT_SIM_TYPE = PROP_INTRO_TYPE + 1;
    public static final int PROP_MASS_SRN_SETTING = PROP_ACCEPT_SIM_TYPE + 1;

    public static final String POP_PROP_INIT_PREFIX = "POP_PROP_INIT_PREFIX_";
    protected String[] propModelInitStr = null;

    public static final String POP_SINGLE_RUN_PARAM_PREFIX = "POP_SINGLE_RUN_PARAM_PREFIX_";
    protected String[] propSingleRunParamStr = null;

    protected Object[] propVal = new Object[PROP_NAME.length + PROP_NAME_ACCEPT.length];
    protected File baseDir = new File("");

    protected boolean stopNextTurn = false;

    @Override
    public void loadProperties(Properties prop) {
        for (int i = 0; i < PROP_NAME.length; i++) {
            String ent = prop.getProperty(PROP_NAME[i]);
            if (ent != null) {
                propVal[i] = PropValUtils.propStrToObject(ent, PROP_CLASS[i]);
            }
        }
        for (int i = PROP_NAME.length; i < propVal.length; i++) {
            String ent = prop.getProperty(PROP_NAME_ACCEPT[i - PROP_NAME.length]);
            if (ent != null) {
                propVal[i] = PropValUtils.propStrToObject(ent, PROP_CLASS_ACCEPT[i - PROP_NAME.length]);
            }
        }

        int maxFieldPropInitNum = 0;
        for (Iterator<Object> it = prop.keySet().iterator(); it.hasNext();) {
            String k = (String) it.next();
            if (k.startsWith(POP_PROP_INIT_PREFIX)) {
                if (prop.getProperty(k) != null) {
                    maxFieldPropInitNum = Math.max(maxFieldPropInitNum,
                            Integer.parseInt(k.substring(POP_PROP_INIT_PREFIX.length())));
                }
            }
        }

        if (maxFieldPropInitNum >= 0) {
            propModelInitStr = new String[maxFieldPropInitNum + 1];
            for (int i = 0; i < propModelInitStr.length; i++) {
                String res = prop.getProperty(POP_PROP_INIT_PREFIX + i);
                if (res != null) {
                    propModelInitStr[i] = res;
                }
            }
        }

        // Single run parameter, if set 
        for (int i = 0; i < Run_Population_ACCEPtPlus_InfectionIntro_Batch.LENGTH_SINGLE_RUN_PARAM; i++) {
            String res = prop.getProperty(POP_SINGLE_RUN_PARAM_PREFIX + i);
            if (res != null) {
                if (propSingleRunParamStr == null) {
                    propSingleRunParamStr = new String[Run_Population_ACCEPtPlus_InfectionIntro_Batch.LENGTH_SINGLE_RUN_PARAM];
                }
                propSingleRunParamStr[i] = res;
            }
        }

    }

    @Override
    public Properties generateProperties() {
        Properties prop = new Properties();
        for (int i = 0; i < PROP_NAME.length; i++) {
            prop.setProperty(PROP_NAME[i], PropValUtils.objectToPropStr(propVal[i], PROP_CLASS[i]));
        }
        for (int i = PROP_CLASS.length; i < propVal.length; i++) {
            prop.setProperty(PROP_NAME_ACCEPT[i - PROP_NAME.length],
                    PropValUtils.objectToPropStr(propVal[i], PROP_CLASS_ACCEPT[i - PROP_CLASS.length]));
        }

        return prop;
    }

    @Override
    public void setBaseDir(File baseDir) {
        this.baseDir = baseDir;
    }

    @Override
    public void setStopNextTurn(boolean stopNextTurn) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public void setSnapshotSetting(PersonClassifier[] snapshotCountClassifier, boolean[] snapshotCountAccum) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public void generateOneResultSet() throws IOException, InterruptedException {
        String[] rArg;
        int simType = propVal[PROP_ACCEPT_SIM_TYPE] == null ? 0 : ((Integer) propVal[PROP_ACCEPT_SIM_TYPE]);

        switch (simType) {

            case 1:
            case 2:
                Abstract_Population_ACCEPtPlus_IntroInfection_Optimisation runOpt;
                if (simType == 2) {
                    rArg = new String[7];
                    rArg[0] = baseDir.getAbsolutePath();
                    rArg[1] = propVal[PROP_POP_IMPORT_PATH] == null ? "" : (String) propVal[PROP_POP_IMPORT_PATH];
                    rArg[2] = Integer.toString(Runtime.getRuntime().availableProcessors());
                    rArg[3] = propVal[PROP_NUM_SIM_PER_SET] == null ? "" : ((Integer) propVal[PROP_NUM_SIM_PER_SET]).toString();
                    rArg[4] = propVal[PROP_POP_SELECT_CSV] == null ? "" : propVal[PROP_POP_SELECT_CSV].toString();
                    rArg[5] = "0";
                    rArg[6] = "500";
                    runOpt = new OptRun_Population_ACCEPtPlus_IntroInfection_GA_Optimisation(rArg);

                } else {
                    rArg = new String[6];
                    rArg[0] = baseDir.getAbsolutePath();
                    rArg[1] = propVal[PROP_POP_IMPORT_PATH] == null ? "" : (String) propVal[PROP_POP_IMPORT_PATH];
                    rArg[2] = Integer.toString(Runtime.getRuntime().availableProcessors());
                    rArg[3] = propVal[PROP_NUM_SIM_PER_SET] == null ? "" : ((Integer) propVal[PROP_NUM_SIM_PER_SET]).toString();
                    rArg[4] = propVal[PROP_POP_SELECT_CSV] == null ? "" : propVal[PROP_POP_SELECT_CSV].toString();
                    rArg[5] = "0";
                    runOpt = new OptRun_Population_ACCEPtPlus_IntroInfection_Optimisation(rArg);
                }

                double[] preOptParam = new double[propModelInitStr.length];
                for (int v = 0; v < propModelInitStr.length; v++) {
                    if (propModelInitStr[v] != null) {
                        // Best fit parameters
                        preOptParam[v] = Double.parseDouble(propModelInitStr[v]);
                    } else {
                        preOptParam[v] = Double.NaN;
                    }
                }
                runOpt.setPreOptParameter(preOptParam);

                if (propVal[PROP_SKIP_DATA_SET] != null) {
                    int indices = (Integer) propVal[PROP_SKIP_DATA_SET];
                    boolean[] tarPrevalSel = runOpt.getTargetPrevalSel();
                    for (int i = 0; i < tarPrevalSel.length; i++) {
                        tarPrevalSel[i] = (indices & 1 << ((tarPrevalSel.length - 1) - i)) != 0;
                    }
                    runOpt.setTargetPrevalSel(tarPrevalSel);

                }

                 {
                    try {
                        runOpt.runOptimisation();
                    } catch (ClassNotFoundException ex) {
                        ex.printStackTrace(System.err);
                    }
                }

                break;

            default:

                rArg = new String[8];
                rArg[0] = baseDir.getAbsolutePath();
                rArg[1] = propVal[PROP_POP_IMPORT_PATH] == null ? "" : (String) propVal[PROP_POP_IMPORT_PATH];
                rArg[2] = propVal[PROP_NUM_SIM_PER_SET] == null ? "" : ((Integer) propVal[PROP_NUM_SIM_PER_SET]).toString();
                rArg[3] = propVal[PROP_SKIP_DATA_SET] == null ? "" : ((Integer) propVal[PROP_SKIP_DATA_SET]).toString();
                rArg[4] = propVal[PROP_INTRO_TYPE] == null ? "" : ((Integer) propVal[PROP_INTRO_TYPE]).toString();
                rArg[5] = propVal[PROP_SNAP_FREQ] == null ? "" : ((Integer) propVal[PROP_SNAP_FREQ]).toString();
                rArg[6] = propVal[PROP_NUM_SNAP] == null ? "" : ((Integer) propVal[PROP_NUM_SNAP]).toString();
                rArg[7] = propVal[PROP_MASS_SRN_SETTING] == null ? "" : Arrays.toString((float[]) propVal[PROP_MASS_SRN_SETTING]);

                try {
                    Run_Population_ACCEPtPlus_InfectionIntro_Batch run = new Run_Population_ACCEPtPlus_InfectionIntro_Batch(rArg);

                    run.set_NUM_THREADS(propVal[PROP_USE_PARALLEL] == null ? Runtime.getRuntime().availableProcessors() : ((Integer) propVal[PROP_USE_PARALLEL]));
                    for (int v = 0; v < propModelInitStr.length; v++) {
                        if (propModelInitStr[v] != null) {
                            // Best fit parameters
                            run.getParameter()[v] = Double.parseDouble(propModelInitStr[v]);
                        }
                    }

                    if (propVal[PROP_POP_SELECT_CSV] != null) {
                        File csv = new File(propVal[PROP_POP_SELECT_CSV].toString());
                        ArrayList<Integer> arr = null;
                        HashMap<Integer, double[]> popParamMap = new HashMap();

                        try {
                            try (BufferedReader lines = new BufferedReader(new FileReader(csv))) {
                                arr = new ArrayList();
                                String line;                                
                                
                                while ((line = lines.readLine()) != null) {                                    
                                    String[] ent = line.split(",");     
                                    Integer popSelect = Integer.parseInt(ent[0]);
                                    arr.add(popSelect);
                                    
                                    if(ent.length > 0){
                                        double[] param = new double[ent.length-1];                                        
                                        for(int p = 0; p < param.length; p++){
                                            param[p] = Double.parseDouble(ent[p+1]);
                                        }                                                                              
                                        popParamMap.put(popSelect, param);                                        
                                    }                                                                       
                                }
                            }
                        } catch (IOException | NumberFormatException ex) {
                            ex.printStackTrace(System.err);
                        }
                        if (arr != null) {
                            Integer[] popSel;
                            popSel = arr.toArray(new Integer[arr.size()]);
                            Arrays.sort(popSel);
                            run.setPopSelction(popSel);
                            run.setPopParamMap(popParamMap);
                        }

                    }

                    if (propSingleRunParamStr != null) {
                        baseDir.mkdirs();
                        run.singleRun(baseDir, propSingleRunParamStr);
                    }

                    if (((Integer) propVal[PROP_SKIP_DATA_SET]) >= 0) {
                        run.batchRun();
                    }

                } catch (ClassNotFoundException ex) {
                    ex.printStackTrace(System.err);
                }
        }

    }

    public static void main(String[] arg) throws IOException, InterruptedException, ClassNotFoundException {
        String path = arg[0]; // Location of .prop file

        File dir = new File(path);

        System.out.println("Generating results set as described in " + dir.getAbsolutePath());

        Simulation_Population_ACCEPtPlus sim = new Simulation_Population_ACCEPtPlus();

        File propFile = new File(dir, SimulationInterface.FILENAME_PROP);

        if (!propFile.exists()) {

            System.err.println("Error: PROP file " + propFile.getCanonicalPath() + " not found.");

        } else {
            Path propFilePath = propFile.toPath();

            Properties prop;
            prop = new Properties();
            try (InputStream inStr = java.nio.file.Files.newInputStream(propFilePath)) {
                prop.loadFromXML(inStr);
            }

            sim.setBaseDir(dir);
            sim.loadProperties(prop);
            sim.generateOneResultSet();

        }
    }

}
