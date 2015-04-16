package delfos.group.grs.penalty;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import delfos.common.aggregationoperators.penalty.functions.PenaltyFunction;
import delfos.common.aggregationoperators.penalty.functions.PenaltyWholeMatrix;
import delfos.common.exceptions.dataset.CannotLoadContentDataset;
import delfos.common.exceptions.dataset.CannotLoadRatingsDataset;
import delfos.common.exceptions.dataset.items.ItemNotFound;
import delfos.common.exceptions.dataset.users.UserNotFound;
import delfos.dataset.basic.rating.Rating;
import delfos.dataset.basic.loader.types.DatasetLoader;
import delfos.dataset.util.DatasetUtilities;
import delfos.group.groupsofusers.GroupOfUsers;
import delfos.group.groupsofusers.GroupOfUsers;
import delfos.group.grs.GroupRecommenderSystemAdapter;
import delfos.group.grs.SingleRecommenderSystemModel;
import static delfos.group.grs.aggregation.AggregationOfIndividualRecommendations.performSingleUserRecommendations;
import static delfos.group.grs.penalty.PenaltyGRS_Ratings.ITEM_GROUPER;
import static delfos.group.grs.penalty.PenaltyGRS_Ratings.PENALTY;
import static delfos.group.grs.penalty.PenaltyGRS_Ratings.SINGLE_USER_RECOMMENDER;
import delfos.group.grs.penalty.grouper.Grouper;
import delfos.group.grs.penalty.grouper.GrouperByIdItem;
import delfos.rs.RecommenderSystem;
import delfos.rs.RecommenderSystemBuildingProgressListener;
import delfos.rs.recommendation.Recommendation;

/**
 * Agregación de valoraciones de los usuarios usando múltiples agregaciones y
 * una función penalty para elegir la agregación que se aplica para cada item.
 *
* @author Jorge Castro Gallardo
 *
 * @version 2-julio-2014
 */
public class PenaltyGRS_Recommendations extends GroupRecommenderSystemAdapter<SingleRecommenderSystemModel, GroupOfUsers> {

    private static final long serialVersionUID = 1L;

    private PenaltyFunction oldPenalty = new PenaltyWholeMatrix();
    private Grouper oldGrouper = new GrouperByIdItem(3);

    public PenaltyGRS_Recommendations() {
        super();
        addParameter(SINGLE_USER_RECOMMENDER);
        addParameter(PENALTY);
        addParameter(ITEM_GROUPER);

        addParammeterListener(() -> {
            PenaltyFunction newPenalty = (PenaltyFunction) getParameterValue(PENALTY);
            Grouper newGrouper = (Grouper) getParameterValue(ITEM_GROUPER);

            String newAlias = getAlias();

            String oldAliasOldParameters
                    = this.getClass().getSimpleName()
                    + "_" + oldPenalty.getAlias()
                    + "_" + oldGrouper.getAlias();

            String newAliasNewParameters
                    = this.getClass().getSimpleName()
                    + "_" + newPenalty.getAlias()
                    + "_" + newGrouper.getAlias();

            if (!oldAliasOldParameters.equals(newAliasNewParameters)) {
                oldPenalty = newPenalty;
                oldGrouper = newGrouper;
                setAlias(newAliasNewParameters);
            }
        });

        String oldAliasOldParameters
                = this.getClass().getSimpleName()
                + "_" + oldPenalty.getAlias()
                + "_" + oldGrouper.getAlias();

        setAlias(oldAliasOldParameters);
    }

    public PenaltyGRS_Recommendations(RecommenderSystem<? extends Object> singleUserRecommender, PenaltyFunction penaltyFunction, Grouper grouper) {
        this();
        setParameterValue(SINGLE_USER_RECOMMENDER, singleUserRecommender);
        setParameterValue(PENALTY, penaltyFunction);
        setParameterValue(ITEM_GROUPER, grouper);
    }

    @Override
    public SingleRecommenderSystemModel build(DatasetLoader<? extends Rating> datasetLoader) throws CannotLoadRatingsDataset, CannotLoadContentDataset {

        RecommenderSystemBuildingProgressListener buildListener = (String actualJob, int percent, long remainingTime) -> {
            fireBuildingProgressChangedEvent(actualJob, percent, remainingTime);
        };

        getSingleUserRecommender().addBuildingProgressListener(buildListener);
        Object build = getSingleUserRecommender().build(datasetLoader);
        getSingleUserRecommender().removeBuildingProgressListener(buildListener);
        return new SingleRecommenderSystemModel(build);
    }

    @Override
    public GroupOfUsers buildGroupModel(DatasetLoader<? extends Rating> datasetLoader, SingleRecommenderSystemModel recommenderSystemModel, GroupOfUsers groupOfUsers) throws UserNotFound, CannotLoadRatingsDataset {
        return new GroupOfUsers(groupOfUsers.getGroupMembers());
    }

    @Override
    public List<Recommendation> recommendOnly(DatasetLoader<? extends Rating> datasetLoader, SingleRecommenderSystemModel recommenderSystemModel, GroupOfUsers groupModel, GroupOfUsers groupOfUsers, Collection<Integer> idItemList) throws UserNotFound, ItemNotFound, CannotLoadRatingsDataset, CannotLoadContentDataset {

        PenaltyFunction penaltyFunction = (PenaltyFunction) getParameterValue(PENALTY);
        Grouper grouper = (Grouper) getParameterValue(ITEM_GROUPER);

        RecommenderSystem singleUserRecommender = getSingleUserRecommender();
        Map<Integer, List<Recommendation>> recommendationsLists_byMember
                = performSingleUserRecommendations(
                        groupOfUsers.getGroupMembers(),
                        singleUserRecommender, datasetLoader,
                        recommenderSystemModel,
                        idItemList);

        Map<Integer, Map<Integer, Number>> predictionsByMember = new TreeMap<>();
        recommendationsLists_byMember.keySet().stream().map((idUser) -> {
            predictionsByMember.put(idUser, new TreeMap<>());
            return idUser;
        }).forEach((idUser) -> {
            recommendationsLists_byMember.get(idUser).stream().forEach((recommendation) -> {
                int idItem = recommendation.getIdItem();
                Number prediction = recommendation.getPreference();
                predictionsByMember.get(idUser).put(idItem, prediction);
            });
        });

        Map<Integer, Map<Integer, Number>> predictionsByItem = DatasetUtilities.transformIndexedByUsersToIndexedByItems_Map(predictionsByMember);

        Map<Integer, Number> aggregatedPredictions = PenaltyMethods.aggregateWithPenalty_combinatory(
                predictionsByItem,
                groupOfUsers,
                penaltyFunction,
                grouper);

        List<Recommendation> groupRecommendations = new ArrayList<>(aggregatedPredictions.size());

        aggregatedPredictions.keySet().stream().forEach((idItem) -> {
            groupRecommendations.add(new Recommendation(idItem, aggregatedPredictions.get(idItem)));
        });

        Collections.sort(groupRecommendations);

        return groupRecommendations;
    }

    @Override
    public boolean isRatingPredictorRS() {
        return true;
    }

    public RecommenderSystem getSingleUserRecommender() {
        return (RecommenderSystem) getParameterValue(SINGLE_USER_RECOMMENDER);
    }

}