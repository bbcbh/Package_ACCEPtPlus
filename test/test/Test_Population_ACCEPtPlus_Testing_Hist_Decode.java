package test;

import java.io.File;
import java.io.FileFilter;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.PrintWriter;
import java.util.Arrays;
import java.util.concurrent.ConcurrentHashMap;
import static sim.Runnable_Population_ACCEPtPlus_Infection.HIST_HEADER_GENDER;
import static sim.Runnable_Population_ACCEPtPlus_Infection.HIST_HEADER_NEXT_PT;
import static sim.Runnable_Population_ACCEPtPlus_Infection.HIST_HEADER_TOTAL_LENGTH;
import static sim.Runnable_Population_ACCEPtPlus_Infection.HIST_OFFSET_TEST_CURRENT_INFECTION_FROM_AGE;
import static sim.Runnable_Population_ACCEPtPlus_Infection.HIST_OFFSET_TEST_INFECTED;
import static sim.Runnable_Population_ACCEPtPlus_Infection.HIST_OFFSET_TEST_TIME;
import static sim.Runnable_Population_ACCEPtPlus_Infection.HIST_OFFSET_TOTAL_LENGTH;

/**
 *
 * @author Ben Hui
 */
public class Test_Population_ACCEPtPlus_Testing_Hist_Decode {

    public static final String DIR_PATH = "C:\\Users\\bhui\\Desktop\\ACCEPtPlusDirVM\\Testing_History";

    public static final int START_TIME = 36000;
    public static final int END_TIME = 54000;
    public static final int SNAP_FREQ = 30 * 3;

    public static final int ROW_NUM_TOTAL = (END_TIME - START_TIME) / SNAP_FREQ;

    // C
    public static final int COL_NUM_NOT_TESTED_LIFETIME = 0;
    // D
    public static final int COL_NUM_TEST_NEG_FIRST_TEST_0_LIFETIME = COL_NUM_NOT_TESTED_LIFETIME + 1;
    public static final int COL_NUM_TEST_NEG_NON_FIRST_TEST_0_LIFETIME = COL_NUM_TEST_NEG_FIRST_TEST_0_LIFETIME + 1;
    // F
    public static final int COL_NUM_TEST_POS_FIRST_TEST_1_POSITIVE_LIFETIME = COL_NUM_TEST_NEG_NON_FIRST_TEST_0_LIFETIME + 1;
    public static final int COL_NUM_TEST_POS_NON_FIRST_TEST_1_POSITIVE_LIFETIME = COL_NUM_TEST_POS_FIRST_TEST_1_POSITIVE_LIFETIME + 1;
    // H
    public static final int COL_NUM_TEST_POS_SUSTAIN_POSTIVE = COL_NUM_TEST_POS_NON_FIRST_TEST_1_POSITIVE_LIFETIME + 1;
    // I
    public static final int COL_NUM_TEST_POS_2_POSITIVE_LIFETIME = COL_NUM_TEST_POS_SUSTAIN_POSTIVE + 1;
    public static final int COL_NUM_TEST_POS_3_POSITIVE_LIFETIME = COL_NUM_TEST_POS_2_POSITIVE_LIFETIME + 1;
    public static final int COL_NUM_TEST_POS_4_POSITIVE_LIFETIME = COL_NUM_TEST_POS_3_POSITIVE_LIFETIME + 1;
    // L 
    public static final int COL_NUM_TEST_NEG_1_POSITIVE_LIFETIME = COL_NUM_TEST_POS_4_POSITIVE_LIFETIME + 1;
    public static final int COL_NUM_TEST_NEG_2_POSITIVE_LIFETIME = COL_NUM_TEST_NEG_1_POSITIVE_LIFETIME + 1;
    public static final int COL_NUM_TEST_NEG_3_POSITIVE_LIFETIME = COL_NUM_TEST_NEG_2_POSITIVE_LIFETIME + 1;
    public static final int COL_NUM_TEST_NEG_4_POSITIVE_LIFETIME = COL_NUM_TEST_NEG_3_POSITIVE_LIFETIME + 1;
    // P 
    public static final int COL_NUM_NON_TEST_0_POSITIVE_LIFETIME = COL_NUM_TEST_NEG_4_POSITIVE_LIFETIME + 1;
    public static final int COL_NUM_NON_TEST_1_POSITIVE_LIFETIME = COL_NUM_NON_TEST_0_POSITIVE_LIFETIME + 1;
    public static final int COL_NUM_NON_TEST_2_POSITIVE_LIFETIME = COL_NUM_NON_TEST_1_POSITIVE_LIFETIME + 1;
    public static final int COL_NUM_NON_TEST_3_POSITIVE_LIFETIME = COL_NUM_NON_TEST_2_POSITIVE_LIFETIME + 1;
    public static final int COL_NUM_NON_TEST_4_POSITIVE_LIFETIME = COL_NUM_NON_TEST_3_POSITIVE_LIFETIME + 1;

