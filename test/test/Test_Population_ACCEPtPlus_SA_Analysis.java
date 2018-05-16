/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package test;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileFilter;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.PrintWriter;
import java.util.Arrays;
import person.AbstractIndividualInterface;
import population.Population_ACCEPtPlus;
import util.Classifier_ACCEPt;
import util.FileZipper;
import util.PersonClassifier;

/**
 *
 * @author Ben
 */
public class Test_Population_ACCEPtPlus_SA_Analysis {

    public static String IMPORT_PATH = "C:\\Users\\Ben\\Desktop\\Single"; //"C:\\Users\\Ben\\OneDrive - UNSW\\ACCEPt\\SA_1000";
    public static String RESULT_FILENAME = "prevalence_by_set.csv";
    public static final PersonClassifier prevalClassifier = new Classifier_ACCEPt();

    public static void main(String[] arg) throws IOException, ClassNotFoundException {
        File importDir = new File(IMPORT_PATH);
        File[] setDirs = importDir.listFiles(new FileFilter() {
            @Override
            public boolean accept(File file) {
                return file.isDirectory() && file.getName().startsWith("Set_");
            }
        });
        System.out.println("# results = " + setDirs.length);

        float[][] resStore = new float[setDirs.length][prevalClassifier.numClass()];

        for (File baseDir : setDirs) {
            int resultKey = Integer.parseInt(baseDir.getName().substring(4));
            File[] resultZips = baseDir.listFiles(new FileFilter() {
                @Override
                public boolean accept(File file) {
                    return file.getName().endsWith(".zip");
                }
            });
            System.out.println("Analysing Set #" + resultKey + " with " + resultZips.length + " zip file(s)");

            float[] prevalenceSum = new float[prevalClassifier.numClass()];
            int numPreval = 0;

            for (int f = 0; f < resultZips.length; f++) {
                Population_ACCEPtPlus pop;
                File popFile = resultZips[f];
                File tempFile = FileZipper.unzipFile(popFile, baseDir);

                try (ObjectInputStream oIStream = new ObjectInputStream(new BufferedInputStream(new FileInputStream(tempFile)))) {
                    pop = Population_ACCEPtPlus.decodeFromStream(oIStream);
                }
                tempFile.delete();
                if (pop != null) {
                    int[] numPerson = new int[prevalClassifier.numClass()];
                    int[] numInfect = new int[prevalClassifier.numClass()];

                    AbstractIndividualInterface[] persons = pop.getPop();

                    for (AbstractIndividualInterface p : persons) {
                        int key = prevalClassifier.classifyPerson(p);
                        if (key >= 0) {
                            numPerson[key]++;
                            if (p.getInfectionStatus()[0] != AbstractIndividualInterface.INFECT_S) {
                                numInfect[key]++;
                            }
                        }
                    }

                    for (int cI = 0; cI < prevalClassifier.numClass(); cI++) {
                        prevalenceSum[cI] += (100f * numInfect[cI]) / numPerson[cI];
                    }
                    numPreval++;
                }
            }

            float[] meanPreval = prevalenceSum;

            for (int cI = 0; cI < prevalClassifier.numClass(); cI++) {
                meanPreval[cI] = prevalenceSum[cI] / numPreval;
            }
            
            try{
                resStore[resultKey] = meanPreval;
            } catch (ArrayIndexOutOfBoundsException ex) { 
                System.out.println("#" + resultKey + ": " + Arrays.toString(meanPreval));
            }
        }

        File outputFile = new File(importDir, RESULT_FILENAME);
        try (PrintWriter pWri = new PrintWriter(outputFile)) {
            StringBuilder s = new StringBuilder();
            
            for (int i = 0; i < prevalClassifier.numClass(); i++) {                
                if (s.length() != 0) {                
                    s.append(',');
                }
                s.append("Prevalence_").append(Integer.toString(i));
                
            }            
            
            pWri.println(s.toString());          
            
            for (float[] resStoreSet : resStore) {
                s = new StringBuilder();
                for (int j = 0; j < resStoreSet.length; j++) {
                    if (s.length() != 0) {                   
                        s.append(',');
                    }
                    s.append(Float.toString(resStoreSet[j]));
                }
                pWri.println(s.toString());
            }
        }

    }

}
