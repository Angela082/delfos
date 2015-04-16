package delfos.group.grs.aggregation;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import delfos.common.aggregationoperators.AggregationOperator;
import delfos.common.aggregationoperators.Mean;
import delfos.common.exceptions.dataset.CannotLoadContentDataset;
import delfos.common.exceptions.dataset.CannotLoadRatingsDataset;
import delfos.common.exceptions.dataset.items.ItemNotFound;
import delfos.common.exceptions.dataset.users.UserNotFound;
import delfos.common.parallelwork.MultiThreadExecutionManager;
import delfos.common.parameters.Parameter;
import delfos.common.parameters.restriction.ParameterOwnerRestriction;
import delfos.common.parameters.restriction.RecommenderSystemParameterRestriction;
import delfos.dataset.basic.rating.Rating;
import delfos.dataset.basic.loader.types.DatasetLoader;
import delfos.experiment.casestudy.parallel.SingleUserRecommendationTask;
import delfos.experiment.casestudy.parallel.SingleUserRecommendationTaskExecutor;
import delfos.group.groupsofusers.GroupOfUsers;
import delfos.group.grs.GroupRecommenderSystemAdapter;
import delfos.group.grs.SingleRecommenderSystemModel;
import delfos.rs.RecommenderSystem;
import delfos.rs.RecommenderSystemBuildingProgressListener;
import delfos.rs.collaborativefiltering.knn.memorybased.KnnMemoryBasedCFRS;
import delfos.rs.recommendation.Recommendation;
//TODO: Completar Javadoc

/**
 * Implementa un sistema de recomendación a grupos que agrega las
 * recomendaciones de cada individuo para componer una lista de recomendaciones
 * para el grupo.
 *
 * @author Jorge Castro Gallardo
 *
 * @version 1.0 Unknown date
 * @version 1.1 9-Mayo-2013
 */
public class AggregationOfIndividualRecommendations extends GroupRecommenderSystemAdapter<SingleRecommenderSystemModel, GroupOfUsers> {

    private static final long serialVersionUID = 1L;
    /**
     * Especifica el sistema de recomendación single user que se extiende para
     * ser usado en recomendación a grupos.
     */
    public static final Parameter SINGLE_USER_RECOMMENDER = new Parameter(
            "SINGLE_USER_RECOMMENDER",
            new RecommenderSystemParameterRestriction(new KnnMemoryBasedCFRS(), RecommenderSystem.class),
            "Especifica el sistema de recomendación single user que se extiende "
            + "para ser usaso en recomendación a grupos.");
    /**
     * Especifica la técnica de agregación para agregar los ratings de los
     * usuarios y formar el perfil del grupo.
     */
    public static final Parameter AGGREGATION_OPPERATOR = new Parameter(
            "AGGREGATION_OPPERATOR",
            new ParameterOwnerRestriction(AggregationOperator.class, new Mean()),
            "Especifica la técnica de agregación para agregar los ratings de "
            + "los usuarios y formar el perfil del grupo.");
    private AggregationOperator oldAggregationOperator = new Mean();

    public AggregationOfIndividualRecommendations() {
        super();
        addParameter(SINGLE_USER_RECOMMENDER);
        addParameter(AGGREGATION_OPPERATOR);

        addParammeterListener(() -> {
            AggregationOperator newAggregationOperator = (AggregationOperator) getParameterValue(AGGREGATION_OPPERATOR);

            String newAlias = getAlias();

            String oldAliasOldParameters
                    = "AOI_Recommendations"
                    + "_" + oldAggregationOperator.getAlias();

            String newAliasNewParameters
                    = "AOI_Recommendations"
                    + "_" + newAggregationOperator.getAlias();

            if (!oldAliasOldParameters.equals(newAliasNewParameters)) {
                oldAggregationOperator = newAggregationOperator;
                setAlias(newAliasNewParameters);
            }
        });

        setAlias("AOI_Recommendations_" + oldAggregationOperator.getAlias());
    }

    public AggregationOfIndividualRecommendations(RecommenderSystem recommenderSystem, AggregationOperator aggregationOperator) {

        this();
        setParameterValue(SINGLE_USER_RECOMMENDER, recommenderSystem);
        setParameterValue(AGGREGATION_OPPERATOR, aggregationOperator);
    }

