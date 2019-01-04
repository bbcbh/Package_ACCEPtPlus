package test;

import util.Snapshot_Population_ACCEPtPlus;
import java.io.EOFException;
import java.io.File;
import java.io.FileFilter;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Arrays;
import person.AbstractIndividualInterface;
import static sim.Runnable_Population_ACCEPtPlus_Infection.PREVAL_STORE_AGE;
import static sim.Runnable_Population_ACCEPtPlus_Infection.PREVAL_STORE_GLOBAL_TIME;
import static sim.Runnable_Population_ACCEPtPlus_Infection.PREVAL_STORE_INFECT_STATUS;
import static sim.Runnable_Population_ACCEPtPlus_Infection.PREVAL_STORE_NUM_LIFETIME_PARTNERS;
import util.FileZipper;

/**
 *
 * @author Ben Hui
 *
 */
public class Test_Population_ACCEPtPlus_Snapshot_Multiple {

    public static boolean decodeBase = !true;
    public static boolean decodePreval = true;

    // Result store format:
    // int[fIndex][# total, # infected]
    public static final int RES_STORE_TOTAL = 0;
    public static final int RES_STORE_ACCEPT = RES_STORE_TOTAL + 1;
    public static final int RES_STORE_LENGTH = RES_STORE_ACCEPT + 1;

    public static void main(String[] arg) throws IOException, ClassNotFoundException {

        File RESULTS_DIR = new File("C:\\Users\\Bhui\\OneDrive - UNSW\\ACCEPt\\TestDir");
        
        if(arg.length > 0){
            RESULTS_DIR = new File(arg[0]);
        }

        File[] resCollections = RESULTS_DIR.listFiles(new FileFilter() {
            @Override
            public boolean accept(File file) {
                return file.isDirectory();
            }
        });

        for (File res : resCollections) {

            if (decodeBase) {
                System.out.println("Decoding results in " + res.getAbsolutePath());
                Snapshot_Population_ACCEPtPlus.decodePopZips(new String[]{res.getAbsolutePath()});
            }
            if (decodePreval) {

                System.out.println("Decoding preval store in " + res.getAbsolutePath());

                File[] preval_store = res.listFiles(new FileFilter() {
                    @Override
                    public boolean accept(File file) {
                        return file.getName().startsWith("preval_store_");
                    }
                });

                ArrayList<Integer> resultStore_time = new ArrayList<>();

                ArrayList<int[][]>[] resultsStore = new ArrayList[RES_STORE_LENGTH];

                int[][] currentStoreEntry;

                for (int i = 0; i < resultsStore.length; i++) {
                    resultsStore[i] = new ArrayList<>();
                }

                int maxTimeStamp = Integer.MIN_VALUE;

                for (int fIndex = 0; fIndex < preval_store.length; fIndex++) {

                    File prevalZip = preval_store[fIndex];

                    System.out.println("Decoding " + prevalZip.getAbsolutePath());

                    try {

                        File tempFile = FileZipper.unzipFile(prevalZip, prevalZip.getParentFile());

                        ObjectInputStream inStream = new ObjectInputStream(new FileInputStream(tempFile));

                        int[] entry;

                        try {
                            while (true) {
                                int timeIndex;
                                entry = (int[]) inStream.readObject();

                                if (maxTimeStamp < entry[PREVAL_STORE_GLOBAL_TIME]) {
                                    // New row
                                    resultStore_time.add(entry[PREVAL_STORE_GLOBAL_TIME]);
                                    for (ArrayList<int[][]> resultsStoreEnt : resultsStore) {
                                        // int[fIndex][# total, # infected]
                                        resultsStoreEnt.add(new int[preval_store.length][2]);
                                    }
                                    timeIndex = resultStore_time.size() - 1;
                                } else {
                                    timeIndex = resultStore_time.lastIndexOf(entry[PREVAL_STORE_GLOBAL_TIME]);
                                }

                                if (timeIndex < 0) {
                                    System.err.println("Time index of " + entry[PREVAL_STORE_GLOBAL_TIME] + " not found. New row added.");

                                    Integer addTime = entry[PREVAL_STORE_GLOBAL_TIME];
                                    Integer[] timeArr = resultStore_time.toArray(new Integer[resultStore_time.size()]);
                                    int insertPt = Arrays.binarySearch(timeArr, addTime);
                                    insertPt = -(insertPt + 1);
                                    resultStore_time.add(insertPt, addTime);

                                    for (ArrayList<int[][]> resultsStoreEnt : resultsStore) {
                                        // int[fIndex][# total, # infected]
                                        resultsStoreEnt.add(insertPt, new int[preval_store.length][2]);
                                    }

                                    timeIndex = insertPt;
                                }

                                for (int storeIndex = 0; storeIndex < resultsStore.length; storeIndex++) {

                                    currentStoreEntry = resultsStore[storeIndex].get(timeIndex);

                                    switch (storeIndex) {
                                        case RES_STORE_TOTAL:
                                            currentStoreEntry[fIndex][0]++;
                                            if (entry[PREVAL_STORE_INFECT_STATUS] != AbstractIndividualInterface.INFECT_S) {
                                                currentStoreEntry[fIndex][1]++;
                                            }
                                            break;
                                        case RES_STORE_ACCEPT:
                                            if (16 * AbstractIndividualInterface.ONE_YEAR_INT <= entry[PREVAL_STORE_AGE]
                                                    && entry[PREVAL_STORE_AGE] < 30 * AbstractIndividualInterface.ONE_YEAR_INT
                                                    && entry[PREVAL_STORE_NUM_LIFETIME_PARTNERS] > 0) {
                                                currentStoreEntry[fIndex][0]++;
                                                if (entry[PREVAL_STORE_INFECT_STATUS] != AbstractIndividualInterface.INFECT_S) {
                                                    currentStoreEntry[fIndex][1]++;
                                                }
                                            }
                                            break;
                                        default:
                                            System.err.println("Result store index #" + storeIndex + " not defined");
                                    }

                                }

                                maxTimeStamp = Math.max(maxTimeStamp, entry[PREVAL_STORE_GLOBAL_TIME]);

                            }
                        } catch (EOFException ex) {

                        }

                        inStream.close();
                        tempFile.delete();
                    } catch (Exception ex) {

                        System.err.println(ex.getClass().getName() + " throw when extracting " + prevalZip.getAbsolutePath() + ". File skipped.");

                    }
                }

                PrintWriter[] pWri = new PrintWriter[RES_STORE_LENGTH];

                pWri[RES_STORE_TOTAL] = new PrintWriter(new File(res, "preval_all.csv"));
                pWri[RES_STORE_ACCEPT] = new PrintWriter(new File(res, "preval_accept.csv"));

                for (int timeIndex = 0; timeIndex < resultStore_time.size(); timeIndex++) {
                    int globalTime = resultStore_time.get(timeIndex);
                    for (int storeIndex = 0; storeIndex < resultsStore.length; storeIndex++) {
                        currentStoreEntry = resultsStore[storeIndex].get(timeIndex);
                        pWri[storeIndex].print(globalTime);
                        for (int fI = 0; fI < currentStoreEntry.length; fI++) {
                            pWri[storeIndex].print(',');
                            pWri[storeIndex].print(((float) currentStoreEntry[fI][1]) / currentStoreEntry[fI][0]);
                        }
                        pWri[storeIndex].println();

                    }
                }

                for (PrintWriter p : pWri) {
                    p.close();
                }

            }

        }

    }

}
