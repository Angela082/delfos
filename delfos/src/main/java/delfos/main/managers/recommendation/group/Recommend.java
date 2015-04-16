package delfos.main.managers.recommendation.group;

import java.io.File;
import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedList;
import java.util.List;
import delfos.ConsoleParameters;
import delfos.ERROR_CODES;
import delfos.UndefinedParameterException;
import delfos.common.Chronometer;
import delfos.common.Global;
import delfos.common.exceptions.dataset.items.ItemNotFound;
import delfos.common.exceptions.dataset.users.UserNotFound;
import delfos.common.exceptions.ratings.NotEnoughtUserInformation;
import delfos.configfile.rs.single.RecommenderSystemConfiguration;
import delfos.configfile.rs.single.RecommenderSystemConfigurationFileParser;
import delfos.dataset.basic.rating.Rating;
import delfos.dataset.basic.loader.types.DatasetLoader;
import delfos.group.groupsofusers.GroupOfUsers;
import delfos.group.grs.GroupRecommenderSystem;
import delfos.group.grs.recommendations.GroupRecommendations;
import delfos.main.managers.CaseUseManager;
import delfos.main.managers.recommendation.ArgumentsRecommendation;
import delfos.recommendationcandidates.RecommendationCandidatesSelector;
import delfos.rs.persistence.FailureInPersistence;
import delfos.rs.persistence.PersistenceMethodStrategy;
import delfos.rs.recommendation.Recommendation;
import delfos.rs.recommendation.RecommendationComputationDetails;

