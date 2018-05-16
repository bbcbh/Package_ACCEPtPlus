package person;

import infection.AbstractInfection;
import java.io.Serializable;
import java.util.Arrays;
import util.LongFieldsInterface;
import util.PartnerHistoryInterface;

/**
 * A person with single infection status
 *
 * @author Ben Hui
 * @version 20160704
 */
public class Person_ACCEPtPlusSingleInflection implements AbstractIndividualInterface,
        Serializable, LongFieldsInterface, TreatablePersonInterface, PartnerHistoryInterface {

    public static final int PERSON_ID = 0;
    public static final int PERSON_GENDER = PERSON_ID + 1;
    public static final int PERSON_AGE = PERSON_GENDER + 1;
    public static final int PERSON_FIRST_SEEK_PARTNER_AGE = PERSON_AGE + 1;
    public static final int PERSON_LAST_PARTNER_AGE = PERSON_FIRST_SEEK_PARTNER_AGE + 1;
    public static final int PERSON_FIRST_SEX_AGE = PERSON_LAST_PARTNER_AGE + 1;
    public static final int PERSON_INF_STAT = PERSON_FIRST_SEX_AGE + 1;
    public static final int PERSON_TIME_TO_NEXT_STAGE = PERSON_INF_STAT + 1;
    public static final int PERSON_LAST_ACT_INFECTED = PERSON_TIME_TO_NEXT_STAGE + 1;
    public static final int PERSON_LAST_INFECTED_AT_AGE = PERSON_LAST_ACT_INFECTED + 1;
    public static final int PERSON_LAST_TREATMENT_AT_AGE = PERSON_LAST_INFECTED_AT_AGE + 1;
    public static final int PERSON_RETEST_AT_AGE = PERSON_LAST_TREATMENT_AT_AGE + 1;
    public static final int PERSON_ENTER_POP_AT = PERSON_RETEST_AT_AGE + 1;
    public static final int PERSON_ENTER_POP_AT_AGE = PERSON_ENTER_POP_AT + 1;
    public static final int PERSON_PREFERRED_PARTNER_AGE = PERSON_ENTER_POP_AT_AGE + 1;
    public static final int PERSON_PREFERRED_GAP_TIME = PERSON_PREFERRED_PARTNER_AGE + 1;
    public static final int PERSON_NOT_SEEK_PARTNER_UNTIL_AGE = PERSON_PREFERRED_GAP_TIME + 1;    
    public final int PERSON_FIELD_LEN = PERSON_NOT_SEEK_PARTNER_UNTIL_AGE + 1;

    private final int INF_ID = 0; // Only single infection

    // PERSON_PREFERRED_PARTNER_AGE, or -1 if not used
    public static final int PARTNER_TYPE_SELF_5YR_YOUNGER = 0;
    public static final int PARTNER_TYPE_SELF_1_TO_5YR_YOUNGER = 1;
    public static final int PARTNER_TYPE_SELF_SAME_AGE = 2;
    public static final int PARTNER_TYPE_SELF_1_TO_5YR_OLDER = 3;
    public static final int PARTNER_TYPE_SELF_5YR_OLDER = 4;

    // PartnerHistoryInterface
    protected int[] partnerHistoryLifetimePID = new int[0];
    protected int[] partnerHistoryLifetimeAtAge = new int[0];
    protected int[] partnerHistoryRelationshipLength = new int[0];
    protected int partnerHistoryLifetimePt = 0;

    protected long[] compfields = new long[PERSON_FIELD_LEN];

    public Person_ACCEPtPlusSingleInflection(int id, boolean isMale,
            int age, int debutAge, int lastPartAge,
            int enterPopAt) {
        compfields[PERSON_ID] = id;
        compfields[PERSON_GENDER] = isMale ? 0 : 1;
        compfields[PERSON_AGE] = age;
        compfields[PERSON_FIRST_SEEK_PARTNER_AGE] = debutAge;
        compfields[PERSON_LAST_PARTNER_AGE] = lastPartAge;
        compfields[PERSON_FIRST_SEX_AGE] = -1;
        compfields[PERSON_INF_STAT] = INFECT_S;  // Only one 
        compfields[PERSON_TIME_TO_NEXT_STAGE] = -1;
        compfields[PERSON_LAST_ACT_INFECTED] = 0;
        compfields[PERSON_LAST_INFECTED_AT_AGE] = -1;
        compfields[PERSON_LAST_TREATMENT_AT_AGE] = -1;
        compfields[PERSON_RETEST_AT_AGE] = -1;
        compfields[PERSON_ENTER_POP_AT] = enterPopAt;
        compfields[PERSON_ENTER_POP_AT_AGE] = age;
        compfields[PERSON_PREFERRED_PARTNER_AGE] = PARTNER_TYPE_SELF_SAME_AGE;
        compfields[PERSON_PREFERRED_GAP_TIME] = 0;        // if < 0 means can overlapp
        compfields[PERSON_NOT_SEEK_PARTNER_UNTIL_AGE] = 0; // 
    }

    public int[] getPreferredPartnerAgeRange() {
        int idealType = (int) getFields()[Person_ACCEPtPlusSingleInflection.PERSON_PREFERRED_PARTNER_AGE];
        int partner_minAge = 16 * 360;
        int partner_maxAge = 60 * 360;

        switch (idealType) {
            case Person_ACCEPtPlusSingleInflection.PARTNER_TYPE_SELF_5YR_YOUNGER:
                partner_minAge = (int) getAge() + 5 * 360 +1;
                break;
            case Person_ACCEPtPlusSingleInflection.PARTNER_TYPE_SELF_1_TO_5YR_YOUNGER:
                partner_minAge = (int) getAge() + 360;
                partner_maxAge = (int) getAge() + 5 * 360;
                break;
            case Person_ACCEPtPlusSingleInflection.PARTNER_TYPE_SELF_SAME_AGE:
                partner_minAge = (int) getAge() - (360+1);
                partner_maxAge = (int) getAge() + (360-1);
                break;
            case Person_ACCEPtPlusSingleInflection.PARTNER_TYPE_SELF_1_TO_5YR_OLDER:              
                partner_minAge = (int) getAge() - 5 * 360;
                partner_maxAge = (int) getAge() - 360;
                break;
            case Person_ACCEPtPlusSingleInflection.PARTNER_TYPE_SELF_5YR_OLDER:
                partner_maxAge = (int) getAge() - (5 * 360 + 1);
                break;
            default:
        }
        return new int[]{partner_minAge, partner_maxAge};
    }

    // AbstractIndividualInterface
    @Override
    public double getAge() {
        return compfields[PERSON_AGE];
    }

    @Override
    public int getId() {
        return (int) compfields[PERSON_ID];
    }

    @Override
    public int[] getInfectionStatus() {
        return new int[]{(int) compfields[PERSON_INF_STAT]};
    }

    @Override
    public int getInfectionStatus(int index) {
        return (int) compfields[PERSON_INF_STAT];
    }

    @Override
    public double getLastInfectedAtAge(int infectionIndex) {
        return (int) compfields[PERSON_LAST_INFECTED_AT_AGE];
    }

    @Override
    public Comparable getParameter(String id) {
        try {
            return getParameter(Integer.parseInt(id));
        } catch (NumberFormatException ex) {
            throw new UnsupportedOperationException(getClass().getName()
                    + ".getParameter: '" + id + "' not support yet. Try getFields() instead.");
        }
    }

    public long getParameter(int id) {
        return compfields[id];
    }

    @Override
    public double getTimeUntilNextStage(int index) {
        return compfields[PERSON_TIME_TO_NEXT_STAGE];
    }

    /**
     * Age the person by deltaT days.
     *
     * @param deltaT
     * @param infectionList
     * @return -1 if the person have yet to reach sexual debut age, -2 if the person already passed partner seeking age and has no infection (so no update
     * required) or time until next status change
     */
    @Override
    public int incrementTime(int deltaT, AbstractInfection[] infectionList) {
        // Ageing               
        compfields[PERSON_AGE] += deltaT;

        // Changing infection status
        if (compfields[PERSON_FIRST_SEEK_PARTNER_AGE] > compfields[PERSON_AGE]) {
            return -1;
        } else if (compfields[PERSON_LAST_PARTNER_AGE] < compfields[PERSON_AGE]
                && getInfectionStatus(INF_ID) == AbstractIndividualInterface.INFECT_S) {
            return -2;
        } else {
            int timeSkip = Integer.MAX_VALUE;
            if (getInfectionStatus(INF_ID) != AbstractIndividualInterface.INFECT_S) {
                if (compfields[PERSON_TIME_TO_NEXT_STAGE] > 0) {
                    compfields[PERSON_TIME_TO_NEXT_STAGE] -= deltaT;
                }
                // Self-progress
                if (compfields[PERSON_TIME_TO_NEXT_STAGE] <= 0) {
                    timeSkip = Math.min((int) infectionList[INF_ID].advancesState(this), timeSkip);
                }
            }
            // Infection from last act
            if (compfields[PERSON_LAST_ACT_INFECTED] > 0) {
                timeSkip = Math.min((int) infectionList[INF_ID].infecting(this), timeSkip);
                if (getInfectionStatus(INF_ID) != AbstractIndividualInterface.INFECT_S) {
                    compfields[PERSON_LAST_INFECTED_AT_AGE] = compfields[PERSON_AGE];
                }
                compfields[PERSON_LAST_ACT_INFECTED] = -1;
            }
            timeSkip = Math.min(deltaT, timeSkip);
            return timeSkip;
        }

    }

    @Override
    public boolean isMale() {
        return compfields[PERSON_GENDER] == 0;
    }

    @Override
    public void setAge(double age) {
        compfields[PERSON_AGE] = (long) age;
    }

    @Override
    public void setInfectionStatus(int index, int newInfectionStatus) {
        compfields[PERSON_INF_STAT] = newInfectionStatus;
    }

    @Override
    public void setLastActInfectious(int infectionIndex, boolean lastActInf) {
        compfields[PERSON_LAST_ACT_INFECTED] = lastActInf ? 1: 0;
    }

    @Override
    public Comparable setParameter(String id, Comparable value) {
        try {
            int idN = Integer.parseInt(id);
            long valueN = ((Number) value).longValue();
            return setParameter(idN, valueN);
        } catch (NumberFormatException ex) {
            throw new UnsupportedOperationException(getClass().getName()
                    + ".setParameter: '" + id + "' not support yet. Try setting using getFields() instead.");
        }
    }

    public long setParameter(int id, long value) {
        long org = compfields[id];
        compfields[id] = value;
        return org;
    }

    @Override
    public void setTimeUntilNextStage(int index, double newTimeUntilNextStage) {
        compfields[PERSON_TIME_TO_NEXT_STAGE] = (long) newTimeUntilNextStage;
    }

    @Override
    public int getEnterPopulationAt() {
        return (int) compfields[PERSON_ENTER_POP_AT];
    }

    @Override
    public double getStartingAge() {
        return compfields[PERSON_FIRST_SEX_AGE];
    }

    @Override
    public void setEnterPopulationAt(int enterPopulationAt) {
        compfields[PERSON_ENTER_POP_AT] = enterPopulationAt;
    }

    @Override
    public void setLastInfectedAtAge(int infectionIndex, double age) {
        compfields[PERSON_LAST_INFECTED_AT_AGE] = (long) age;
    }

    // TreatablePersonInterface
    @Override
    public int getLastTreatedAt() {
        return (int) compfields[PERSON_LAST_TREATMENT_AT_AGE];
    }

    @Override
    public void setLastTreatedAt(int lastTreatedAt) {
        compfields[PERSON_LAST_TREATMENT_AT_AGE] = lastTreatedAt;
    }

    // LongFieldsInterface
    @Override
    public long[] getFields() {
        return compfields;
    }

    @Override
    public void setFields(long[] newFields) {
        compfields = newFields;
    }

    // PartnerHistoryInterface
    @Override
    public int[] getPartnerHistoryLifetimePID() {
        return partnerHistoryLifetimePID;
    }

    @Override
    public int[] getPartnerHistoryLifetimeAtAge() {
        return partnerHistoryLifetimeAtAge;
    }

    @Override
    public int[] getPartnerHistoryRelLength() {
        return partnerHistoryRelationshipLength;
    }

    @Override
    public int getPartnerHistoryLifetimePt() {
        return partnerHistoryLifetimePt;
    }

    @Override
    public void setPartnerHistoryLifetimePt(int partnerHistoryLifetimePt) {
        this.partnerHistoryLifetimePt = partnerHistoryLifetimePt;
    }

    @Override
    public void addPartnerAtAge(int age, int partnerId, int relLength) {
        ensureHistoryLength(getPartnerHistoryLifetimePt() + 1);
        partnerHistoryLifetimeAtAge[getPartnerHistoryLifetimePt()] = age;
        partnerHistoryLifetimePID[getPartnerHistoryLifetimePt()] = partnerId;
        partnerHistoryRelationshipLength[getPartnerHistoryLifetimePt()] = relLength;
        partnerHistoryLifetimePt++;
    }

    @Override
    public void ensureHistoryLength(int ensuredHistoryLength) {
        if (partnerHistoryLifetimePID.length < ensuredHistoryLength) {
            partnerHistoryLifetimePID = Arrays.copyOf(partnerHistoryLifetimePID, ensuredHistoryLength);
            partnerHistoryLifetimeAtAge = Arrays.copyOf(partnerHistoryLifetimeAtAge, ensuredHistoryLength);
            partnerHistoryRelationshipLength = Arrays.copyOf(partnerHistoryRelationshipLength, ensuredHistoryLength);
        }
    }

    @Override
    public int numPartnerFromAge(double ageToCheck) {        
        // Including current partner 
        int count = 0;
        for (int i = partnerHistoryLifetimePt - 1; i >= 0; i--) {            
            if (partnerHistoryLifetimeAtAge[i] >= ageToCheck || 
                    (partnerHistoryLifetimeAtAge[i] + partnerHistoryRelationshipLength[i] >= ageToCheck)) {
                count++;
            } 
        }
        return count;
    }

    @Override
    public int[][] getPartnerHistoryPastYear() {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public int getNumPartnerInPastYear() {
        return numPartnerFromAge(getAge() - ONE_YEAR_INT);
    }

    @Override
    public void copyPartnerHistory(PartnerHistoryInterface clone) {
        partnerHistoryLifetimePID = Arrays.copyOf(clone.getPartnerHistoryLifetimePID(), clone.getPartnerHistoryLifetimePID().length);
        partnerHistoryLifetimeAtAge = Arrays.copyOf(clone.getPartnerHistoryLifetimeAtAge(), clone.getPartnerHistoryLifetimeAtAge().length);
        partnerHistoryRelationshipLength = Arrays.copyOf(clone.getPartnerHistoryRelLength(), clone.getPartnerHistoryRelLength().length);
        partnerHistoryLifetimePt = clone.getPartnerHistoryLifetimePt();
    }

}
