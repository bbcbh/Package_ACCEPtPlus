package sim;

import infection.TreatableInfectionInterface;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.concurrent.ConcurrentHashMap;
import person.AbstractIndividualInterface;
import person.Person_ACCEPtPlusSingleInflection;
import random.MersenneTwisterFastRandomGenerator;
import random.RandomGenerator;
import relationship.RelationshipMap;
import relationship.SingleRelationship;
import static sim.Runnable_Population_ACCEPtPlus.RUNNABLE_EXPORT_AT;
import static sim.Runnable_Population_ACCEPtPlus.RUNNABLE_EXPORT_PATH;
import static sim.Runnable_Population_ACCEPtPlus.RUNNABLE_SIM_DURATION;
import util.AppendableObjOutstream;
import util.ArrayUtilsRandomGenerator;
import util.Classifier_ACCEPt;
import util.FileZipper;
import util.PersonClassifier;

public class Runnable_Population_ACCEPtPlus_Infection extends Runnable_Population_ACCEPtPlus {

    public static final PersonClassifier ACCEPT_CLASSIFIER = new Classifier_ACCEPt();

    public static final int RUNNABLE_INFECTION_TESTING_COVERAGE = RUNNABLE_EXPORT_AT + 1;
    public static final int RUNNABLE_INFECTION_TESTING_CLASSIFIER = RUNNABLE_INFECTION_TESTING_COVERAGE + 1;
    public static final int RUNNABLE_INFECTION_INTRO_RATE = RUNNABLE_INFECTION_TESTING_CLASSIFIER + 1;
    public static final int RUNNABLE_INFECTION_INTRO_CLASSIFIER = RUNNABLE_INFECTION_INTRO_RATE + 1;
    public static final int RUNNABLE_INFECTION_RETEST_RATE = RUNNABLE_INFECTION_INTRO_CLASSIFIER + 1;
    public static final int RUNNABLE_INFECTION_RETEST_CLASSIFIER = RUNNABLE_INFECTION_RETEST_RATE + 1;
    public static final int RUNNABLE_INFECTION_PARTNER_TREATMENT_RATE = RUNNABLE_INFECTION_RETEST_CLASSIFIER + 1;
    public static final int RUNNABLE_INFECTION_PARTNER_TREATMENT_CLASSIFIER = RUNNABLE_INFECTION_PARTNER_TREATMENT_RATE + 1;
    public static final int RUNNABLE_INFECTION_TEST_SENSITIVITY = RUNNABLE_INFECTION_PARTNER_TREATMENT_CLASSIFIER + 1;
    public static final int RUNNABLE_INFECTION_CONTINUE_TEST_30_PLUS_CLASSIFIER = RUNNABLE_INFECTION_TEST_SENSITIVITY + 1;
    public static final int RUNNABLE_INFECTION_CONTINUE_TEST_30_PLUS_RATE = RUNNABLE_INFECTION_CONTINUE_TEST_30_PLUS_CLASSIFIER + 1;

    public static final int RUNNABLE_INFECTION_MASS_SCREENING_SETTING = RUNNABLE_INFECTION_CONTINUE_TEST_30_PLUS_RATE + 1;

