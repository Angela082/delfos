/*
 * Copyright (C) 2016 jcastro
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package delfos.group.grs.aggregation;

import delfos.common.Global;
import delfos.common.aggregationoperators.AggregationOperator;
import delfos.common.aggregationoperators.Mean;
import delfos.common.exceptions.dataset.CannotLoadContentDataset;
import delfos.common.exceptions.dataset.CannotLoadRatingsDataset;
import delfos.common.exceptions.dataset.items.ItemNotFound;
import delfos.common.exceptions.dataset.users.UserNotFound;
import delfos.common.exceptions.ratings.NotEnoughtUserInformation;
import delfos.common.parameters.Parameter;
import delfos.common.parameters.restriction.ParameterOwnerRestriction;
import delfos.common.parameters.restriction.RecommenderSystemParameterRestriction;
import delfos.dataset.basic.loader.types.DatasetLoader;
import delfos.dataset.basic.rating.Rating;
import delfos.dataset.generated.modifieddatasets.PseudoUserRatingsDataset;
import delfos.dataset.loaders.given.DatasetLoaderGivenRatingsDataset;
import delfos.dataset.util.DatasetPrinterDeprecated;
import delfos.dataset.util.DatasetUtilities;
import delfos.factories.AggregationOperatorFactory;
import delfos.group.groupsofusers.GroupOfUsers;
import delfos.group.grs.GroupRecommenderSystemAdapter;
import delfos.group.grs.SingleRecommendationModel;
import delfos.rs.RecommendationModelBuildingProgressListener;
import delfos.rs.RecommenderSystem;
import delfos.rs.collaborativefiltering.knn.memorybased.KnnMemoryBasedCFRS;
import delfos.rs.explanation.GroupModelWithExplanation;
import delfos.rs.recommendation.Recommendation;
import java.util.Arrays;
import java.util.Collection;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;

/**
 * Implementa un sistema de recomendación a grupos que agrega las valoraciones
 * de cada individuo para formar un perfil asociado al grupo. Una vez obtenido
 * este perfil, calcula las recomendaciones al grupo como si éste se tratara de
 * un usuario individual.
 *
 * La técnica utilizada para la agregación de preferencias es calcular la media
 * de las valoraciones de los usuarios en cada producto
 *
 * @author jcastro-inf ( https://github.com/jcastro-inf )
 *
 * @version Unknown date.
 * @version 12-Enero-2014
 */
