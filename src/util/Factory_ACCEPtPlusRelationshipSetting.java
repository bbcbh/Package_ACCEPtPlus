package util;

import java.util.Arrays;
import static population.AbstractFieldsArrayPopulation.ONE_YEAR_INT;
import person.AbstractIndividualInterface;
import relationship.Relationship_ACCEPtPlus;
import random.RandomGenerator;

/**
 * <p>
 * A simple object for the calculation and frequency of acts in ACCEPtPopulation, using AHSR2 data.
 * </p>
 *
 * <p>
 * Currently it included:</p>
 * <ul>
 * <li>Condom usage from ASHR2
 * <pre>Rissel C, Badcock PB, Smith AMA, et al. Heterosexual experience and recent heterosexual encounters among Australian adults:
 * the Second Australian Study of Health and Relationships. Sexual health 2014;11(5):416-26.</pre></li>
 *
 * <li>Act frequency from ASHR2
 * <pre>Badcock PB, Smith AMA, Richters J, et al. Characteristics of heterosexual regular relationships among a representative sample of adults:
 * the Second Australian Study of Health and Relationships. Sexual health 2014;11(5):427-38.</pre></li>
 *
 * <li> Duration of partnership from from ASHR2
 * <pre>Badcock PB, Smith AMA, Richters J, et al. Characteristics of heterosexual regular relationships among a representative sample of adults:
 * the Second Australian Study of Health and Relationships. Sexual health 2014;11(5):427-38.</pre></li>
 *
 * </ul>
 *
 *
 * @author Ben Hui
 * @version 20160704
 *
 */
public class Factory_ACCEPtPlusRelationshipSetting {

    private final RandomGenerator rng;

    private static final float[] ASHR2_CONDOM_USAGE = {0.255f, 0.211f};

    private static final int[] ASHR_AGE_GRP = {
        16 * ONE_YEAR_INT, 20 * ONE_YEAR_INT, 30 * ONE_YEAR_INT, 40 * ONE_YEAR_INT, 50 * ONE_YEAR_INT, 60 * ONE_YEAR_INT};

    private static final float[][] ASHR2_ACT_FREQ_PER_DAY // gender, age
            = { // Male
                {1.97f/7, 2.16f/7, 1.39f/7, 1.36f/7, 1.15f/7, 1.00f/7},
                // Female               
                {1.88f/7, 1.99f/7, 1.43f/7, 1.37f/7, 1.20f/7, 0.93f/7}
            };

    private static final int[] ASHR_RELATIONSHIP_DURATION = {
        0, ONE_YEAR_INT, 3 * ONE_YEAR_INT, 6 * ONE_YEAR_INT, 11 * ONE_YEAR_INT, 20 * ONE_YEAR_INT, 60 * ONE_YEAR_INT
    };

    private static final float[][] ASHR_RELATIONSHIP_DURATION_DIST
            = { // Male
                {
                    0.102f,
                    0.102f + 0.062f,
                    0.102f + 0.062f + 0.112f,
                    0.102f + 0.062f + 0.112f + 0.157f,
                    0.102f + 0.062f + 0.112f + 0.157f + 0.227f,
                    0.102f + 0.062f + 0.112f + 0.157f + 0.227f + 0.339f + 0.001f // round off
                },
                // Female               
                {
                    0.084f,
                    0.084f + 0.073f,
                    0.084f + 0.073f + 0.112f,
                    0.084f + 0.073f + 0.112f + 0.158f,
                    0.084f + 0.073f + 0.112f + 0.158f + 0.229f,
                    0.084f + 0.073f + 0.112f + 0.158f + 0.229f + 0.341f + 0.003f // round off
                }
            };

    public Factory_ACCEPtPlusRelationshipSetting(RandomGenerator rng) {
        this.rng = rng;
    }

    public final boolean sexActToday(Relationship_ACCEPtPlus rel, AbstractIndividualInterface[] pair) {

        boolean acted = false;
        for (int i = 0; i < pair.length && acted == false; i++) {
            int aI = Arrays.binarySearch(ASHR_AGE_GRP, (int) pair[i].getAge());
            if (aI < 0) {
                aI = -(aI + 1) - 1; // Left inclusive - i.e. age cannot be less than 16
            }
            float prob = rng.nextFloat();            
            acted = prob < ASHR2_ACT_FREQ_PER_DAY[pair[i].isMale()?0:1][aI];
        }

        return acted;

    }

    public final void setInitialRelationshipSetting(Relationship_ACCEPtPlus rel, AbstractIndividualInterface[] pair) {
        // Set freq of act and duration        

        int relDur = 0;

        for (int i = 0; i < pair.length; i++) {
            float prob = rng.nextFloat();

            int dI = Arrays.binarySearch(ASHR_RELATIONSHIP_DURATION_DIST[i], prob);
            if (dI < 0) {
                dI = -(dI + 1);
            }

            relDur += ASHR_RELATIONSHIP_DURATION[dI]
                    + rng.nextInt(ASHR_RELATIONSHIP_DURATION[dI + 1] - ASHR_RELATIONSHIP_DURATION[dI]);

        }
        rel.setDurations(relDur / 2);                        

        // Condom usage  - randomly selected across gender               
        rel.setCondomFreq(ASHR2_CONDOM_USAGE[rng.nextInt(1)]);

    }

}
