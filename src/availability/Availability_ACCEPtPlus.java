package availability;

import java.util.Arrays;
import java.util.HashMap;
import person.AbstractIndividualInterface;
import person.Person_ACCEPtPlusSingleInflection;
import random.RandomGenerator;
import util.ArrayUtilsRandomGenerator;

/**
 *
 * @author Ben Hui
 */
public class Availability_ACCEPtPlus extends AbstractAvailability {

    protected AbstractIndividualInterface[][] available;
    protected AbstractIndividualInterface[][] pairing;

    // Id, [genderIndex][columnIndex]
    protected HashMap<Integer, int[]> mapping = new HashMap<>();
    protected boolean[][] selected = new boolean[2][];
    protected int[][] availableByAge = new int[2][];
    
    
    public static final String KEY_AGE_MATCH = "KEY_AGE_MATCH";
    private boolean twoWaysAgeMatch = false; // if false, age matching only goes one way 

    public Availability_ACCEPtPlus(RandomGenerator RNG) {
        super(RNG);
    }

    @Override
    public AbstractIndividualInterface[][] getPairing() {
        return pairing;
    }

    @Override
    public boolean setParameter(String id, Object value) {
        if(KEY_AGE_MATCH.equals(id)){
            twoWaysAgeMatch = (Boolean) value;
            return true;
        }                
        return false;
    }

    @Override
    public Object getParameter(String id) {
        if(KEY_AGE_MATCH.equals(id)){
            return twoWaysAgeMatch;
        }                        
        return null;
    }

    @Override
    public void setAvailablePopulation(AbstractIndividualInterface[][] available) {
        mapping.clear();
        // Should already sorted by age and gender
        this.available = available;
        for (int g = 0; g < available.length; g++) {
            selected[g] = new boolean[available[g].length];
            availableByAge[g] = new int[available[g].length];
            if (mapping != null) {
                for (int i = 0; i < available[g].length; i++) {
                    mapping.put(available[g][i].getId(), new int[]{g, i});
                    availableByAge[g][i] = (int) available[g][i].getAge();
                }
            }
        }
    }

    @Override
    public boolean removeMemberAvailability(AbstractIndividualInterface p) {
        int[] ga = mapping.get(p.getId());
        if (ga != null) {
            return removeMemberAvailability(ga);
        } else {
            return false;
        }
    }

    private boolean removeMemberAvailability(int[] ga) {
        if (selected[ga[0]][ga[1]]) {
            return false;
        } else {
            selected[ga[0]][ga[1]] = true;
            return true;
        }
    }

    @Override
    public boolean memberAvailable(AbstractIndividualInterface p) {
        int[] ga = mapping.get(p.getId());
        if (ga != null) {
            return memberAvailable(ga);
        } else {
            return false;
        }
    }

    private boolean memberAvailable(int[] ga) {
        return !selected[ga[0]][ga[1]];
    }

    private AbstractIndividualInterface getMemberByIndex(int[] ga) {
        return available[ga[0]][ga[1]];
    }

    @Override
    public int generatePairing() {
        pairing = new AbstractIndividualInterface[Math.min(available[0].length, available[1].length)][];
        int pairFormed = 0;
        Integer[] ids = mapping.keySet().toArray(new Integer[mapping.size()]);

        ArrayUtilsRandomGenerator.shuffleArray(ids, getRNG());

        for (Integer selectorId : ids) {
            int[] ga = mapping.get(selectorId);
            
            // First check if the person is available in the first place
            if (memberAvailable(ga)) {       
                 AbstractIndividualInterface person = getMemberByIndex(ga);
                 
                // Determine the  age range for preferred partner
                int partner_minAge = 16 * Person_ACCEPtPlusSingleInflection.ONE_YEAR_INT;
                int partner_maxAge = 60 * Person_ACCEPtPlusSingleInflection.ONE_YEAR_INT;                               
                
                                
                //if (getMemberByIndex(ga) instanceof Person_ACCEPtPlusSingleInflection 
                //        && getMemberByIndex(ga).getAge() >= 21 * Person_ACCEPtPlusSingleInflection.ONE_YEAR_INT) {
                    int[] ageR = ((Person_ACCEPtPlusSingleInflection) person).getPreferredPartnerAgeRange();                    
                    partner_minAge = ageR[0];
                    partner_maxAge = ageR[1];                    
                //}
                
                
                // Fixed age range so it is within global limit
                partner_minAge = Math.max(partner_minAge, 16 * Person_ACCEPtPlusSingleInflection.ONE_YEAR_INT);
                partner_maxAge = Math.max(Math.min(partner_maxAge, 60 * Person_ACCEPtPlusSingleInflection.ONE_YEAR_INT),
                        partner_minAge + 2*Person_ACCEPtPlusSingleInflection.ONE_YEAR_INT);
                
                
                
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
                            
                            if(possible && twoWaysAgeMatch && 
                                    partner_testing instanceof Person_ACCEPtPlusSingleInflection){
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
                        pairing[pairFormed][person.isMale()?  0:1]  = person;
                        pairing[pairFormed][partner.isMale()? 0:1]  = partner;
                        pairFormed++;
                    }
                }

            }
        }

        return pairFormed;
    }

}
