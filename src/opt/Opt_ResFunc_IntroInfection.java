package opt;

import java.io.File;
import java.io.IOException;
import java.nio.file.FileVisitResult;
import java.nio.file.FileVisitor;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.Arrays;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import optimisation.AbstractResidualFunc;
import static opt.OptRun_Population_ACCEPtPlus_IntroInfection_Optimisation.TARGET_PREVAL;

/**
 * Residual function for optimisation
 *
 * @author Ben Hui
 * @version 20181024
 *
 */
public class Opt_ResFunc_IntroInfection extends AbstractResidualFunc {

    int numThreads;
    int[] simSelect;
    String importPath;
    String basePath;

    private final File[] OPT_RES_DIR_COLLECTION;
    private final double[] OPT_RES_SUM_SQS;
    private final boolean[] OPT_TARGET_PREVAL_SEL;
    private final double[] PRE_OPT_PARAM;

    public Opt_ResFunc_IntroInfection(int numThreads, int[] simSel,             
            String basePath,
            String importPath, 
            boolean[] OPT_TARGET_PREVAL_SEL, 
            double[] PRE_OPT_PARAM,
            File[] OPT_RES_DIR_COLLECTION, double[] OPT_RES_SUM_SQS) {

        this.numThreads = numThreads;
        this.simSelect = simSel;

        this.importPath = importPath;
        this.basePath = basePath;

        this.OPT_TARGET_PREVAL_SEL = OPT_TARGET_PREVAL_SEL;
        this.PRE_OPT_PARAM = PRE_OPT_PARAM;
        this.OPT_RES_DIR_COLLECTION = OPT_RES_DIR_COLLECTION;
        this.OPT_RES_SUM_SQS = OPT_RES_SUM_SQS;
    }

