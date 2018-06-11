package sim;

import java.io.File;
import java.io.IOException;
import java.util.Iterator;
import java.util.Properties;
import run.Run_Population_ACCEPtPlus_InfectionIntro_Batch;
import static sim.SimulationInterface.PROP_CLASS;
import static sim.SimulationInterface.PROP_NAME;
import util.PersonClassifier;
import util.PropValUtils;

/**
 *
 * @author Ben Hui
 * @version 20180601
 */
public class Simulation_Population_ACCEPtPlus implements SimulationInterface {

    public static final String[] PROP_NAME_ACCEPT = {
        "PROP_SKIP_DATA_SET", // Binary number indic to skip which scenario. eg. 524286 - Baseline only,   5116 - ACCEPt Manscript              
        "PROP_INTRO_TYPE"
    };
    public static final Class[] PROP_CLASS_ACCEPT = {
        Integer.class, Integer.class
    };

    public static final int PROP_SKIP_DATA_SET = PROP_NAME.length;
    public static final int PROP_INTRO_TYPE = PROP_SKIP_DATA_SET + 1;

    public static final String POP_PROP_INIT_PREFIX = "POP_PROP_INIT_PREFIX_";
    protected String[] propModelInitStr = null;

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

        int maxFieldNum = 0;
        for (Iterator<Object> it = prop.keySet().iterator(); it.hasNext();) {
            String k = (String) it.next();
            if (k.startsWith(POP_PROP_INIT_PREFIX)) {
                if (prop.getProperty(k) != null) {
                    maxFieldNum = Math.max(maxFieldNum,
                            Integer.parseInt(k.substring(POP_PROP_INIT_PREFIX.length())));
                }
            }
        }

        if (maxFieldNum >= 0) {
            propModelInitStr = new String[maxFieldNum + 1];
            for (int i = 0; i < propModelInitStr.length; i++) {
                String res = prop.getProperty(POP_PROP_INIT_PREFIX + i);
                if (res != null) {
                    propModelInitStr[i] = res;
                }
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

        rArg = new String[6];
        rArg[0] = baseDir.getAbsolutePath();
        rArg[1] = propVal[PROP_POP_IMPORT_PATH] == null ? "" : (String) propVal[PROP_POP_IMPORT_PATH];
        rArg[2] = propVal[PROP_NUM_SIM_PER_SET] == null ? "" : ((Integer) propVal[PROP_NUM_SIM_PER_SET]).toString();
        rArg[3] = propVal[PROP_SKIP_DATA_SET] == null ? "" : ((Integer) propVal[PROP_SKIP_DATA_SET]).toString();
        rArg[4] = propVal[PROP_INTRO_TYPE] == null ? "" : ((Integer) propVal[PROP_INTRO_TYPE]).toString();
        rArg[5] = propVal[PROP_SNAP_FREQ] == null ? "" : ((Integer) propVal[PROP_SNAP_FREQ]).toString();

        try {
            Run_Population_ACCEPtPlus_InfectionIntro_Batch run = new Run_Population_ACCEPtPlus_InfectionIntro_Batch(rArg);
            for(int v = 0; v < propModelInitStr.length; v++){
                if(propModelInitStr[v] != null){
                    // Best fit parameters
                    run.getParameter()[v] = Double.parseDouble(propModelInitStr[v]);
                }
                
            }                                    
            run.batchRun();

        } catch (ClassNotFoundException ex) {
            ex.printStackTrace(System.err);
        }

    }

}
