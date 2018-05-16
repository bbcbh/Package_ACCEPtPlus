package population;

import availability.AbstractAvailability;
import infection.AbstractInfection;
import java.io.IOException;
import java.util.Arrays;
import java.util.Comparator;
import java.util.HashMap;
import person.AbstractIndividualInterface;
import person.Person_ACCEPtPlusSingleInflection;
import static population.AbstractFieldsArrayPopulation.FIELDS_AVAIL;
import static population.AbstractFieldsArrayPopulation.FIELDS_RNG;
import static population.AbstractFieldsArrayPopulation.ONE_YEAR_INT;
import random.MersenneTwisterRandomGenerator;
import random.RandomGenerator;
import relationship.Relationship_ACCEPtPlus;
import relationship.SingleRelationship;
import util.PersonClassifier;

public class Population_ACCEPtPlus_MixedBehaviour extends Population_ACCEPtPlus {

    public static final int ACCEPT_MIXED_BEHAVIOUR_FIELDS_CLASSIFIER = ACCEPT_FIELDS_PERCENTAGE_VIRGINS + 1;
    public static final int ACCEPT_MIXED_BEHAVIOUR_SWITCH_AGE = ACCEPT_MIXED_BEHAVIOUR_FIELDS_CLASSIFIER + 1;
    public static final int ACCEPT_MIXED_BEHAVIOUR_FIELDS_PROPORTION_IN_ACCEPT_BEHAVIOUR = ACCEPT_MIXED_BEHAVIOUR_SWITCH_AGE + 1;
    public static final int ACCEPT_MIXED_BEHAVIOUR_FIELDS_PARTNER_IN_12_MONTHS_PARTNER_RANGE = ACCEPT_MIXED_BEHAVIOUR_FIELDS_PROPORTION_IN_ACCEPT_BEHAVIOUR + 1;
    public static final int ACCEPT_MIXED_BEHAVIOUR_FIELDS_PARTNER_IN_12_MONTHS_PROB = ACCEPT_MIXED_BEHAVIOUR_FIELDS_PARTNER_IN_12_MONTHS_PARTNER_RANGE + 1;
    public static final int ACCEPT_MIXED_BEHAVIOUR_IN_MIXED_BEHAVIOR = ACCEPT_MIXED_BEHAVIOUR_FIELDS_PARTNER_IN_12_MONTHS_PROB + 1;
    
    // Defined from ASHR2 (Rissel C 2014), while assuming 20-24 same as 25-29 and 20-29
    protected final float[] PARTNER_IN_12_MONTHS_TARGET_MIXED = new float[]{
        // Male 16-19, 20-24, 25-29, 30-39, 40-49, 50-59, 60-69, (not used)        
        1.4f, 1.4f, 1.4f, 1.2f, 1.1f, 1.0f, 0.9f, Float.NaN,
        // Female 16-19, 20-24, 25-29, 30-39, 40-49, 50-59, 60-69, (not used) 
        1.0f, 1.1f, 1.4f, 1.0f, 0.9f, 0.8f, 0.6f, Float.NaN,};

