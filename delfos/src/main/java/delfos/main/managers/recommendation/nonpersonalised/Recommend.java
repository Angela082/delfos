package delfos.main.managers.recommendation.nonpersonalised;

import java.util.Collection;
import java.util.List;
import delfos.ConsoleParameters;
import delfos.ERROR_CODES;
import delfos.common.Chronometer;
import delfos.common.exceptions.dataset.CannotLoadContentDataset;
import delfos.common.exceptions.dataset.CannotLoadRatingsDataset;
import delfos.common.exceptions.dataset.items.ItemNotFound;
import delfos.common.exceptions.dataset.users.UserNotFound;
import delfos.configfile.rs.single.RecommenderSystemConfiguration;
import delfos.configfile.rs.single.RecommenderSystemConfigurationFileParser;
import delfos.dataset.basic.user.User;
import delfos.dataset.basic.loader.types.ContentDatasetLoader;
import delfos.main.managers.CaseUseManager;
import delfos.main.managers.recommendation.ArgumentsRecommendation;
import delfos.main.managers.recommendation.singleuser.ArgumentsSingleUserRecommendation;
import delfos.rs.nonpersonalised.NonPersonalisedRecommender;
import delfos.rs.persistence.FailureInPersistence;
import delfos.rs.persistence.PersistenceMethodStrategy;
import delfos.rs.recommendation.Recommendation;
import delfos.rs.recommendation.RecommendationComputationDetails;
import delfos.rs.recommendation.SingleUserRecommendations;

/**
 *
 * @version 22-oct-2014
 * @author Jorge Castro Gallardo
 */
public class Recommend implements CaseUseManager {

    public static Recommend getInstance() {
        return Holder.INSTANCE;
    }

    private static class Holder {

        private static final Recommend INSTANCE = new Recommend();
    }

    private Recommend() {
    }

    @Override
    public boolean isRightManager(ConsoleParameters consoleParameters) {
        return consoleParameters.isDefined(ArgumentsNonPersonalised.NON_PERSONALISED_MODE)
                && consoleParameters.isDefined(ArgumentsRecommendation.RECOMMEND);
    }

    @Override
    public void manageCaseUse(ConsoleParameters consoleParameters) {

        String configurationFile = ArgumentsRecommendation.extractConfigurationFile(consoleParameters);

        User user;
        if (consoleParameters.isDefined(ArgumentsSingleUserRecommendation.TARGET_USER)) {
            String idUser = consoleParameters.getValue(ArgumentsSingleUserRecommendation.TARGET_USER);
            user = new User(Integer.parseInt(idUser));
        } else {
            user = User.ANONYMOUS_USER;
        }

        RecommenderSystemConfiguration rsc = RecommenderSystemConfigurationFileParser.loadConfigFile(configurationFile);

        if (rsc.recommenderSystem instanceof NonPersonalisedRecommender) {
            Chronometer chronometer = new Chronometer();
            NonPersonalisedRecommender nonPersonalisedRecommender = (NonPersonalisedRecommender) rsc.recommenderSystem;
            Object recommendationModel;
            try {
                recommendationModel = PersistenceMethodStrategy.loadModel(rsc);
            } catch (FailureInPersistence ex) {
                ERROR_CODES.FAILURE_IN_PERSISTENCE.exit(ex);
                throw new IllegalStateException(ex);
            }

            Collection<Integer> candidateItems;
            try {
                candidateItems = rsc.recommendationCandidatesSelector.candidateItems(rsc.datasetLoader, user);
            } catch (UserNotFound ex) {
                if (rsc.datasetLoader instanceof ContentDatasetLoader) {
                    ContentDatasetLoader contentDatasetLoader = (ContentDatasetLoader) rsc.datasetLoader;
                    candidateItems = contentDatasetLoader.getContentDataset().allID();
                } else {
                    candidateItems = rsc.datasetLoader.getRatingsDataset().allRatedItems();
                }
            }

            try {
                List<Recommendation> recommendOnly = nonPersonalisedRecommender.recommendOnly(rsc.datasetLoader, recommendationModel, candidateItems);

                long timeTaken = chronometer.getTotalElapsed();
                rsc.recommdendationsOutputMethod.writeRecommendations(new SingleUserRecommendations(user, recommendOnly, new RecommendationComputationDetails().addDetail(RecommendationComputationDetails.DetailField.TimeTaken, timeTaken)));
            } catch (ItemNotFound ex) {
                ERROR_CODES.ITEM_NOT_FOUND.exit(ex);
            } catch (CannotLoadRatingsDataset ex) {
                ERROR_CODES.CANNOT_LOAD_RATINGS_DATASET.exit(ex);
            } catch (CannotLoadContentDataset ex) {
                ERROR_CODES.CANNOT_LOAD_CONTENT_DATASET.exit(ex);
            }
        } else {
            IllegalStateException ise = new IllegalStateException(rsc.recommenderSystem.getAlias() + " is not a non-personalised recommender system (Must implement " + NonPersonalisedRecommender.class);
            ERROR_CODES.NOT_A_RECOMMENDER_SYSTEM.exit(ise);
        }
    }

    @Override
    public String getUserFriendlyHelpForThisCaseUse() {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }
}