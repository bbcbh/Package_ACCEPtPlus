package util;

import person.AbstractIndividualInterface;
import person.Person_ACCEPtPlusSingleInflection;

/**
 * Classifier based on ACCEPt selection criteria
 *
 * @author Ben Hui
 * @version 20181024
 */
public class Classifier_ACCEPt implements PersonClassifier {

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

}