/**
 *
 * @version 20-oct-2014
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
        return consoleParameters.isDefined(ArgumentsGroupRecommendation.GROUP_MODE)
                && consoleParameters.isDefined(ArgumentsGroupRecommendation.TARGET_GROUP);
    }

    @Override
    public void manageCaseUse(ConsoleParameters consoleParameters) {
        manageGroupRecommendation(consoleParameters);
    }

    public static void manageGroupRecommendation(ConsoleParameters consoleParameters) {

        String configurationFile = ArgumentsRecommendation.extractConfigurationFile(consoleParameters);

        GroupOfUsers targetGroup = extractTargetGroup(consoleParameters);

        recommendToGroup(configurationFile, targetGroup);
    }

    public static void recommendToGroup(String configurationFile, GroupOfUsers targetGroup) {

        if (configurationFile != null && new File(configurationFile).exists()) {

            Chronometer chronometer = new Chronometer();

            RecommenderSystemConfiguration rsc = RecommenderSystemConfigurationFileParser.loadConfigFile(configurationFile);

            GroupRecommenderSystem<Object, Object> groupRecommenderSystem = getGroupRecommenderSystem(rsc);
            DatasetLoader<? extends Rating> datasetLoader = rsc.datasetLoader;
            RecommendationCandidatesSelector candidatesSelector = rsc.recommendationCandidatesSelector;

            List<Recommendation> recommendations = null;

            Collection<Integer> idItemList;
            try {
                idItemList = candidatesSelector.candidateItems(datasetLoader, targetGroup);
            } catch (UserNotFound ex) {
                ERROR_CODES.USER_NOT_FOUND.exit(ex);
                throw new IllegalArgumentException(ex);
            }

            if (Global.isVerboseAnnoying()) {
                Global.showMessage("List of candidate items for group " + targetGroup.getGroupMembers() + " size: " + idItemList.size() + "\n");
                Global.showMessage("\t" + idItemList + "\n");
            }

            Object recommenderSystemModel;
            try {
                Global.showMessageTimestamped("Loading recommendation model");
                recommenderSystemModel = PersistenceMethodStrategy.loadModel(groupRecommenderSystem, rsc.persistenceMethod, targetGroup.getGroupMembers(), idItemList);
                Global.showMessageTimestamped("Loaded recommendation model");
            } catch (FailureInPersistence ex) {
                ERROR_CODES.FAILURE_IN_PERSISTENCE.exit(ex);
                throw new IllegalArgumentException(ex);
            }

            Object groupModel;
            try {
                groupModel = groupRecommenderSystem.buildGroupModel(datasetLoader, recommenderSystemModel, targetGroup);
                recommendations = groupRecommenderSystem.recommendOnly(datasetLoader, recommenderSystemModel, groupModel, targetGroup, idItemList);
            } catch (UserNotFound ex) {
                ERROR_CODES.USER_NOT_FOUND.exit(ex);
                throw new IllegalArgumentException(ex);
            } catch (ItemNotFound ex) {
                ERROR_CODES.ITEM_NOT_FOUND.exit(ex);
                throw new IllegalArgumentException(ex);
            } catch (NotEnoughtUserInformation ex) {
                Global.showWarning("Recommender system '" + groupRecommenderSystem.getName() + "' reported: Not enought user information (group=" + targetGroup + ").");
                //ERROR_CODES.USER_NOT_ENOUGHT_INFORMATION.exit(ex);
                recommendations = new ArrayList<>();
            }

            if (Global.isVerboseAnnoying()) {
                if (recommendations.isEmpty()) {
                    Global.showWarning("Recommendation list for group '" + targetGroup + "' is empty, check for causes.");
                } else {
                    Global.showMessage("Recommendation list for group '" + targetGroup + "' of size " + recommendations.size() + "\n");
                    Global.showMessage("\t" + recommendations.toString() + "\n");
                }
            }

            long timeTaken = chronometer.getTotalElapsed();
            chronometer.reset();

            Global.showMessageTimestamped("Writting recommendations\n");
            rsc.recommdendationsOutputMethod.writeRecommendations(new GroupRecommendations(targetGroup, recommendations, new RecommendationComputationDetails().addDetail(RecommendationComputationDetails.DetailField.TimeTaken, timeTaken)));
            Global.showMessageTimestamped("Wrote recommendations\n");

        } else {
            IllegalArgumentException ex = new IllegalArgumentException("Configuration file '" + configurationFile + "' not found");
            Global.showWarning("Configuration file not found: '" + configurationFile + "'\n");
            Global.showError(ex);
            ERROR_CODES.CONFIG_FILE_NOT_EXISTS.exit(ex);
        }
    }

    private static GroupRecommenderSystem getGroupRecommenderSystem(RecommenderSystemConfiguration rsc) throws RuntimeException {
        if (rsc.recommenderSystem instanceof GroupRecommenderSystem) {
            GroupRecommenderSystem groupRecommenderSystem = (GroupRecommenderSystem) rsc.recommenderSystem;
            return groupRecommenderSystem;
        } else {
            IllegalStateException ise = new IllegalStateException("Recommender '" + rsc.recommenderSystem.getAlias() + "' (class '" + rsc.recommenderSystem.getClass() + "') is not a group recomender system)");
            ERROR_CODES.NOT_A_GROUP_RECOMMENDER_SYSTEM.exit(ise);
            throw ise;
        }
    }

    @Override
    public String getUserFriendlyHelpForThisCaseUse() {
        if (1 == 1) {
            throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
        } else {
            return null;
        }
    }

    private static GroupOfUsers extractTargetGroup(ConsoleParameters consoleParameters) {

        List<String> groupMembers;

        try {
            groupMembers = consoleParameters.getValues(ArgumentsGroupRecommendation.TARGET_GROUP);
        } catch (UndefinedParameterException ex) {
            Global.showWarning("Target group members must be specified through parameter '" + ArgumentsGroupRecommendation.TARGET_GROUP + "' values\n");
            ERROR_CODES.GROUP_NOT_DEFINED.exit(ex);
            throw ex;
        }

        Collection<Integer> members = new LinkedList<>();
        for (String member : groupMembers) {
            try {
                int idMember = Integer.parseInt(member);
                members.add(idMember);
            } catch (NumberFormatException ex) {
                Global.showWarning("Value '" + member + "' is not a valid user id.\n");
                ERROR_CODES.USER_ID_NOT_RECOGNISED.exit(ex);
                throw ex;
            }
        }

        return new GroupOfUsers(members);
    }
}