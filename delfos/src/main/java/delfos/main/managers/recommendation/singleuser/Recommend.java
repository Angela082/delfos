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
package delfos.main.managers.recommendation.singleuser;

import delfos.ConsoleParameters;
import delfos.ERROR_CODES;
import delfos.UndefinedParameterException;
import delfos.common.Chronometer;
import delfos.common.Global;
import delfos.common.exceptions.dataset.CannotLoadContentDataset;
import delfos.common.exceptions.dataset.CannotLoadRatingsDataset;
import delfos.common.exceptions.dataset.items.ItemNotFound;
import delfos.common.exceptions.dataset.users.UserNotFound;
import delfos.common.exceptions.ratings.NotEnoughtUserInformation;
import delfos.configfile.rs.single.RecommenderSystemConfiguration;
import delfos.configfile.rs.single.RecommenderSystemConfigurationFileParser;
import delfos.dataset.basic.item.ContentDataset;
import delfos.dataset.basic.loader.types.ContentDatasetLoader;
import delfos.dataset.basic.loader.types.DatasetLoader;
import delfos.dataset.basic.rating.Rating;
import delfos.dataset.basic.user.User;
import delfos.main.managers.CaseUseSubManager;
import static delfos.main.managers.recommendation.ArgumentsRecommendation.RECOMMEND;
import static delfos.main.managers.recommendation.ArgumentsRecommendation.RECOMMEND_SHORT;
import static delfos.main.managers.recommendation.ArgumentsRecommendation.extractConfigurationFile;
import static delfos.main.managers.recommendation.singleuser.SingleUserRecommendation.SINGLE_USER_MODE;
import delfos.rs.RecommenderSystem;
import delfos.rs.persistence.FailureInPersistence;
import delfos.rs.persistence.PersistenceMethodStrategy;
import delfos.rs.recommendation.Recommendation;
import delfos.rs.recommendation.RecommendationComputationDetails;
import delfos.rs.recommendation.SingleUserRecommendations;
import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Set;

/**
 *
 * @version 20-oct-2014
 * @author Jorge Castro Gallardo
 */
class Recommend extends CaseUseSubManager {

    /**
     * Parámetro de la linea de comandos para especificar a qué usuario se desea
     * recomendar.
     */
    public final static String USER_COMMAND_LINE_PARAMETER = "-u";

    public static Recommend getInstance() {
        return Holder.INSTANCE;
    }

    private static class Holder {

        private static final Recommend INSTANCE = new Recommend();
    }

    private Recommend() {
        super(SingleUserRecommendation.getInstance());
    }

    public boolean isSingleUserRecommendationCaseUse(ConsoleParameters consoleParameters) {
        return consoleParameters.isFlagDefined(SINGLE_USER_MODE)
                && (consoleParameters.isFlagDefined(RECOMMEND)
                || consoleParameters.isFlagDefined(RECOMMEND_SHORT));
    }

    @Override
    public boolean isRightManager(ConsoleParameters consoleParameters) {
        return isSingleUserRecommendationCaseUse(consoleParameters);
    }

    @Override
    public void manageCaseUse(ConsoleParameters consoleParameters) {
        manageSingleUserRecommendation(consoleParameters);
    }

    public static void manageSingleUserRecommendation(ConsoleParameters consoleParameters) {

        String configurationFile = extractConfigurationFile(consoleParameters);
        Integer idUser = 0;
        String idUserString;
        try {
            idUserString = consoleParameters.getValue(USER_COMMAND_LINE_PARAMETER);
        } catch (UndefinedParameterException ex) {
            Global.showWarning("Not defined user to recommend, use parameter '" + ex.getParameterMissing() + "'\n");
            Global.showError(ex);
            ERROR_CODES.MANAGE_RATING_DATABASE_USER_NOT_DEFINED.exit(ex);
            idUserString = null;
        }
        try {
            idUser = Integer.parseInt(idUserString);
        } catch (NumberFormatException ex) {
            Global.showError(ex);
            ERROR_CODES.USER_ID_NOT_RECOGNISED.exit(ex);
        }
        recommendToUser(configurationFile, idUser);
    }

