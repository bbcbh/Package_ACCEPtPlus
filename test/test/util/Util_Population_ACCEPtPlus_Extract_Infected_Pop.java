package test.util;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileFilter;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.PrintWriter;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import person.AbstractIndividualInterface;
import population.Population_ACCEPtPlus;
import population.Population_ACCEPtPlus_MixedBehaviour;
import util.FileZipper;

/**
 *
 * @author Bhui
 */
public class Util_Population_ACCEPtPlus_Extract_Infected_Pop {

    public static void main(String[] arg) throws IOException, ClassNotFoundException {
        File baseDir = new File("C:\\Users\\Bhui\\Desktop\\FTP\\ACCEPt\\Pop_Infection\\Baseline");
        File targetDir = new File("C:\\Users\\Bhui\\OneDrive - UNSW\\ACCEPt\\1000_Runs_NonExtinct");

        File[] popFiles = baseDir.listFiles(new FileFilter() {
            @Override
            public boolean accept(File file) {
                return file.getName().endsWith(".zip");
            }
        });

        targetDir.mkdirs();

        PrintWriter mappingInfoWri = new PrintWriter(new File(targetDir, "mapping.txt"));

        int validPop = 0;

        for (File popZip : popFiles) {
            Population_ACCEPtPlus pop = null;
            File tempFile = FileZipper.unzipFile(popZip, targetDir);
            ObjectInputStream oIStream = new ObjectInputStream(new BufferedInputStream(new FileInputStream(tempFile)));
            pop = Population_ACCEPtPlus_MixedBehaviour.decodeFromStream(oIStream);
            oIStream.close();

            boolean hasInfection = false;
            if (pop != null) {
                tempFile.delete();
                AbstractIndividualInterface[] people = pop.getPop();
                for (AbstractIndividualInterface person : people) {
                    for (int i = 0; i < person.getInfectionStatus().length && !hasInfection; i++) {
                        hasInfection |= person.getInfectionStatus()[i] != AbstractIndividualInterface.INFECT_S;
                    }
                    if (hasInfection) {
                        break;
                    }
                }
            }

            if (hasInfection) {
                Path targetPath = new File(targetDir, "pop_S" + validPop + "_T0.zip").toPath();
                Files.copy(popZip.toPath(), targetPath, StandardCopyOption.REPLACE_EXISTING);
                String info = "Copying " + popZip.getAbsolutePath() + " to " + targetPath.toString();
                mappingInfoWri.println(info);
                System.out.println(info);
                validPop++;
            }

        }

        mappingInfoWri.close();

        System.out.println("Extracting completed. " + validPop + " out of " + popFiles.length + " population copied");

    }

}
