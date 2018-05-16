package availability;

import java.util.Arrays;
import person.AbstractIndividualInterface;
import person.Person_ACCEPtPlusSingleInflection;
import random.RandomGenerator;
import util.ArrayUtilsRandomGenerator;

/**
 *
 * @author Ben Hui
 * @deprecated If possible, use Availability_ACCEPtPlus_SelectiveMixing_Rand instead
 */
public class Availability_ACCEPtPlus_SelectiveMixing extends Availability_ACCEPtPlus {

    public static final String[] KEY_STRING = new String[]{"KEY_MATCH_TYPE"};

    public static final int KEY_MATCH_TYPE_ID = 0;    

    private Object[] param_Obj = new Object[]{
        //KEY_MATCH_TYPE
        // -1: None - so will match with any
        // 0: Forced - has to match both gender
        // 1: Match based on selector (i.e. random)
        // 2: Match based on male
        // 3: Match based on female
        // 4: Prioritise forced, then random if none are found
        // 5: Prioritise selected, then random if none are found
        new Integer(-1),};

    public Availability_ACCEPtPlus_SelectiveMixing(RandomGenerator RNG) {
        super(RNG);
    }

    protected boolean memberAvailable(int[] ga) {
        return !selected[ga[0]][ga[1]];
    }

    protected AbstractIndividualInterface getMemberByIndex(int[] ga) {
        return available[ga[0]][ga[1]];
    }

    protected boolean removeMemberAvailability(int[] ga) {
        if (selected[ga[0]][ga[1]]) {
            return false;
        } else {
            selected[ga[0]][ga[1]] = true;
            return true;
        }
    }