    public static final Object[] DEFAULT_RUNNABLE_PARAM_INFECTION = {
        // 2: RUNNABLE_INFECTION_TESTING_COVERAGE
        // Annual testing, for male, female,    - From meeting note page 40
        new float[]{0.072f, 0.169f},
        // 3: RUNNABLE_INFECTION_TESTING_CLASSIFIER     
        new PersonClassifier() {
            @Override
            public int classifyPerson(AbstractIndividualInterface p) {
                Person_ACCEPtPlusSingleInflection person = (Person_ACCEPtPlusSingleInflection) p;

                if (p.getAge() >= 16 * AbstractIndividualInterface.ONE_YEAR_INT
                        && p.getAge() < 29 * AbstractIndividualInterface.ONE_YEAR_INT
                        && person.getPartnerHistoryLifetimePt() > 0) {
                    return p.isMale() ? 1 : 2;
                } else if (p.getAge() >= 16 * AbstractIndividualInterface.ONE_YEAR_INT
                        && p.getAge() < 29 * AbstractIndividualInterface.ONE_YEAR_INT
                        && person.getPartnerHistoryLifetimePt() > 0) {
                    // Has at least one partner                                                                                
                    return -(p.isMale() ? 1 : 2);
                } else {
                    return 0;
                }
            }

            @Override
            public int numClass() {
                return 2; // Inital, annual
            }
        },
        // 4: RUNNABLE_INFECTION_INTRO_RATE
        // Assumed 2% (or, 40% of 5%) and 4% (or, 40% of 9%) of individual in this model 
        // will already be infected with chlamydia on his or her 16th birthday respectively.
        new float[]{0.02f, 0.04f},
        // 5: RUNNABLE_INFECTION_INTRO_CLASSIFIER
        new PersonClassifier() {
            @Override
            public int classifyPerson(AbstractIndividualInterface p) {
                if (p.getAge() == 16 * AbstractIndividualInterface.ONE_YEAR_INT) {
                    return p.isMale() ? 0 : 1;
                } else {
                    return -1;
                }
            }

            @Override
            public int numClass() {
                return 2;
            }
        },
        //5: RUNNABLE_INFECTION_RETEST_RATE 
        // Format: float[classId][][]{number of days up to one year,probability}
        // From email 20171019 TC Summary
        new float[][][]{
            new float[][]{
                new float[]{3 * 30}, new float[]{0.236f}
            },},
        //6: RUNNABLE_INFECTION_RETEST_CLASSIFIER    
        // Everyone that is positive
        new PersonClassifier() {
            @Override
            public int classifyPerson(AbstractIndividualInterface p) {
                return p.getInfectionStatus()[0] != AbstractIndividualInterface.INFECT_S ? 0 : -1;
            }

            @Override
            public int numClass() {
                return 1;
            }
        },
        //7: RUNNABLE_INFECTION_PARTNER_TREATMENT_RATE
        // Format: float[classID] or float[classId][rate_default, time_1, rate_since_time_1....]
        // From email 20171019 TC Summary
        new float[]{0.3f},
        // From Jane's email 20160826       
        //new float[]{0.828f * 0.035f},
        //8: RUNNABLE_INFECTION_PARTNER_TREATMENT_CLASSIFIER
        new PersonClassifier() {
            @Override
            public int classifyPerson(AbstractIndividualInterface p) {
                if (p.getAge() >= 16 * AbstractIndividualInterface.ONE_YEAR_INT
                        && p.getAge() < 30 * AbstractIndividualInterface.ONE_YEAR_INT) {
                    return 0;
                }
                return -1;
            }

            @Override
            public int numClass() {
                return 1;
            }
        },
        // 9: RUNNABLE_TEST_SENSITIVITY
        1f,
        // 10: RUNNABLE_INFECTION_CONTINUE_TEST_30_PLUS_CLASSIFIER
        new PersonClassifier() {

            @Override
            public int classifyPerson(AbstractIndividualInterface p) {
                int aI = -1;
                if (p.getAge() < 30 * AbstractIndividualInterface.ONE_YEAR_INT) {
                    return -1;
                } else {
                    if (p.getAge() >= 30 * AbstractIndividualInterface.ONE_YEAR_INT) {
                        aI++;
                        if (p.getAge() >= 40 * AbstractIndividualInterface.ONE_YEAR_INT) {
                            aI++;
                            if (p.getAge() >= 50 * AbstractIndividualInterface.ONE_YEAR_INT) {
                                aI++;
                            }
                        }
                    }
                }

                return aI;
            }

            @Override
            public int numClass() {
                return 3;
            }

        },
        // 11: RUNNABLE_INFECTION_CONTINUE_TEST_30_PLUS_RATE
        new float[]{1f, 1f, 1f},
        // 12: RUNNABLE_INFECTION_MASS_SCREENING_SETTING
        // Object[]{ PersonClassifier, treatment_rate_by_classIndex, int[][]{[at, duration]... } 
        null,};

