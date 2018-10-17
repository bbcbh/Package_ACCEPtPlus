package test.presim;

import util.Snapshot_Population_ACCEPtPlus;
import java.io.File;
import java.io.FileFilter;
import java.io.IOException;
import run.Run_Population_ACCEPtPlus_InfectionIntro_Batch;

/**
 *
 * @author Ben Hui
 * @deprecated  - if possible use Test_Population_ACCEPtPlus_Simulation_Batch instead
 */
public class Test_Population_ACCEPtPlus_InfectionIntro_Batch {

   public static void main(String[] arg) throws IOException, ClassNotFoundException, InterruptedException{                     
       
       String[] rArg = new String[]{ 
           "C:\\Users\\Bhui\\OneDrive - UNSW\\ACCEPt\\1000_Runs_VarTran_2Trans_5", 
           "C:\\Users\\Bhui\\OneDrive - UNSW\\ACCEPt\\ImportDir",           
           "1000", "524286"} ;
       // 524286 - Baseline only
       // 5116 - ACCEPt Manscript                    
       
       Run_Population_ACCEPtPlus_InfectionIntro_Batch.main(rArg);
       
       File[] targetDirs = new File(rArg[0]).listFiles(new FileFilter() {
           @Override
           public boolean accept(File file) {
               return file.isDirectory();
           }
       });
       
       for(int i = 0; i < targetDirs.length; i++){
           System.out.println("Decoding results in " + targetDirs[i].getAbsolutePath());
           Snapshot_Population_ACCEPtPlus.decodePopZips(new String[]{targetDirs[i].getAbsolutePath()});                                            
       }
       
       
   }

}
