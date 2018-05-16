package relationship;

/**
 *
 * @author Ben Hui
 */
public class RelationshipMap_ACCEPtPlus extends RelationshipMap {

    @Override
    public SingleRelationship addEdge(Integer arg0, Integer arg1) {
        Relationship_ACCEPtPlus rel = new Relationship_ACCEPtPlus(new Integer[]{arg0, arg1});
        super.addEdge(arg0, arg1, rel);
        return rel;
    }
}
