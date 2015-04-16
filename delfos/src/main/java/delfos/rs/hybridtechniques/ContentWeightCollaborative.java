package delfos.rs.hybridtechniques;

import java.util.Collection;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import delfos.common.aggregationoperators.AggregationOperator;
import delfos.common.aggregationoperators.Mean;
import delfos.common.aggregationoperators.TwoValuesAggregator;
import delfos.common.exceptions.dataset.CannotLoadContentDataset;
import delfos.common.exceptions.dataset.CannotLoadRatingsDataset;
import delfos.common.exceptions.dataset.CannotLoadUsersDataset;
import delfos.common.exceptions.dataset.items.ItemNotFound;
import delfos.common.exceptions.dataset.users.UserNotFound;
import delfos.common.exceptions.ratings.NotEnoughtUserInformation;
import delfos.common.parameters.Parameter;
import delfos.common.parameters.restriction.ParameterOwnerRestriction;
import delfos.common.parameters.restriction.RecommenderSystemParameterRestriction;
import delfos.dataset.basic.rating.Rating;
import delfos.dataset.basic.loader.types.DatasetLoader;
import delfos.rs.RecommenderSystem;
import delfos.rs.RecommenderSystemBuildingProgressListener;
import delfos.rs.collaborativefiltering.CollaborativeRecommender;
import delfos.rs.collaborativefiltering.knn.memorybased.KnnMemoryBasedCFRS;
import delfos.rs.contentbased.ContentBasedRecommender;
import delfos.rs.contentbased.vsm.booleanvsm.tfidf.TfIdfCBRS;
import delfos.rs.recommendation.Recommendation;

/**
 * Sistema de recomendación híbrido que utiliza como fuente un sistema de
 * recomendación colaborativo y otro basado en contenido.
 *
 * @author Jorge Castro Gallardo (Universidad de Jaén, Sinbad2)
 *
 * @version 1.1 21-Jan-2013
 * @version 1.0 Unknow date
 */
public class ContentWeightCollaborative extends HybridRecommender<HybridRecommenderSystemModel> {

    private static final long serialVersionUID = -3387516993124229948L;

    public static final Parameter AGGREGATION_OPERATOR = new Parameter(
            "AGGREGATION_OPERATOR",
            new ParameterOwnerRestriction(AggregationOperator.class, new Mean()));

    static {
        @SuppressWarnings({"rawtypes", "unchecked"})
        Class<? extends RecommenderSystem>[] values = (Class<? extends RecommenderSystem>[]) new Class[1];
        values[0] = CollaborativeRecommender.class;
        COLLABORATIVE_TECHNIQUE = new Parameter("Collaborative_technique", new RecommenderSystemParameterRestriction(new KnnMemoryBasedCFRS(), values));

        values = (Class<? extends RecommenderSystem<Object>>[]) new Class[1];
        values[0] = ContentBasedRecommender.class;
        CONTENT_BASED_TECHNIQUE = new Parameter("Content_based_technique", new RecommenderSystemParameterRestriction(new TfIdfCBRS(), values));
    }
    public static final Parameter COLLABORATIVE_TECHNIQUE;
    public static final Parameter CONTENT_BASED_TECHNIQUE;

    public ContentWeightCollaborative() {
        addParameter(AGGREGATION_OPERATOR);
        addParameter(COLLABORATIVE_TECHNIQUE);
        addParameter(CONTENT_BASED_TECHNIQUE);

    }

    @Override
    public HybridRecommenderSystemModel build(DatasetLoader<? extends Rating> datasetLoader) throws CannotLoadRatingsDataset, CannotLoadContentDataset, CannotLoadUsersDataset {
        ContentBasedRecommender<Object, Object> contentBasedAlgorithm = (ContentBasedRecommender<Object, Object>) getParameterValue(CONTENT_BASED_TECHNIQUE);

        RecommenderSystemBuildingProgressListener contentBasedListener = (String actualJob, int percent, long remainingSeconds) -> {
            ContentWeightCollaborative.this.fireBuildingProgressChangedEvent(actualJob, percent / 2, -1);
        };
        contentBasedAlgorithm.addBuildingProgressListener(contentBasedListener);
        Object contentBasedModel = contentBasedAlgorithm.build(datasetLoader);

        CollaborativeRecommender collaborativeFilteringTechnique = (CollaborativeRecommender) getParameterValue(COLLABORATIVE_TECHNIQUE);
        RecommenderSystemBuildingProgressListener collaborativeListener = (String actualJob, int percent, long remainingSeconds) -> {
            ContentWeightCollaborative.this.fireBuildingProgressChangedEvent(actualJob, percent / 2 + 50, -1);
        };
        collaborativeFilteringTechnique.addBuildingProgressListener(collaborativeListener);
        Object collaborativeModel = collaborativeFilteringTechnique.build(datasetLoader);

        return new HybridRecommenderSystemModel(contentBasedModel, collaborativeModel);
    }

