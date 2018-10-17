package test.util;

import java.io.File;
import java.io.FileFilter;
import java.io.IOException;

/**
 *
 * @author bhui
 */
public class Util_Population_ACCEPtPlus_CreateDummyZip {            
    public static void main(String[] arg) throws IOException{
        File dirPath = new File("C:\\Users\\Bhui\\Desktop\\ACCEPt_VM\\1000_Runs\\Increase_Testing");
        File dummyPath = new File("C:\\Users\\Bhui\\Desktop\\VM_FTP\\ACCEPt_VM\\1000_Runs\\Increase_Testing");
        
        dummyPath.mkdirs();
        
        File[] popZips =  dirPath.listFiles(new FileFilter() {
            @Override
            public boolean accept(File file) {
                return file.getName().endsWith(".zip");
            }
        });
        
        int counter = 0;
        
        for(int i = 0; i < popZips.length; i++){
            File dummyFile = new File(dummyPath, popZips[i].getName());
            if(dummyFile.createNewFile()){
                counter++;            
            }else{
                System.err.println("File " + dummyFile.getAbsolutePath() + " already existed.");
                System.exit(1);
            }
        }
        
        System.out.println(counter + " dummy file created at " + dummyPath.getAbsolutePath());
       
        
        
        
        
        
        
        
    }
    
}