    @Override
    public double[] generateResidual(double[] param) {

        File optOutputDir = new File(basePath, Long.toString(System.currentTimeMillis()));

        if (OPT_RES_SUM_SQS.length > 0 && OPT_RES_SUM_SQS.length > 0) {

            // Skip until a new folder is generated.
            while (optOutputDir.exists()) {
                try {
                    Runtime.getRuntime().wait(1);
                } catch (InterruptedException ex) {
                }
                optOutputDir = new File(basePath, Long.toString(System.currentTimeMillis()));
            }

            optOutputDir.mkdirs();
        }

        double[] res = new double[TARGET_PREVAL.length];
        final boolean useParallel = numThreads > 1;

        ExecutorService executor = null;

        Future<float[]>[] outcomeCollection = new Future[simSelect.length];
        long timestamp = System.currentTimeMillis();

        for (int opt = 0; opt < simSelect.length; opt++) {
            if (useParallel && executor == null) {
                executor = Executors.newFixedThreadPool(numThreads);
            }

            Callable_Opt_Prevalence_IntroInfection runnable
                    = new Callable_Opt_Prevalence_IntroInfection(opt, 
                            simSelect[opt], timestamp,
                            new File(basePath),
                            new File(importPath), 
                            PRE_OPT_PARAM,
                            OPT_TARGET_PREVAL_SEL,                            
                            param);

            if (OPT_RES_DIR_COLLECTION.length == 0 || OPT_RES_SUM_SQS.length == 0) {
                runnable.setExportPopPath(new int[0], new File[0]); // Remove export if none are kept
            }

            if (useParallel) {

                Future<float[]> outcomeFuture = executor.submit(runnable);
                outcomeCollection[opt] = outcomeFuture;

            } else {
                try {
                    final float[] outcome = runnable.call();

                    outcomeCollection[opt] = new Future<float[]>() {
                        @Override
                        public boolean cancel(boolean bln) {
                            throw new UnsupportedOperationException("Not supported yet.");
                        }

                        @Override
                        public boolean isCancelled() {
                            return false;
                        }

                        @Override
                        public boolean isDone() {
                            return true;
                        }

                        @Override
                        public float[] get() throws InterruptedException, ExecutionException {
                            return outcome;
                        }

                        @Override
                        public float[] get(long l, TimeUnit tu) throws InterruptedException, ExecutionException, TimeoutException {
                            return outcome;
                        }
                    };
                } catch (Exception ex) {
                    ex.printStackTrace();
                }
            }
        }

        if (useParallel) {
            try {
                executor.shutdown();
                if (!executor.awaitTermination(2, TimeUnit.DAYS)) {
                    System.out.println("Inf Thread time-out!");
                }
            } catch (InterruptedException ex) {
                ex.printStackTrace(System.err);
            }
            executor = null;
        }

        double[][] resAll = new double[res.length][simSelect.length];

        for (int opt = 0; opt < simSelect.length; opt++) {
            try {
                float[] outcome = outcomeCollection[opt].get();
                for (int r = 0; r < res.length; r++) {
                    //res[r] += outcome[r] / popSelectIndex.length;                    
                    resAll[r][opt] = outcome[r];

                }
            } catch (InterruptedException | ExecutionException ex) {
                ex.printStackTrace(System.err);
            }
        }

        org.apache.commons.math3.stat.descriptive.rank.Percentile per
                = new org.apache.commons.math3.stat.descriptive.rank.Percentile();

        for (int r = 0; r < res.length; r++) {
            Arrays.sort(resAll[r]);
            double median, q1, q3;

            median = per.evaluate(resAll[r], 0.5);
            q1 = per.evaluate(resAll[r], 0.25);
            q3 = per.evaluate(resAll[r], 0.75);

            /*
            // Use median 
            if (resAll.length % 2 == 1) {
                median = resAll[r][(popSelectIndex.length - 1) / 2];
            } else {
                median = (resAll[r][(popSelectIndex.length - 1) / 2] + resAll[r][(popSelectIndex.length - 1) / 2 + 1]) / 2;
            }
             */
            res[r] = Math.abs(q1) + Math.abs(median) + Math.abs(q3);
        }

        // Check if need to store this result
        if (OPT_RES_SUM_SQS.length > 0 && OPT_RES_SUM_SQS.length > 0) {

            double resTotal = 0;
            File toBeClearDir = optOutputDir;

            for (int r = 0; r < res.length; r++) {
                resTotal += res[r] * res[r];
            }

            int insertIndex = Arrays.binarySearch(OPT_RES_SUM_SQS, resTotal);

            if (insertIndex < 0) {
                insertIndex = -(insertIndex + 1);
                if (insertIndex < OPT_RES_SUM_SQS.length) {
                    toBeClearDir = OPT_RES_DIR_COLLECTION[OPT_RES_DIR_COLLECTION.length - 1];
                    for (int kr = OPT_RES_SUM_SQS.length - 1; kr > insertIndex; kr--) {
                        OPT_RES_SUM_SQS[kr] = OPT_RES_SUM_SQS[kr - 1];
                        OPT_RES_DIR_COLLECTION[kr] = OPT_RES_DIR_COLLECTION[kr - 1];
                    }
                    OPT_RES_SUM_SQS[insertIndex] = resTotal;
                    OPT_RES_DIR_COLLECTION[insertIndex] = optOutputDir;
                }
            }

            if (toBeClearDir != null) {
                try {
                    Files.walkFileTree(toBeClearDir.toPath(), new FileVisitor<Path>() {
                        @Override
                        public FileVisitResult preVisitDirectory(Path t, BasicFileAttributes bfa) throws IOException {
                            return FileVisitResult.CONTINUE;
                        }

                        @Override
                        public FileVisitResult visitFile(Path t, BasicFileAttributes bfa) throws IOException {
                            Files.delete(t);
                            return FileVisitResult.CONTINUE;
                        }

                        @Override
                        public FileVisitResult visitFileFailed(Path t, IOException ioe) throws IOException {
                            return FileVisitResult.CONTINUE;
                        }

                        @Override
                        public FileVisitResult postVisitDirectory(Path t, IOException ioe) throws IOException {
                            Files.delete(t);
                            return FileVisitResult.CONTINUE;
                        }
                    });
                } catch (IOException ex) {
                    ex.printStackTrace(System.err);
                }
            }
        }

        return res;

    }

}
