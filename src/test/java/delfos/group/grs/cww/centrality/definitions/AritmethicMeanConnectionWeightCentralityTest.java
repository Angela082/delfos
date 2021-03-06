package delfos.group.grs.cww.centrality.definitions;

import delfos.group.grs.cww.WeightedGraphMOCK;
import delfos.rs.trustbased.WeightedGraph;
import static org.junit.Assert.assertEquals;
import org.junit.Test;

/**
 *
 * @author jcastro-inf ( https://github.com/jcastro-inf )
 */
public class AritmethicMeanConnectionWeightCentralityTest {

    public AritmethicMeanConnectionWeightCentralityTest() {
    }

    @Test
    public void testCentralityOfUser() {

        WeightedGraph<Integer> weightedGraph = new WeightedGraphMOCK();

        AritmethicMeanConnectionWeightCentrality instance = new AritmethicMeanConnectionWeightCentrality();

        assertEquals(0.32500000000000, instance.centrality(weightedGraph, 1), 0.001);
        assertEquals(0.52500000000000, instance.centrality(weightedGraph, 2), 0.001);
        assertEquals(0.37500000000000, instance.centrality(weightedGraph, 3), 0.001);
        assertEquals(0.42500000000000, instance.centrality(weightedGraph, 4), 0.001);
        assertEquals(0.20000000000000, instance.centrality(weightedGraph, 5), 0.001);
    }

}
