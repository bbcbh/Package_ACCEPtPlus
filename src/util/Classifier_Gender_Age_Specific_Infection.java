package util;

import person.AbstractIndividualInterface;

/**
 *
 * @author Bhui
 */
public class Classifier_Gender_Age_Specific_Infection implements PersonClassifier {

    @Override
    public int classifyPerson(AbstractIndividualInterface p) {
        int cI = -1;

        if (p.getAge() >= 16 * AbstractIndividualInterface.ONE_YEAR_INT
                && p.getAge() < 30 * AbstractIndividualInterface.ONE_YEAR_INT) {
            cI = 0;
            if (p.getAge() >= 20 * AbstractIndividualInterface.ONE_YEAR_INT) {
                cI++;
            }
            if (p.getAge() >= 25 * AbstractIndividualInterface.ONE_YEAR_INT) {
                cI++;
            }
            cI += p.isMale() ? 0 : 3;
        }

        return cI;
    }

    @Override
    public int numClass() {
        return 6;
    }

}
