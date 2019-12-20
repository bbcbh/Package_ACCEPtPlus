
package test.util;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;

public class Util_Population_ACCEPtPLus_Sel_Sim_Run {
    
    public static void main(String[] arg) throws FileNotFoundException, IOException{
        File src = new File("C:\\Users\\Bhui\\OneDrive - UNSW\\ACCEPt\\Baseline_Full\\ACCEPt_Select_resample.csv");
        File tar = new File("C:\\Users\\Bhui\\OneDrive - UNSW\\ACCEPt\\Baseline_Full\\ACCEPt_Select_resample_100.csv");
        File selIndex = new File("C:\\Users\\Bhui\\OneDrive - UNSW\\ACCEPt\\MATLAB\\selIndex.csv");
        
        
        BufferedReader srcR = new BufferedReader(new FileReader(src));
        PrintWriter tarW = new PrintWriter(new FileWriter(tar));
        BufferedReader selIndexR = new BufferedReader(new FileReader(selIndex));
        
        String lineSel, lineSrc;
        while((lineSel = selIndexR.readLine()) != null){
            
            search_loop:
            while((lineSrc = srcR.readLine()) != null){
                if(lineSrc.startsWith(lineSel+",")){
                    tarW.println(lineSrc);
                    break search_loop;
                }
            }                                    
        }    
        
        srcR.close();
        selIndexR.close();
        tarW.close();
    }
    
}
