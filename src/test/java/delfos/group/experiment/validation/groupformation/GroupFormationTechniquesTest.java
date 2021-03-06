package delfos.group.experiment.validation.groupformation;

import delfos.common.exceptions.dataset.CannotLoadContentDataset;
import delfos.common.exceptions.dataset.CannotLoadRatingsDataset;
import delfos.common.exceptions.dataset.items.ItemNotFound;
import delfos.common.exceptions.dataset.users.UserNotFound;
import delfos.dataset.basic.item.ContentDataset;
import delfos.dataset.basic.loader.types.DatasetLoader;
import delfos.dataset.basic.rating.Rating;
import delfos.dataset.basic.rating.RatingsDataset;
import delfos.dataset.basic.rating.RelevanceCriteria;
import delfos.dataset.basic.user.User;
import delfos.dataset.basic.user.UsersDatasetAdapter;
import delfos.dataset.generated.random.RandomContentDataset;
import delfos.dataset.generated.random.RandomRatingsDatasetFactory;
import delfos.dataset.loaders.given.DatasetLoaderGivenRatingsContent;
import delfos.experiment.validation.validationtechnique.HoldOut_Ratings;
import delfos.experiment.validation.validationtechnique.NoPartitions;
import delfos.experiment.validation.validationtechnique.ValidationTechnique;
import delfos.group.casestudy.defaultcase.GroupCaseStudy;
import delfos.group.experiment.validation.predictionvalidation.GroupPredictionProtocol;
import delfos.group.experiment.validation.predictionvalidation.HoldOutPrediction;
import delfos.group.factories.GroupEvaluationMeasuresFactory;
import delfos.group.grs.GroupRecommenderSystemAdapter;
import delfos.group.grs.RandomGroupRecommender;
import delfos.group.grs.aggregation.AggregationOfIndividualRecommendations;
import delfos.group.results.groupevaluationmeasures.GroupEvaluationMeasure;
import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedList;
import java.util.List;
import java.util.Random;
import java.util.stream.Collectors;
import static org.junit.Assert.fail;
import org.junit.BeforeClass;
import org.junit.Test;

/**
 * Test para comprobar las técnicas de formación de grupos.
 *
 * @author jcastro-inf ( https://github.com/jcastro-inf )
 * @version 1.0 16-Jan-2013
 */
public class GroupFormationTechniquesTest {

    private static DatasetLoader<? extends Rating> datasetLoader;
    /**
     * Dataset aleatorio que se utiliza en los test.
     */
    private static RatingsDataset<? extends Rating> ratingsDataset;
    /**
     * Dataset de contenido generado aleatoriamente;
     */
    private static ContentDataset contentDataset;
    private static Collection<GroupEvaluationMeasure> groupEvaluationMeasures;
    private static UsersDatasetAdapter usersDataset;

    public GroupFormationTechniquesTest() {
    }

    @BeforeClass
    public static void setUpClass() {
        long seedValue = System.currentTimeMillis();
        //Creo un dataset de valoraciones aleatorio.
        ratingsDataset = RandomRatingsDatasetFactory.createRatingsDatasetWithLoadFactor(1000, 1000, 0.01, Rating.DEFAULT_INTEGER_DOMAIN, seedValue);

        int numNumericFeatures = 5;
        int numNominalFeatures = 5;

        int numValues = 10;

        contentDataset = new RandomContentDataset(ratingsDataset, numNumericFeatures, numNominalFeatures, numValues, seedValue);
        usersDataset = new UsersDatasetAdapter(ratingsDataset.allUsers().stream().map(idUser -> new User(idUser)).collect(Collectors.toSet()));

        groupEvaluationMeasures = GroupEvaluationMeasuresFactory.getInstance().getAllClasses();

        datasetLoader = new DatasetLoaderGivenRatingsContent(ratingsDataset, contentDataset, usersDataset);

    }

    /**
     * Test para comprobar que las técnicas de generación de grupos aleatorias
     * generan grupos iguales en diferentes casos con la misma semilla.
     *
     * @throws delfos.common.exceptions.dataset.users.UserNotFound
     * @throws delfos.common.exceptions.dataset.items.ItemNotFound
     */
    @Test
    public void testRandomFormationEquality() throws CannotLoadContentDataset, CannotLoadRatingsDataset, UserNotFound, ItemNotFound {
        // TODO implement this test
        if (1 == 1) {
            fail("You must implement this test.");
            return;
        }

        int numEjecuciones = 1;
        RelevanceCriteria criteria = datasetLoader.getDefaultRelevanceCriteria();

        for (ValidationTechnique validationTechnique : getValidationTechniques()) {
            for (GroupPredictionProtocol groupPredictionProtocol : getGroupPredictionProtocols()) {
                for (GroupRecommenderSystemAdapter groupRecommenderSystem : getGroupRecommenderSystems()) {
                    for (long seed : getSeeds(numEjecuciones)) {
                        GroupFormationTechnique groupFormationTechnique = new FixedGroupSize_OnlyNGroups(5, 5);

                        GroupCaseStudy caseStudyGroupRecommendation = new GroupCaseStudy(
                                datasetLoader,
                                groupRecommenderSystem,
                                groupFormationTechnique,
                                validationTechnique, groupPredictionProtocol,
                                groupEvaluationMeasures,
                                criteria,
                                numEjecuciones);

                        caseStudyGroupRecommendation.setSeedValue(seed);
                        caseStudyGroupRecommendation.execute();

                    }
                }
            }
        }
    }

    /**
     * Obtiene los sistemas de recomendación que se evaluan.
     *
     * @return
     */
    public static List<GroupRecommenderSystemAdapter> getGroupRecommenderSystems() {
        List<GroupRecommenderSystemAdapter> groupRecommenderSystems = new ArrayList<>();

        groupRecommenderSystems.add(new RandomGroupRecommender());
        groupRecommenderSystems.add(new AggregationOfIndividualRecommendations());

        return groupRecommenderSystems;
    }

    public static List<Long> getSeeds(int numSeeds) {
        List<Long> seeds = new LinkedList<>();
        Random random = new Random(654987);

        for (int i = 0; i < numSeeds; i++) {
            seeds.add(random.nextLong());
        }

        return seeds;
    }

    private static Iterable<GroupPredictionProtocol> getGroupPredictionProtocols() {
        List<GroupPredictionProtocol> ret = new LinkedList<>();
        ret.add(new HoldOutPrediction());
        return ret;
    }

    private Iterable<ValidationTechnique> getValidationTechniques() {
        List<ValidationTechnique> ret = new LinkedList<>();
        ret.add(new HoldOut_Ratings());
        ret.add(new NoPartitions());
        return ret;
    }
}