    @Override
    public boolean isRatingPredictorRS() {
        return getSingleUserRecommender().isRatingPredictorRS();
    }

    public AggregationOperator getAggregationOperator() {
        return (AggregationOperator) getParameterValue(AGGREGATION_OPPERATOR);
    }

    public RecommenderSystem getSingleUserRecommender() {
        return (delfos.rs.RecommenderSystem) getParameterValue(SINGLE_USER_RECOMMENDER);
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
    public GroupOfUsers buildGroupModel(DatasetLoader<? extends Rating> datasetLoader, SingleRecommenderSystemModel recommenderSystemModel, GroupOfUsers groupOfUsers) throws UserNotFound, CannotLoadRatingsDataset, CannotLoadContentDataset {
        return new GroupOfUsers(groupOfUsers.getGroupMembers());
    }

    @Override
    public List<Recommendation> recommendOnly(DatasetLoader<? extends Rating> datasetLoader, SingleRecommenderSystemModel recommenderSystemModel, GroupOfUsers groupModel, GroupOfUsers groupOfUsers, Collection<Integer> idItemList) throws UserNotFound, ItemNotFound, CannotLoadRatingsDataset, CannotLoadContentDataset {

        RecommenderSystem singleUserRecommender = getSingleUserRecommender();
        Map<Integer, List<Recommendation>> recommendationsLists_byMember = performSingleUserRecommendations(groupOfUsers.getGroupMembers(), singleUserRecommender, datasetLoader, recommenderSystemModel, idItemList);

        List<Recommendation> groupRecommendations = aggregateLists(getAggregationOperator(), recommendationsLists_byMember);
        return groupRecommendations;
    }

    public static List<Recommendation> aggregateLists(AggregationOperator aggregationOperator, Map<Integer, List<Recommendation>> groupUtilityList) {

        //Reordeno las predicciones.
        Map<Integer, Collection<Number>> prediction_byItem = new TreeMap<>();
        for (int idUser : groupUtilityList.keySet()) {
            for (Recommendation r : groupUtilityList.get(idUser)) {
                int idItem = r.getIdItem();
                Number preference = r.getPreference();

                if (!prediction_byItem.containsKey(idItem)) {
                    prediction_byItem.put(idItem, new LinkedList<>());
                }

                prediction_byItem.get(idItem).add(preference);
            }
        }

        //agrego las predicciones de cada item.
        ArrayList<Recommendation> recommendations = new ArrayList<>();
        for (int idItem : prediction_byItem.keySet()) {
            Collection<Number> predictionsThisItem = prediction_byItem.get(idItem);

            if (prediction_byItem.isEmpty()) {
                continue;
            }

            float aggregateValue = aggregationOperator.aggregateValues(predictionsThisItem);
            recommendations.add(new Recommendation(idItem, aggregateValue));
        }

        //Ordeno las recomendaciones.
        Collections.sort(recommendations);

        return recommendations;
    }

    public static Map<Integer, List<Recommendation>> performSingleUserRecommendations(Collection<Integer> users, RecommenderSystem singleUserRecommender, DatasetLoader<? extends Rating> datasetLoader, SingleRecommenderSystemModel recommenderSystemModel, Collection<Integer> idItemList) throws UserNotFound {

        List<SingleUserRecommendationTask> tasks = new LinkedList<>();
        for (int idUser : users) {
            tasks.add(
                    new SingleUserRecommendationTask(
                            singleUserRecommender,
                            datasetLoader,
                            recommenderSystemModel.getRecommenderSystemModel(),
                            idUser,
                            idItemList));

        }
        MultiThreadExecutionManager<SingleUserRecommendationTask> executionManager = new MultiThreadExecutionManager<>(
                "Prediction of each member",
                tasks,
                SingleUserRecommendationTaskExecutor.class);
        executionManager.run();
        Map<Integer, List<Recommendation>> singleUserRecomendationLists = new TreeMap<>();
        for (SingleUserRecommendationTask task : executionManager.getAllFinishedTasks()) {
            List<Recommendation> recommendations = task.getRecommendationList();
            if (recommendations == null) {
                throw new UserNotFound(task.getIdUser());
            }
            singleUserRecomendationLists.put(task.getIdUser(), recommendations);
        }
        return singleUserRecomendationLists;
    }
}