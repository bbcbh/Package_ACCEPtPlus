package util;

import person.AbstractIndividualInterface;

/**
 * <p>
 * An extension of ACCEPtPlusPerson with additional parameter catering the results from
 * <a link="http://www.publish.csiro.au/view/journals/dsp_journal_fulltext.cfm?nid=164&f=SH14105">ASHR2</a></p>
 *
 * <p>
 * Fields replacement when compared to ACCEPtPlusPerson</p>
 * <ul>
 * <li>PERSON_NUM_PARTNER_PER_WINDOW now referred to number of partners until next generation.</li>
 * <li>PERSON_WINDOW_LENGTH now referred to age where next expansion of lifetime partners occurs </li>
 * </ul>
 *
 * @version 20150702
 * @author Ben Hui
 *
 * History:
 *
 * 20150702: Renaming, and change into interface
 */
public interface PartnerHistoryInterface extends AbstractIndividualInterface, LongFieldsInterface {   

    public int[] getPartnerHistoryLifetimePID();

    public int[] getPartnerHistoryLifetimeAtAge();

    public int[] getPartnerHistoryRelLength();

    public int getPartnerHistoryLifetimePt();

    public void setPartnerHistoryLifetimePt(int partnerHistoryLifetimePt);

    public void addPartnerAtAge(int age, int partnerId, int relLength);

    public void ensureHistoryLength(int ensuredHistoryLength);

    public int numPartnerFromAge(double ageToCheck);

    public int[][] getPartnerHistoryPastYear();

    public int getNumPartnerInPastYear();
    
    public void copyPartnerHistory(PartnerHistoryInterface clone);

}