    public static final Object[] DEFAULT_ACCEPT_FIELDS_MIXED_BEHAVIOUR = {
        // ACCEPT_MIXED_BEHAVIOUR_FIELDS_CLASSIFIER
        // PersonClassifier 
        new PersonClassifier() {
            @Override
            public int classifyPerson(AbstractIndividualInterface p) {
                if (p.getAge() >= AbstractIndividualInterface.ONE_YEAR_INT * 16
                        && p.getAge() < AbstractIndividualInterface.ONE_YEAR_INT * 30) {
                    int cI = 0;
                    if (p.getAge() >= AbstractIndividualInterface.ONE_YEAR_INT * 20) {
                        cI++;
                    }
                    if (p.getAge() >= AbstractIndividualInterface.ONE_YEAR_INT * 25) {
                        cI++;
                    }
                    return p.isMale() ? cI : cI + 3;
                } else {
                    return -1;
                }
            }

            @Override
            public int numClass() {
                return 2 * 3;
            }
        },
        // ACCEPT_MIXED_BEHAVIOUR_SWITCH_AGE
        // int[] age 
        new int[]{16 * AbstractIndividualInterface.ONE_YEAR_INT,
            20 * AbstractIndividualInterface.ONE_YEAR_INT,
            25 * AbstractIndividualInterface.ONE_YEAR_INT},
        // ACCEPT_MIXED_BEHAVIOUR_FIELDS_PROPORTION_IN_ACCEPT_BEHAVIOUR
        //  float[cI]
        new float[]{
            0.f,
            0.f,
            0.f,
            0.f,
            0.f,
            0.f,
        },
        // ACCEPT_MIXED_BEHAVIOUR_FIELDS_PARTNER_IN_12_MONTHS_PARTNER_RANGE
        // int[classId][Prob]{low, upper}
        new int[][][]{
            new int[][]{new int[]{1, 1}, new int[]{2, 4}, new int[]{5, 12}},
            new int[][]{new int[]{1, 1}, new int[]{2, 4}, new int[]{5, 12}},
            new int[][]{new int[]{1, 1}, new int[]{2, 4}, new int[]{5, 12}},
            new int[][]{new int[]{1, 1}, new int[]{2, 4}, new int[]{5, 12}},
            new int[][]{new int[]{1, 1}, new int[]{2, 4}, new int[]{5, 12}},
            new int[][]{new int[]{1, 1}, new int[]{2, 4}, new int[]{5, 12}},},
        //ACCEPT_MIXED_BEHAVIOUR_FIELDS_PARTNER_IN_12_MONTHS_PROB
        //float[classId][cumultive likelihood]
        // from ACCEPT
        new float[][]{
            new float[]{0.486111111f, 0.888888889f, 1f},
            new float[]{0.455284553f, 0.849593496f, 1f},
            new float[]{0.466666667f, 0.816666667f, 1f},
            new float[]{0.617647059f, 0.953431373f, 1f},
            new float[]{0.618510158f, 0.957110609f, 1f},
            new float[]{0.679802956f, 0.9408867f, 1f},},
        // ACCEPT_MIXED_BEHAVIOUR_IN_MIXED_BEHAVIOR
        // An HashMap<Id, Max_Number_of_partner_in_last_12_months>          
        null, //new HashMap<Integer, Integer>(),
    };
    
    protected final PersonClassifier CLASSIFIER_ASHR2_MIXED_GENDER_AGE_GRP = new PersonClassifier() {

         // 16-19, 20-24, 25-29, 30-39, 40-49, 50-59, 60-69  
        final int[] ASHR2_MIXED_AGE_GRP = new int[]{
            16 * ONE_YEAR_INT, 20 * ONE_YEAR_INT, 25 * ONE_YEAR_INT,
            30 * ONE_YEAR_INT, 40 * ONE_YEAR_INT, 50 * ONE_YEAR_INT, 
            60 * ONE_YEAR_INT, 70 * ONE_YEAR_INT
        };

        @Override
        public int classifyPerson(AbstractIndividualInterface person) {
            int age = (int) person.getAge();
            int pt = Arrays.binarySearch(ASHR2_MIXED_AGE_GRP, age);
            if (pt < 0) {
                pt = -(pt + 1) - 1; // Left inclusive, and to -1 if < 16 yr
            }
            if (pt >= 0 && !person.isMale()) {
                pt += ASHR2_MIXED_AGE_GRP.length;
            }
            return pt;
        }

        @Override
        public int numClass() {
            return ASHR2_MIXED_AGE_GRP.length * 2;
        }
    
    };

    public Population_ACCEPtPlus_MixedBehaviour(long seed) {
        super(seed);
        Object[] ACCEPt_fields;
        ACCEPt_fields = Arrays.copyOf(fields, fields.length + DEFAULT_ACCEPT_FIELDS_MIXED_BEHAVIOUR.length);
        System.arraycopy(DEFAULT_ACCEPT_FIELDS_MIXED_BEHAVIOUR, 0,
                ACCEPt_fields, fields.length, DEFAULT_ACCEPT_FIELDS_MIXED_BEHAVIOUR.length);
        
        ACCEPt_fields[ACCEPT_FIELDS_PARTNER_IN_12_MONTHS_TARGET_WEIGHTING] 
                = // Replacement instead of weighting if entry is 0 or -ive
                new float[]{
                    1f, 1f,1f, 1f, 1f, 1f, 1f, Float.NaN,
                    1f, 1f, 1f, 1f, 1f, 1f, 1f, Float.NaN};                        
        setFields(ACCEPt_fields);
    }

