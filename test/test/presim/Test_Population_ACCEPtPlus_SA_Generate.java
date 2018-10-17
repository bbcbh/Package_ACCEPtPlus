/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package test.presim;

import java.io.IOException;
import run.Run_Population_ACCEPtPlus_SA_Generate;

/**
 *
 * @author bhui
 */
public class Test_Population_ACCEPtPlus_SA_Generate {
    
    public static void main(String[] arg) throws IOException, ClassNotFoundException, InterruptedException{
        
        String[] rArg = new String[]{
            "8",
            "C:\\Users\\Ben\\OneDrive - UNSW\\ACCEPt\\SA_1000",
            "C:\\Users\\Ben\\OneDrive - UNSW\\ACCEPt\\ImportDir",       
            "8",
            "668",
            "669"
        };                
        
        Run_Population_ACCEPtPlus_SA_Generate.main(rArg);
    }
}