    @Override
    public List<Recommendation> recommendOnly(DatasetLoader<? extends Rating> datasetLoader, HybridRecommenderSystemModel model, Integer idUser, Collection<Integer> idItemList) throws UserNotFound, CannotLoadRatingsDataset, CannotLoadContentDataset, ItemNotFound, NotEnoughtUserInformation {

        ContentBasedRecommender<Object, Object> contentBasedAlgorithm = (ContentBasedRecommender<Object, Object>) getParameterValue(CONTENT_BASED_TECHNIQUE);
        CollaborativeRecommender<Object> collaborativeFilteringTechnique = (CollaborativeRecommender<Object>) getParameterValue(COLLABORATIVE_TECHNIQUE);

        List<Recommendation> content = contentBasedAlgorithm.recommendOnly(datasetLoader, model.getModel(0), idUser, idItemList);
        List<Recommendation> collaborative = collaborativeFilteringTechnique.recommendOnly(datasetLoader, model.getModel(1), idUser, idItemList);

        return joinRecommendationLists(content, collaborative);
    }

    @Override
    protected List<RecommenderSystem<Object>> getHybridizedRecommenderSystems() {
        ContentBasedRecommender<Object, Object> contentBasedAlgorithm = (ContentBasedRecommender<Object, Object>) getParameterValue(CONTENT_BASED_TECHNIQUE);
        CollaborativeRecommender<Object> collaborativeFilteringTechnique = (CollaborativeRecommender<Object>) getParameterValue(COLLABORATIVE_TECHNIQUE);
        List<RecommenderSystem<Object>> ret = new LinkedList<>();

        ret.add(contentBasedAlgorithm);
        ret.add(collaborativeFilteringTechnique);
        return ret;
    }

    /**
     * Une dos listas de recomendación utilizando el operador definido.
     *
     * @param l1 Lista de recomendaciones.
     * @param l2 Lista de recomendaciones.
     * @return Lista de recomendaciones unida.
     */
    private List<Recommendation> joinRecommendationLists(List<Recommendation> l1, List<Recommendation> l2) {
        int size = Math.max(l1.size(), l2.size());

        class rank implements Comparable<rank> {

            public rank(int idItem) {
                this.idItem = idItem;
            }
            final int idItem;
            float content = 0;
            float collaborative = 0;
            float valorCombinado = 0;

            @Override
            public int compareTo(rank o) {
                if (valorCombinado < o.valorCombinado) {
                    return 1;
                } else {
                    if (valorCombinado > o.valorCombinado) {
                        return -1;
                    } else {
                        return 0;
                    }
                }
            }

            @Override
            public String toString() {
                return idItem + "->" + valorCombinado;
            }
        }
        TwoValuesAggregator aggregationOperator = (TwoValuesAggregator) getParameterValue(AGGREGATION_OPERATOR);

        Map<Integer, rank> mapa = new TreeMap<>();
        int i = 0;
        for (Recommendation r : l1) {
            rank ranking = new rank(r.getIdItem());
            ranking.content = 1 - (((float) i) / size);
            mapa.put(r.getIdItem(), ranking);
            i++;
        }

        i = 0;
        for (Recommendation r : l2) {
            rank ranking;
            if (mapa.containsKey(r.getIdItem())) {
                ranking = mapa.get(r.getIdItem());
            } else {
                ranking = new rank(r.getIdItem());
            }
            ranking.collaborative = 1 - (((float) i) / size);
            mapa.put(r.getIdItem(), ranking);
            i++;
        }

        List<rank> finalList = new LinkedList<>(mapa.values());

        finalList.stream().forEach((r) -> {
            r.valorCombinado = aggregationOperator.aggregateTwoValues(r.collaborative, r.content);
        });

        Collections.sort(finalList);

        List<Recommendation> ret = new LinkedList<>();

        finalList.stream().filter((r) -> (r.valorCombinado != 0)).forEach((r) -> {
            ret.add(new Recommendation(r.idItem, r.valorCombinado));
        });
        return ret;
    }
}