    //<editor-fold defaultstate="collapsed" desc="public static Population_ACCEPtPlus decodeFromStream(java.io.ObjectInputStream inStr)"> 
    public static Population_ACCEPtPlus_MixedBehaviour decodeFromStream(java.io.ObjectInputStream inStr)
            throws IOException, ClassNotFoundException {
        int gTime = inStr.readInt();
        AbstractInfection[] infList = (AbstractInfection[]) inStr.readObject();
        Object[] decoded_fields = (Object[]) inStr.readObject();

        Population_ACCEPtPlus_MixedBehaviour res = new Population_ACCEPtPlus_MixedBehaviour((long) decoded_fields[0]);

        res.setGlobalTime(gTime);
        res.setInfList(infList);

        if (decoded_fields.length != res.getFields().length) {
            // Filling in default extra field from previous version

            Object[] ACCEPt_fields = Arrays.copyOf(decoded_fields, res.getFields().length);

            int end = DEFAULT_ACCEPT_FIELDS_MIXED_BEHAVIOUR.length;

            for (int r = ACCEPt_fields.length - 1; r >= decoded_fields.length && end > 0; r--) {
                if (ACCEPt_fields[r] == null) {
                    ACCEPt_fields[r] = DEFAULT_ACCEPT_FIELDS_MIXED_BEHAVIOUR[end - 1];
                    end--;
                }
            }
            decoded_fields = ACCEPt_fields;
        }

        // Set the new RNG
        decoded_fields[FIELDS_RNG] = new MersenneTwisterRandomGenerator(res.getSeed());
        AbstractAvailability[] avail = (AbstractAvailability[]) decoded_fields[FIELDS_AVAIL];
        for (int i = 0; i < avail.length; i++) {
            avail[i].setRNG((RandomGenerator) decoded_fields[FIELDS_RNG]);
        }
        decoded_fields[FIELDS_AVAIL] = avail;
        
        
        decoded_fields[ACCEPT_FIELDS_PARTNER_IN_12_MONTHS_TARGET_WEIGHTING] 
                = // Replacement instead of weighting if entry is 0 or -ive
                new float[]{
                    1f, 1f,1f, 1f, 1f, 1f, 1f, Float.NaN,
                    1f, 1f, 1f, 1f, 1f, 1f, 1f, Float.NaN};                      
        
        res.setFields(decoded_fields);

        res.setRNG((RandomGenerator) decoded_fields[FIELDS_RNG]);

        // The return population should already initialised
        return res;
    }
    //</editor-fold>   

    public HashMap<Integer, Integer> refreshACCEPtBehaviourCollection() {

        HashMap<Integer, Integer> ACCEPT_Behaviour_Collection = new HashMap<>();

        PersonClassifier ACCEPT_Behaviour_Classifier = (PersonClassifier) getFields()[ACCEPT_MIXED_BEHAVIOUR_FIELDS_CLASSIFIER];
        float[] probInACCEPtBehavior = (float[]) getFields()[ACCEPT_MIXED_BEHAVIOUR_FIELDS_PROPORTION_IN_ACCEPT_BEHAVIOUR];

        for (AbstractIndividualInterface p : getPop()) {
            int cI = ACCEPT_Behaviour_Classifier.classifyPerson(p);
            if (cI >= 0) {
                float inACCEPtBehaviour;
                if (probInACCEPtBehavior.length == 2) {
                    inACCEPtBehaviour = probInACCEPtBehavior[p.isMale() ? 0 : 1];
                } else {
                    inACCEPtBehaviour = probInACCEPtBehavior[cI];
                }
                insertNewPersonIntoACCEPtBehaviourCollection(p, ACCEPT_Behaviour_Collection, cI, inACCEPtBehaviour);
            }

        }

        getFields()[ACCEPT_MIXED_BEHAVIOUR_IN_MIXED_BEHAVIOR] = ACCEPT_Behaviour_Collection;

        return ACCEPT_Behaviour_Collection;

    }