    protected ConcurrentHashMap<Integer, int[]> testingHistory = null;
    private final int DEFAULT_HIST_LENG = HIST_OFFSET_TOTAL_LENGTH * 5;

    // Format 
    // Key - Person Id
    // Value - int[], with {nextPt, gender, age at history entry, 
    //                       Test time at test 1, Age at test 1, outcome 1, Test type, ...}
    public static final int HIST_HEADER_NEXT_PT = 0;
    public static final int HIST_HEADER_GENDER = HIST_HEADER_NEXT_PT + 1;
    public static final int HIST_HEADER_AGE_AT_ENTRY = HIST_HEADER_GENDER + 1;
    public static final int HIST_HEADER_TOTAL_LENGTH = HIST_HEADER_AGE_AT_ENTRY + 1;

    public static final int HIST_OFFSET_TEST_TIME = 0;
    public static final int HIST_OFFSET_TEST_AGE = HIST_OFFSET_TEST_TIME + 1;
    public static final int HIST_OFFSET_TEST_INFECTED = HIST_OFFSET_TEST_AGE + 1;
    public static final int HIST_OFFSET_TEST_TREATMENT = HIST_OFFSET_TEST_INFECTED + 1;
    public static final int HIST_OFFSET_TEST_TYPE = HIST_OFFSET_TEST_TREATMENT + 1;
    public static final int HIST_OFFSET_TEST_CURRENT_INFECTION_FROM_AGE = HIST_OFFSET_TEST_TYPE + 1;
    public static final int HIST_OFFSET_TOTAL_LENGTH = HIST_OFFSET_TEST_CURRENT_INFECTION_FROM_AGE + 1;

    public static final int TEST_TYPE_ANNUAL = 0;
    public static final int TEST_TYPE_RETEST = 1;
    public static final int TEST_TYPE_PARTNER_TEST = 2;
    public static final int TEST_TYPE_MASS_SCREEN = 3;

    protected int prevalStoreFreq = -1;
    protected File prevalStorePath = null;

    public static final int PREVAL_STORE_GLOBAL_TIME = 0;
    public static final int PREVAL_STORE_PERSON_ID = PREVAL_STORE_GLOBAL_TIME + 1;
    public static final int PREVAL_STORE_GENDER = PREVAL_STORE_PERSON_ID + 1;
    public static final int PREVAL_STORE_AGE = PREVAL_STORE_GENDER + 1;
    public static final int PREVAL_STORE_NUM_LIFETIME_PARTNERS = PREVAL_STORE_AGE + 1;
    public static final int PREVAL_STORE_INFECT_STATUS = PREVAL_STORE_NUM_LIFETIME_PARTNERS + 1;
    public static final int PREVAL_STORE_TOTAL_LENGTH = PREVAL_STORE_INFECT_STATUS + 1;

    public void setPrevalStoreFreq(int prevalStoreFreq, File prevalStorePath) {
        this.prevalStoreFreq = prevalStoreFreq;
        this.prevalStorePath = prevalStorePath;
    }

    public ConcurrentHashMap<Integer, int[]> getTestingHistory() {
        return testingHistory;
    }

    public void setTestingHistory(ConcurrentHashMap<Integer, int[]> testingHistory) {
        this.testingHistory = testingHistory;
    }

    public Runnable_Population_ACCEPtPlus_Infection(int runnableId) {
        super(runnableId);
        int org_len = runnableParam.length;
        runnableParam = Arrays.copyOf(runnableParam, runnableParam.length + DEFAULT_RUNNABLE_PARAM_INFECTION.length);
        System.arraycopy(DEFAULT_RUNNABLE_PARAM_INFECTION, 0, runnableParam, org_len, DEFAULT_RUNNABLE_PARAM_INFECTION.length);

    }