    public static final int COL_NUM_TOTAL = COL_NUM_NON_TEST_4_POSITIVE_LIFETIME + 1;

    private static int[][] testDetailsByTime(int[] testHist) {
        int[] testTime = new int[(testHist[HIST_HEADER_NEXT_PT] - HIST_HEADER_TOTAL_LENGTH) / HIST_OFFSET_TOTAL_LENGTH];
        int[] numPosTestByTime = new int[testTime.length];
        int numPosTestAcc = 0;
        if (testTime.length > 0) {
            int pt = 0;
            int offset = HIST_HEADER_TOTAL_LENGTH;
            while (pt < testTime.length) {
                testTime[pt] = testHist[offset + HIST_OFFSET_TEST_TIME];
                numPosTestAcc += testHist[offset + HIST_OFFSET_TEST_INFECTED] > 0 ? 1 : 0;
                numPosTestByTime[pt] = numPosTestAcc;
                offset += HIST_OFFSET_TOTAL_LENGTH;
                pt++;
            }
        }
        return new int[][]{testTime, numPosTestByTime};
    }

    private static int allocateColumn(int[] testHist, int[][] testDetails, int time_start) {
        int col = -1;

        int[] testTime = testDetails[0];
        int[] numPosTestByTime = testDetails[1];

        int startIndex = Arrays.binarySearch(testTime, time_start - SNAP_FREQ);
        int endIndex = Arrays.binarySearch(testTime, time_start);

        if (startIndex < 0) {
            startIndex = -(startIndex + 1); //(-(insertion point) - 1)
        }

        if (endIndex < 0) {
            endIndex = -(endIndex + 1); //(-(insertion point) - 1)
        }

        // Looking at entry between start age and end age                
        if (endIndex == 0) {
            col = COL_NUM_NOT_TESTED_LIFETIME;
        } else {
            boolean testedInRange = endIndex > startIndex;
            int numPosTestPreRange = startIndex > 0 ? numPosTestByTime[startIndex - 1] : 0;
            int numPosTestSoFar = numPosTestByTime[endIndex - 1];
            int numPosTestInRange = numPosTestByTime[endIndex - 1] - numPosTestPreRange;
            boolean positiveTestInRange = numPosTestInRange > 0;

            boolean sustain_inf = testedInRange && positiveTestInRange; // possible sustained infection

            int[] testDetailInRange = null, testDetailPrevious = null;

            if (sustain_inf) {
                testDetailInRange = Arrays.copyOfRange(testHist,
                        HIST_HEADER_TOTAL_LENGTH + (startIndex) * HIST_OFFSET_TOTAL_LENGTH,
                        HIST_HEADER_TOTAL_LENGTH + (endIndex) * HIST_OFFSET_TOTAL_LENGTH);
                int prevTestPt = HIST_HEADER_TOTAL_LENGTH + (startIndex - 1) * HIST_OFFSET_TOTAL_LENGTH;

                while (prevTestPt > 0 && testTime[endIndex - 1]
                        - testHist[prevTestPt + HIST_OFFSET_TEST_TIME] < SNAP_FREQ) {
                    // Skip test done within 3 month period
                    prevTestPt -= HIST_OFFSET_TOTAL_LENGTH;
                }

                if (prevTestPt >= 0) {
                    testDetailPrevious = Arrays.copyOfRange(testHist,
                            prevTestPt,
                            HIST_HEADER_TOTAL_LENGTH + (startIndex) * HIST_OFFSET_TOTAL_LENGTH);
                }
                sustain_inf = prevTestPt >= 0
                        && testHist[prevTestPt + HIST_OFFSET_TEST_INFECTED] > 0;

            }

            if (testedInRange) {
                if (positiveTestInRange) {
                    if (sustain_inf) {
                        col = COL_NUM_TEST_POS_SUSTAIN_POSTIVE;
                    } else {
                        switch (numPosTestSoFar) {
                            case 1:
                                if (endIndex == 1) {
                                    col = COL_NUM_TEST_POS_FIRST_TEST_1_POSITIVE_LIFETIME;
                                } else {
                                    col = COL_NUM_TEST_POS_NON_FIRST_TEST_1_POSITIVE_LIFETIME;
                                }
                                break;
                            case 2:
                                col = COL_NUM_TEST_POS_2_POSITIVE_LIFETIME;
                                break;
                            case 3:
                                col = COL_NUM_TEST_POS_3_POSITIVE_LIFETIME;
                                break;
                            default:
                                col = COL_NUM_TEST_POS_4_POSITIVE_LIFETIME;
                                break;
                        }
                    }
                } else {
                    switch (numPosTestSoFar) {
                        case 0:
                            if (endIndex == 1) {
                                // First ever test         
                                col = COL_NUM_TEST_NEG_FIRST_TEST_0_LIFETIME;
                            } else {
                                col = COL_NUM_TEST_NEG_NON_FIRST_TEST_0_LIFETIME;
                            }
                            break;
                        case 1:
                            col = COL_NUM_TEST_NEG_1_POSITIVE_LIFETIME;
                            break;
                        case 2:
                            col = COL_NUM_TEST_NEG_2_POSITIVE_LIFETIME;
                            break;
                        case 3:
                            col = COL_NUM_TEST_NEG_3_POSITIVE_LIFETIME;
                            break;
                        default:
                            col = COL_NUM_TEST_NEG_4_POSITIVE_LIFETIME;
                            break;
                    }
                }
            } else {
                switch (numPosTestSoFar) {
                    case 0:
                        col = COL_NUM_NON_TEST_0_POSITIVE_LIFETIME;
                        break;
                    case 1:
                        col = COL_NUM_NON_TEST_1_POSITIVE_LIFETIME;
                        break;
                    case 2:
                        col = COL_NUM_NON_TEST_2_POSITIVE_LIFETIME;
                        break;
                    case 3:
                        col = COL_NUM_NON_TEST_3_POSITIVE_LIFETIME;
                        break;
                    default:
                        col = COL_NUM_NON_TEST_4_POSITIVE_LIFETIME;
                        break;
                }
            }

        }

        if (col < 0) {
            System.err.println("Undefine column for entry " + Arrays.toString(testHist));
        }

        return col;

    }

