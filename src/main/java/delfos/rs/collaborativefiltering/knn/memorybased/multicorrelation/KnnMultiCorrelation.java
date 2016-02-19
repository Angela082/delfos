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
package delfos.rs.collaborativefiltering.knn.memorybased.multicorrelation;

import delfos.common.Global;
import delfos.common.exceptions.CouldNotPredictRating;
import delfos.common.exceptions.dataset.CannotLoadRatingsDataset;
import delfos.common.exceptions.dataset.items.ItemNotFound;
import delfos.common.exceptions.dataset.users.UserNotFound;
import delfos.common.parallelwork.MultiThreadExecutionManager;
import delfos.common.parameters.Parameter;
import delfos.common.parameters.restriction.ParameterOwnerRestriction;
import delfos.dataset.basic.loader.types.DatasetLoader;
import delfos.dataset.basic.rating.Rating;
import delfos.dataset.basic.rating.RatingsDataset;
import delfos.rs.collaborativefiltering.CollaborativeRecommender;
import delfos.rs.collaborativefiltering.knn.KnnCollaborativeRecommender;
import delfos.rs.collaborativefiltering.knn.MatchRating;
import delfos.rs.collaborativefiltering.knn.RecommendationEntity;
import delfos.rs.collaborativefiltering.predictiontechniques.PredictionTechnique;
import delfos.rs.collaborativefiltering.profile.Neighbor;
import delfos.rs.persistence.DatabasePersistence;
import delfos.rs.persistence.FailureInPersistence;
import delfos.rs.recommendation.Recommendation;
import delfos.similaritymeasures.PearsonCorrelationCoefficient;
import delfos.similaritymeasures.useruser.RelevanceFactor;
import delfos.similaritymeasures.useruser.UserUserSimilarity;
import delfos.similaritymeasures.useruser.UserUserSimilarityWrapper;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

/**
 *
 * @version 08-may-2014
 * @author jcastro-inf ( https://github.com/jcastro-inf )
 */
public class KnnMultiCorrelation extends CollaborativeRecommender<KnnMultiCorrelation_Model> {

    static {

        ParameterOwnerRestriction similarityMeasure = new ParameterOwnerRestriction(
                UserUserSimilarity.class,
                new UserUserSimilarityWrapper(new PearsonCorrelationCoefficient()));

        ParameterOwnerRestriction preFilterSimilarityMeasure = new ParameterOwnerRestriction(
                UserUserSimilarity.class,
                new RelevanceFactor(30));

        PREFILTER_SIMILARITY_MEASURE = new Parameter(
                "PreFilter_Similarity_measure",
                preFilterSimilarityMeasure);

        MULTI_CORRELATION_SIMILARITY_MEASURE = new Parameter(
                "MULTI_CORRELATION_SIMILARITY_MEASURE",
                similarityMeasure);

    }

    private static final long serialVersionUID = 1L;
    /**
     * Parámetro para indicar la medida de similitud que el sistema de
     * recomendación utiliza para filtrar los vecinos más cercanos. Si no se
     * modifica, su valor por defecto es RelevanceFactor(30)
     */
    public static final Parameter PREFILTER_SIMILARITY_MEASURE;

    /**
     * Parámetro para indicar la medida de similitud que el sistema de
     * recomendación utiliza para el cálculo de los vecinos más cercanos. Si no
     * se modifica, su valor por defecto es el coeficiente de correlación de
     * pearson.
     */
    public static final Parameter MULTI_CORRELATION_SIMILARITY_MEASURE;

    public KnnMultiCorrelation() {
        super();
        addParameter(KnnCollaborativeRecommender.NEIGHBORHOOD_SIZE);
        addParameter(PREFILTER_SIMILARITY_MEASURE);
        addParameter(MULTI_CORRELATION_SIMILARITY_MEASURE);
        addParameter(KnnCollaborativeRecommender.PREDICTION_TECHNIQUE);
    }

    public KnnMultiCorrelation(
            UserUserSimilarity similarityMeasure,
            int neighborhoodSize,
            PredictionTechnique predictionTechnique) {

        this();

        setParameterValue(MULTI_CORRELATION_SIMILARITY_MEASURE, similarityMeasure);
        setParameterValue(KnnCollaborativeRecommender.NEIGHBORHOOD_SIZE, neighborhoodSize);
        setParameterValue(KnnCollaborativeRecommender.PREDICTION_TECHNIQUE, predictionTechnique);
    }

    @Override
    public KnnMultiCorrelation_Model buildRecommendationModel(DatasetLoader<? extends Rating> datasetLoader) {
        //No se necesitan perfiles porque se examina la base de datos directamente
        return new KnnMultiCorrelation_Model();
    }

