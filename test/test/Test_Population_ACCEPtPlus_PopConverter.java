package test;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileFilter;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import population.Population_ACCEPtPlus;
import random.MersenneTwisterFastRandomGenerator;
import util.FileZipper;

/**
 *
 * @author bhui
 */
public class Test_Population_ACCEPtPlus_PopConverter {

    public static void main(String[] arg) throws IOException, ClassNotFoundException, InterruptedException {
        final long BASE_SEED = 2251912970037127827l;
        MersenneTwisterFastRandomGenerator rng = new MersenneTwisterFastRandomGenerator(BASE_SEED);

        File importDir = new File("C:\\Users\\Bhui\\OneDrive - UNSW\\ACCEPt\\ImportDir_Org");
        File exportDir = new File("C:\\Users\\Bhui\\OneDrive - UNSW\\ACCEPt\\ImportDir");

        final int NUM_THREADS = Runtime.getRuntime().availableProcessors();

        exportDir.mkdirs();

        File[] popFiles = importDir.listFiles(new FileFilter() {
            @Override
            public boolean accept(File file) {
                return file.getName().endsWith(".zip");
            }
        });

        System.out.println("Number of population = " + popFiles.length);

        ExecutorService executor = null;
        executor = Executors.newFixedThreadPool(NUM_THREADS);

        for (File f : popFiles) {
            Thread_convertPop thread = new Thread_convertPop(exportDir, f);
            executor.submit(thread);
        }

        executor.shutdown();
        if (!executor.awaitTermination(2, TimeUnit.DAYS)) {
            System.out.println("Thread time-out!");
        }

    }

    private static class Thread_convertPop implements Runnable {

        File exportDir;
        File srcFile;

        public Thread_convertPop(File exportDir, File popFile) {
            this.exportDir = exportDir;
            this.srcFile = popFile;
        }

        @Override
        public void run() {
            try {
                Population_ACCEPtPlus pop;
                
                System.out.println("Pop imported from " + srcFile.getAbsolutePath());

                File targetFile = new File(exportDir, srcFile.getName());
                File popFile = FileZipper.unzipFile(srcFile, exportDir);
                String popName = srcFile.getName().split("\\.")[0];
                File rawPop = new File(exportDir, popName);

                ObjectInputStream oIStream = new ObjectInputStream(new BufferedInputStream(new FileInputStream(popFile)));
                pop = Population_ACCEPtPlus.decodeFromStream(oIStream);
                oIStream.close();

                for (int i = 0; i < 20 * Population_ACCEPtPlus.ONE_YEAR_INT; i++) {
                    pop.advanceTimeStep(1);
                }

                ObjectOutputStream oOStream = new ObjectOutputStream(new BufferedOutputStream(new FileOutputStream(rawPop)));
                pop.encodePopToStream(oOStream);
                oOStream.close();
                FileZipper.zipFile(rawPop, targetFile);
                rawPop.deleteOnExit();
                
                System.out.println("Pop exported to " + targetFile.getAbsolutePath());
            } catch (IOException | ClassNotFoundException ex) {
                ex.printStackTrace();
            }
        }

    }

}
