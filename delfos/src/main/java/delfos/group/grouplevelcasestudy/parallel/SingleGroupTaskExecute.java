package delfos.group.grouplevelcasestudy.parallel;

import delfos.ERROR_CODES;
import delfos.common.exceptions.dataset.CannotLoadContentDataset;
import delfos.common.exceptions.dataset.CannotLoadRatingsDataset;
import delfos.common.exceptions.dataset.items.ItemNotFound;
import delfos.common.exceptions.dataset.users.UserNotFound;
import delfos.common.exceptions.ratings.NotEnoughtUserInformation;
import delfos.common.parallelwork.SingleTaskExecute;
import delfos.dataset.basic.loader.types.DatasetLoader;
import delfos.dataset.basic.rating.Rating;
import delfos.dataset.storage.validationdatasets.PairOfTrainTestRatingsDataset;
import delfos.group.casestudy.parallelisation.SingleGroupRecommendationTaskInput;
import delfos.group.casestudy.parallelisation.SingleGroupRecommendationTaskOutput;
import delfos.group.experiment.validation.predictionvalidation.GroupPredictionProtocol;
import delfos.group.experiment.validation.predictionvalidation.GroupRecommendationRequest;
import delfos.group.experiment.validation.validationtechniques.GroupValidationTechnique;
import delfos.group.grouplevelcasestudy.GroupLevelCaseStudy;
import delfos.group.grouplevelcasestudy.GroupLevelResults;
import delfos.group.groupsofusers.GroupOfUsers;
import delfos.group.groupsofusers.measuresovergroups.GroupMeasure;
import delfos.group.grs.GroupRecommenderSystem;
import delfos.group.results.groupevaluationmeasures.GroupEvaluationMeasure;
import delfos.group.results.groupevaluationmeasures.GroupEvaluationMeasureResult;
import delfos.group.results.grouprecomendationresults.GroupRecommenderSystemResult;
import delfos.rs.recommendation.Recommendation;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.TreeSet;

/**
 *
 * @author Jorge Castro Gallardo
 */
public class SingleGroupTaskExecute implements SingleTaskExecute<SingleGroupTask> {

    @Override
    public void executeSingleTask(SingleGroupTask task) {
        GroupLevelResults[] groupResults = null;
        try {
            groupResults = computeGroup(task.getSeed(), task.getValidationTechnique(), task.getDatasetLoader(), task.getGroup(), task.getGroupRecommenderSystems(), task.getPredictionProtocol(), task.getGrouMeasures(), task.getEvaluationMeasures());
        } catch (NotEnoughtUserInformation ex) {
            //Cannot be computed, lack of information from group.
        } catch (UserNotFound ex) {
            ERROR_CODES.USER_NOT_FOUND.exit(ex);
        } catch (ItemNotFound ex) {
            ERROR_CODES.ITEM_NOT_FOUND.exit(ex);
        } catch (CannotLoadRatingsDataset ex) {
            ERROR_CODES.CANNOT_LOAD_RATINGS_DATASET.exit(ex);
        } catch (CannotLoadContentDataset ex) {
            ERROR_CODES.CANNOT_LOAD_CONTENT_DATASET.exit(ex);
        }

        task.setGroupLevelResults(groupResults);
    }

