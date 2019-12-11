package test;

import util.Snapshot_Population_ACCEPtPlus;
import java.io.File;
import java.io.FileFilter;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.concurrent.ExecutionException;

/**
 *
 * @author Ben Hui
 *
 */
public class Test_Population_ACCEPtPlus_Snapshot_Multiple {

    public static boolean decodeBase = !true;
    public static boolean decodePreval = true;

    public static void main(String[] arg) throws IOException, ClassNotFoundException, ExecutionException, FileNotFoundException, InterruptedException {

        File RESULTS_DIR = new File("C:\\Users\\Bhui\\OneDrive - UNSW\\ACCEPt\\SingleRun");

        if (arg.length > 0) {
            RESULTS_DIR = new File(arg[0]);
        }

        File[] resCollections = RESULTS_DIR.listFiles(new FileFilter() {
            @Override
            public boolean accept(File file) {
                return file.isDirectory();
            }
        });

        for (File res : resCollections) {
            Snapshot_Population_ACCEPtPlus.decodeResults(res, decodeBase, decodePreval);
        }
    }
}
