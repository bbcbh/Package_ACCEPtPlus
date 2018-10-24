package test.presim;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileFilter;
import java.io.FileReader;
import java.io.IOException;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.Arrays;

/**
 *
 * @author Ben Hui
 *
 */
public class Test_Population_ACCEPtPlus_SelectPopByCSV {

    public static void main(String[] arg) throws IOException, ClassNotFoundException, InterruptedException {

        File propDir = new File("C:\\Users\\Bhui\\OneDrive - UNSW\\ACCEPt\\TestDir");
        File csvFile = new File(propDir, "ACCEPt_Select_100.csv");
        
        String[] prefix = new String[]{"output_sim_","pop_S","testing_history_"};
        String[] suffix = new String[]{".txt","_T0.zip",".obj"};
        

        ArrayList<Integer> arr = null;
        try {
            try (BufferedReader lines = new BufferedReader(new FileReader(csvFile))) {
                arr = new ArrayList();
                String line;
                while ((line = lines.readLine()) != null) {
                    arr.add(Integer.parseInt(line));
                }
            }
        } catch (IOException | NumberFormatException ex) {
            ex.printStackTrace(System.err);
        }
        if (arr != null) {
            Integer[] popSel;
            popSel = arr.toArray(new Integer[arr.size()]);
            Arrays.sort(popSel);

            if (popSel.length > 0) {
                System.out.println("# pop selected = " + popSel.length);
            }

            File[] dirList = propDir.listFiles(new FileFilter() {
                @Override
                public boolean accept(File file) {
                    return file.isDirectory();
                }
            });

            for (File dir : dirList) {                
                String dirName = dir.getName();
                File orgDir = new File(propDir, dirName + "_" + Long.toString(System.currentTimeMillis()));                                
                Files.move(dir.toPath() , orgDir.toPath());
                dir.mkdir();
                for(Integer pId : popSel){
                    for(int s = 0; s < prefix.length; s++){
                        Files.move(new File(orgDir,prefix[s]+pId.toString() + suffix[s]).toPath(), 
                                new File(dir,prefix[s]+pId.toString() + suffix[s]).toPath());
                    }                    
                }
                
                util.Snapshot_Population_ACCEPtPlus.decodePopZips(dir.getAbsolutePath());
                
            }

        }

    }

}
