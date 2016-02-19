package delfos.group.grs.svd;

import delfos.common.Global;
import delfos.dataset.generated.random.RandomDatasetLoader;
import delfos.dataset.util.DatasetPrinter;
import delfos.group.groupsofusers.GroupOfUsers;
import delfos.group.grs.recommendations.GroupRecommendations;
import delfos.rs.collaborativefiltering.svd.TryThisAtHomeSVD;
import delfos.rs.collaborativefiltering.svd.TryThisAtHomeSVDModel;
import delfos.rs.output.RecommendationsOutputStandardRaw;
import delfos.rs.recommendation.Recommendation;
import delfos.rs.recommendation.RecommendationComputationDetails;
import java.util.Collection;
import java.util.Set;
import java.util.TreeSet;
import org.junit.Test;

/**
 *
 * @author jcastro-inf ( https://github.com/jcastro-inf )
 */
public class SVDforGroup_ratingsAggregationTest {

    public SVDforGroup_ratingsAggregationTest() {
    }

    @Test
    public void testWithSomeUsers() throws Exception {

        RandomDatasetLoader randomDataset = new RandomDatasetLoader(50, 100, 0.5);

        SVDforGroup_ratingsAggregation grs = new SVDforGroup_ratingsAggregation();
        grs.setParameterValue(TryThisAtHomeSVD.LEARNING_RATE, 0.02f);
        grs.setParameterValue(TryThisAtHomeSVD.K, 0.02f);
        TryThisAtHomeSVDModel RecommendationModel = grs.buildRecommendationModel(randomDataset);

        GroupOfUsers group = new GroupOfUsers(1, 2, 3);
        GroupSVDModel groupModel = grs.buildGroupModel(randomDataset, RecommendationModel, group);

        Set<Integer> candidateItems = new TreeSet<>();
        for (int idUser : group) {
            candidateItems.addAll(randomDataset.getRatingsDataset().getUserRated(idUser));
        }
        Collection<Recommendation> recommendOnly = grs.recommendOnly(randomDataset, RecommendationModel, groupModel, group, candidateItems);

        Global.showInfoMessage(DatasetPrinter.printCompactRatingTable(randomDataset.getRatingsDataset(), group.getIdMembers(), candidateItems));

        RecommendationsOutputStandardRaw output = new RecommendationsOutputStandardRaw();
        output.writeRecommendations(new GroupRecommendations(group, recommendOnly, RecommendationComputationDetails.EMPTY_DETAILS));
    }
}