    public static void recommendToUser(String configurationFile, int idUser) {

        if (configurationFile != null && new File(configurationFile).exists()) {
            //llamada a la clase que realiza el manejo de este caso de uso
            Chronometer chronometer = new Chronometer();
            RecommenderSystemConfiguration rsc
                    = RecommenderSystemConfigurationFileParser.loadConfigFile(configurationFile);

            chronometer.reset();

            @SuppressWarnings("unchecked")
            RecommenderSystem<Object> recommender = (RecommenderSystem<Object>) rsc.recommenderSystem;

            DatasetLoader<? extends Rating> datasetLoader = rsc.datasetLoader;
            Collection<Recommendation> recommendations = null;
            try {
                if (rsc.datasetLoader instanceof ContentDatasetLoader) {
                    ContentDatasetLoader contentDatasetLoader = (ContentDatasetLoader) datasetLoader;
                    Global.showMessageTimestamped("Loading content dataset");
                    ContentDataset contentDataset = contentDatasetLoader.getContentDataset();
                    Global.showMessageTimestamped("Loaded content dataset");
                } else {
                    throw new CannotLoadContentDataset("The dataset loader is not a ContentDatasetLoader, cannot apply a content-based ");
                }

            } catch (CannotLoadRatingsDataset ex) {
                ERROR_CODES.CANNOT_LOAD_RATINGS_DATASET.exit(ex);
                throw new IllegalArgumentException(ex);
            } catch (CannotLoadContentDataset ex) {
                ERROR_CODES.CANNOT_LOAD_CONTENT_DATASET.exit(ex);
                throw new IllegalArgumentException(ex);
            }

            Set<Integer> candidateItems;
            try {
                candidateItems = rsc.recommendationCandidatesSelector.candidateItems(datasetLoader, new User(idUser));
            } catch (UserNotFound ex) {
                ERROR_CODES.USER_NOT_FOUND.exit(ex);
                throw new IllegalArgumentException(ex);
            }

            if (Global.isVerboseAnnoying()) {
                Global.showInfoMessage("List of candidate items for user " + idUser + " size: " + candidateItems.size() + "\n");
                Global.showInfoMessage("\t" + candidateItems + "\n");
            }

            Object RecommendationModel;
            try {
                Global.showMessageTimestamped("Computing recommendations");
                RecommendationModel = PersistenceMethodStrategy.loadModel(recommender, rsc.persistenceMethod, Arrays.asList(idUser), candidateItems);
                Global.showMessageTimestamped("Computed recommendations");
            } catch (FailureInPersistence ex) {
                ERROR_CODES.FAILURE_IN_PERSISTENCE.exit(ex);
                throw new IllegalArgumentException(ex);
            }

            try {
                recommendations = recommender.recommendToUser(datasetLoader, RecommendationModel, idUser, candidateItems);
            } catch (UserNotFound ex) {
                ERROR_CODES.USER_NOT_FOUND.exit(ex);
                throw new IllegalArgumentException(ex);
            } catch (ItemNotFound ex) {
                ERROR_CODES.ITEM_NOT_FOUND.exit(ex);
                throw new IllegalArgumentException(ex);
            } catch (NotEnoughtUserInformation ex) {
                Global.showWarning("Recommender system '" + recommender.getName() + "' reported: Not enought user information (idUser=" + idUser + ".");
                //ERROR_CODES.USER_NOT_ENOUGHT_INFORMATION.exit(ex);
                recommendations = new ArrayList<>();
            }

            if (Global.isVerboseAnnoying()) {
                if (recommendations.isEmpty()) {
                    Global.showWarning("Recommendation list for user '" + idUser + "' is empty, check for causes.");
                } else {
                    Global.showInfoMessage("Recommendation list for user '" + idUser + "' of size " + recommendations.size() + "\n");
                    Global.showInfoMessage("\t" + recommendations.toString() + "\n");
                }
            }

            Global.showMessageTimestamped("Writting recommendations\n");

            long timeTaken = chronometer.getTotalElapsed();
            rsc.recommdendationsOutputMethod.writeRecommendations(new SingleUserRecommendations(new User(idUser), recommendations, new RecommendationComputationDetails().addDetail(RecommendationComputationDetails.DetailField.TimeTaken, timeTaken)));

            Global.showMessageTimestamped("Wrote recommendations\n");

        } else {
            IllegalArgumentException ex = new IllegalArgumentException("Configuration file '" + configurationFile + "' not found");
            Global.showWarning("Configuration file not found: '" + configurationFile + "'\n");
            Global.showError(ex);
            ERROR_CODES.CONFIG_FILE_NOT_EXISTS.exit(ex);
        }
    }

}
