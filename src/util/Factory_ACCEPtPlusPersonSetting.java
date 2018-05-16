package util;

import java.util.Arrays;
import person.Person_ACCEPtPlusSingleInflection;
import random.RandomGenerator;

/**
 * <p>
 * An object for setting person's properties.</p>
 * <p>
 * Currently it included:</p>
 * <ul>
 * <li>Age of individual's partners i.e. mixing, from ASHR2
 * <pre>Rissel C, Badcock PB, Smith AMA, et al. Heterosexual experience and recent heterosexual encounters among Australian adults:
 * the Second Australian Study of Health and Relationships. Sexual health 2014;11(5):416-26.</pre></li>
 * 
 * <li>Possible overlap, from ASHR2 (Extradyadic sex)
 * <pre>Badcock PB, Smith AMA, Richters J, et al. Characteristics of heterosexual regular relationships among a representative sample of adults:
 * the Second Australian Study of Health and Relationships. Sexual health 2014;11(5):427-38.</pre></li>
 * </li>
 * 
 * </ul>
 *
 * @author Ben Hui
 * @version 20460704
 *
 */
public class Factory_ACCEPtPlusPersonSetting {

    private final RandomGenerator rng;

    // Relative age of parnter, from ASHR2
    // RELATIVE_AGE_DIFF_PROB[gender]{ 
    // Person_ACCEPtPlusSingleInflection.PARTNER_TYPE_SELF_5YR_YOUNGER, ...}
    private final float[][] RELATIVE_AGE_DIFF_PROB = {
        // Male
        new float[]{
            0.029f,
            0.029f + 0.180f,
            0.029f + 0.180f + 0.127f + 0.014f, // round-off
            0.029f + 0.180f + 0.127f + 0.014f + 0.468f,
            0.029f + 0.180f + 0.127f + 0.014f + 0.468f + 0.182f,},
        // Female
        new float[]{
            0.168f,
            0.168f + 0.466f,
            0.168f + 0.466f + 0.141f + 0.014f, // round-off,               
            0.168f + 0.466f + 0.141f + 0.014f + 0.167f,
            0.168f + 0.466f + 0.141f + 0.014f + 0.167f + 0.044f,},};
    
    
    private final int DEFAULT_MIN_HISTORY_LENGTH = 10;
    
    private final float[] PROB_OVERLAPPING_PARTNERSHIP = {0.043f, 0.021f}; // Male, female
    

    public Factory_ACCEPtPlusPersonSetting(RandomGenerator rng) {
        this.rng = rng;

    }
    
    

    public final void setInitalPersonSetting(Person_ACCEPtPlusSingleInflection person) {                                          
        person.ensureHistoryLength(DEFAULT_MIN_HISTORY_LENGTH);
        
        setPartnerAgeDifferencesPreference(person);      
        setOverlapPreference(person);

    }

    public final void setOverlapPreference(Person_ACCEPtPlusSingleInflection person) {
        // Set preferred gap time / if overlap
        if(rng.nextFloat() < PROB_OVERLAPPING_PARTNERSHIP[person.isMale()?0:1]){
            person.getFields()[Person_ACCEPtPlusSingleInflection.PERSON_PREFERRED_GAP_TIME] = -1;
        }
    }

    public final void setPartnerAgeDifferencesPreference(Person_ACCEPtPlusSingleInflection person) {
        // Set preferred age difference
        float[] preferredAgeProb = RELATIVE_AGE_DIFF_PROB[person.isMale() ? 0 : 1];
        float prob = rng.nextFloat();
        
        int pt = Arrays.binarySearch(preferredAgeProb, prob);
        if (pt < 0) {
            pt = -(pt + 1);
        }
        
        // From Person_ACCEPtPlusSingleInflection:
        // PARTNER_TYPE_SELF_5YR_YOUNGER = 0;
        // PARTNER_TYPE_SELF_1_TO_5YR_YOUNGER = 1;
        // PARTNER_TYPE_SELF_SAME_AGE = 2;
        // PARTNER_TYPE_SELF_1_TO_5YR_OLDER = 3;
        // PARTNER_TYPE_SELF_5YR_OLDER = 4;
        
        person.getFields()[Person_ACCEPtPlusSingleInflection.PERSON_PREFERRED_PARTNER_AGE] = pt;
    }

}
