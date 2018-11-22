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
        // 4194302- Baseline only
        // 4194048  - ACCEPt Manscript (with intervention rate)     
        // 524286  - ACCEPt Manscript (with baseline rate)   
        // 4190462  - Baseline + MassScreen

    }

}