    private void insertNewPersonIntoACCEPtBehaviourCollection(AbstractIndividualInterface p,
            HashMap<Integer, Integer> ACCEPT_Behaviour_Collection, int cI, float inACCEPtBehaviour) {

        float[][] cumlProbNumPartners = (float[][]) getFields()[ACCEPT_MIXED_BEHAVIOUR_FIELDS_PARTNER_IN_12_MONTHS_PROB];
        int[][][] numPartValues = (int[][][]) getFields()[ACCEPT_MIXED_BEHAVIOUR_FIELDS_PARTNER_IN_12_MONTHS_PARTNER_RANGE];

        ACCEPT_Behaviour_Collection.remove(p.getId());

        boolean inBehavior = inACCEPtBehaviour >= 1;

        if (!inBehavior && inACCEPtBehaviour > 0) {
            inBehavior = getRNG().nextFloat() < inACCEPtBehaviour;
        }

        if (inBehavior) {
            float pPart = getRNG().nextFloat();
            int key = 0;
            while (pPart > cumlProbNumPartners[cI][key]) {
                key++;
            }
            int diff = numPartValues[cI][key][1] - numPartValues[cI][key][0];
            int maxNumPartner = numPartValues[cI][key][0];
            if (diff > 0) {
                maxNumPartner += getRNG().nextInt(diff);
            }
            ACCEPT_Behaviour_Collection.put(p.getId(), maxNumPartner);
        }

    }

