package delfos.group.results.groupevaluationmeasures;

import delfos.ERROR_CODES;
import delfos.common.exceptions.dataset.users.UserNotFound;
import delfos.common.statisticalfuncions.MeanIterative;
import delfos.dataset.basic.rating.Rating;
import delfos.dataset.basic.rating.RatingsDataset;
import delfos.dataset.basic.rating.RelevanceCriteria;
import delfos.group.groupsofusers.GroupOfUsers;
import delfos.group.results.grouprecomendationresults.GroupRecommendationResult;
import delfos.rs.recommendation.Recommendation;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.TreeMap;

/**
 * Medida de evaluación para calcular el error cuadrático medio del sistema de
 * recomendación evaluado. Calcula la diferencia entre la valoración hecha para
 * el grupo y la valoración individual que cada usuario dió para el producto, si
 * lo ha valorado.
 *
 * <p>
 * Es una extensión de la medida de evaluación
 * {@link delfos.Results.EvaluationMeasures.RatingPrediction.RMSE} para
 * recomendaciones individuales.
 *
 * @author Jorge Castro Gallardo
 *
 * @version 1.0 (22-01-2013)
 * @see delfos.Results.EvaluationMeasures.RatingPrediction.MAE_ForGroups
 */
public class RMSE extends GroupEvaluationMeasure {

    @Override
    public GroupMeasureResult getMeasureResult(GroupRecommendationResult recommendationResults, RatingsDataset<? extends Rating> testDataset, RelevanceCriteria relevanceCriteria) {

        MeanIterative rmse = new MeanIterative();
        for (Entry<GroupOfUsers, List<Recommendation>> entry : recommendationResults) {
            GroupOfUsers group = entry.getKey();
            Collection<Recommendation> recommendationsToGroup = entry.getValue();

            Map<Integer, Map<Integer, ? extends Rating>> groupTrueRatings = new TreeMap<>();
            for (int idUser : group.getIdMembers()) {
                try {
                    groupTrueRatings.put(idUser, testDataset.getUserRatingsRated(idUser));
                } catch (UserNotFound ex) {
                    ERROR_CODES.USER_NOT_FOUND.exit(ex);
                }
            }

            for (Recommendation r : recommendationsToGroup) {
                int idItem = r.getIdItem();
                for (int idUser : group.getIdMembers()) {
                    if (groupTrueRatings.get(idUser).containsKey(idItem)) {
                        double trueRating = groupTrueRatings.get(idUser).get(idItem).getRatingValue().doubleValue();
                        double predicted = r.getPreference().doubleValue();
                        rmse.addValue(Math.pow(predicted - trueRating, 2));
                    }
                }
            }
        }

        if (rmse.getNumValues() == 0) {
            return new GroupMeasureResult(this, Double.NaN);
        } else {
            float rmseValue = (float) Math.sqrt(rmse.getMean());
            return new GroupMeasureResult(this, rmseValue);
        }

    }

    @Override
    public boolean usesRatingPrediction() {
        return true;
    }
}