    // 
    @Override
    public void run() {
        int[] exportAt = (int[]) runnableParam[RUNNABLE_EXPORT_AT];
        File[] exportPath = (File[]) runnableParam[RUNNABLE_EXPORT_PATH];
        int exportPt = 0;
        RandomGenerator testRNG = new MersenneTwisterFastRandomGenerator(population.getSeed());

        // Annual test
        PersonClassifier TEST_CLASSIFIER = (PersonClassifier) runnableParam[RUNNABLE_INFECTION_TESTING_CLASSIFIER];
        float[] TEST_COVERAGE = (float[]) runnableParam[RUNNABLE_INFECTION_TESTING_COVERAGE];

        AbstractIndividualInterface[][] testSchedule = new AbstractIndividualInterface[TEST_CLASSIFIER.numClass()][];
        int[] testPt = new int[TEST_CLASSIFIER.numClass()];
        int[] minTestPerDay = new int[TEST_CLASSIFIER.numClass()];
        int[] extraTest = new int[TEST_CLASSIFIER.numClass()];

        int offset = population.getGlobalTime();

        // Mass screening                 
        Object[] massSrnSetting = (Object[]) runnableParam[RUNNABLE_INFECTION_MASS_SCREENING_SETTING];
        PersonClassifier msc = null;
        float[] rateByClass = null;
        int[][] startAt = null;
        int[] startAtPointer = null;
        AbstractIndividualInterface[][] massSrnSchedule = null;
        int[][] massSrnScheduleAt = null;
        int[] massSrnSchedulePt = null;

        if (massSrnSetting != null) {
            msc = (PersonClassifier) massSrnSetting[0];
            rateByClass = (float[]) massSrnSetting[1];
            startAt = (int[][]) massSrnSetting[2];
            startAtPointer = new int[msc.numClass()];
            massSrnSchedule = new AbstractIndividualInterface[msc.numClass()][];
            massSrnScheduleAt = new int[msc.numClass()][];
            massSrnSchedulePt = new int[msc.numClass()];
        }

        if (testingHistory != null) {

            // Pre-allocate testing cohort
            for (AbstractIndividualInterface p : population.getPop()) {
                if (p.getAge() >= 16 * AbstractIndividualInterface.ONE_YEAR_INT
                        && p.getAge() < 17 * AbstractIndividualInterface.ONE_YEAR_INT) {

                    int[] entry = new int[HIST_HEADER_TOTAL_LENGTH + DEFAULT_HIST_LENG];
                    entry[HIST_HEADER_NEXT_PT] = HIST_HEADER_TOTAL_LENGTH;
                    entry[HIST_HEADER_GENDER] = p.isMale() ? 1 : 0;
                    entry[HIST_HEADER_AGE_AT_ENTRY] = (int) p.getAge();
                    testingHistory.put(p.getId(), entry);

                }
            }

        }

        if (prevalStoreFreq > 0) {
            File zipStore = new File(prevalStorePath.getParentFile(), prevalStorePath.getName() + ".zip");
            if (zipStore.exists()) {
                if (population.getGlobalTime() == offset) {
                    outputPrintStream.println(population.getGlobalTime()
                            + ": Removing old preval store at " + zipStore.getAbsolutePath());                   
                    zipStore.delete();
                }
            }

        }

        for (int s = 0; s < ((int[]) getRunnableParam()[RUNNABLE_SIM_DURATION])[0]; s++) {
            for (int f = 0; f < ((int[]) getRunnableParam()[RUNNABLE_SIM_DURATION])[1]; f++) {

                if ((population.getGlobalTime() - offset) % AbstractIndividualInterface.ONE_YEAR_INT == 0) {
                    // Determine test schedule

                    Arrays.fill(testPt, 0);
                    int[] counter = new int[TEST_CLASSIFIER.numClass()];
                    int[] testTotal = new int[counter.length];

                    ArrayList<AbstractIndividualInterface> testSrc = new ArrayList<>();

                    for (AbstractIndividualInterface p : population.getPop()) {
                        int cI = TEST_CLASSIFIER.classifyPerson(p);

                        if (population.getGlobalTime() == offset) {
                            cI = Math.abs(cI);    // Inital version                                                    
                        }
                        if (cI > 0) {
                            counter[cI - 1]++;
                            testSrc.add(p);
                        }
                    }

                    for (int c = 0; c < TEST_CLASSIFIER.numClass(); c++) {
                        testTotal[c] = Math.round(counter[c] * TEST_COVERAGE[c]);
                        testSchedule[c] = new AbstractIndividualInterface[testTotal[c]];
                    }

                    for (AbstractIndividualInterface p : testSrc) {
                        int cI = TEST_CLASSIFIER.classifyPerson(p);

                        if (population.getGlobalTime() == offset) {
                            cI = Math.abs(cI);    // Inital version                                                    
                        }
                        cI--; // Offset by one

                        if (testTotal[cI] > 0
                                && testRNG.nextInt(counter[cI]) < testTotal[cI]) {
                            testSchedule[cI][testSchedule[cI].length - testTotal[cI]] = p;
                            testTotal[cI]--;
                        }
                        counter[cI]--;
                    }

                    for (int c = 0; c < TEST_CLASSIFIER.numClass(); c++) {
                        ArrayUtilsRandomGenerator.shuffleArray(testSchedule[c], testRNG);
                        minTestPerDay[c] = testSchedule[c].length / AbstractIndividualInterface.ONE_YEAR_INT;
                        extraTest[c] = testSchedule[c].length - minTestPerDay[c] * AbstractIndividualInterface.ONE_YEAR_INT;
                    }
                }

                // Determine mass screening schedule
                if (msc != null) {
                    boolean[] newMassSrnSchedule = new boolean[msc.numClass()];
                    boolean needSchdule = false;

                    for (int cI = 0; cI < startAtPointer.length; cI++) {
                        newMassSrnSchedule[cI] = startAtPointer[cI] < startAt[cI].length
                                && startAt[cI][startAtPointer[cI]] == (population.getGlobalTime() - offset) + 1; // +1 as screening took place after advance time step
                        needSchdule |= newMassSrnSchedule[cI];

                    }
                    if (needSchdule) {
                        ArrayList<AbstractIndividualInterface>[] testSrc = new ArrayList[msc.numClass()];

                        for (AbstractIndividualInterface p : population.getPop()) {
                            int cI = msc.classifyPerson(p);
                            if (cI >= 0) {
                                if (testSrc[cI] == null) {
                                    testSrc[cI] = new ArrayList<>();
                                }
                                testSrc[cI].add(p);
                            }
                        }
                        for (int cI = 0; cI < startAtPointer.length; cI++) {
                            if (newMassSrnSchedule[cI]) {
                                massSrnSchedule[cI] = testSrc[cI].toArray(new AbstractIndividualInterface[testSrc[cI].size()]);
                                ArrayUtilsRandomGenerator.shuffleArray(massSrnSchedule[cI], testRNG);
                                int numInSchedule = Math.round(rateByClass[cI] * massSrnSchedule[cI].length);
                                massSrnSchedule[cI] = Arrays.copyOf(massSrnSchedule[cI], numInSchedule);
                                massSrnScheduleAt[cI] = new int[massSrnSchedule[cI].length];
                                int period = startAt[cI][startAtPointer[cI] + 1];
                                for (int k = 0; k < massSrnScheduleAt[cI].length; k++) {
                                    massSrnScheduleAt[cI][k] = population.getGlobalTime() + 1 + testRNG.nextInt(period);
                                }
                                Arrays.sort(massSrnScheduleAt[cI]);
                                massSrnSchedulePt[cI] = 0;

                                startAtPointer[cI] += 2;
                            }
                        }
                    }

                }

                PersonClassifier introClassifier = (PersonClassifier) runnableParam[RUNNABLE_INFECTION_INTRO_CLASSIFIER];
                float[] introRate = (float[]) runnableParam[RUNNABLE_INFECTION_INTRO_RATE];

                if (introClassifier != null) {
                    //System.out.println(this.getRunnableId() +  ": Instant infection at " + population.getGlobalTime());
                    population.setInstantInfection(0, introClassifier, introRate, 296);
                }

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

                // Scheduled annual test
                for (int c = 0; c < TEST_CLASSIFIER.numClass(); c++) {
                    int testToday = minTestPerDay[c];
                    if (extraTest[c] > 0) {
                        int keyDiff = AbstractIndividualInterface.ONE_YEAR_INT
                                - ((population.getGlobalTime() - offset) % AbstractIndividualInterface.ONE_YEAR_INT) + 1;
                        if (testRNG.nextInt(keyDiff) < extraTest[c]) {
                            testToday++;
                            extraTest[c]--;
                        }
                    }
                    if (testToday > 0) {
                        for (int i = 0; i < testToday; i++) {
                            Person_ACCEPtPlusSingleInflection p_tested = (Person_ACCEPtPlusSingleInflection) testSchedule[c][testPt[c]];

                            testPerson(p_tested, TEST_TYPE_ANNUAL, testRNG, offset);
                            testPt[c]++;
                        }
                    }
                }

                // Retesting
                for (AbstractIndividualInterface p : population.getPop()) {
                    Person_ACCEPtPlusSingleInflection person = (Person_ACCEPtPlusSingleInflection) p;
                    if (person.getParameter(Person_ACCEPtPlusSingleInflection.PERSON_RETEST_AT_AGE) == person.getAge()) {
                        testPerson(person, TEST_TYPE_RETEST, testRNG, offset);
                    }
                }

                // Mass screening if any
                if (msc != null) {
                    for (int cI = 0; cI < msc.numClass(); cI++) {
                        if (massSrnSchedule[cI] != null) {
                            while (massSrnSchedulePt[cI] < massSrnSchedule[cI].length
                                    && massSrnScheduleAt[cI][massSrnSchedulePt[cI]] == population.getGlobalTime()) {
                                Person_ACCEPtPlusSingleInflection person = (Person_ACCEPtPlusSingleInflection) massSrnSchedule[cI][massSrnSchedulePt[cI]];
                                testPerson(person, TEST_TYPE_MASS_SCREEN, testRNG, offset);
                                massSrnSchedulePt[cI]++;
                            }

                        }
                    }

                }

                // Export prevalence if needed 
                if (prevalStoreFreq > 0) {
                    if ((population.getGlobalTime() - offset) % prevalStoreFreq == 0) {
                        try {

                            File zipStore = new File(prevalStorePath.getParentFile(), prevalStorePath.getName() + ".zip");

                            if (zipStore.exists()) {

                                File tempFile = FileZipper.unzipFile(zipStore, prevalStorePath.getParentFile());
                                tempFile.renameTo(prevalStorePath);
                                zipStore.delete();

                            }

                            ObjectOutputStream outStream = AppendableObjOutstream.generateFromFile(prevalStorePath);

                            for (AbstractIndividualInterface person : population.getPop()) {
                                int[] entry = new int[PREVAL_STORE_TOTAL_LENGTH];
                                entry[PREVAL_STORE_GLOBAL_TIME] = population.getGlobalTime();
                                entry[PREVAL_STORE_PERSON_ID] = person.getId();
                                entry[PREVAL_STORE_GENDER] = person.isMale() ? 0 : 1;
                                entry[PREVAL_STORE_AGE] = (int) person.getAge();
                                entry[PREVAL_STORE_NUM_LIFETIME_PARTNERS] = ((Person_ACCEPtPlusSingleInflection) person).getPartnerHistoryLifetimePt();
                                entry[PREVAL_STORE_INFECT_STATUS] = person.getInfectionStatus()[0];
                                outStream.writeObject(entry);
                            }

                            outStream.flush();
                            outStream.close();

                            FileZipper.zipFile(prevalStorePath, zipStore);
                            prevalStorePath.delete();

                        } catch (IOException ex) {
                            ex.printStackTrace(outputPrintStream);
                        }
                    }
                }

                if (population.getGlobalTime() % 30 == 0) {

                    int[] total = new int[ACCEPT_CLASSIFIER.numClass()]; // 16-17, 18-20, 21-24, 25-29 
                    int[] infect = new int[ACCEPT_CLASSIFIER.numClass()];

                    for (AbstractIndividualInterface p : population.getPop()) {

                        int index = ACCEPT_CLASSIFIER.classifyPerson(p);

                        if (index != -1) {
                            total[index]++;

                            if (p.getInfectionStatus()[0] != AbstractIndividualInterface.INFECT_S) {
                                infect[index]++;
                            }
                        }
                    }

                    StringBuilder inf_output = new StringBuilder();

                    for (int i = 0; i < total.length; i++) {
                        if (i == 0) {
                            inf_output.append(population.getGlobalTime());
                            inf_output.append(",");
                        } else {
                            inf_output.append(',');
                        }
                        inf_output.append(((float) infect[i]) / total[i]);
                    }
                    outputPrintStream.println(inf_output.toString());
                }
            }
        }

    }

