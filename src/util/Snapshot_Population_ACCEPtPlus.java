package util;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileFilter;
import java.io.FileInputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.PrintWriter;
import java.util.Arrays;
import java.util.Comparator;
import java.util.Iterator;
import person.AbstractIndividualInterface;
import person.Person_ACCEPtPlusSingleInflection;
import population.Population_ACCEPtPlus;
import relationship.SingleRelationship;

/**
 *
 * @author Ben Hui
 */
public class Snapshot_Population_ACCEPtPlus {

    public static final String DIR_PATH
            =  "C:\\Users\\Bhui\\OneDrive - UNSW\\ACCEPt\\16_Runs\\Baseline";
            

    static final PersonClassifier CLASSIFIER_ACCEPT_GENDER_AGE_GRP = new PersonClassifier() {
        @Override
        public int classifyPerson(AbstractIndividualInterface p) {

            Person_ACCEPtPlusSingleInflection person = (Person_ACCEPtPlusSingleInflection) p;

            if (person.getAge() >= 30 * AbstractIndividualInterface.ONE_YEAR_INT
                    || person.getAge() < 16 * AbstractIndividualInterface.ONE_YEAR_INT) {
                return -1;
            }

            int index = -1;
            if (p.getAge() >= 16 * AbstractIndividualInterface.ONE_YEAR_INT) {
                index++;
            }
            if (p.getAge() >= 18 * AbstractIndividualInterface.ONE_YEAR_INT) {
                index++;
            }
            if (p.getAge() >= 21 * AbstractIndividualInterface.ONE_YEAR_INT) {
                index++;
            }
            if (p.getAge() >= 25 * AbstractIndividualInterface.ONE_YEAR_INT) {
                index++;
            }
            if (index != -1) {
                index += p.isMale() ? 0 : 4;
            }
            return index;
        }

        @Override
        public int numClass() {
            return 8;
        }
    };
    public static void decodePopZips(String[] dirPaths) throws IOException, ClassNotFoundException {
        for (String dirPath : dirPaths) {
            decodePopZips(dirPath);
        }
        
    }

