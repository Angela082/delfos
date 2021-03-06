package delfos.group.grs.cww.centrality.definitions;

import delfos.group.grs.cww.WeightedGraphMOCK;
import delfos.rs.trustbased.WeightedGraph;
import static org.junit.Assert.assertEquals;
import org.junit.Test;

/**
 *
 * @author jcastro-inf ( https://github.com/jcastro-inf )
 */
public class ClosenessCentralityTest {

    public ClosenessCentralityTest() {
    }

    @Test
    public void testCentralityOfUser() {

        WeightedGraph<Integer> weightedGraph = new WeightedGraphMOCK();

        ClosenessCentrality instance = new ClosenessCentrality();

        assertEquals(0.504545455, instance.centrality(weightedGraph, 1), 0.001);
        assertEquals(0.642156863, instance.centrality(weightedGraph, 2), 0.001);
        assertEquals(0.576923077, instance.centrality(weightedGraph, 3), 0.001);
        assertEquals(0.483333333, instance.centrality(weightedGraph, 4), 0.001);
        assertEquals(0.390292061, instance.centrality(weightedGraph, 5), 0.001);
    }

}
