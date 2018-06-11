package test;

import java.io.File;
import java.io.FileFilter;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Path;
import java.util.Properties;
import sim.SimulationInterface;
import sim.Simulation_Population_ACCEPtPlus;

/**
 *
 * @author Ben Hui
 * @version 20180606
 */
public class Test_Population_ACCEPtPlus_Simulation_Batch {

    public static void main(String[] arg) throws IOException, ClassNotFoundException, InterruptedException {
        File baseDir = new File("C:\\Users\\Bhui\\OneDrive - UNSW\\ACCEPt\\Simulations");

        File[] resultSets = baseDir.listFiles(new FileFilter() {
            @Override
            public boolean accept(File file) {
                return file.isDirectory() && new File(file, SimulationInterface.FILENAME_PROP).exists();
            }
        });

        for (File dir : resultSets) {
            System.out.println("Generating results set as described in " + dir.getAbsolutePath());

            Simulation_Population_ACCEPtPlus sim = new Simulation_Population_ACCEPtPlus();

            Path propFile = new File(dir, SimulationInterface.FILENAME_PROP).toPath();
            Properties prop;
            prop = new Properties();
            try (InputStream inStr = java.nio.file.Files.newInputStream(propFile)) {
                prop.loadFromXML(inStr);
            }

            sim.setBaseDir(dir);
            sim.loadProperties(prop);
            sim.generateOneResultSet();     

            File[] singleResultSet = dir.listFiles(new FileFilter() {
                @Override
                public boolean accept(File file) {
                    return file.isDirectory();
                }
            });

            for (File f : singleResultSet) {
                Test_Population_ACCEPtPlus_Snapshot_Single.main(new String[]{f.getAbsolutePath()});

            }

        }

    }

}