    private GroupLevelResults[] computeGroup(
            long seed,
            GroupValidationTechnique validationTechnique,
            DatasetLoader<? extends Rating> originalDatasetLoader,
            GroupOfUsers group,
            GroupRecommenderSystem[] groupRecommenderSystems,
            GroupPredictionProtocol predictionProtocol,
            GroupMeasure[] grouMeasures,
            Collection<GroupEvaluationMeasure> evaluationMeasures)
            throws UserNotFound, CannotLoadRatingsDataset, CannotLoadContentDataset, ItemNotFound, NotEnoughtUserInformation {

        GroupOfUsers[] groups = new GroupOfUsers[1];
        groups[0] = group;
        PairOfTrainTestRatingsDataset[] pairs = validationTechnique.shuffle(originalDatasetLoader, groups);

        validationTechnique.setSeedValue(seed);
        predictionProtocol.setSeedValue(seed);
        int numSplit = validationTechnique.getNumberOfSplits();

        GroupLevelResults[] ret = new GroupLevelResults[numSplit];

        for (int split = 0; split < numSplit; split++) {

            GroupLevelResults groupLevelResults = new GroupLevelResults(group);
            ret[split] = groupLevelResults;

            DatasetLoader<? extends Rating> trainingDatasetLoader = pairs[split].getTrainingDatasetLoader();
            DatasetLoader<? extends Rating> testDatasetLoader = pairs[split].getTestDatasetLoader();

            for (GroupRecommenderSystem groupRecommenderSystem : groupRecommenderSystems) {

                Object recommendationModel = groupRecommenderSystem.buildRecommendationModel(trainingDatasetLoader);
                Collection<Recommendation> allPredictions = new ArrayList<>();
                Set<Integer> requests = new TreeSet<>();

                Collection<GroupRecommendationRequest> allRequests = predictionProtocol.getGroupRecommendationRequests(trainingDatasetLoader, testDatasetLoader, group);
                for (GroupRecommendationRequest groupRecommendationRequest : allRequests) {

                    Object groupModel = groupRecommenderSystem.buildGroupModel(
                            groupRecommendationRequest.predictionPhaseDatasetLoader,
                            recommendationModel,
                            group);

                    Collection<Recommendation> groupRecommendations = groupRecommenderSystem.recommendOnly(
                            groupRecommendationRequest.predictionPhaseDatasetLoader,
                            recommendationModel,
                            groupModel,
                            group,
                            groupRecommendationRequest.itemsToPredict);

                    allPredictions.addAll(groupRecommendations);
                    requests.addAll(groupRecommendationRequest.itemsToPredict);
                }

                for (GroupMeasure groupMeasure : grouMeasures) {
                    double groupMeasureValue = groupMeasure.getMeasure(originalDatasetLoader, group);
                    groupLevelResults.setGroupMeasure(groupMeasure, groupMeasureValue);
                }

                for (GroupEvaluationMeasure evaluationMeasure : evaluationMeasures) {
                    Map<GroupOfUsers, Collection<Integer>> _requests = new TreeMap<>();
                    _requests.put(group, requests);
                    Map<GroupOfUsers, Collection<Recommendation>> _recommendations = new TreeMap<>();
                    _recommendations.put(group, allPredictions);

                    List<SingleGroupRecommendationTaskInput> singleGroupRecommendationInputs = Arrays.asList(
                            new SingleGroupRecommendationTaskInput(
                                    groupRecommenderSystem,
                                    originalDatasetLoader,
                                    recommendationModel,
                                    group,
                                    requests));

                    List<SingleGroupRecommendationTaskOutput> singleGroupRecommendationOutputs = Arrays.asList(
                            new SingleGroupRecommendationTaskOutput(group, allPredictions, 0, 0)
                    );

                    GroupRecommenderSystemResult groupRecommendationResult = new GroupRecommenderSystemResult(
                            singleGroupRecommendationInputs,
                            singleGroupRecommendationOutputs,
                            GroupLevelCaseStudy.class.getSimpleName(), 0, 0);

                    GroupEvaluationMeasureResult measureResult = evaluationMeasure.getMeasureResult(
                            groupRecommendationResult,
                            originalDatasetLoader,
                            testDatasetLoader.getRatingsDataset(),
                            originalDatasetLoader.getDefaultRelevanceCriteria(),
                            trainingDatasetLoader,
                            testDatasetLoader);

                    groupLevelResults.setEvaluationMeasure(
                            groupRecommenderSystem,
                            evaluationMeasure,
                            measureResult);
                }
            }
        }
        return ret;
    }
}