    public void testPerson(Person_ACCEPtPlusSingleInflection p_tested, int testType,
            RandomGenerator testRNG, int pop_time_offset) {

        // Check for retesting
        PersonClassifier RETEST_CLASSIFIER = (PersonClassifier) runnableParam[RUNNABLE_INFECTION_RETEST_CLASSIFIER];
        float[][][] RETEST_RATE = (float[][][]) runnableParam[RUNNABLE_INFECTION_RETEST_RATE];

        // See if retested within a year or so
        if (RETEST_CLASSIFIER != null) {
            int reTestI = RETEST_CLASSIFIER.classifyPerson(p_tested);
            if (reTestI >= 0) {
                float[] retest_date = RETEST_RATE[reTestI][0];
                float[] retest_prob = RETEST_RATE[reTestI][1];
                int index = retest_prob.length;

                float p = testRNG.nextFloat();

                while (index > 0 && retest_prob[index - 1] > p) {
                    index--;
                }

                if (index < retest_prob.length) {
                    int maxRetestDate = (int) retest_date[index];
                    int minRetestDate = 0;
                    if (index > 0) {
                        minRetestDate = (int) retest_date[index - 1];
                    }
                    int retestDate = minRetestDate + testRNG.nextInt(maxRetestDate - minRetestDate);
                    p_tested.setParameter(Person_ACCEPtPlusSingleInflection.PERSON_RETEST_AT_AGE,
                            (long) p_tested.getAge() + retestDate);
                }
            }
        }

        // Repeat annual testing if not retested 
        if (testType != TEST_TYPE_MASS_SCREEN) { // Retesting only valid for non-mass screen
            long retestAge = p_tested.getParameter(Person_ACCEPtPlusSingleInflection.PERSON_RETEST_AT_AGE);

            boolean needScheduleRetest = retestAge <= p_tested.getAge()
                    && p_tested.getAge() < 30 * AbstractIndividualInterface.ONE_YEAR_INT;

            if (!needScheduleRetest) {
                PersonClassifier TEST_30PLUS_CLASSIFIER = (PersonClassifier) runnableParam[RUNNABLE_INFECTION_CONTINUE_TEST_30_PLUS_CLASSIFIER];
                float[] TEST_30_RATE = (float[]) runnableParam[RUNNABLE_INFECTION_CONTINUE_TEST_30_PLUS_RATE];
                int cI = TEST_30PLUS_CLASSIFIER.classifyPerson(p_tested);
                if (cI >= 0) {
                    needScheduleRetest = testRNG.nextFloat() < TEST_30_RATE[cI];
                }
            }

            if (needScheduleRetest) {

                p_tested.setParameter(Person_ACCEPtPlusSingleInflection.PERSON_RETEST_AT_AGE,
                        (long) p_tested.getAge() + AbstractIndividualInterface.ONE_YEAR_INT);
            }
        }

        // Check for partner treatment
        PersonClassifier PARTNER_TREAT_CLASSIFIER = (PersonClassifier) runnableParam[RUNNABLE_INFECTION_PARTNER_TREATMENT_CLASSIFIER];

        float[] partnerTreatByClass;
        if (runnableParam[RUNNABLE_INFECTION_PARTNER_TREATMENT_RATE] instanceof float[]) {
            partnerTreatByClass = (float[]) runnableParam[RUNNABLE_INFECTION_PARTNER_TREATMENT_RATE];
        } else {
            float[][] partnerTreatByClass_timeVar = (float[][]) runnableParam[RUNNABLE_INFECTION_PARTNER_TREATMENT_RATE];

            partnerTreatByClass = new float[partnerTreatByClass_timeVar.length];
            for (int cI = 0; cI < partnerTreatByClass.length; cI++) {
                partnerTreatByClass[cI] = partnerTreatByClass_timeVar[cI][0]; // default rate                
                for (int r = 1; r < partnerTreatByClass_timeVar[cI].length; r += 2) {
                    if ((population.getGlobalTime() - pop_time_offset) >= partnerTreatByClass_timeVar[cI][r]) {
                        partnerTreatByClass[cI] = partnerTreatByClass_timeVar[cI][r + 1];
                    }
                }
            }

        }

        if (PARTNER_TREAT_CLASSIFIER != null && testType != TEST_TYPE_PARTNER_TEST) { // Level 1 partner test only
            if (p_tested.getInfectionStatus()[0] != AbstractIndividualInterface.INFECT_S) {
                int pTreatI = PARTNER_TREAT_CLASSIFIER.classifyPerson(p_tested);
                if (pTreatI >= 0) {
                    float p = testRNG.nextFloat();
                    if (p < partnerTreatByClass[pTreatI]) {
                        RelationshipMap[] relMap = population.getRelMap();
                        for (RelationshipMap r : relMap) {
                            if (r.containsVertex(p_tested.getId())) {
                                for (SingleRelationship rel : r.edgesOf(p_tested.getId())) {
                                    Integer partnerId = rel.getLinks()[p_tested.isMale() ? 1 : 0];
                                    Person_ACCEPtPlusSingleInflection partner
                                            = (Person_ACCEPtPlusSingleInflection) population.getLocalDataMap().get(partnerId);

                                    testPerson(partner, TEST_TYPE_PARTNER_TEST, testRNG, pop_time_offset);

                                }
                            }
                        }
                    }
                }
            }
        }

        boolean applyTreatment = false;
        boolean wasInfected = p_tested.getInfectionStatus()[0] != AbstractIndividualInterface.INFECT_S;

        if (wasInfected) {
            applyTreatment = true;

            if ((float) runnableParam[RUNNABLE_INFECTION_TEST_SENSITIVITY] < 1) {
                applyTreatment = testRNG.nextFloat()
                        < (float) runnableParam[RUNNABLE_INFECTION_TEST_SENSITIVITY];
            }

            if (applyTreatment) {
                ((TreatableInfectionInterface) population.getInfList()[0]).applyTreatmentAt(p_tested, population.getGlobalTime());
            }

        }

        if (testingHistory != null) {

            int[] entry;
            int nextPt;

            if (testingHistory.containsKey(p_tested.getId())) {
                entry = testingHistory.get(p_tested.getId());

                nextPt = entry[HIST_HEADER_NEXT_PT];
                if (nextPt >= entry.length) {
                    entry = Arrays.copyOf(entry, entry.length + DEFAULT_HIST_LENG);
                }

                entry[nextPt + HIST_OFFSET_TEST_TIME] = population.getGlobalTime();
                entry[nextPt + HIST_OFFSET_TEST_AGE] = (int) p_tested.getAge();
                entry[nextPt + HIST_OFFSET_TEST_INFECTED] = wasInfected ? 1 : -1;
                entry[nextPt + HIST_OFFSET_TEST_TREATMENT] = applyTreatment ? 1 : -1;
                entry[nextPt + HIST_OFFSET_TEST_TYPE] = testType;
                entry[nextPt + HIST_OFFSET_TEST_CURRENT_INFECTION_FROM_AGE] = wasInfected ? (int) p_tested.getLastInfectedAtAge(0) : -1;
                entry[HIST_HEADER_NEXT_PT] += HIST_OFFSET_TOTAL_LENGTH;

                testingHistory.put(p_tested.getId(), entry);

            }

        }

    }

}