    @Override
    public Collection<Recommendation> recommendToUser(DatasetLoader<? extends Rating> datasetLoader, KnnMultiCorrelation_Model model, Integer idUser, java.util.Set<Integer> candidateItems) throws UserNotFound {

        if (Global.isVerboseAnnoying()) {
            Global.showInfoMessage(new Date().toGMTString() + " --> Recommending for user '" + idUser + "'\n");
        }

        try {
            List<Neighbor> neighbors = getNeighbors(datasetLoader, idUser);

            Collection<Recommendation> ret = recommendWithNeighbors(datasetLoader.getRatingsDataset(), idUser, neighbors, candidateItems);
            if (Global.isVerboseAnnoying()) {
                Global.showInfoMessage("Finished recommendations for user '" + idUser + "'\n");
            }
            return ret;
        } catch (CannotLoadRatingsDataset ex) {
            throw new IllegalArgumentException(ex);
        }
    }

    /**
     * Calcula los vecinos mas cercanos del usuario indicado por parámetro. Para
     * ello, utiliza los valores especificados en los parámetros del algoritmo y
     * los datasets de valoraciones y productos que se indicaron al sistema
     *
     * @param datasetLoader Dataset de entrada.
     * @param idUser id del usuario para el que se calculan sus vecinos
     * @return Devuelve una lista ordenada por similitud de los vecinos más
     * cercanos al usuario indicado
     * @throws UserNotFound Si el usuario indicado no existe en el conjunto de
     * datos
     */
    public List<Neighbor> getNeighbors(DatasetLoader<? extends Rating> datasetLoader, int idUser) throws UserNotFound {

        List<KnnMultiCorrelation_Task> tasks = new ArrayList<>();
        for (int idNeighbor : datasetLoader.getRatingsDataset().allUsers()) {
            try {
                tasks.add(new KnnMultiCorrelation_Task(datasetLoader, idUser, idNeighbor, this));
            } catch (UserNotFound ex) {
            }
        }

        MultiThreadExecutionManager<KnnMultiCorrelation_Task> multiThreadExecutionManager = new MultiThreadExecutionManager<>(
                this.getAlias() + ":computeNeighborsOf" + idUser,
                tasks,
                KnnMultiCorrelation_SingleNeighborCalculator.class);

        multiThreadExecutionManager.run();

        List<Neighbor> ret = new ArrayList<>();
        //Recompongo los resultados.
        for (KnnMultiCorrelation_Task task : multiThreadExecutionManager.getAllFinishedTasks()) {
            Neighbor neighbor = task.getNeighbor();
            if (neighbor != null) {
                ret.add(neighbor);
            }
        }
        Collections.sort(ret);

        return ret;
    }

    /**
     * Devuelva las recomendaciones, teniendo en cuenta sólo los productos
     * indicados por parámetro, para el usuario activo a partir de los vecinos
     * indicados por parámetro
     *
     * @param ratingsDataset Conjunto de valoraciones.
     * @param idUser Id del usuario activo
     * @param vecinos Vecinos del usuario activo
     * @param candidateItems Lista de productos que se consideran recomendables, es
     * decir, que podrían ser recomendados si la predicción es alta
     * @return Lista de recomendaciones para el usuario, ordenadas por
     * valoracion predicha.
     * @throws UserNotFound Si el usuario activo o alguno de los vecinos
     * indicados no se encuentra en el dataset.
     */
    public Collection<Recommendation> recommendWithNeighbors(
            RatingsDataset<? extends Rating> ratingsDataset,
            Integer idUser,
            List<Neighbor> vecinos,
            Collection<Integer> candidateItems)
            throws UserNotFound {

        PredictionTechnique predictionTechnique_ = (PredictionTechnique) getParameterValue(KnnCollaborativeRecommender.PREDICTION_TECHNIQUE);

        //Predicción de la valoración
        Collection<Recommendation> recommendationList = new LinkedList<>();

        int numVecinos = (Integer) getParameterValue(KnnCollaborativeRecommender.NEIGHBORHOOD_SIZE);

        for (int idItem : candidateItems) {
            Collection<MatchRating> match = new LinkedList<>();

            int numNeighborsUsed = 0;

            try {
                Map<Integer, ? extends Rating> itemRatingsRated = ratingsDataset.getItemRatingsRated(idItem);
                for (Neighbor neighbor : vecinos) {

                    Rating rating = itemRatingsRated.get(neighbor.getIdNeighbor());
                    if (rating != null) {
                        match.add(new MatchRating(RecommendationEntity.ITEM, neighbor.getIdNeighbor(), idItem, rating.getRatingValue(), neighbor.getSimilarity()));
                        numNeighborsUsed++;
                    }

                    if (numNeighborsUsed >= numVecinos) {
                        break;
                    }
                }

                try {
                    double predicted = predictionTechnique_.predictRating(idUser, idItem, match, ratingsDataset);
                    recommendationList.add(new Recommendation(idItem, predicted));

                } catch (CouldNotPredictRating ex) {
                }
            } catch (ItemNotFound ex) {
                Global.showError(ex);
            }
        }

        return recommendationList;
    }

    @Override
    public KnnMultiCorrelation_Model loadRecommendationModel(DatabasePersistence databasePersistence, Collection<Integer> users, Collection<Integer> items) throws FailureInPersistence {
        return new KnnMultiCorrelation_Model();
    }

    @Override
    public void saveRecommendationModel(DatabasePersistence databasePersistence, KnnMultiCorrelation_Model model) throws FailureInPersistence {
        //No hay modelo que guardar.

    }
}