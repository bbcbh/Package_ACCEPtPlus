/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package test.presim;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileFilter;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.PrintWriter;
import java.util.Arrays;
import java.util.Comparator;
import person.AbstractIndividualInterface;
import person.Person_ACCEPtPlusSingleInflection;
import population.Population_ACCEPtPlus;
import util.Classifier_ACCEPt;
import util.FileZipper;
import util.PersonClassifier;

public class Test_Population_ACCEPtPlus_OptSnapShot {

    static final String OPT_DIR_PATH = "C:\\Users\\Bhui\\Desktop\\VM_FTP\\ACCEPt\\OptResult";
    static final String OUTPUT_PATH = "C:\\Users\\bhui\\Desktop\\ACCEPt_OptRes";

    // Target Prevalence, from ACCEPt     
    static final float[] TARGET_PREVAL = new float[]{0.046f, 0.071f, 0.054f, 0.037f,
        0.090f, 0.081f, 0.038f, 0.013f};

    public static void main(String[] arg) throws FileNotFoundException, IOException, ClassNotFoundException {

        File optDir = new File(OPT_DIR_PATH);
        File outDir = new File(OUTPUT_PATH);
        outDir.mkdirs();

        File[] dirList = optDir.listFiles(new FileFilter() {
            @Override
            public boolean accept(File file) {
                return file.isDirectory();
            }
        });

        PersonClassifier prevalence_classifier = new Classifier_ACCEPt();

        Arrays.sort(dirList, new Comparator<File>() {
            @Override
            public int compare(File t, File t1) {
                return Long.compare(Long.parseLong(t.getName()),Long.parseLong(t1.getName()));
            }
        });

        System.out.println("Reading optimisation result from " + optDir.getAbsolutePath()
                + " Total # of result = " + dirList.length);

        PrintWriter outputWri
                = new PrintWriter(new File(outDir, Long.toString(System.currentTimeMillis()) + "_step.csv"));

        for (File importDir : dirList) {
            System.out.println("Reading pop file from " + importDir.getAbsolutePath());
            File[] popFiles = importDir.listFiles(new FileFilter() {
                @Override
                public boolean accept(File file) {
                    return file.getName().endsWith(".zip");
                }
            });

            float[][] prevalence = new float[popFiles.length][prevalence_classifier.numClass()];

            for (int filePt = 0; filePt < popFiles.length; filePt++) {
                File popFile = popFiles[filePt];
                System.out.print("Decoding " + popFile.getName() + "...");

                int[] num_indiv = new int[prevalence_classifier.numClass()]; // M 16-19, 20-24, 25-29 
                int[] num_infected = new int[num_indiv.length];

                Population_ACCEPtPlus pop;
                popFile = FileZipper.unzipFile(popFile, popFile.getParentFile());
                ObjectInputStream oIStream = new ObjectInputStream(new BufferedInputStream(new FileInputStream(popFile)));
                pop = Population_ACCEPtPlus.decodeFromStream(oIStream);
                oIStream.close();
                if (pop != null) {
                    popFile.delete();
                }
                AbstractIndividualInterface[] pArr = pop.getPop();

                for (AbstractIndividualInterface p : pArr) {
                    Person_ACCEPtPlusSingleInflection person = (Person_ACCEPtPlusSingleInflection) p;
                    int gI = prevalence_classifier.classifyPerson(p);

                    if (gI >= 0) {
                        num_indiv[gI]++;
                        if (person.getInfectionStatus()[0] != Person_ACCEPtPlusSingleInflection.INFECT_S) {
                            num_infected[gI]++;
                        }
                    }
                }

                System.out.println("done.");
                System.out.println("Num Indiv: " + Arrays.toString(num_indiv));
                System.out.println("Num Infect: " + Arrays.toString(num_infected));
                for (int gI = 0; gI < num_indiv.length; gI++) {
                    prevalence[filePt][gI] = ((float) num_infected[gI]) / num_indiv[gI];

                }
                System.out.println("Prevalence: " + Arrays.toString(prevalence[filePt]));

            }

            float[] summaryPreval = new float[prevalence_classifier.numClass()];
            float[] diffInPreval = new float[prevalence_classifier.numClass()];
            for (int filePt = 0; filePt < popFiles.length; filePt++) {
                for (int gI = 0; gI < summaryPreval.length; gI++) {
                    summaryPreval[gI] += prevalence[filePt][gI] / popFiles.length;
                    diffInPreval[gI] += (prevalence[filePt][gI] - TARGET_PREVAL[gI]) / popFiles.length;
                }
            }
            
            System.out.println("For " + importDir.getName());

            System.out.println("Average prevalence:");
            System.out.println(Arrays.toString(summaryPreval));

            System.out.println("Average difference:");
            System.out.println(Arrays.toString(diffInPreval));

            // Output file
            outputWri.print(importDir.getName());

            for (int gI = 0; gI < summaryPreval.length; gI++) {
                outputWri.print(',');
                outputWri.print(summaryPreval[gI]);
            }

            float sqCost = 0;
            for (int gI = 0; gI < diffInPreval.length; gI++) {
                outputWri.print(',');
                outputWri.print(diffInPreval[gI]);
                sqCost += diffInPreval[gI] * diffInPreval[gI];
            }
            outputWri.print(',');
            outputWri.print(sqCost);
            outputWri.println();
            outputWri.flush();

        }

        outputWri.close();

    }
}
