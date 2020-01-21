package population;

import availability.Availability_ACCEPtPlus;
import availability.AbstractAvailability;
import infection.AbstractInfection;
import infection.ChlamydiaInfection;
import java.io.IOException;
import java.util.Arrays;
import java.util.Comparator;
import java.util.Map;
import person.AbstractIndividualInterface;
import person.Person_ACCEPtPlusSingleInflection;
import static population.AbstractFieldsArrayPopulation.ONE_YEAR_INT;
import random.*;
import relationship.RelationshipMap;
import relationship.RelationshipMap_ACCEPtPlus;
import relationship.Relationship_ACCEPtPlus;
import relationship.SingleRelationship;
import util.Factory_ACCEPtPlusPersonSetting;
import util.Factory_ACCEPtPlusRelationshipSetting;
import util.PersonClassifier;

/**
 * Base population for ACCEPTPlus population
 *
 * Partner last year from ASHR2,
 * <pre>Rissel C, Badcock PB, Smith AMA, et al. Heterosexual experience and recent heterosexual encounters among Australian adults:
 * the Second Australian Study of Health and Relationships. Sexual health 2014;11(5):416-26.</pre>
 *
 * @author Ben Hui
 * @version 20171020
 *
 * History:
 * <table>
 * <tr>
 * <td>20160817</td>
 * <td>Add field ACCEPT_FIELDS_PARTNER_IN_12_MONTHS_TARGET_WEIGHTING to adjust the weighting for PARTNER_IN_12_MONTHS_TARGET, more option for ACCEPT_FIELDS_NUM_INDIV_PER_GRP</td>
 * <td>20171020</td>
 * <td>Add field ACCEPT_FIELDS_PERCENTAGE_VIRGINS and support functions</td>
 * </tr>
 * </table>
 *
 *
 */
public class Population_ACCEPtPlus extends AbstractFieldsArrayPopulation {

    public static final int ACCEPT_FIELDS_AGE_RANGE = AbstractFieldsArrayPopulation.LENGTH_FIELDS;
    public static final int ACCEPT_FIELDS_ACTIVE_AGE_RANGE = ACCEPT_FIELDS_AGE_RANGE + 1;
    public static final int ACCEPT_FIELDS_NUM_INDIV_PER_GRP = ACCEPT_FIELDS_ACTIVE_AGE_RANGE + 1;
    public static final int ACCEPT_FIELDS_PARTNER_IN_12_MONTHS_TARGET_WEIGHTING = ACCEPT_FIELDS_NUM_INDIV_PER_GRP + 1;
    public static final int ACCEPT_FIELDS_PERCENTAGE_VIRGINS = ACCEPT_FIELDS_PARTNER_IN_12_MONTHS_TARGET_WEIGHTING + 1;

    private final int ID_INF = 0;

    public static final Object[] DEFAULT_ACCEPT_FIELDS = {
        // 0: ACCEPT_FIELDS_AGE_RANGE
        new int[]{14 * ONE_YEAR_INT, 60 * ONE_YEAR_INT},
        // 1: ACCEPT_FIELDS_ACTIVE_AGE_RANGE
        new int[]{16 * ONE_YEAR_INT, 60 * ONE_YEAR_INT},
        // 2: ACCEPT_FIELDS_NUM_INDIV_PER_GRP 
        // a single integer or an int array
        1000,
        // 3: ACCEPT_FIELDS_PARTNER_IN_12_MONTHS_TARGET_WEIGHTING        
        // Replacement instead of weighting if entry is 0 or -ive
        new float[]{
            1f, 1f, 1f, 1f, 1f, 1f, Float.NaN,
            1f, 1f, 1f, 1f, 1f, 1f, Float.NaN},
        // 4: ACCEPT_FIELDS_PERCENTAGE_VIRGINS
        // From ASHR2, in conjuction with Classifier_ACCEPt_Age
        new float[]{
            0.311f, 0.115f, 0.055f, 0.028f,
            0.319f, 0.159f, 0.100f, 0.031f,},};

    // Static helper objects
    protected Factory_ACCEPtPlusRelationshipSetting RELATIONSHIP_SETTING;
    protected Factory_ACCEPtPlusPersonSetting PERSON_SETTING;

