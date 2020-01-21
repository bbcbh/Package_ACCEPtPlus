package test;

import java.io.File;
import java.io.FileFilter;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.concurrent.ExecutionException;
import sim.SimulationInterface;
import sim.Simulation_Population_ACCEPtPlus;

/**
 *
 * @author Ben Hui
 * @version 20190104
 */
public class Test_Population_ACCEPtPlus_Simulation_Batch {

    public static void main(String[] arg) throws IOException, ClassNotFoundException, InterruptedException, FileNotFoundException, ExecutionException {

       

        String path = "C:\\Users\\Bhui\\OneDrive - UNSW\\ACCEPt\\Test\\"; // Location of .prop file

        if (new File(new File(path), SimulationInterface.FILENAME_PROP).exists()) {
            Simulation_Population_ACCEPtPlus.main(new String[]{path});
            //if (decodePreval) {
            //    Test_Population_ACCEPtPlus_Snapshot_Multiple.main(new String[]{path});
            //}
        } else {

            File[] propDirs = new File(path).listFiles(new FileFilter() {
                @Override
                public boolean accept(File file) {
                    return file.isDirectory()
                            && new File(file, SimulationInterface.FILENAME_PROP).exists();
                }
            });

            for (File propDir : propDirs) {
                System.out.println("Generating results from " + propDir.getAbsolutePath());
                String[] rArg = new String[]{propDir.getAbsolutePath()};
                Simulation_Population_ACCEPtPlus.main(rArg);
                //if (decodePreval) {
                //    Snapshot_Population_ACCEPtPlus.decodeResults(propDir, false, true);
                //

            }

        }

        // PROP_SKIP_DATA_SET
        // 4194302- Baseline only
        // 4194048  - ACCEPt Manscript (with intervention rate)     
        // 524286  - ACCEPt Manscript (with baseline rate)   
        // 4190462  - Baseline + MassScreen
    }

}
