package delfos.group.grs.benchmark.polylens;

import java.util.Collection;
import java.util.List;
import delfos.common.aggregationoperators.Mean;
import delfos.common.exceptions.dataset.CannotLoadContentDataset;
import delfos.common.exceptions.dataset.CannotLoadRatingsDataset;
import delfos.common.exceptions.dataset.items.ItemNotFound;
import delfos.common.exceptions.dataset.users.UserNotFound;
import delfos.common.exceptions.ratings.NotEnoughtUserInformation;
import delfos.common.parameters.Parameter;
import delfos.common.parameters.restriction.IntegerParameter;
import delfos.dataset.basic.rating.Rating;
import delfos.dataset.basic.loader.types.DatasetLoader;
import delfos.group.groupsofusers.GroupOfUsers;
import delfos.group.grs.GroupRecommenderSystemAdapter;
import delfos.group.grs.SingleRecommenderSystemModel;
import delfos.group.grs.aggregation.AggregationOfIndividualRatings;
import delfos.group.grs.aggregation.GroupModelPseudoUser;
import delfos.rs.collaborativefiltering.knn.memorybased.nwr.KnnMemoryBasedNWR;
import delfos.rs.collaborativefiltering.predictiontechniques.WeightedSum;
import delfos.rs.explanation.GroupModelWithExplanation;
import delfos.rs.recommendation.Recommendation;
import delfos.similaritymeasures.PearsonCorrelationCoefficient;

/**
 * Sistema que propone el paper
 *
 * <p>
 * <p>
 * PolyLens: A Recommender System for Groups of Users
 *
 * <p>
 * Mark O'Connor, Dan Cosley, Joseph A. Konstan and John Riedl
 *
 * <p>
 * Published in: Proceeding ECSCW'01 Proceedings of the seventh conference on
 * European Conference on Computer Supported Cooperative Work Pages, 199 - 218.
 *
* @author Jorge Castro Gallardo
 * @version 1.0 20-May-2013
 */
public class PolyLens_FLINS2014 extends GroupRecommenderSystemAdapter<SingleRecommenderSystemModel, GroupModelPseudoUser> {

    private final AggregationOfIndividualRatings aggregationOfIndividualRatings;

    /**
     * Parámetro para almacenar el número de vecinos que se tienen en cuenta
     * para la predicción de la valoración. Si no se modifica, su valor por
     * defecto es 20
     */
    public static final Parameter neighborhoodSize = new Parameter("Neighborhood_size", new IntegerParameter(1, 9999, 60));

    public PolyLens_FLINS2014() {
        final KnnMemoryBasedNWR knnMemory = new KnnMemoryBasedNWR(new PearsonCorrelationCoefficient(), 20, null, false, 1, 60, new WeightedSum());
        aggregationOfIndividualRatings = new AggregationOfIndividualRatings(knnMemory, new Mean());
        addParameter(neighborhoodSize);

        addParammeterListener(() -> {
            knnMemory.setParameterValue(KnnMemoryBasedNWR.NEIGHBORHOOD_SIZE, getParameterValue(neighborhoodSize));
        });
    }

    public PolyLens_FLINS2014(int neighborhoodSize) {
        this();

        setParameterValue(PolyLens_FLINS2014.neighborhoodSize, neighborhoodSize);
    }

    @Override
    public boolean isRatingPredictorRS() {
        return true;
    }

    @Override
    public SingleRecommenderSystemModel build(DatasetLoader<? extends Rating> datasetLoader) throws CannotLoadRatingsDataset, CannotLoadContentDataset {
        return aggregationOfIndividualRatings.build(datasetLoader);
    }

    @Override
    public GroupModelPseudoUser buildGroupModel(
            DatasetLoader<? extends Rating> datasetLoader,
            SingleRecommenderSystemModel recommenderSystemModel,
            GroupOfUsers groupOfUsers)
            throws UserNotFound, CannotLoadRatingsDataset, CannotLoadContentDataset, NotEnoughtUserInformation {
        return aggregationOfIndividualRatings.buildGroupModel(datasetLoader, recommenderSystemModel, groupOfUsers).getGroupModel();
    }

    @Override
    public Collection<Recommendation> recommendOnly(
            DatasetLoader<? extends Rating> datasetLoader, SingleRecommenderSystemModel recommenderSystemModel, GroupModelPseudoUser groupModel, GroupOfUsers groupOfUsers, java.util.Set<Integer> idItemList)
            throws UserNotFound, ItemNotFound, CannotLoadRatingsDataset, CannotLoadContentDataset, NotEnoughtUserInformation {
        return aggregationOfIndividualRatings.recommendOnly(datasetLoader, recommenderSystemModel, new GroupModelWithExplanation<>(groupModel, "No Explanation Provided"), groupOfUsers, idItemList);
    }

}