    public static void decodeSingleCase(File f) throws IOException, ClassNotFoundException {

        File exportCSV_File;

        int[][][] total_count = new int[2][ROW_NUM_TOTAL][COL_NUM_TOTAL];

        File[] histFiles = f.listFiles(new FileFilter() {

            @Override
            public boolean accept(File file) {
                return file.getName().endsWith(".obj");
            }
        });

        System.out.println("Examining history within " + f.getAbsolutePath() + ", Num of file = " + histFiles.length);

        for (File histFile : histFiles) {

            System.out.println("Examining history at " + histFile.getAbsolutePath());
            ObjectInputStream objIn = new ObjectInputStream(new FileInputStream(histFile));
            ConcurrentHashMap<Integer, int[]> histMap = (ConcurrentHashMap<Integer, int[]>) objIn.readObject();
            objIn.close();

            for (int[] testHist : histMap.values()) {
                int gender = testHist[HIST_HEADER_GENDER];
                int[][] testDetails = testDetailsByTime(testHist);
                int t_start = START_TIME;
                for (int t = 0; t < ROW_NUM_TOTAL; t++) {
                    int col_ent = allocateColumn(testHist, testDetails, t_start);
                    total_count[gender][t][col_ent]++;
                    t_start += SNAP_FREQ;
                }

                //if(gender == 0){
                //    exportCSV_File = new File(histFile.getParentFile(), "export_matrix.csv");
                //    exportCountMatrixRaw(total_count[gender], exportCSV_File);
                //    System.out.println("Matrix printed at " + exportCSV_File.getAbsolutePath());
                //}                
            }

            //exportCSV_File = new File(f, "export_matrix_F_Raw.csv");
            //exportCountMatrixRaw(total_count[0], exportCSV_File);
            //System.out.println("Matrix printed at " + exportCSV_File.getAbsolutePath());
            //exportCSV_File = new File(f, "export_matrix_M_Raw.csv");
            //exportCountMatrixRaw(total_count[1], exportCSV_File);
            //System.out.println("Matrix printed at " + exportCSV_File.getAbsolutePath());
        }

        exportCSV_File = new File(f, f.getName() + "_F.csv");
        exportCountMatrixCustom(total_count[0], exportCSV_File);
        System.out.println("Matrix printed at " + exportCSV_File.getAbsolutePath());

        exportCSV_File = new File(f, f.getName()+"_M.csv");
        exportCountMatrixCustom(total_count[1], exportCSV_File);
        System.out.println("Matrix printed at " + exportCSV_File.getAbsolutePath());

    }

