package simulation;

import availability.AbstractAvailability;
import availability.Availability_ACCEPtPlus_SelectiveMixing;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.io.PrintStream;
import java.io.PrintWriter;
import java.io.StringWriter;
import population.Population_ACCEPtPlus;
import util.FileZipper;

/**
 *
 * @author Ben Hui
 */
public class Runnable_Population_ACCEPtPlus implements Runnable {

    protected Population_ACCEPtPlus population;
    protected Object[] runnableParam;

    public final int runnableId;
    public static final int RUNNABLE_SIM_DURATION = 0;
    public static final int RUNNABLE_EXPORT_PATH = 1;
    public static final int RUNNABLE_EXPORT_AT = 2;

    private final Object[] DEFAULT_RUNNABLE_PARAM = new Object[]{
        // 0: RUNNABLE_SIM_DURATION 
        // {# snapshots, frequency of snapshot
        new int[]{25 * 12, 30},
        // 1: RUNNABLE_EXPORT_PATH
        // Shared by all sim if length == 1
        new File[]{},
        // 2: RUNNABLE_EXPORT_AT
        // Time at which the popualtion is exported
        new int[]{},};
    private Object pop;
    protected PrintStream outputPrintStream = System.out;

    public int getRunnableId() {
        return runnableId;
    }

    public Runnable_Population_ACCEPtPlus(int runnableId) {
        this.runnableId = runnableId;
        runnableParam = DEFAULT_RUNNABLE_PARAM;

    }

    public Object[] getRunnableParam() {
        return runnableParam;
    }

    public Population_ACCEPtPlus getPopulation() {
        return population;
    }

    public void setPopulation(Population_ACCEPtPlus population) {
        this.population = population;
    }

    @Override
    public void run() {
        int[] exportAt = (int[]) runnableParam[RUNNABLE_EXPORT_AT];
        File[] exportPath = (File[]) runnableParam[RUNNABLE_EXPORT_PATH];
        int exportPt = 0;

        if (population.getAvailability() == null) {
            population.initialise();
            // Initalised partnership
            population.advanceTimeStep(1);
            AbstractAvailability[] avail = population.getAvailability();
            Integer mixType = 1;
            for (int i = 0; i < avail.length; i++) {
                avail[i] = new Availability_ACCEPtPlus_SelectiveMixing(population.getRNG());
                avail[i].setRelationshipMap(population.getRelMap()[i]);
                ((Availability_ACCEPtPlus_SelectiveMixing) avail[i]).setParameter("KEY_MATCH_TYPE", mixType);
            }
        }        

        for (int s = 0; s < ((int[]) getRunnableParam()[RUNNABLE_SIM_DURATION])[0]; s++) {
            for (int f = 0; f < ((int[]) getRunnableParam()[RUNNABLE_SIM_DURATION])[1]; f++) {
                population.advanceTimeStep(1);

                // Possible export
                if (exportPt < exportAt.length) {
                    if (population.getGlobalTime() == exportAt[exportPt]) {
                        File exportDir = exportPath[exportPt < exportPath.length ? exportPt : 0];
                        String popName = "pop_S" + runnableId + "_T" + exportPt;

                        File zipFile = new File(exportDir, popName + ".zip");
                        File tempFile = new File(exportDir, popName);

                        try (ObjectOutputStream outStream = new ObjectOutputStream(
                                new BufferedOutputStream(new FileOutputStream(tempFile)))) {
                            population.encodePopToStream(outStream);
                            outStream.close();
                            FileZipper.zipFile(tempFile, zipFile);
                        } catch (IOException ex) {
                            handleExceptions(ex);
                        }
                        outputPrintStream.println("File exported to " + zipFile.getAbsolutePath());
                        tempFile.delete();
                        exportPt++;
                    }
                }
            }
        }

    }

    protected void handleExceptions(Exception ex) {
        StringWriter errWri = new StringWriter();
        PrintWriter err = new PrintWriter(errWri);
        ex.printStackTrace(err);
        System.err.println(errWri.toString());
    }

    public void setOutputPrintStream(PrintStream outputPrint) {
        this.outputPrintStream = outputPrint;
    }

}