    @Override
    public void advanceTimeStep(int deltaT) {

        incrementTime(deltaT);

        AbstractIndividualInterface[][] ageGenderCollection_can_seek_partners
                = new AbstractIndividualInterface[CLASSIFIER_ASHR2_MIXED_GENDER_AGE_GRP.numClass()][getPop().length];
        int[] ageGenderCollection_can_seek_partners_EndPt = new int[CLASSIFIER_ASHR2_MIXED_GENDER_AGE_GRP.numClass()];

        AbstractIndividualInterface[][] ageGenderCollection_has_partners
                = new AbstractIndividualInterface[CLASSIFIER_ASHR2_MIXED_GENDER_AGE_GRP.numClass()][getPop().length];
        int[] ageGenderCollection_has_partners_EndPt = new int[CLASSIFIER_ASHR2_MIXED_GENDER_AGE_GRP.numClass()];

        int[] sumNumPartnerLastYr = new int[CLASSIFIER_ASHR2_MIXED_GENDER_AGE_GRP.numClass()];
        int[] numInGenderAgeGrpTotal = new int[CLASSIFIER_ASHR2_MIXED_GENDER_AGE_GRP.numClass()];

        int[] numInGenderACCEPtAgeGrp = new int[CLASSIFIER_ACCEPT_GENDER_AGE_GRP.numClass()];
        int[] numVirgin = new int[CLASSIFIER_ACCEPT_GENDER_AGE_GRP.numClass()];

        // Candidate for partnership formation
        AbstractIndividualInterface[][] avail = new AbstractIndividualInterface[2][getPop().length];
        int[] availPt = new int[2];

        PersonClassifier ACCEPT_Behaviour_Classifier = (PersonClassifier) getFields()[ACCEPT_MIXED_BEHAVIOUR_FIELDS_CLASSIFIER];

        HashMap<Integer, Integer> ACCEPT_Behaviour_Collection = (HashMap<Integer, Integer>) getFields()[ACCEPT_MIXED_BEHAVIOUR_IN_MIXED_BEHAVIOR];
        int[] behaviorSwitchAge = (int[]) getFields()[ACCEPT_MIXED_BEHAVIOUR_SWITCH_AGE];

        if (ACCEPT_Behaviour_Collection == null) {
            ACCEPT_Behaviour_Collection = refreshACCEPtBehaviourCollection();
        }

        // Update indivudals 
        for (int index = 0; index < getPop().length; index++) {
            AbstractIndividualInterface p = getPop()[index];

            p.incrementTime(deltaT, getInfList());

            if (p.getAge() > ((int[]) getFields()[ACCEPT_FIELDS_AGE_RANGE])[1]) {
                ACCEPT_Behaviour_Collection.remove(p.getId());
                p = replaceAgeOutIndivudal(p, index);

            }

            int bsI = Arrays.binarySearch(behaviorSwitchAge, (int) p.getAge());

            if (bsI >= 0) {

                float inACCEPtBehaviour;
                float[] inACCEPtBehaviourArr = ((float[]) getFields()[ACCEPT_MIXED_BEHAVIOUR_FIELDS_PROPORTION_IN_ACCEPT_BEHAVIOUR]);

                if (inACCEPtBehaviourArr.length == 2) {
                    if (bsI == 0) {
                        inACCEPtBehaviour = inACCEPtBehaviourArr[p.isMale() ? 0 : 1];
                    } else {
                        inACCEPtBehaviour = ACCEPT_Behaviour_Collection.containsKey(p.getId()) ? 1 : 0;
                    }
                } else {
                    int cI = ACCEPT_Behaviour_Classifier.classifyPerson(p);
                    inACCEPtBehaviour = inACCEPtBehaviourArr[cI];
                }

                // New person - check if it is in ACCEPt_Behaviour Group
                insertNewPersonIntoACCEPtBehaviourCollection(p,
                        ACCEPT_Behaviour_Collection,
                        ACCEPT_Behaviour_Classifier.classifyPerson(p),
                        inACCEPtBehaviour
                );
            }

            // Copy person in various collection
            Person_ACCEPtPlusSingleInflection person = (Person_ACCEPtPlusSingleInflection) p;

            if (ACCEPT_Behaviour_Classifier.classifyPerson(p) >= 0
                    && ACCEPT_Behaviour_Collection.containsKey(person.getId())) {
                int numPartIn12Months = person.getNumPartnerInPastYear();
                int numPartIn12Months_Max = ACCEPT_Behaviour_Collection.get(person.getId());

                if (numPartIn12Months < numPartIn12Months_Max) {
                    int genderPt = person.isMale() ? 0 : 1;
                    avail[genderPt][availPt[genderPt]] = person;
                    availPt[genderPt]++;
                }

            } else {
                int cI = CLASSIFIER_ASHR2_MIXED_GENDER_AGE_GRP.classifyPerson(person);

                if (cI >= 0) {
                    sumNumPartnerLastYr[cI] += person.getNumPartnerInPastYear();
                    numInGenderAgeGrpTotal[cI]++;

                    int numCurrentPartners = getRelMap()[0].containsVertex(person.getId()) ? getRelMap()[0].degreeOf(person.getId()) : 0;
                    boolean canSeekPartner = person.getFields()[Person_ACCEPtPlusSingleInflection.PERSON_NOT_SEEK_PARTNER_UNTIL_AGE] < person.getAge()
                            && (numCurrentPartners == 0 || person.getFields()[Person_ACCEPtPlusSingleInflection.PERSON_PREFERRED_GAP_TIME] < 0);

                    if (canSeekPartner) {
                        // Including those who have no partner or can have overlap partnership                
                        ageGenderCollection_can_seek_partners[cI][ageGenderCollection_can_seek_partners_EndPt[cI]] = person;
                        ageGenderCollection_can_seek_partners_EndPt[cI]++;
                    }

                    if (numCurrentPartners > 0) {
                        // Those who have at least one partner 
                        ageGenderCollection_has_partners[cI][ageGenderCollection_has_partners_EndPt[cI]] = person;
                        ageGenderCollection_has_partners_EndPt[cI]++;
                    }
                }

            }

            int cI_ACCEPT = CLASSIFIER_ACCEPT_GENDER_AGE_GRP.classifyPerson(person);
            if (cI_ACCEPT >= 0) {
                numInGenderACCEPtAgeGrp[cI_ACCEPT]++;
                if (person.getPartnerHistoryLifetimePt() == 0) {
                    numVirgin[cI_ACCEPT]++;
                }
            }

        }

        int[] numToRemainVirgin = new int[CLASSIFIER_ACCEPT_GENDER_AGE_GRP.numClass()];

        for (int v = 0; v < CLASSIFIER_ACCEPT_GENDER_AGE_GRP.numClass(); v++) {
            numToRemainVirgin[v]
                    = Math.round(numInGenderACCEPtAgeGrp[v]
                            * ((float[]) getFields()[ACCEPT_FIELDS_PERCENTAGE_VIRGINS])[v]);
        }

        // Select those need to get new partner or dissolve partnership
        float[] weighting = (float[]) getFields()[ACCEPT_FIELDS_PARTNER_IN_12_MONTHS_TARGET_WEIGHTING];

        for (int g = 0; g < numInGenderAgeGrpTotal.length; g++) {

            int numInGenderAgeGrp = numInGenderAgeGrpTotal[g];

            if (numInGenderAgeGrp > 0) { // Only run if it is not empty

                float meanNumPartnerLastYr = ((float) sumNumPartnerLastYr[g]) / numInGenderAgeGrp;

                float weightedEntry = weighting[g] > 0 ? PARTNER_IN_12_MONTHS_TARGET_MIXED[g] * weighting[g] : -weighting[g];
                float meanNumPartnerLastYr_diff = weightedEntry - meanNumPartnerLastYr;

                if (meanNumPartnerLastYr_diff != 0) {
                    int numSwitch = Math.round(meanNumPartnerLastYr_diff * numInGenderAgeGrp);
                    int numSwitchAbs = Math.abs(numSwitch);

                    AbstractIndividualInterface[] collection;
                    int numInCollection;

                    if (numSwitch > 0) {
                        // Form new partnership
                        collection = ageGenderCollection_can_seek_partners[g];
                        numInCollection = ageGenderCollection_can_seek_partners_EndPt[g];
                    } else {
                        // Dissolve partnership
                        collection = ageGenderCollection_has_partners[g];
                        numInCollection = ageGenderCollection_has_partners_EndPt[g];
                    }

                    for (int s = 0; s < numInCollection && numSwitchAbs > 0; s++) {
                        if (getRNG().nextInt(numInCollection - s) < numSwitchAbs) {
                            // Selected for addition / removal
                            AbstractIndividualInterface person = collection[s];
                            numSwitchAbs--;

                            if (numSwitch > 0) {
                                // Can form partnership in this turn
                                // PERSON_SETTING.setOverlapPreference((Person_ACCEPtPlusSingleInflection) person);   

                                boolean canSeekPartnerToday = true;
                                int cI_ACCEPt = CLASSIFIER_ACCEPT_GENDER_AGE_GRP.classifyPerson(person);

                                if (cI_ACCEPt >= 0) {
                                    boolean isVirgin = ((Person_ACCEPtPlusSingleInflection) person).getPartnerHistoryLifetimePt() == 0;

                                    if (isVirgin && numVirgin[cI_ACCEPt] > 0 && numToRemainVirgin[cI_ACCEPt] > 0) {
                                        canSeekPartnerToday = !(getRNG().nextInt(numVirgin[cI_ACCEPt]) < numToRemainVirgin[cI_ACCEPt]);
                                        if (!canSeekPartnerToday) {
                                            numToRemainVirgin[cI_ACCEPt]--;
                                        }
                                        numVirgin[cI_ACCEPt]--;
                                    }

                                }

                                if (canSeekPartnerToday) {

                                    int genderPt = person.isMale() ? 0 : 1;
                                    avail[genderPt][availPt[genderPt]] = person;
                                    availPt[genderPt]++;
                                }
                            } else {
                                // Dissolve partnership

                                if (getRelMap()[0].containsVertex(person.getId())) {

                                    int numEdges = getRelMap()[0].degreeOf(person.getId());
                                    SingleRelationship[] rel = getRelMap()[0].edgesOf(person.getId()).toArray(new SingleRelationship[numEdges]);
                                    if (rel.length > 1) {
                                        Arrays.sort(rel, new Comparator<SingleRelationship>() {
                                            @Override
                                            public int compare(SingleRelationship t, SingleRelationship t1) {
                                                return Double.compare(t.getDurations(), t1.getDurations());
                                            }
                                        });
                                    }
                                    // Select the shortest one for removal                                        
                                    Relationship_ACCEPtPlus toBeRemoved = (Relationship_ACCEPtPlus) rel[0];
                                    removeRelationship(getRelMap()[0], toBeRemoved, toBeRemoved.getLinks(getLocalData()));
                                }

                            }
                        }
                    }
                }
            }
        }

        updateRelationships(deltaT);

        for (int g = 0; g < avail.length; g++) {
            // Trim and sort array
            avail[g] = Arrays.copyOf(avail[g], availPt[g]);
            Arrays.sort(avail[g], new Comparator<AbstractIndividualInterface>() {
                @Override
                public int compare(AbstractIndividualInterface t, AbstractIndividualInterface t1) {
                    return Double.compare(t.getAge(), t1.getAge());
                }
            });
        }
        generatePairing(avail);

    }

}