    // Final objects
    // Defined from ASHR2 (Rissel C 2014)   
    protected final float[] PARTNER_IN_12_MONTHS_TARGET = new float[]{
        // Male 16-19, 20-29, 30-39, 40-49, 50-59, 60-69, (not used)        
        1.4f, 1.4f, 1.2f, 1.1f, 1.0f, 0.9f, Float.NaN,
        // Female 16-19, 20-29, 30-39, 40-49, 50-59, 60-69, (not used) 
        1.0f, 1.1f, 1.0f, 0.9f, 0.8f, 0.6f, Float.NaN,};
    //<editor-fold defaultstate="collapsed" desc="PersonClassifier CLASSIFIER_ASHR2_GENDER_AGE_GRP">
    protected final PersonClassifier CLASSIFIER_ASHR2_GENDER_AGE_GRP = new PersonClassifier() {
        // 16-19, 20-29, 30-39, 40-49, 50-59, 60-69  
        final int[] ASHR2_AGE_GRP = new int[]{
            16 * ONE_YEAR_INT, 20 * ONE_YEAR_INT, 30 * ONE_YEAR_INT,
            40 * ONE_YEAR_INT, 50 * ONE_YEAR_INT, 60 * ONE_YEAR_INT,
            70 * ONE_YEAR_INT
        };

        @Override
        public int classifyPerson(AbstractIndividualInterface person) {
            int age = (int) person.getAge();
            int pt = Arrays.binarySearch(ASHR2_AGE_GRP, age);
            if (pt < 0) {
                pt = -(pt + 1) - 1; // Left inclusive, and to -1 if < 16 yr
            }
            if (pt >= 0 && !person.isMale()) {
                pt += ASHR2_AGE_GRP.length;
            }
            return pt;
        }

        @Override
        public int numClass() {
            return ASHR2_AGE_GRP.length * 2;
        }
    };
    //</editor-fold>
    //<editor-fold defaultstate="collapsed" desc="PersonClassifier CLASSIFIER_ACCEPT_GENDER_AGE_GRP">
    protected final PersonClassifier CLASSIFIER_ACCEPT_GENDER_AGE_GRP = new PersonClassifier() {
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
    //</editor-fold>

    public Population_ACCEPtPlus(long seed) {
        super();
        Object[] ACCEPt_fields;
        ACCEPt_fields = Arrays.copyOf(fields, fields.length + DEFAULT_ACCEPT_FIELDS.length);
        System.arraycopy(DEFAULT_ACCEPT_FIELDS, 0,
                ACCEPt_fields, fields.length, DEFAULT_ACCEPT_FIELDS.length);
        setFields(ACCEPt_fields);
        // Set pre-defined decoded_fields
        setSeed(seed);
        //setRNG(new MersenneTwisterFastRandomGenerator(seed));

        setRNG(new MersenneTwisterRandomGenerator(seed));

        RELATIONSHIP_SETTING = new Factory_ACCEPtPlusRelationshipSetting(getRNG());
        PERSON_SETTING = new Factory_ACCEPtPlusPersonSetting(getRNG());

    }

    //<editor-fold defaultstate="collapsed" desc="public static Population_ACCEPtPlus decodeFromStream(java.io.ObjectInputStream inStr)"> 
    public static Population_ACCEPtPlus decodeFromStream(java.io.ObjectInputStream inStr)
            throws IOException, ClassNotFoundException {
        int gTime = inStr.readInt();
        AbstractInfection[] infList = (AbstractInfection[]) inStr.readObject();
        Object[] decoded_fields = (Object[]) inStr.readObject();

        Population_ACCEPtPlus res = new Population_ACCEPtPlus((long) decoded_fields[0]);

        res.setGlobalTime(gTime);
        res.setInfList(infList);

        if (decoded_fields.length != res.getFields().length) {
            // Filling in default extra field from previous version

            Object[] ACCEPt_fields = Arrays.copyOf(decoded_fields, res.getFields().length);

            int end = DEFAULT_ACCEPT_FIELDS.length;

            for (int r = ACCEPt_fields.length - 1; r >= decoded_fields.length && end > 0; r--) {
                if (ACCEPt_fields[r] == null) {
                    ACCEPt_fields[r] = DEFAULT_ACCEPT_FIELDS[end - 1];
                    end--;
                }

            }

            decoded_fields = ACCEPt_fields;
        }

        // Set the new RNG
        decoded_fields[FIELDS_RNG] = new MersenneTwisterRandomGenerator(res.getSeed()); //res.getRNG();
        AbstractAvailability[] avail = (AbstractAvailability[]) decoded_fields[FIELDS_AVAIL];
        for (int i = 0; i < avail.length; i++) {
            avail[i].setRNG((RandomGenerator) decoded_fields[FIELDS_RNG]);
        }
        decoded_fields[FIELDS_AVAIL] = avail;

        //for(int i = 0; i < infList.length; i++){
        //    infList[i].setRNG(res.getRNG());           
        //}        
        //res.setInfList(infList);
        res.setFields(decoded_fields);

        res.setRNG((RandomGenerator) decoded_fields[FIELDS_RNG]);

        // The return population should already initialised
        return res;
    }
    //</editor-fold>    

    // Generate pairing
    protected void generatePairing(AbstractIndividualInterface[][] avail) {

        AbstractAvailability availability = getAvailability()[0];
        RelationshipMap relMap = getRelMap()[0];

        availability.setAvailablePopulation(avail);
        int numPairs = availability.generatePairing();

        AbstractIndividualInterface[][] pairs = availability.getPairing();

        for (int pairId = 0; pairId < numPairs; pairId++) {
            SingleRelationship rel;
            rel = formRelationship(pairs[pairId], relMap, -1, -1);

            if (rel != null) {
                performAct((Relationship_ACCEPtPlus) rel, pairs[pairId]);
            }
        }
    }

    protected void updateRelationships(int deltaT) {
        // Update relationship and remove if expired
        SingleRelationship[] relArr;
        relArr = getRelMap()[0].getRelationshipArray();

        if (relArr.length == 0) {
            relArr = getRelMap()[0].edgeSet().toArray(relArr);
        }

        Arrays.sort(relArr, new Comparator<SingleRelationship>() {

            @Override
            public int compare(SingleRelationship t, SingleRelationship t1) {
                Integer[] tId = t.getLinks();
                Integer[] t1_Id = t.getLinks();

                int res = tId[0].compareTo(t1_Id[0]);

                if (res == 0) {
                    res = tId[1].compareTo(t1_Id[1]);
                }

                return res;
            }
        });

        for (int k = 0; k < relArr.length; k++) {
            Relationship_ACCEPtPlus rel = (Relationship_ACCEPtPlus) relArr[k];
            RelationshipMap relMap = getRelMap()[0];
            AbstractIndividualInterface[] partners = rel.getLinks(getLocalData());
            double expiryTime = rel.incrementTime(deltaT);
            if (expiryTime <= 0) {
                removeRelationship(relMap, rel, partners);
            } else {
                performAct(rel, partners);
            }
        }
    }

    // Check if an act is performed
    protected void performAct(Relationship_ACCEPtPlus rel, AbstractIndividualInterface[] partners) {

        if (RELATIONSHIP_SETTING.sexActToday(rel, partners)) {
            // Check if condom was used
            if (getRNG().nextFloat() >= rel.getCondomFreq()) {
                SingleRelationship.performAct(partners, getGlobalTime(), getInfList(), new boolean[]{true, true});
            }

            for (AbstractIndividualInterface p : partners) {
                Person_ACCEPtPlusSingleInflection person = (Person_ACCEPtPlusSingleInflection) p;
                if (person.getParameter(Person_ACCEPtPlusSingleInflection.PERSON_FIRST_SEX_AGE) < 0) {
                    person.setParameter(Person_ACCEPtPlusSingleInflection.PERSON_FIRST_SEX_AGE, (long) person.getAge());
                }
            }
        }
    }

    @Override
    protected SingleRelationship formRelationship(AbstractIndividualInterface[] pair, RelationshipMap relMap,
            int duration, int mapType_not_used) {
        Relationship_ACCEPtPlus rel; // Return null if the relationship is not formed
        RelationshipMap_ACCEPtPlus rMap = (RelationshipMap_ACCEPtPlus) relMap;

        for (AbstractIndividualInterface person : pair) {
            if (!rMap.containsVertex(person.getId())) {
                rMap.addVertex(person.getId());
            }
        }

        rel = new Relationship_ACCEPtPlus(new Integer[]{pair[0].getId(), pair[1].getId()});

        if (!rMap.addEdge(pair[0].getId(), pair[1].getId(), rel)) {
            return null;
        }

        rel.setRelStartTime(getGlobalTime());

        RELATIONSHIP_SETTING.setInitialRelationshipSetting(rel, pair);

        // Forced duration if needed
        if (duration > 0) {
            rel.setDurations(duration);
        }

        for (int p = 0; p < pair.length; p++) {
            Person_ACCEPtPlusSingleInflection person = (Person_ACCEPtPlusSingleInflection) pair[p];
            person.addPartnerAtAge((int) person.getAge(), pair[(p + 1) % pair.length].getId(), (int) rel.getDurations());
        }

        return rel;
    }

    public Factory_ACCEPtPlusRelationshipSetting getRELATIONSHIP_SETTING() {
        return RELATIONSHIP_SETTING;
    }

    public Factory_ACCEPtPlusPersonSetting getPERSON_SETTING() {
        return PERSON_SETTING;
    }
    
    

    protected void removeRelationship(RelationshipMap relMap, Relationship_ACCEPtPlus toBeRemoved, AbstractIndividualInterface[] partners) {
        int duration = getGlobalTime() - toBeRemoved.getRelStartTime();
        for (AbstractIndividualInterface p : partners) {
            Person_ACCEPtPlusSingleInflection person = (Person_ACCEPtPlusSingleInflection) p;
            person.getPartnerHistoryRelLength()[person.getPartnerHistoryLifetimePt() - 1] = duration;
        }
        relMap.removeEdge(toBeRemoved);
    }

    protected AbstractIndividualInterface addNewPerson(int id, boolean isMale, int age,
            int firstSeekAge, int lastSeekAge, int enterPopAt) {
        Person_ACCEPtPlusSingleInflection newPerson
                = new Person_ACCEPtPlusSingleInflection(id, isMale, age, firstSeekAge, lastSeekAge, enterPopAt);
        PERSON_SETTING.setInitalPersonSetting(newPerson);
        return newPerson;
    }

    //<editor-fold defaultstate="collapsed" desc="public void initialise()"> 
    @Override
    public void initialise() {
        // Initialise population 
        final int[] ageSpan = (int[]) getFields()[ACCEPT_FIELDS_AGE_RANGE];
        final int[] ageGrp = new int[(ageSpan[1] - ageSpan[0]) / ONE_YEAR_INT];
        int minAge = ageSpan[0];
        for (int i = 0; i < ageGrp.length; i++) {
            ageGrp[i] = minAge;
            minAge += ONE_YEAR_INT;
        }

        int numTotal;
        int[] numPerGrp;

        if (getFields()[ACCEPT_FIELDS_NUM_INDIV_PER_GRP] instanceof int[]) {
            numTotal = 0;
            numPerGrp = (int[]) getFields()[ACCEPT_FIELDS_NUM_INDIV_PER_GRP];
            for (int n : numPerGrp) {
                numTotal += n;
            }

        } else {
            int numPerGrpSame = (int) getFields()[ACCEPT_FIELDS_NUM_INDIV_PER_GRP];
            numPerGrp = new int[2 * ageGrp.length];
            Arrays.fill(numPerGrp, numPerGrpSame);
            numTotal = 2 * ageGrp.length * numPerGrpSame;
        }

        AbstractIndividualInterface[] pop = new AbstractIndividualInterface[numTotal];
        int pid = 0;

        for (int gI = 0; gI < 2; gI++) {
            for (int aI = 0; aI < ageGrp.length; aI++) {
                for (int c = 0; c < numPerGrp[gI * ageGrp.length + aI]; c++) {
                    int id = pid;
                    boolean isMale = gI == 0;
                    int age = ageGrp[aI] + getRNG().nextInt(ONE_YEAR_INT);
                    int firstSeekAge = ((int[]) getFields()[ACCEPT_FIELDS_ACTIVE_AGE_RANGE])[0];
                    int lastSeekAge = ((int[]) getFields()[ACCEPT_FIELDS_ACTIVE_AGE_RANGE])[1];
                    int enterPopAt = 0;

                    AbstractIndividualInterface newPerson
                            = addNewPerson(id, isMale, age, firstSeekAge, lastSeekAge, enterPopAt);

                    pop[pid] = newPerson;
                    pid++;
                }

            }
        }
        setPop(pop);
        getFields()[FIELDS_NEXT_ID] = pid;

        // Initialise infection
        AbstractInfection[] infList = new AbstractInfection[]{
            new ChlamydiaInfection(new random.MersenneTwisterFastRandomGenerator(getRNG().nextLong())), // Using a separate RNG for infection
        };
        setInfList(infList);
        getInfList()[ID_INF].setInfectionIndex(ID_INF);
        updateInfectionParameters();

        // Initalise infection map
        setRelMap(new RelationshipMap[]{new RelationshipMap_ACCEPtPlus()});

        setAvailability(new AbstractAvailability[]{
            new Availability_ACCEPtPlus(getRNG())});
        for (int i = 0; i < getAvailability().length; i++) {
            getAvailability()[i].setRelationshipMap(getRelMap()[i]);
        }

    }
    //</editor-fold>

    @Override
    public void advanceTimeStep(int deltaT) {
        incrementTime(deltaT);

        AbstractIndividualInterface[][] ageGenderCollection_can_seek_partners
                = new AbstractIndividualInterface[CLASSIFIER_ASHR2_GENDER_AGE_GRP.numClass()][getPop().length];
        int[] ageGenderCollection_can_seek_partners_EndPt = new int[CLASSIFIER_ASHR2_GENDER_AGE_GRP.numClass()];

        AbstractIndividualInterface[][] ageGenderCollection_has_partners
                = new AbstractIndividualInterface[CLASSIFIER_ASHR2_GENDER_AGE_GRP.numClass()][getPop().length];
        int[] ageGenderCollection_has_partners_EndPt = new int[CLASSIFIER_ASHR2_GENDER_AGE_GRP.numClass()];

        int[] sumNumPartnerLastYr = new int[CLASSIFIER_ASHR2_GENDER_AGE_GRP.numClass()];
        int[] numInGenderAgeGrpTotal = new int[CLASSIFIER_ASHR2_GENDER_AGE_GRP.numClass()];

        int[] numInGenderACCEPtAgeGrp = new int[CLASSIFIER_ACCEPT_GENDER_AGE_GRP.numClass()];
        int[] numVirgin = new int[CLASSIFIER_ACCEPT_GENDER_AGE_GRP.numClass()];

        // Candidate for partnership formation
        AbstractIndividualInterface[][] avail = new AbstractIndividualInterface[2][getPop().length];
        int[] availPt = new int[2];

        // Update indivudals 
        for (int index = 0; index < getPop().length; index++) {

            AbstractIndividualInterface p = getPop()[index];

            p.incrementTime(deltaT, getInfList());

            if (p.getAge() > ((int[]) getFields()[ACCEPT_FIELDS_AGE_RANGE])[1]) {
                p = replaceAgeOutIndivudal(p, index);
            }

            // Copy person in various collection
            Person_ACCEPtPlusSingleInflection person = (Person_ACCEPtPlusSingleInflection) p;

            int cI = CLASSIFIER_ASHR2_GENDER_AGE_GRP.classifyPerson(person);

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
        
        //System.out.println(getGlobalTime()+ ": ");
        //System.out.println(" numVirgin_Pop  = " + Arrays.toString(numVirgin));
        //System.out.println(" numVirgin_Data = " + Arrays.toString(numToRemainVirgin));

        // Select those need to get new partner or dissolve partnership
        float[] weighting = (float[]) getFields()[ACCEPT_FIELDS_PARTNER_IN_12_MONTHS_TARGET_WEIGHTING];

        for (int g = 0; g < numInGenderAgeGrpTotal.length; g++) {

            int numInGenderAgeGrp = numInGenderAgeGrpTotal[g];

            if (numInGenderAgeGrp > 0) { // Only run if it is not empty

                float meanNumPartnerLastYr = ((float) sumNumPartnerLastYr[g]) / numInGenderAgeGrp;

                float weightedEntry = weighting[g] > 0 ? PARTNER_IN_12_MONTHS_TARGET[g] * weighting[g] : -weighting[g];
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
                                    boolean isVirgin = ((Person_ACCEPtPlusSingleInflection) person).getPartnerHistoryLifetimePt() ==0;
                                    
                                    if(isVirgin && numVirgin[cI_ACCEPt] > 0 &&  numToRemainVirgin[cI_ACCEPt] > 0){
                                        canSeekPartnerToday = !(getRNG().nextInt(numVirgin[cI_ACCEPt]) < numToRemainVirgin[cI_ACCEPt]);                                        
                                        if(!canSeekPartnerToday){
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

    protected AbstractIndividualInterface replaceAgeOutIndivudal(AbstractIndividualInterface p, int index) {
        // Dissolved all involved partnership
        for (RelationshipMap relMap : getRelMap()) {
            if (relMap.containsVertex(p.getId())) {
                SingleRelationship[] relArr = new SingleRelationship[0];
                relArr = relMap.edgesOf(p.getId()).toArray(relArr);
                
                for (SingleRelationship rel : relArr) {
                    Relationship_ACCEPtPlus r = (Relationship_ACCEPtPlus) rel;
                    int timeLeft = (int) (r.getRelStartTime() + r.getDurations() - getGlobalTime());
                    AbstractIndividualInterface[] partners = rel.getLinks(getLocalData());
                    removeRelationship(relMap, r, partners);
                    
                    for (AbstractIndividualInterface partner : partners) {
                        ((Person_ACCEPtPlusSingleInflection) partner).getFields()[Person_ACCEPtPlusSingleInflection.PERSON_NOT_SEEK_PARTNER_UNTIL_AGE]
                                = (long) (partner.getAge() + timeLeft);
                    }
                    
                }
                relMap.removeVertex(p);
            }
        }
        // Aged out indivudal
        getLocalData().remove(p.getId());
        // Generate new indivdual
        int nextId = (int) getFields()[FIELDS_NEXT_ID];
        int age = ((int[]) getFields()[ACCEPT_FIELDS_AGE_RANGE])[0];
        int firstSeekAge = ((int[]) getFields()[ACCEPT_FIELDS_ACTIVE_AGE_RANGE])[0];
        int lastSeekAge = ((int[]) getFields()[ACCEPT_FIELDS_ACTIVE_AGE_RANGE])[1];
        int enterPopAt = getGlobalTime();
        AbstractIndividualInterface newPerson
                = addNewPerson(nextId, p.isMale(), age, firstSeekAge, lastSeekAge, enterPopAt);
        p = newPerson;
        getPop()[index] = p;
        getLocalData().put(index, p);
        getFields()[FIELDS_NEXT_ID] = nextId + 1;
        return p;
    }

    //<editor-fold defaultstate="collapsed" desc="public void updateInfectionParameters()"> 
    public void updateInfectionParameters() {
        String paramStr;
        ChlamydiaInfection infection = (ChlamydiaInfection) getInfList()[ID_INF];
        double[] val;

        // Exposure
        paramStr = ChlamydiaInfection.PARAM_DIST_PARAM_INDEX_REGEX.replaceFirst("\\d+",
                Integer.toString(ChlamydiaInfection.DIST_EXPOSED_DUR_INDEX));

        val = (double[]) infection.getParameter(paramStr);
        val[0] = 12; // Althaus 2010
        val[1] = 0;
        infection.setParameter(paramStr, val);

        // Infectious 
        paramStr = ChlamydiaInfection.PARAM_DIST_PARAM_INDEX_REGEX.replaceFirst("\\d+",
                Integer.toString(ChlamydiaInfection.DIST_INFECT_ASY_DUR_INDEX));

        val = (double[]) infection.getParameter(paramStr);
        val[0] = 433; // Althaus 2011
        val[1] = 7;
        infection.setParameter(paramStr, val);

        paramStr = ChlamydiaInfection.PARAM_DIST_PARAM_INDEX_REGEX.replaceFirst("\\d+",
                Integer.toString(ChlamydiaInfection.DIST_INFECT_SYM_DUR_INDEX));

        val = (double[]) infection.getParameter(paramStr);
        val[0] = 433; // Althaus 2011
        val[1] = 7;
        infection.setParameter(paramStr, val);

        // Immunity
        paramStr = ChlamydiaInfection.PARAM_DIST_PARAM_INDEX_REGEX.replaceFirst("\\d+",
                Integer.toString(ChlamydiaInfection.DIST_IMMUNE_DUR_INDEX));

        val = (double[]) infection.getParameter(paramStr);
        val[0] = 45; // Assumption
        val[1] = 0;
        infection.setParameter(paramStr, val);

        // Tranmission prob
        paramStr = ChlamydiaInfection.PARAM_DIST_PARAM_INDEX_REGEX.replaceFirst("\\d+",
                Integer.toString(ChlamydiaInfection.DIST_TRANS_MF_INDEX));
        val = (double[]) infection.getParameter(paramStr);

        val[0] = 0.16; // Johnson 2010
        val[1] = 0.10;

        infection.setParameter(paramStr, val);

        paramStr = ChlamydiaInfection.PARAM_DIST_PARAM_INDEX_REGEX.replaceFirst("\\d+",
                Integer.toString(ChlamydiaInfection.DIST_TRANS_FM_INDEX));
        val = (double[]) infection.getParameter(paramStr);

        val[0] = 0.12; // Johnson 2010
        val[1] = 0.06;
        infection.setParameter(paramStr, val);

    }
    //</editor-fold> 

    public Map<Integer, AbstractIndividualInterface> getLocalDataMap() {
        Map<Integer, AbstractIndividualInterface> map = getLocalData();
        return map;
    }

    @Override
    public RandomGenerator getRNG() {
        return super.getRNG();
    }

}
