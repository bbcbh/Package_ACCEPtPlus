package test;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileFilter;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.util.Arrays;
import transform.ParameterConstraintTransformSineCurve;

/**
 *
 * @author Bhui
 */
public class Util_Population_ACCEPtPlus_Select_OptPop {

    public static void main(String[] arg) throws FileNotFoundException, IOException, ClassNotFoundException {
        final File RES_DIR = new File("C:\\Users\\Bhui\\Desktop\\FTP\\OptResult_AST");

        final String[] LINE_TO_MATCH = new String[]{            
            "distId #5: None [Mean,SD] = [0.12000425695182458, 0.0]",};
        final int[] LINE_NUM = new int[]{ 16, };
        final String TARGET_FILE_NAME = "opt_run_134.txt";

        if (LINE_TO_MATCH == null) {
            ParameterConstraintTransformSineCurve[] constraints;

            System.out.println("Reading contraint file....");
            File costrainFile = new File(RES_DIR, "ParamConstriants.csv");
            try (BufferedReader constraintReader = new BufferedReader(new FileReader(costrainFile))) {
                int lnNum = 0;
                String line;
                while (constraintReader.readLine() != null) {
                    lnNum++;
                }
                constraints = new ParameterConstraintTransformSineCurve[lnNum];
                lnNum = 0;
                BufferedReader constraintReader2 = new BufferedReader(new FileReader(costrainFile));

                while ((line = constraintReader2.readLine()) != null) {
                    String[] ent = line.split(",");
                    constraints[lnNum] = new ParameterConstraintTransformSineCurve(new double[]{
                        Double.parseDouble(ent[0]), Double.parseDouble(ent[1])});
                    lnNum++;
                }
            }

            File preSimplexFile = new File(RES_DIR, "ParamSimplex.obj");
            System.out.println("Reading previous simplex....");

            double[][] sX = null;
            double[][] sR = null;

            try (ObjectInputStream objStr = new ObjectInputStream(new FileInputStream(preSimplexFile))) {
                sX = (double[][]) objStr.readObject();
                sR = (double[][]) objStr.readObject();
            }

            if (sX != null) {
                double[] p0 = new double[sX[0].length];

                for (int i = 0; i < p0.length; i++) {
                    p0[i] = sX[0][i];
                    if (constraints[i] != null) {
                        p0[i] = constraints[i].toContrainted(p0[i]);
                    }
                }
                System.out.println("P0 = " + Arrays.toString(p0));
            }

        } else {

            File[] optDir = RES_DIR.listFiles(new FileFilter() {
                @Override
                public boolean accept(File file) {
                    return file.isDirectory();
                }
            });

            System.out.println("Number of results = " + optDir.length);

            Arrays.sort(optDir);

            for (int i = optDir.length - 1; i >= 0; i--) {
                File tarFile = new File(optDir[i], TARGET_FILE_NAME);

                if (tarFile.exists()) {
                    BufferedReader reader = new BufferedReader(new FileReader(tarFile));
                    String line = "";
                    int currentLineNum = 0;
                    int currentLinePt = 0;
                    
                    boolean allMatch = true;

                    while (currentLinePt < LINE_NUM.length
                            && currentLineNum < LINE_NUM[LINE_NUM.length - 1]) {
                        
                        line = reader.readLine();
                        currentLineNum++;

                        if (currentLineNum == LINE_NUM[currentLinePt]) {
                            allMatch &= line.startsWith(LINE_TO_MATCH[currentLinePt]);
                            currentLinePt++; 
                        }
                    }
                    reader.close();
                    if(allMatch){                    
                        System.out.println("Matching result at " + optDir[i].getAbsolutePath());
                    }
                }
            }
        }

        System.out.println("All done");

    }

}
