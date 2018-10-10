package test;

import java.io.IOException;
import sim.Simulation_Population_ACCEPtPlus;

/**
 *
 * @author Ben Hui
 * @version 20180606
 */
public class Test_Population_ACCEPtPlus_Simulation_Batch {

    public static void main(String[] arg) throws IOException, ClassNotFoundException, InterruptedException {
        String path = "C:\\Users\\Bhui\\OneDrive - UNSW\\ACCEPt\\TestDir"; // Location of .prop file

        //path = "C:\\Users\\Bhui\\OneDrive - UNSW\\ACCEPt\\SimResults\\BestSoFar";

        Simulation_Population_ACCEPtPlus.main(new String[]{path});
        
        
        // PROP_SKIP_DATA_SET
        // 524286 - Baseline only
        // 5116 - ACCEPt Manscript      
        // 520447 - MassScreen only

    }

}