    public static void decodePopZips(String dirPath) throws IOException, ClassNotFoundException {
        File importDir = new File(DIR_PATH);

        if (dirPath != null) {
            importDir = new File(dirPath);
        }
        
        final int OUTPUT_NUM_PARTNER_LAST_YEAR = 0;
        final int OUTPUT_NUM_PARTNER_LAST_YEAR_BY_AGE = 1;
        final int OUTPUT_LIFETIME_PARTNERS_ALL = 2;
        final int OUTPUT_PARTNERSHIP_DURATION_ALL = 3;
        final int OUTPUT_GAP_TIME_ALL = 4;
        final int OUTPUT_NUM_PARTNER_LAST_YEAR_BY_AGE_ALL = 5;
        final int OUTPUT_NUM_PARTNER_LAST_YEAR_BY_AGE_MALE = 6;
        final int OUTPUT_NUM_PARTNER_LAST_YEAR_BY_AGE_FEMALE = 7;
        final int OUTPUT_PREVALENCE_BY_AGE_GENDER = 8;
        final int OUTPUT_PREVALENCE_BY_AGE_GENDER_ALL = 9;
        final int OUTPUT_PREVALENCE_BY_ACTIVITIY = 10;
        final int OUTPUT_LORENZ = 11;
        final int OUTPUT_PREVALENCE_BY_AGE_GENDER_ASHC = 12;
        final int OUTPUT_PERCENT_VIRGIN = 13;
        final int OUTPUT_PARTNERSHIP_AGE_DIFF = 14;

        File[] outputFiles = new File[]{
            new File(importDir, "output_num_partner_last_year.csv"),
            new File(importDir, "output_num_partner_last_year_by_age.csv"),
            new File(importDir, "output_lifetime_partner_all.csv"),
            new File(importDir, "output_partnership_duration_all.csv"),
            new File(importDir, "output_gap_time_all.csv"),
            new File(importDir, "output_num_partner_last_year_by_age_ASHR2_all.csv"),
            new File(importDir, "output_num_partner_last_year_by_age_ASHR2_male.csv"),
            new File(importDir, "output_num_partner_last_year_by_age_ASHR2_female.csv"),
            new File(importDir, "output_prevalence_by_age_gender_accept.csv"),
            new File(importDir, "output_prevalence_by_age_gender_all.csv"),
            new File(importDir, "output_prevalence_by_activity.csv"),
            new File(importDir, "output_lorenz.csv"),
            new File(importDir, "output_prevalence_by_age_gender_ASHC.csv"),
            new File(importDir, "output_percent_virgin.csv"),
            new File(importDir, "output_partnership_age_difference.csv"),};

        PrintWriter[] outputWri = new PrintWriter[outputFiles.length];

        for (int i = 0; i < outputFiles.length; i++) {
            outputWri[i] = new PrintWriter(new FileWriter(outputFiles[i]));

            switch (i) {
                case OUTPUT_NUM_PARTNER_LAST_YEAR:
                    outputWri[i].println("Sim,0,1,2,3,4,5+");
                    break;
                case OUTPUT_NUM_PARTNER_LAST_YEAR_BY_AGE:
                    StringBuilder header = new StringBuilder("Sim");
                    for (int h = 16; h < 45; h++) {
                        header.append(',');
                        header.append("Age ");
                        header.append(h);
                    }
                    outputWri[i].println(header.toString());
                    break;

                case OUTPUT_LIFETIME_PARTNERS_ALL:
                    outputWri[i].println("# lifetime partners, cumulative dist");
                    break;
                case OUTPUT_PARTNERSHIP_DURATION_ALL:
                    outputWri[i].println("Duration of 2nd most recent partnership, cumulative dist");
                    break;
                case OUTPUT_GAP_TIME_ALL:
                    outputWri[i].println("Gap time between most and 2nd most recent partnership, cumulative dist");
                    break;

                case OUTPUT_NUM_PARTNER_LAST_YEAR_BY_AGE_ALL:
                case OUTPUT_NUM_PARTNER_LAST_YEAR_BY_AGE_MALE:
                case OUTPUT_NUM_PARTNER_LAST_YEAR_BY_AGE_FEMALE:
                    outputWri[i].println("Sim, Age 16-19, Age 20-29, Age 30-39, Age 40-49, Age 50-59, Age 60-69");
                    break;
                case OUTPUT_PREVALENCE_BY_AGE_GENDER:
                    outputWri[i].println("Sim,Male,,,Female,,,");
                    outputWri[i].println(", Age 16-19, Age 20-24, Age 25-29, Age 16-19, Age 20-24, Age 25-29");
                    break;
                case OUTPUT_PREVALENCE_BY_AGE_GENDER_ALL:
                    outputWri[i].println("Sim,Male,,,,,Female,,,,");
                    outputWri[i].println(", Age 16-19, Age 20-29, Age 30-39, Age 40-49, Age 50-59, Age 16-19, Age 20-29, Age 30-39, Age 40-49, Age 50-59");
                    break;

                case OUTPUT_PREVALENCE_BY_ACTIVITIY:
                    outputWri[i].println("Partners,0,1,2,3,4,5+");
                    break;

                case OUTPUT_LORENZ:
                    outputWri[i].println("NumPartnerInLast12months,Cumulative dist pop, Cumulative dist infect");
                    break;

                case OUTPUT_PREVALENCE_BY_AGE_GENDER_ASHC:
                    outputWri[i].println("Sim, Male,,,, Female,,,,");
                    outputWri[i].println(", Age 16-19, Age 20-24, Age 25-29, Age 30-34, Age 16-19, Age 20-24, Age 25-29, Age 30-34,");
                    break;

                case OUTPUT_PERCENT_VIRGIN:
                    outputWri[i].println("Sim, Male,,,, Female,,,,");
                    outputWri[i].println(", Age 16-17, Age 18-29, Age 20-24, Age 25-29, Age 16-17, Age 18-29, Age 20-24, Age 25-29,");
                    break;

                case OUTPUT_PARTNERSHIP_AGE_DIFF:
                    outputWri[i].println("Sim, Age Difference (Male-Female)");
                    break;

            }
        }

        File[] popFiles = importDir.listFiles(new FileFilter() {

            @Override
            public boolean accept(File file) {
                return file.getName().startsWith("pop") && file.getName().endsWith(".zip");
            }
        });

        Arrays.sort(popFiles, new Comparator<File>() {

            @Override
            public int compare(File t, File t1) {
                return t.getName().compareTo(t1.getName());
            }
        });

        int[] lifeTimePartner_count = new int[100]; // Expand if needed
        int lifeTimePartner_total = 0;

        int[] duration_partnership_length = new int[0];
        int[] duration_partnership_count = new int[0];
        int duration_partnership_total = 0;

        int[] gap_time_length = new int[0];
        int[] gap_time_count = new int[0];
        int gap_time_total = 0;

        int[] partner_in_12_months_all = new int[6]; // 0,1,2,3,4,5+
        int[] num_infect_by_activity_all = new int[6];

        int[] lorenz_partner_in_12_months_all = new int[0]; // Update size as it see fit
        int[] lorenz_partner_in_12_months_count = new int[0];
        int[] lorenz_infect_by_partner_all = new int[0];

        PrintWriter errWri = new PrintWriter(new File(importDir, "Err.txt"));
        int numErr = 0;

        for (int filePt = 0; filePt < popFiles.length; filePt++) {
            File popFile = popFiles[filePt];
            int[] numByACCEPtGenderAge = new int[CLASSIFIER_ACCEPT_GENDER_AGE_GRP.numClass()];
            int[] numVirginByACCEPtGenderAge = new int[CLASSIFIER_ACCEPT_GENDER_AGE_GRP.numClass()];

            Population_ACCEPtPlus pop;

            try {
                popFile = FileZipper.unzipFile(popFile, popFile.getParentFile());
            } catch (Exception ex) {
                errWri.println("Error in unzipping file " + popFile.getAbsolutePath());
                System.err.println("Error in unzipping file " + popFile.getAbsolutePath());
                numErr++;
                continue;
            }
            ObjectInputStream oIStream = new ObjectInputStream(new BufferedInputStream(new FileInputStream(popFile)));
            pop = Population_ACCEPtPlus.decodeFromStream(oIStream);
            oIStream.close();
            if (pop != null) {
                popFile.delete();
            }

            AbstractIndividualInterface[] pArr = pop.getPop();

            int partner_in_12_months_counter = 0;
            int[] partner_in_12_months = new int[6]; // 0,1,2,3,4,5+
            int[] partner_in_12_months_by_age_sum = new int[45 - 16];
            int[] partner_in_12_months_by_age_counter = new int[partner_in_12_months_by_age_sum.length];

            int[][] partner_in_12_months_by_age_gender_sum = new int[6][3]; // Age 16-19, Age 20-29, Age 30-39, Age 40-49, Age 50-59, Age 60-69
            int[][] partner_in_12_months_by_age_gender_counter = new int[partner_in_12_months_by_age_gender_sum.length][3];

            int[] num_indiv = new int[6]; // M 16-19, 20-24, 25-29 
            int[] num_infected = new int[num_indiv.length];

            int[] num_indiv_all = new int[10]; //  Age 16-19, Age 20-29, Age 30-39, Age 40-49, Age 50-59,
            int[] num_infected_all = new int[num_indiv_all.length];

            int[] num_indiv_ASHC = new int[8]; // Age 16-19, Age 20-24, Age 25-29, Age 30-34
            int[] num_infected_ASHC = new int[num_indiv_ASHC.length];

            PersonClassifier prevalence_classifier = new PersonClassifier() {

                @Override
                public int classifyPerson(AbstractIndividualInterface p) {
                    Person_ACCEPtPlusSingleInflection person = (Person_ACCEPtPlusSingleInflection) p;
                    if (person.getPartnerHistoryLifetimePt() == 0
                            || person.getAge() > 30 * AbstractIndividualInterface.ONE_YEAR_INT
                            || person.getAge() < 16 * AbstractIndividualInterface.ONE_YEAR_INT) {
                        return -1;
                    }

                    int index = -1;
                    if (p.getAge() >= 16 * AbstractIndividualInterface.ONE_YEAR_INT) {
                        index++;
                    }
                    if (p.getAge() >= 20 * AbstractIndividualInterface.ONE_YEAR_INT) {
                        index++;
                    }
                    if (p.getAge() >= 25 * AbstractIndividualInterface.ONE_YEAR_INT) {
                        index++;
                    }
                    if (index != -1) {
                        index += p.isMale() ? 0 : 3;
                    }
                    return index;
                }

                @Override
                public int numClass() {
                    return 6;
                }
            };

            for (AbstractIndividualInterface p : pArr) {
                Person_ACCEPtPlusSingleInflection person = (Person_ACCEPtPlusSingleInflection) p;
                int gI = prevalence_classifier.classifyPerson(p);

                if (gI >= 0) {
                    num_indiv[gI]++;
                    if (person.getInfectionStatus()[0] != Person_ACCEPtPlusSingleInflection.INFECT_S) {
                        num_infected[gI]++;
                    }
                }

                int aI = (int) Math.floor(p.getAge() / (10 * Person_ACCEPtPlusSingleInflection.ONE_YEAR_INT) - 1);
                aI += p.isMale() ? 0 : num_indiv_all.length / 2;
                num_indiv_all[aI]++;
                if (person.getInfectionStatus()[0] != Person_ACCEPtPlusSingleInflection.INFECT_S) {
                    num_infected_all[aI]++;
                }

                PersonClassifier ASHC = new PersonClassifier() {

                    @Override
                    public int classifyPerson(AbstractIndividualInterface p) {
                        int index = -1;
                        if (p.getAge() >= 16 * AbstractIndividualInterface.ONE_YEAR_INT) {
                            index++;
                        }
                        if (p.getAge() >= 20 * AbstractIndividualInterface.ONE_YEAR_INT) {
                            index++;
                        }
                        if (p.getAge() >= 25 * AbstractIndividualInterface.ONE_YEAR_INT) {
                            index++;
                        }
                        if (p.getAge() >= 30 * AbstractIndividualInterface.ONE_YEAR_INT) {
                            index++;
                        }
                        if (p.getAge() >= 35 * AbstractIndividualInterface.ONE_YEAR_INT) {
                            index = -1;
                        }
                        if (index != -1) {
                            index += p.isMale() ? 0 : 4;
                        }
                        return index;
                    }

                    @Override
                    public int numClass() {
                        return 8;
                    }
                };
                int aI_ASHC = ASHC.classifyPerson(p);
                if (aI_ASHC >= 0) {
                    num_indiv_ASHC[aI_ASHC]++;
                    if (person.getInfectionStatus()[0] != Person_ACCEPtPlusSingleInflection.INFECT_S) {
                        num_infected_ASHC[aI_ASHC]++;
                    }
                }

                if (person.getAge() >= 16 * Person_ACCEPtPlusSingleInflection.ONE_YEAR_INT
                        && person.getAge() < 70 * AbstractIndividualInterface.ONE_YEAR_INT) {
                    int numPartnerInPastYear = person.getNumPartnerInPastYear();
                    int ageIndex = (int) Math.floor(person.getAge() / (10 * Person_ACCEPtPlusSingleInflection.ONE_YEAR_INT)) - 1;
                    int genderIndxex = person.isMale() ? 1 : 2;

                    partner_in_12_months_by_age_gender_sum[ageIndex][0] += numPartnerInPastYear;
                    partner_in_12_months_by_age_gender_sum[ageIndex][genderIndxex] += numPartnerInPastYear;

                    partner_in_12_months_by_age_gender_counter[ageIndex][0]++;
                    partner_in_12_months_by_age_gender_counter[ageIndex][genderIndxex]++;

                }

                if (person.getAge() >= 16 * Person_ACCEPtPlusSingleInflection.ONE_YEAR_INT
                        && person.getAge() < 45 * AbstractIndividualInterface.ONE_YEAR_INT) {

                    int ageIndex = (int) Math.floor(person.getAge() / Person_ACCEPtPlusSingleInflection.ONE_YEAR_INT) - 16;
                    partner_in_12_months_by_age_sum[ageIndex] += person.getNumPartnerInPastYear();
                    partner_in_12_months_by_age_counter[ageIndex]++;

                    int numLifeTimePartners = person.getPartnerHistoryLifetimePt();

                    while (numLifeTimePartners >= lifeTimePartner_count.length) {
                        lifeTimePartner_count = Arrays.copyOf(lifeTimePartner_count, lifeTimePartner_count.length * 2); // Double in size                                                
                    }
                    lifeTimePartner_count[numLifeTimePartners]++;
                    lifeTimePartner_total++;

                    if (numLifeTimePartners > 1) { // Only include those have more than one lifetime partner (as we are looking at 2nd most recent partnership)

                        // Duration
                        int duration_2nd_recent = person.getPartnerHistoryRelLength()[person.getPartnerHistoryLifetimePt() - 2];
                        int insertIndex = Arrays.binarySearch(duration_partnership_length, duration_2nd_recent);
                        if (insertIndex < 0) {
                            insertIndex = -(insertIndex + 1);

                            int[] duration_partnership_length_org = duration_partnership_length;
                            int[] duration_partnership_count_org = duration_partnership_count;
                            duration_partnership_length = new int[duration_partnership_length.length + 1];
                            duration_partnership_count = new int[duration_partnership_count.length + 1];

                            System.arraycopy(duration_partnership_length_org, 0, duration_partnership_length, 0, insertIndex);
                            System.arraycopy(duration_partnership_count_org, 0, duration_partnership_count, 0, insertIndex);

                            if (insertIndex < duration_partnership_length_org.length) {
                                System.arraycopy(duration_partnership_length_org, insertIndex, duration_partnership_length, insertIndex + 1,
                                        duration_partnership_length_org.length - insertIndex);
                                System.arraycopy(duration_partnership_count_org, insertIndex, duration_partnership_count, insertIndex + 1,
                                        duration_partnership_count_org.length - insertIndex);
                            }
                            duration_partnership_length[insertIndex] = duration_2nd_recent;
                        }
                        duration_partnership_count[insertIndex]++;
                        duration_partnership_total++;

                        // Gap time
                        int personAgeAtStartOfLatestPartnership = person.getPartnerHistoryLifetimeAtAge()[person.getPartnerHistoryLifetimePt() - 1];
                        int personAgeAtEndOf2ndLastPartnership = person.getPartnerHistoryLifetimeAtAge()[person.getPartnerHistoryLifetimePt() - 2]
                                + person.getPartnerHistoryRelLength()[person.getPartnerHistoryLifetimePt() - 2];

                        int gapTime = personAgeAtStartOfLatestPartnership - personAgeAtEndOf2ndLastPartnership;
                        insertIndex = Arrays.binarySearch(gap_time_length, gapTime);

                        if (insertIndex < 0) {
                            insertIndex = -(insertIndex + 1);

                            int[] gap_time_length_org = gap_time_length;
                            int[] gap_time_count_org = gap_time_count;
                            gap_time_length = new int[gap_time_length.length + 1];
                            gap_time_count = new int[gap_time_count.length + 1];

                            System.arraycopy(gap_time_length_org, 0, gap_time_length, 0, insertIndex);
                            System.arraycopy(gap_time_count_org, 0, gap_time_count, 0, insertIndex);

                            if (insertIndex < gap_time_length_org.length) {
                                System.arraycopy(gap_time_length_org, insertIndex, gap_time_length, insertIndex + 1,
                                        gap_time_length_org.length - insertIndex);
                                System.arraycopy(gap_time_count_org, insertIndex, gap_time_count, insertIndex + 1,
                                        gap_time_count_org.length - insertIndex);
                            }
                            gap_time_length[insertIndex] = gapTime;
                        }

                        gap_time_count[insertIndex]++;
                        gap_time_total++;

                    }

                    if (person.getAge() >= 18 * Person_ACCEPtPlusSingleInflection.ONE_YEAR_INT) {
                        int numPartnerLastYrAbs = person.getNumPartnerInPastYear();

                        int numPartnerLastYr = Math.min(numPartnerLastYrAbs, 5);
                        partner_in_12_months[numPartnerLastYr]++;
                        partner_in_12_months_counter++;

                        partner_in_12_months_all[numPartnerLastYr]++;
                        if (person.getInfectionStatus()[0] != Person_ACCEPtPlusSingleInflection.INFECT_S) {
                            num_infect_by_activity_all[numPartnerLastYr]++;
                        }

                        // Lorenz curve
                        int key = Arrays.binarySearch(lorenz_partner_in_12_months_all, numPartnerLastYrAbs);

                        if (key < 0) {

                            key = -(key + 1);
                            int[] new_partner_12_months_all = Arrays.copyOf(lorenz_partner_in_12_months_all, lorenz_partner_in_12_months_all.length + 1);
                            int[] new_lorenz_partner_in_12_months_count = Arrays.copyOf(lorenz_partner_in_12_months_count, lorenz_partner_in_12_months_count.length + 1);
                            int[] new_lorenz_infect_by_partner_all = Arrays.copyOf(lorenz_infect_by_partner_all, lorenz_infect_by_partner_all.length + 1);

                            new_partner_12_months_all[key] = numPartnerLastYrAbs;
                            new_lorenz_partner_in_12_months_count[key] = 0;
                            new_lorenz_infect_by_partner_all[key] = 0;

                            if (key < lorenz_partner_in_12_months_all.length) {
                                System.arraycopy(lorenz_partner_in_12_months_all, key, new_partner_12_months_all, key + 1,
                                        lorenz_partner_in_12_months_all.length - key);
                                System.arraycopy(lorenz_partner_in_12_months_count, key, new_lorenz_partner_in_12_months_count, key + 1,
                                        lorenz_partner_in_12_months_count.length - key);
                                System.arraycopy(lorenz_infect_by_partner_all, key, new_lorenz_infect_by_partner_all, key + 1,
                                        lorenz_infect_by_partner_all.length - key);

                            }

                            lorenz_partner_in_12_months_all = new_partner_12_months_all;
                            lorenz_partner_in_12_months_count = new_lorenz_partner_in_12_months_count;
                            lorenz_infect_by_partner_all = new_lorenz_infect_by_partner_all;

                        }

                        lorenz_partner_in_12_months_count[key]++;
                        if (person.getInfectionStatus()[0] != Person_ACCEPtPlusSingleInflection.INFECT_S) {
                            lorenz_infect_by_partner_all[key]++;
                        }

                    }

                }

                int cI_ACCEPT = CLASSIFIER_ACCEPT_GENDER_AGE_GRP.classifyPerson(p);

                if (cI_ACCEPT >= 0) {
                    numByACCEPtGenderAge[cI_ACCEPT]++;
                    if (person.getPartnerHistoryLifetimePt() == 0) {
                        numVirginByACCEPtGenderAge[cI_ACCEPT]++;
                    }
                }

            }

            //OUTPUT_PREVALENCE_BY_AGE_GENDER_ALL
            outputWri[OUTPUT_PREVALENCE_BY_AGE_GENDER_ALL].print(filePt);
            for (int k = 0; k < num_infected_all.length; k++) {
                outputWri[OUTPUT_PREVALENCE_BY_AGE_GENDER_ALL].print(',');
                outputWri[OUTPUT_PREVALENCE_BY_AGE_GENDER_ALL].print((100f * num_infected_all[k]) / num_indiv_all[k]);
            }
            outputWri[OUTPUT_PREVALENCE_BY_AGE_GENDER_ALL].println();

            //OUTPUT_PREVALENCE_BY_AGE_GENDER_ASHC
            outputWri[OUTPUT_PREVALENCE_BY_AGE_GENDER_ASHC].print(filePt);
            for (int k = 0; k < num_infected_ASHC.length; k++) {
                outputWri[OUTPUT_PREVALENCE_BY_AGE_GENDER_ASHC].print(',');
                outputWri[OUTPUT_PREVALENCE_BY_AGE_GENDER_ASHC].print((100f * num_infected_ASHC[k]) / num_indiv_ASHC[k]);
            }
            outputWri[OUTPUT_PREVALENCE_BY_AGE_GENDER_ASHC].println();

            //OUTPUT_PREVALENCE_BY_AGE_GENDER
            outputWri[OUTPUT_PREVALENCE_BY_AGE_GENDER].print(filePt);
            for (int k = 0; k < num_indiv.length; k++) {
                outputWri[OUTPUT_PREVALENCE_BY_AGE_GENDER].print(',');
                outputWri[OUTPUT_PREVALENCE_BY_AGE_GENDER].print((100f * num_infected[k]) / num_indiv[k]);
            }
            outputWri[OUTPUT_PREVALENCE_BY_AGE_GENDER].println();

            //OUTPUT_NUM_PARTNER_LAST_YEAR
            outputWri[OUTPUT_NUM_PARTNER_LAST_YEAR].print(filePt);
            for (int k = 0; k < partner_in_12_months.length; k++) {
                outputWri[OUTPUT_NUM_PARTNER_LAST_YEAR].print(',');
                outputWri[OUTPUT_NUM_PARTNER_LAST_YEAR].print((100f * partner_in_12_months[k]) / partner_in_12_months_counter);
            }
            outputWri[OUTPUT_NUM_PARTNER_LAST_YEAR].println();

            //OUTPUT_NUM_PARTNER_LAST_YEAR_BY_AGE
            outputWri[OUTPUT_NUM_PARTNER_LAST_YEAR_BY_AGE].print(filePt);
            for (int k = 0; k < partner_in_12_months_by_age_sum.length; k++) {
                outputWri[OUTPUT_NUM_PARTNER_LAST_YEAR_BY_AGE].print(',');
                outputWri[OUTPUT_NUM_PARTNER_LAST_YEAR_BY_AGE].print(((float) partner_in_12_months_by_age_sum[k]) / partner_in_12_months_by_age_counter[k]);
            }
            outputWri[OUTPUT_NUM_PARTNER_LAST_YEAR_BY_AGE].println();

            //OUTPUT_NUM_PARTNER_LAST_YEAR_BY_AGE_ALL
            //OUTPUT_NUM_PARTNER_LAST_YEAR_BY_AGE_MALE
            //OUTPUT_NUM_PARTNER_LAST_YEAR_BY_AGE_FEMALE
            outputWri[OUTPUT_NUM_PARTNER_LAST_YEAR_BY_AGE_ALL].print(filePt);
            outputWri[OUTPUT_NUM_PARTNER_LAST_YEAR_BY_AGE_MALE].print(filePt);
            outputWri[OUTPUT_NUM_PARTNER_LAST_YEAR_BY_AGE_FEMALE].print(filePt);

            for (int k = 0; k < partner_in_12_months_by_age_gender_sum.length; k++) {
                outputWri[OUTPUT_NUM_PARTNER_LAST_YEAR_BY_AGE_ALL].print(',');
                outputWri[OUTPUT_NUM_PARTNER_LAST_YEAR_BY_AGE_ALL].print(((float) partner_in_12_months_by_age_gender_sum[k][0])
                        / partner_in_12_months_by_age_gender_counter[k][0]);

                outputWri[OUTPUT_NUM_PARTNER_LAST_YEAR_BY_AGE_MALE].print(',');
                outputWri[OUTPUT_NUM_PARTNER_LAST_YEAR_BY_AGE_MALE].print(((float) partner_in_12_months_by_age_gender_sum[k][1])
                        / partner_in_12_months_by_age_gender_counter[k][1]);

                outputWri[OUTPUT_NUM_PARTNER_LAST_YEAR_BY_AGE_FEMALE].print(',');
                outputWri[OUTPUT_NUM_PARTNER_LAST_YEAR_BY_AGE_FEMALE].print(((float) partner_in_12_months_by_age_gender_sum[k][2])
                        / partner_in_12_months_by_age_gender_counter[k][2]);
            }

            outputWri[OUTPUT_NUM_PARTNER_LAST_YEAR_BY_AGE_ALL].println();
            outputWri[OUTPUT_NUM_PARTNER_LAST_YEAR_BY_AGE_MALE].println();
            outputWri[OUTPUT_NUM_PARTNER_LAST_YEAR_BY_AGE_FEMALE].println();

            System.out.println("Analysis for " + popFiles[filePt].getAbsolutePath() + " done");
            //System.out.println("Number of partnership " + pop.getRelMap()[0].edgeSet().size());
            //System.out.println("Next Inf Long = " + pop.getInfList()[0].getRNG().nextLong());

            // OUTPUT_PERCENT_VIRGIN
            outputWri[OUTPUT_PERCENT_VIRGIN].print(filePt);
            for (int v = 0; v < numByACCEPtGenderAge.length; v++) {
                outputWri[OUTPUT_PERCENT_VIRGIN].print(',');
                outputWri[OUTPUT_PERCENT_VIRGIN].print(100f * ((float) numVirginByACCEPtGenderAge[v])
                        / numByACCEPtGenderAge[v]);
            }
            outputWri[OUTPUT_PERCENT_VIRGIN].println();

            //OUTPUT_PARTNERSHIP_AGE_DIFF
            relationship.RelationshipMap[] relMap = pop.getRelMap();

            for (int r = 0; r < relMap.length; r++) {
                Iterator<SingleRelationship> edges = relMap[r].edgeSet().iterator();

                while (edges.hasNext()) {
                    SingleRelationship rel = edges.next();                    
                    AbstractIndividualInterface[] partners =  rel.getLinks(pop.getLocalDataMap());                    
                    double maleAge =  partners[0].isMale()? partners[0].getAge() :  partners[1].getAge();
                    double femaleAge =  partners[0].isMale()? partners[1].getAge() :  partners[0].getAge();                                                           

                    outputWri[OUTPUT_PARTNERSHIP_AGE_DIFF].print(filePt);
                    outputWri[OUTPUT_PARTNERSHIP_AGE_DIFF].print(',');
                    outputWri[OUTPUT_PARTNERSHIP_AGE_DIFF].print(maleAge - femaleAge);
                    outputWri[OUTPUT_PARTNERSHIP_AGE_DIFF].println();
                }
            }
        }

        errWri.println("Num of error = " + numErr);
        errWri.close();
        System.out.println("Analysis for all files done");

        int totalPartner = 0;
        int totalInf = 0;
        for (int i = 0; i < partner_in_12_months_all.length; i++) {
            totalPartner += partner_in_12_months_all[i];
            totalInf += num_infect_by_activity_all[i];
        }
        // OUTPUT_LORENZ
        int[] cumlativeCount = new int[2];
        for (int i = 0; i < lorenz_infect_by_partner_all.length; i++) {
            cumlativeCount[0] += lorenz_partner_in_12_months_count[i];
            cumlativeCount[1] += lorenz_infect_by_partner_all[i];

            outputWri[OUTPUT_LORENZ].print(lorenz_partner_in_12_months_all[i]);
            outputWri[OUTPUT_LORENZ].print(',');
            outputWri[OUTPUT_LORENZ].print(1f * cumlativeCount[0] / totalPartner);
            outputWri[OUTPUT_LORENZ].print(',');
            outputWri[OUTPUT_LORENZ].print(1f * cumlativeCount[1] / totalInf);
            outputWri[OUTPUT_LORENZ].println();
        }

        // OUTPUT_PREVALENCE_BY_ACTIVITIY
        StringBuilder percentByAct = new StringBuilder("Distribution");
        StringBuilder percentByInf = new StringBuilder("Infection");
        StringBuilder prevalenceByAct = new StringBuilder("Prevalence");
        for (int i = 0; i < partner_in_12_months_all.length; i++) {
            percentByAct.append(',');
            percentByAct.append(100f * partner_in_12_months_all[i] / totalPartner);

            percentByInf.append(',');
            percentByInf.append(100f * num_infect_by_activity_all[i] / totalInf);

            prevalenceByAct.append(',');
            prevalenceByAct.append(100f * num_infect_by_activity_all[i] / partner_in_12_months_all[i]);
        }

        outputWri[OUTPUT_PREVALENCE_BY_ACTIVITIY].println(percentByAct.toString());
        outputWri[OUTPUT_PREVALENCE_BY_ACTIVITIY].println(percentByInf.toString());
        outputWri[OUTPUT_PREVALENCE_BY_ACTIVITIY].println(prevalenceByAct.toString());

        //OUTPUT_LIFETIME_PARTNERS_ALL
        float accumCount = 0;
        for (int i = 0; i < lifeTimePartner_count.length; i++) {
            if (lifeTimePartner_count[i] != 0) {
                accumCount += lifeTimePartner_count[i];
                outputWri[OUTPUT_LIFETIME_PARTNERS_ALL].print(i);
                outputWri[OUTPUT_LIFETIME_PARTNERS_ALL].print(',');
                outputWri[OUTPUT_LIFETIME_PARTNERS_ALL].println(accumCount / lifeTimePartner_total);
            }

        }

        //OUTPUT_PARTNERSHIP_DURATION_ALL
        accumCount = 0;
        for (int i = 0; i < duration_partnership_length.length; i++) {
            accumCount += duration_partnership_count[i];
            outputWri[OUTPUT_PARTNERSHIP_DURATION_ALL].print(duration_partnership_length[i]);
            outputWri[OUTPUT_PARTNERSHIP_DURATION_ALL].print(',');
            outputWri[OUTPUT_PARTNERSHIP_DURATION_ALL].println(accumCount / duration_partnership_total);
        }

        //OUTPUT_GAP_TIME_ALL
        accumCount = 0;
        for (int i = 0; i < gap_time_length.length; i++) {
            accumCount += gap_time_count[i];
            outputWri[OUTPUT_GAP_TIME_ALL].print(gap_time_length[i]);
            outputWri[OUTPUT_GAP_TIME_ALL].print(',');
            outputWri[OUTPUT_GAP_TIME_ALL].println(accumCount / gap_time_total);
        }

        for (PrintWriter wri : outputWri) {
            wri.close();
        }
    }
}