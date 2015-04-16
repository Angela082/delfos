package delfos.group.results.groupevaluationmeasures;

import java.util.List;
import java.util.Map.Entry;
import delfos.dataset.basic.rating.Rating;
import delfos.dataset.basic.rating.RatingsDataset;
import delfos.dataset.basic.rating.RelevanceCriteria;
import delfos.rs.recommendation.Recommendation;
import delfos.common.statisticalfuncions.MeanIterative;
import delfos.group.groupsofusers.GroupOfUsers;
import delfos.group.results.grouprecomendationresults.GroupRecommendationResult;

/**
 * Medida de evaluación para calcular el número de medio de predicciones que se
 * calcularon por grupo.
 *
* @author Jorge Castro Gallardo
 *
 * @version 1.0 (26-01-2013)
 * @see delfos.Results.EvaluationMeasures.RatingPrediction.MAE_ForGroups
 */
public class AverageNumberOfRecommendations extends GroupEvaluationMeasure {

    @Override
    public GroupMeasureResult getMeasureResult(GroupRecommendationResult recommendationResults, RatingsDataset<? extends Rating> testDataset, RelevanceCriteria relevanceCriteria) {

        MeanIterative mean = new MeanIterative();

        for (Entry<GroupOfUsers, List<Recommendation>> entry : recommendationResults) {
            List<Recommendation> recommendationsToGroup = entry.getValue();
            mean.addValue(recommendationsToGroup.size());

        }
        return new GroupMeasureResult(this, mean.getMean());
    }

    @Override
    public boolean usesRatingPrediction() {
        return true;
    }
}