public class AggregationOfIndividualRatings
        extends GroupRecommenderSystemAdapter<SingleRecommendationModel, GroupModelWithExplanation<GroupModelPseudoUser, ? extends Object>> {

    private static final long serialVersionUID = 1L;
    /**
     * "Especifica el sistema de recomendación single user que se extiende para
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
    public static final Parameter AGGREGATION_OPERATOR = new Parameter(
            "AGGREGATION_OPERATOR",
            new ParameterOwnerRestriction(AggregationOperatorFactory.getInstance(), new Mean()),
            "Especifica la técnica de agregación para agregar los ratings de "
            + "los usuarios y formar el perfil del grupo.");
    private AggregationOperator oldAggregationOperator = new Mean();

    public AggregationOfIndividualRatings() {
        super();
        addParameter(SINGLE_USER_RECOMMENDER);
        addParameter(AGGREGATION_OPERATOR);

        addParammeterListener(() -> {
            AggregationOperator newAggregationOperator = (AggregationOperator) getParameterValue(AGGREGATION_OPERATOR);

            String newAlias = getAlias();

            String oldAliasOldParameters
                    = "AOI_Ratings"
                    + "_" + oldAggregationOperator.getAlias();

            String newAliasNewParameters
                    = "AOI_Ratings"
                    + "_" + newAggregationOperator.getAlias();

            if (!oldAliasOldParameters.equals(newAliasNewParameters)) {
                oldAggregationOperator = newAggregationOperator;
                setAlias(newAliasNewParameters);
            }
        });

        setAlias("AOI_Ratings_" + oldAggregationOperator.getAlias());
    }

    public AggregationOfIndividualRatings(RecommenderSystem singleUserRecommender) {

        this();
        setParameterValue(SINGLE_USER_RECOMMENDER, singleUserRecommender);
    }

    public AggregationOfIndividualRatings(
            RecommenderSystem singleUserRecommender,
            AggregationOperator aggregationOperator) {

        this();
        setParameterValue(SINGLE_USER_RECOMMENDER, singleUserRecommender);
        setParameterValue(AGGREGATION_OPERATOR, aggregationOperator);
    }

    @Override
    public SingleRecommendationModel buildRecommendationModel(DatasetLoader<? extends Rating> datasetLoader) throws CannotLoadRatingsDataset, CannotLoadContentDataset {

        RecommendationModelBuildingProgressListener buildListener = this::fireBuildingProgressChangedEvent;
        getSingleUserRecommender().addRecommendationModelBuildingProgressListener(buildListener);
        Object build = getSingleUserRecommender().buildRecommendationModel(datasetLoader);
        getSingleUserRecommender().removeRecommendationModelBuildingProgressListener(buildListener);
        return new SingleRecommendationModel(build);
    }

    @Override
    public GroupModelWithExplanation<GroupModelPseudoUser, ? extends Object> buildGroupModel(DatasetLoader<? extends Rating> datasetLoader, SingleRecommendationModel RecommendationModel, GroupOfUsers groupOfUsers) throws UserNotFound, CannotLoadRatingsDataset, CannotLoadContentDataset, NotEnoughtUserInformation {

        GroupModelWithExplanation<GroupModelPseudoUser, ? extends Object> groupModelWithExplanation;
        AggregationOperator aggregationOperator = getAggregationOperator();
        Map<Integer, Number> groupProfile = getGroupProfile(datasetLoader, aggregationOperator, groupOfUsers);
        groupModelWithExplanation = new GroupModelWithExplanation<>(new GroupModelPseudoUser(groupOfUsers, groupProfile), "No explanantion");

        return groupModelWithExplanation;
    }

    @Override
    public Collection<Recommendation> recommendOnly(
            DatasetLoader<? extends Rating> datasetLoader, SingleRecommendationModel RecommendationModel, GroupModelWithExplanation<GroupModelPseudoUser, ? extends Object> groupModel, GroupOfUsers groupOfUsers, java.util.Set<Integer> candidateItems)
            throws UserNotFound, ItemNotFound, CannotLoadRatingsDataset, CannotLoadContentDataset, NotEnoughtUserInformation {

        //Recojo los parámetros en variables
        RecommenderSystem recommenderSystem = getSingleUserRecommender();
        Map<Integer, Number> groupRatings_Number = groupModel.getGroupModel().getRatings();
        Collection<Recommendation> groupRecom = recommendWithGroupRatings(datasetLoader, recommenderSystem, RecommendationModel, groupRatings_Number, candidateItems);

        return groupRecom;
    }

    @Override
    public boolean isRatingPredictorRS() {
        return true;
    }

    public AggregationOperator getAggregationOperator() {
        return (AggregationOperator) getParameterValue(AGGREGATION_OPERATOR);
    }

    public RecommenderSystem getSingleUserRecommender() {
        return (RecommenderSystem) getParameterValue(SINGLE_USER_RECOMMENDER);
    }

    public static Map<Integer, Number> getGroupProfile(
            DatasetLoader<? extends Rating> datasetLoader,
            AggregationOperator aggregationOperator,
            GroupOfUsers groupOfUsers) throws UserNotFound, CannotLoadRatingsDataset {

        //Generate groupProfile:
        Map<Integer, List<Number>> groupRatingsList = new TreeMap<>();

        for (int idUser : groupOfUsers.getIdMembers()) {
            Map<Integer, ? extends Rating> userRatingsRated = datasetLoader.getRatingsDataset().getUserRatingsRated(idUser);
            userRatingsRated.keySet().stream().map((idItem) -> {
                if (!groupRatingsList.containsKey(idItem)) {
                    groupRatingsList.put(idItem, new LinkedList<>());
                }
                return idItem;
            }).forEach((idItem) -> {
                groupRatingsList.get(idItem).add(userRatingsRated.get(idItem).getRatingValue());
            });
        }

        //Aggregate profiles
        Map<Integer, Number> groupRatings = new TreeMap<>();
        groupRatingsList.keySet().stream().forEach((idItem) -> {
            List<Number> lista = groupRatingsList.get(idItem);
            float aggregateValue = aggregationOperator.aggregateValues(lista);
            groupRatings.put(idItem, aggregateValue);
        });

        return groupRatings;
    }

    public static Collection<Recommendation> recommendWithGroupRatings(
            DatasetLoader<? extends Rating> datasetLoader,
            RecommenderSystem recommenderSystem,
            SingleRecommendationModel RecommendationModel,
            Map<Integer, Number> groupRatings, Set<Integer> candidateItems) throws ItemNotFound, NotEnoughtUserInformation, UserNotFound, CannotLoadContentDataset, CannotLoadRatingsDataset {

        Map<Integer, Rating> groupRatings_Ratings = DatasetUtilities.getUserMap_Rating(-1, groupRatings);
        PseudoUserRatingsDataset<Rating> ratingsDataset_withPseudoUser = new PseudoUserRatingsDataset<>(
                datasetLoader.getRatingsDataset(),
                groupRatings_Ratings);
        final int idGroup = ratingsDataset_withPseudoUser.getIdPseudoUser();
        if (Global.isVerboseAnnoying()) {
            DatasetPrinterDeprecated.printCompactRatingTable(ratingsDataset_withPseudoUser, Arrays.asList(idGroup), ratingsDataset_withPseudoUser.getUserRated(idGroup));
        }
        Collection<Recommendation> groupRecom;
        groupRecom = recommenderSystem.recommendToUser(new DatasetLoaderGivenRatingsDataset(datasetLoader, ratingsDataset_withPseudoUser),
                RecommendationModel.getRecommendationModel(),
                idGroup,
                candidateItems);
        return groupRecom;
    }
}
