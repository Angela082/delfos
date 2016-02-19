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
package delfos.group.grs.benchmark.polylens;

import java.util.Collection;
import java.util.List;
import delfos.common.aggregationoperators.MinimumValue;
import delfos.common.exceptions.dataset.CannotLoadContentDataset;
import delfos.common.exceptions.dataset.CannotLoadRatingsDataset;
import delfos.common.exceptions.dataset.items.ItemNotFound;
import delfos.common.exceptions.dataset.users.UserNotFound;
import delfos.common.parameters.Parameter;
import delfos.common.parameters.ParameterListener;
import delfos.common.parameters.restriction.IntegerParameter;
import delfos.dataset.basic.rating.Rating;
import delfos.dataset.basic.loader.types.DatasetLoader;
import delfos.group.groupsofusers.GroupOfUsers;
import delfos.group.grs.GroupRecommenderSystemAdapter;
import delfos.group.grs.SingleRecommendationModel;
import delfos.group.grs.aggregation.AggregationOfIndividualRecommendations;
import delfos.rs.collaborativefiltering.knn.memorybased.nwr.KnnMemoryBasedNWR;
import delfos.rs.collaborativefiltering.predictiontechniques.WeightedSum;
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
 * @author jcastro-inf ( https://github.com/jcastro-inf )
 * @version 1.0 20-May-2013
 */
public class PolyLens extends GroupRecommenderSystemAdapter<SingleRecommendationModel, GroupOfUsers> {

    private final AggregationOfIndividualRecommendations aggregationOfIndividualRecommendations;

    /**
     * Parámetro para almacenar el número de vecinos que se tienen en cuenta
     * para la predicción de la valoración. Si no se modifica, su valor por
     * defecto es 20
     */
    public static final Parameter neighborhoodSize = new Parameter("Neighborhood_size", new IntegerParameter(1, 9999, 60));

    public PolyLens() {
        final KnnMemoryBasedNWR knnMemory = new KnnMemoryBasedNWR(new PearsonCorrelationCoefficient(), 20, null, false, 1, 60, new WeightedSum());
        aggregationOfIndividualRecommendations = new AggregationOfIndividualRecommendations(knnMemory, new MinimumValue());
        addParameter(neighborhoodSize);
        addParammeterListener(new ParameterListener() {
            @Override
            public void parameterChanged() {
                knnMemory.setParameterValue(KnnMemoryBasedNWR.NEIGHBORHOOD_SIZE, getParameterValue(neighborhoodSize));
            }
        });
    }

    public PolyLens(int neighborhoodSize) {
        this();

        setParameterValue(PolyLens.neighborhoodSize, neighborhoodSize);
    }

    @Override
    public boolean isRatingPredictorRS() {
        return true;
    }

    @Override
    public SingleRecommendationModel buildRecommendationModel(DatasetLoader<? extends Rating> datasetLoader) throws CannotLoadRatingsDataset, CannotLoadContentDataset {
        return aggregationOfIndividualRecommendations.buildRecommendationModel(datasetLoader);
    }

    @Override
    public GroupOfUsers buildGroupModel(DatasetLoader<? extends Rating> datasetLoader, SingleRecommendationModel RecommendationModel, GroupOfUsers groupOfUsers) throws UserNotFound, CannotLoadRatingsDataset, CannotLoadContentDataset {
        return aggregationOfIndividualRecommendations.buildGroupModel(datasetLoader, RecommendationModel, groupOfUsers);
    }

    @Override
    public Collection<Recommendation> recommendOnly(DatasetLoader<? extends Rating> datasetLoader, SingleRecommendationModel RecommendationModel, GroupOfUsers groupModel, GroupOfUsers groupOfUsers, java.util.Set<Integer> candidateItems) throws UserNotFound, ItemNotFound, CannotLoadRatingsDataset, CannotLoadContentDataset {
        return aggregationOfIndividualRecommendations.recommendOnly(datasetLoader, RecommendationModel, groupModel, groupOfUsers, candidateItems);
    }

}