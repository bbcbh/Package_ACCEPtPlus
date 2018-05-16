package test;

import java.io.File;
import java.io.FileFilter;
import java.io.IOException;
import run.Run_Population_ACCEPtPlus_InfectionIntro_Batch;

/**
 *
 * @author Administrator
 */
public class Test_Population_ACCEPtPlus_InfectionSim_Batch {

   public static void main(String[] arg) throws IOException, ClassNotFoundException, InterruptedException{                     
       
       String[] rArg = new String[]{ 
           "C:\\Users\\Bhui\\OneDrive - UNSW\\ACCEPt\\MassSrnEffect_N", 
           "C:\\Users\\Bhui\\OneDrive - UNSW\\ACCEPt\\5_Runs_NonExtinct",           
           "5", "262143", "0", "30"} ;
      
       
       Run_Population_ACCEPtPlus_InfectionIntro_Batch.main(rArg);
       
       File[] targetDirs = new File(rArg[0]).listFiles(new FileFilter() {
           @Override
           public boolean accept(File file) {
               return file.isDirectory();
           }
       });
       
       for(int i = 0; i < targetDirs.length; i++){
           System.out.println("Decodeing results in " + targetDirs[i].getAbsolutePath());
           Test_Population_ACCEPtPlus_Snapshot_Single.main(new String[]{targetDirs[i].getAbsolutePath()});
           
       }
       
       
       
       
   }

}
