/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package test;

import java.io.File;
import java.io.FileFilter;
import java.io.IOException;
import java.nio.file.CopyOption;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.Arrays;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 *
 * @author bhui
 */
public class Util_Population_ACCEPtPlus_CleanOutputs {

    public static void main(String[] arg) throws IOException {
        File collectionDir = new File("C:\\Users\\Bhui\\Desktop\\VM_FTP\\1000_Runs\\PartTreat_MIN");
        File newPopDir = null;

        Pattern outputPattern = Pattern.compile("output_sim_(\\d+).txt");

        File[] outputFiles = collectionDir.listFiles(new FileFilter() {
            @Override
            public boolean accept(File file) {
                return file.getName().endsWith(".txt");
            }
        });

        int[] ids = new int[outputFiles.length];

        for (int i = 0; i < ids.length; i++) {
            String fileName = outputFiles[i].getName();
            Matcher m = outputPattern.matcher(fileName);

            if (m.matches()) {
                ids[i] = Integer.parseInt(m.group(1));
            }
        }

        Arrays.sort(ids);

        int max_id = ids[ids.length - 1];

        File tobeRemove, popFileInCollection;
        int fileRemoved = 0;

        for (int i = 0; i <= max_id; i++) {
            popFileInCollection = new File(collectionDir, "pop_S" + i + "_T0.zip");
            if (!popFileInCollection.exists()) {
                System.out.println("Population data for #" + i + " not found. Deleting associated files");
                tobeRemove = new File(collectionDir, "output_sim_" + i + ".txt");
                if (tobeRemove.exists()) {
                    tobeRemove.delete();
                    fileRemoved++;
                }
                tobeRemove = new File(collectionDir, "testing_history_" + i + ".obj");
                if (tobeRemove.exists()) {
                    tobeRemove.delete();
                    fileRemoved++;
                }

            }
        }

        System.out.println("File Removed = " + fileRemoved);

        if (newPopDir != null) {
            File[] popZipsToBeAdded = newPopDir.listFiles(new FileFilter() {
                @Override
                public boolean accept(File file) {
                    return file.getName().endsWith(".zip");
                }
            });

            Pattern popPattern = Pattern.compile("pop_S(\\d+)_T0.zip");

            int fileAdded = 0;

            for (int i = 0; i < popZipsToBeAdded.length; i++) {
                String fileName = popZipsToBeAdded[i].getName();
                Matcher m = popPattern.matcher(fileName);

                if (m.matches()) {
                    int addId = Integer.parseInt(m.group(1));
                    popFileInCollection = new File(collectionDir, "pop_S" + addId + "_T0.zip");

                    if (!popFileInCollection.exists()) {
                        Path src, tar;
                        System.out.println("Adding oopulation data #" + addId + " to " + collectionDir.getAbsolutePath());

                        src = popZipsToBeAdded[i].toPath();
                        tar = popFileInCollection.toPath();
                        Files.copy(src, tar, StandardCopyOption.REPLACE_EXISTING);
                        fileAdded++;

                        src = new File(newPopDir, "output_sim_" + addId + ".txt").toPath();
                        tar = new File(collectionDir, "output_sim_" + addId + ".txt").toPath();
                        Files.copy(src, tar, StandardCopyOption.REPLACE_EXISTING);
                        fileAdded++;

                        src = new File(newPopDir, "testing_history_" + addId + ".obj").toPath();
                        tar = new File(collectionDir, "testing_history_" + addId + ".obj").toPath();
                        Files.copy(src, tar, StandardCopyOption.REPLACE_EXISTING);
                        fileAdded++;

                    }
                }

            }

            System.out.println("File Added = " + fileAdded);

        }

    }

}
