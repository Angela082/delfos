package delfos.results.evaluationmeasures;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import delfos.ERROR_CODES;
import delfos.common.exceptions.dataset.users.UserNotFound;
import delfos.common.statisticalfuncions.MeanIterative;
import delfos.dataset.basic.rating.Rating;
import delfos.dataset.basic.rating.RatingsDataset;
import delfos.dataset.basic.rating.RelevanceCriteria;
import delfos.io.xml.evaluationmeasures.NDCGXML;
import delfos.results.MeasureResult;
import delfos.results.RecommendationResults;
import delfos.rs.recommendation.Recommendation;

/**
 * Evalúa las recomendaciones de un sistema aplicando NDCG, usando logaritmo en
 * base 2. Se calcula el nDCG por usuarios y luego se hace la media.
 *
 * @author Jorge Castro Gallardo (Universidad de Jaén, Sinbad2)
 *
 * @version 18-Noviembre-2013
 */
public class NDCG extends EvaluationMeasure {

    private static final long serialVersionUID = 1L;

    @Override
    public MeasureResult getMeasureResult(RecommendationResults recommendationResults, RatingsDataset<? extends Rating> testDataset, RelevanceCriteria relevanceCriteria) {

        List<Double> ndcgPerUser = new ArrayList<>();

        for (int idUser : testDataset.allUsers()) {
            try {

                List<Recommendation> recommendations = recommendationResults.getRecommendationsForUser(idUser);
                if (recommendations.isEmpty()) {
                    continue;
                }

                List<Recommendation> idealRecommendations = new ArrayList<>(recommendations.size());
                Map<Integer, Rating> userRatings = (Map<Integer, Rating>) testDataset.getUserRatingsRated(idUser);

                for (Recommendation recommendation : recommendations) {
                    int idItem = recommendation.getIdItem();
                    idealRecommendations.add(new Recommendation(idItem, userRatings.get(idItem).ratingValue));
                }
                Collections.sort(idealRecommendations);

                double idealGain = computeDCG(idealRecommendations, userRatings);
                double gain = computeDCG(recommendations, userRatings);
                double score = gain / idealGain;
                ndcgPerUser.add(score);

            } catch (UserNotFound ex) {
                ERROR_CODES.USER_NOT_FOUND.exit(ex);
            }
        }

        return new MeasureResult(this, (float) new MeanIterative(ndcgPerUser).getMean(), NDCGXML.getElement(ndcgPerUser), ndcgPerUser);
    }

    @Override
    public boolean usesRatingPrediction() {
        return false;
    }

    /**
     * Compute the DCG of a list of items with respect to a value vector.
     *
     * @param items
     * @param values
     * @return
     */
    public static double computeDCG(List<Recommendation> items, Map<Integer, ? extends Rating> values) {
        final double lg2 = Math.log(2);

        double gain = 0;
        int rank = 0;

        Iterator<Recommendation> iit = items.iterator();
        while (iit.hasNext()) {
            final int idItem = iit.next().getIdItem();
            final double rating = values.get(idItem).ratingValue.doubleValue();
            rank++;
            if (rank < 2) {
                gain += rating;
            } else {
                gain += rating * lg2 / Math.log(rank);
            }
        }

        return gain;
    }

    @Override
    public MeasureResult agregateResults(Collection<MeasureResult> results) {

        List<Double> ndcgJoin = new ArrayList<>();

        for (MeasureResult result : results) {
            List<Double> ndcgPerUser = (List<Double>) result.getDetailedResult();
            ndcgJoin.addAll(ndcgPerUser);
        }

        Collections.sort(ndcgJoin);

        return new MeasureResult(this, (float) new MeanIterative(ndcgJoin).getMean(), NDCGXML.getElement(ndcgJoin), ndcgJoin);

    }

}