    public static void exportCountMatrixRaw(int[][] matrix, File filePath) throws FileNotFoundException {
        PrintWriter wri = new PrintWriter(filePath);
        for (int[] row : matrix) {
            for (int c = 0; c < row.length; c++) {
                if (c > 0) {
                    wri.print(',');
                }
                wri.print(row[c]);
            }
            wri.println();
        }
        wri.close();
    }

    public static void exportCountMatrixCustom(int[][] matrix, File filePath) throws FileNotFoundException {
        PrintWriter wri = new PrintWriter(filePath);
        float rowSum = 0;
        float timeStart = 0;
        float timeSnap = 0.25f; // 3 months
        /*
         String header = "Years,Number of patients who have never been tested in their lifetime, "
         + "Number of patients who are tested in the 3-month cycle and who reported a negative result"
         + " and who have never had been tested prior to this test, "
         + "Number of patients who are tested in the 3-month cycle and who reported a negative result"
         + " and who have been tested before but never had a positive result in their lifetime (i.e. always tested negative), "
         + "\"Number of patients were tested in the 3-month cycle, who had never been tested previously,"
         + " and who reported a positive result for the first time in their lifetime\","
         + "\"Number of patients were tested in the 3-month cycle, who had been tested in the past but who had always tested negative previously,"
         + " and who reported a positive result for the first time in their lifetime\","
         + "Number of patients who were tested in the 3-month cycle and who reported a sustained positive result"
         + " (i.e. tested positive again within 3 months of a positive test in the previous cycle),"
         + "Number of patients who were tested in the 3-month cycle and who reported a positive result"
         + " for the 2nd time in a lifetime (with more than 3 months since first positive test),"
         + "Number of patients who were tested in the 3-month cycle and who reported a positive result for the third time in a lifetime"
         + " (with more than 3 months since last positive test),"
         + "Number of patients who were tested and who reported a positive result for the fourth (or more) time in a lifetime"
         + " (with more than 3 months since last positive test),"
         + "Number of patients who were tested in the cycle and who tested negative in the cycle"
         + " but who have tested positive once previously in their lifetime,"
         + "Number of patients who were tested in the cycle and who tested negative in the cycle"
         + " but who have tested positive twice previously in a lifetime,"
         + "Number of patients who were tested in the cycle and who tested negative in the cycle"
         + " but who have tested positive three times previously in their lifetime,"
         + "Number of patients who were tested in the cycle and who tested negative in the cycle"
         + " but who have tested positive four or more times previously in their lifetime,"
         + "Number of patients who were NOT tested in the cycle but who have been tested in their lifetime"
         + " and who have no history of positive results in their lifetime (i.e. always tested negative),"
         + "Number of patients who were NOT tested in the cycle but who have tested positive once previously in their lifetime,"
         + "Number of patients who were NOT tested in the cycle but who have tested positive twice previously in their lifetime,"
         + "Number of patients who were NOT tested in the cycle but who have tested positive three times previously in a lifetime,"
         + "Number of patients who were NOT tested in the cycle but who have tested positive four or more times previously in a lifetime";

         wri.println(header);
         */

        for (int[] row : matrix) {
            rowSum = 0;
            for (int c = 0; c < row.length; c++) {
                rowSum += row[c];
            }

            // Time columns
            wri.print(timeStart);
            timeStart += timeSnap;

            for (int c = 0; c < row.length; c++) {
                wri.print(',');
                wri.print(((float) row[c]) / rowSum);
            }
            wri.println();
        }
        wri.close();

    }

    public static void main(String[] arg) throws IOException, ClassNotFoundException {
        File baseDir = new File(DIR_PATH);

        File[] caseDirs = baseDir.listFiles(new FileFilter() {

            @Override
            public boolean accept(File file) {
                return file.isDirectory();
            }
        });

        for (File f : caseDirs) {
            decodeSingleCase(f);
        }
    }

}