    @Override
    public int generatePairing() {

        pairing = new AbstractIndividualInterface[Math.min(available[0].length, available[1].length)][];
        int pairFormed = 0;
        Integer[] ids = mapping.keySet().toArray(new Integer[mapping.size()]);

        ArrayUtilsRandomGenerator.shuffleArray(ids, getRNG());

        int matchType = ((Number) param_Obj[KEY_MATCH_TYPE_ID]).intValue();

        for (Integer selectorId : ids) {                        
            
            int[] ga = mapping.get(selectorId);

            // First check if the person is available in the first place
            if (memberAvailable(ga)) {
                AbstractIndividualInterface person = getMemberByIndex(ga);

                // Determine the  age range for preferred partner
                int partner_minAge = 16 * Person_ACCEPtPlusSingleInflection.ONE_YEAR_INT;
                int partner_maxAge = 60 * Person_ACCEPtPlusSingleInflection.ONE_YEAR_INT;

                boolean useMixing = matchType == 0
                        || matchType == 1 || matchType == 4 || matchType == 5
                        || (person.isMale() && matchType == 2)
                        || (!person.isMale() && matchType == 3);

                if (useMixing) {
                    int[] ageR = ((Person_ACCEPtPlusSingleInflection) person).getPreferredPartnerAgeRange();
                    partner_minAge = ageR[0];
                    partner_maxAge = ageR[1];
                }

                // Fixed age range so it is within global limit
                partner_minAge = Math.max(partner_minAge, 16 * Person_ACCEPtPlusSingleInflection.ONE_YEAR_INT);
                partner_maxAge = Math.max(Math.min(partner_maxAge, 60 * Person_ACCEPtPlusSingleInflection.ONE_YEAR_INT),
                        partner_minAge + 2 * Person_ACCEPtPlusSingleInflection.ONE_YEAR_INT);

                // List all partners which are within limit
                int altGender = person.isMale() ? 1 : 0;
                int[] ageIndex = availableByAge[altGender];

                int minIndex = Arrays.binarySearch(ageIndex, partner_minAge - 1);
                if (minIndex < 0) {  //aI = (-(insertion point) - 1)
                    minIndex = -(minIndex + 1);
                }

                int maxIndex = Arrays.binarySearch(ageIndex, partner_maxAge + 1);
                if (maxIndex < 0) {  //aI = (-(insertion point) - 1)
                    maxIndex = -(maxIndex + 1);
                }

                boolean partnerFound = false;

                if (maxIndex > minIndex) {
                    int[][] possiblePartnersIndex
                            = new int[maxIndex - minIndex][2];

                    AbstractIndividualInterface[] possiblePartners
                            = new AbstractIndividualInterface[possiblePartnersIndex.length];
                    int possiblePartnerPt = 0;

                    for (int k = minIndex; k < maxIndex; k++) {
                        if (partner_minAge <= ageIndex[k]
                                && ageIndex[k] <= partner_maxAge) {

                            int[] partner_testing_index = new int[]{altGender, k};
                            AbstractIndividualInterface partner_testing = getMemberByIndex(partner_testing_index);

                            // Age matched and available
                            boolean possible = memberAvailable(partner_testing_index);

                            if (possible && ((Boolean) getParameter(KEY_AGE_MATCH)
                                    || matchType == 0
                                    || partner_testing.isMale() && matchType == 2
                                    || !partner_testing.isMale() && matchType == 3)
                                    && partner_testing instanceof Person_ACCEPtPlusSingleInflection) {
                                int[] partner_testing_pref_range
                                        = ((Person_ACCEPtPlusSingleInflection) partner_testing).getPreferredPartnerAgeRange();

                                possible &= partner_testing_pref_range[0] <= person.getAge();
                                possible &= partner_testing_pref_range[1] >= person.getAge();

                            }

                            if (possible) {
                                // Can be a partner                                
                                possiblePartners[possiblePartnerPt] = partner_testing;
                                possiblePartnersIndex[possiblePartnerPt] = partner_testing_index;
                                possiblePartnerPt++;
                            }

                        }
                    }

                    // Forming pairing
                    if (possiblePartnerPt > 0) {
                        // Randomly select one from available partners
                        int sel = getRNG().nextInt(possiblePartnerPt);
                        AbstractIndividualInterface partner = possiblePartners[sel];
                        // Remove selected from availability
                        removeMemberAvailability(possiblePartnersIndex[sel]);
                        // Remove the selector from availability
                        removeMemberAvailability(ga);

                        pairing[pairFormed] = new AbstractIndividualInterface[2];
                        pairing[pairFormed][person.isMale() ? 0 : 1] = person;
                        pairing[pairFormed][partner.isMale() ? 0 : 1] = partner;
                        pairFormed++;
                        partnerFound = true;

                    }
                }

                if (!partnerFound && (matchType == 4 || matchType == 5)) {
                    // Try one more time without mixing                  

                    int totalMin = Arrays.binarySearch(ageIndex, 16 * Person_ACCEPtPlusSingleInflection.ONE_YEAR_INT - 1);
                    int totalMax = Arrays.binarySearch(ageIndex, 60 * Person_ACCEPtPlusSingleInflection.ONE_YEAR_INT + 1);

                    if (totalMin < 0) {  //aI = (-(insertion point) - 1)
                        totalMin = -(totalMin + 1);
                    }
                    if (totalMax < 0) {  //aI = (-(insertion point) - 1)
                        totalMax = -(totalMax + 1);
                    }

                    int[][] possiblePartnersNonMatchIndex = new int[totalMax - totalMin][2];
                    AbstractIndividualInterface[] possiblePartnersNonMatch = new AbstractIndividualInterface[possiblePartnersNonMatchIndex.length];
                    int possiblePartnersNonMatchPt = 0;

                    for (int k = totalMin; k < totalMax; k++) {
                        if (16 * Person_ACCEPtPlusSingleInflection.ONE_YEAR_INT <= ageIndex[k]
                                && ageIndex[k] <= 60 * Person_ACCEPtPlusSingleInflection.ONE_YEAR_INT) {

                            int[] partner_testing_index = new int[]{altGender, k};
                            AbstractIndividualInterface partner_testing = getMemberByIndex(partner_testing_index);

                            // Age matched and available
                            boolean possible = memberAvailable(partner_testing_index);

                            if (possible) {
                                // Can be a partner                                
                                possiblePartnersNonMatch[possiblePartnersNonMatchPt] = partner_testing;
                                possiblePartnersNonMatchIndex[possiblePartnersNonMatchPt] = partner_testing_index;
                                possiblePartnersNonMatchPt++;
                            }

                        }
                    }

                    if (possiblePartnersNonMatchPt > 0) {
                        // Form pairing for non-match (if allowed)
                        int sel = getRNG().nextInt(possiblePartnersNonMatchPt);
                        AbstractIndividualInterface partner = possiblePartnersNonMatch[sel];
                        // Remove selected from availability
                        removeMemberAvailability(possiblePartnersNonMatchIndex[sel]);
                        // Remove the selector from availability
                        removeMemberAvailability(ga);

                        pairing[pairFormed] = new AbstractIndividualInterface[2];
                        pairing[pairFormed][person.isMale() ? 0 : 1] = person;
                        pairing[pairFormed][partner.isMale() ? 0 : 1] = partner;
                        pairFormed++;
                    }

                }

            }
        }

        return pairFormed;
    }

    @Override
    public Object getParameter(String id) {
        for (int i = 0; i < KEY_STRING.length; i++) {
            if (KEY_STRING[i].equals(id)) {
                return param_Obj[i];
            }
        }
        return super.getParameter(id);
    }

    @Override
    public boolean setParameter(String id, Object value) {
        for (int i = 0; i < KEY_STRING.length; i++) {
            if (KEY_STRING[i].equals(id)) {
                param_Obj[i] = value;
                return true;
            }
        }
        return super.setParameter(id, value);
    }

}
