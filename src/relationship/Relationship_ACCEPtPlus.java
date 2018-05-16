package relationship;

/**
 *
 * @author Ben Hui
 * @version 20140923
 */
public class Relationship_ACCEPtPlus extends SingleRelationship {

   
    float condomFreq = 0;
    int relStartTime;

    public Relationship_ACCEPtPlus(Integer[] links) {
        super(links);
    }    
   

    public float getCondomFreq() {
        return condomFreq;
    }

    public void setCondomFreq(float condomFreq) {
        this.condomFreq = condomFreq;
    }   

    public int getRelStartTime() {
        return relStartTime;
    }

    public void setRelStartTime(int relStartTime) {
        this.relStartTime = relStartTime;
    }

}
