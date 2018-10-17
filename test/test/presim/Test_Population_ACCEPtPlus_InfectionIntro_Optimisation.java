package test.presim;

import java.io.IOException;
import opt.OptRun_Population_ACCEPtPlus_IntroInfection_GA_Optimisation;
import opt.OptRun_Population_ACCEPtPlus_IntroInfection_Optimisation;

/**
 *
 * @author Ben Hui
 */
public class Test_Population_ACCEPtPlus_InfectionIntro_Optimisation {

    public static void main(String[] arg) throws IOException, ClassNotFoundException {

        boolean useGA = false;

        if (useGA) {
            String[] rArg = new String[]{
                // 0: Base Dir
                "C:\\Users\\Bhui\\OneDrive - UNSW\\ACCEPt\\OptResult_ASTD_GA",
                //"C:\\Users\\Bhui\\OneDrive - UNSW\\ACCEPt\\OptResult_MTDB",
                //"C:\\Users\\Bhui\\OneDrive - UNSW\\ACCEPt\\OptResult_AgeSpecTran_GA", 
                //"C:\\Users\\Bhui\\OneDrive - UNSW\\ACCEPt\\OptResults_GA_DEBUG",
                //"C:\\Users\\Bhui\\Desktop\\FTP\\OptResult_AST_GA",
                // 1: Import Dir
                "C:\\Users\\Bhui\\OneDrive - UNSW\\ACCEPt\\ImportDir",
                // 2: Num thread
                "8",
                // 3: Num sim
                "2",
                // 4: Random Sel
                "",
                // 5: Num to keep
                "0",
                // 6: GA_Pop size
                "500",};
            OptRun_Population_ACCEPtPlus_IntroInfection_GA_Optimisation.main(rArg);

        } else {
            String[] rArg = new String[]{
                // 0: Base Dir
                "C:\\Users\\Bhui\\OneDrive - UNSW\\ACCEPt\\OptResult_VarTran",                
                // 1: Import Dir
                "C:\\Users\\Bhui\\OneDrive - UNSW\\ACCEPt\\ImportDir",
                // 2: Num thread
                "8",
                // 3: Num sim
                "8",
                // 4: Random Sel
                "",
                // 5: Num to keep
                "1",};
            OptRun_Population_ACCEPtPlus_IntroInfection_Optimisation.main(rArg);
          
        }

    }
}
