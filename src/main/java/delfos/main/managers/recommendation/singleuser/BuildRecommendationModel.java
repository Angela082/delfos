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
import delfos.common.Global;
import delfos.common.exceptions.dataset.CannotLoadContentDataset;
import delfos.common.exceptions.dataset.CannotLoadRatingsDataset;
import delfos.common.exceptions.dataset.CannotLoadUsersDataset;
import delfos.configfile.rs.single.RecommenderSystemConfiguration;
import delfos.configfile.rs.single.RecommenderSystemConfigurationFileParser;
import delfos.main.managers.CaseUseSubManager;
import static delfos.main.managers.recommendation.ArgumentsRecommendation.BUILD_RECOMMENDATION_MODEL;
import static delfos.main.managers.recommendation.ArgumentsRecommendation.BUILD_RECOMMENDATION_MODEL_SHORT;
import static delfos.main.managers.recommendation.ArgumentsRecommendation.extractConfigurationFile;
import delfos.rs.GenericRecommenderSystem;
import delfos.rs.persistence.FailureInPersistence;
import delfos.rs.persistence.PersistenceMethodStrategy;
import delfos.view.SwingGUI;
import java.io.File;

/**
 *
 * @version 20-oct-2014
 * @author jcastro-inf ( https://github.com/jcastro-inf )
 */
class BuildRecommendationModel extends CaseUseSubManager {

    public static BuildRecommendationModel getInstance() {
        return Holder.INSTANCE;
    }

    private static class Holder {

        private static final BuildRecommendationModel INSTANCE = new BuildRecommendationModel();
    }

    private BuildRecommendationModel() {
        super(SingleUserRecommendation.getInstance());
    }

    @Override
    public boolean isRightManager(ConsoleParameters consoleParameters) {
        if (consoleParameters.isFlagDefined(SingleUserRecommendation.SINGLE_USER_MODE)) {
            return consoleParameters.isFlagDefined(BUILD_RECOMMENDATION_MODEL_SHORT) || consoleParameters.isFlagDefined(BUILD_RECOMMENDATION_MODEL);
        } else {
            return false;
        }
    }

    @Override
    public void manageCaseUse(ConsoleParameters consoleParameters) {
        manageBuildRecommendationModel(consoleParameters);
    }

    public static void manageBuildRecommendationModel(ConsoleParameters consoleParameters) {
        String configurationFile = extractConfigurationFile(consoleParameters);

        if (new File(configurationFile).exists()) {
            buildRecommendationModel(configurationFile);
        } else {
            SwingGUI.initRSBuilderGUI(configurationFile);
        }
    }

    public static void buildRecommendationModel(String configurationFile) {
        try {
            RecommenderSystemConfiguration rsc
                    = RecommenderSystemConfigurationFileParser
                    .loadConfigFile(configurationFile);

            @SuppressWarnings("unchecked")
            GenericRecommenderSystem<Object> recommender
                    = (GenericRecommenderSystem<Object>) rsc.recommenderSystem;

            Object recomenderSystemModel;
            Global.showMessageTimestamped("Building recommendation model.");
            recomenderSystemModel = recommender.buildRecommendationModel(rsc.datasetLoader);
            Global.showMessageTimestamped("Built recommendation model.");

            PersistenceMethodStrategy.saveModel(
                    rsc.recommenderSystem,
                    rsc.persistenceMethod,
                    recomenderSystemModel);

        } catch (CannotLoadRatingsDataset ex) {
            ERROR_CODES.CANNOT_LOAD_RATINGS_DATASET.exit(ex);
        } catch (CannotLoadContentDataset ex) {
            ERROR_CODES.CANNOT_LOAD_CONTENT_DATASET.exit(ex);
        } catch (CannotLoadUsersDataset ex) {
            ERROR_CODES.CANNOT_LOAD_USERS_DATASET.exit(ex);
        } catch (FailureInPersistence ex) {
            ERROR_CODES.FAILURE_IN_PERSISTENCE.exit(ex);
        }
    }

    public String getSynopsis() {
        return "EMPTY_SYNOPSIS";
    }
}
