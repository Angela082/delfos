package delfos.group.casestudy.definedcases.hesitant.experiment0;

import delfos.Constants;
import delfos.common.FileUtilities;
import delfos.common.Global;
import delfos.configureddatasets.ConfiguredDatasetLoader;
import delfos.constants.DelfosTest;
import delfos.dataset.basic.loader.types.DatasetLoader;
import delfos.dataset.basic.rating.RelevanceCriteria;
import delfos.experiment.casestudy.cluster.TuringPreparator;
import delfos.experiment.validation.validationtechnique.CrossFoldValidation_Ratings;
import delfos.group.casestudy.defaultcase.GroupCaseStudy;
import delfos.group.experiment.validation.groupformation.GroupFormationTechnique;
import delfos.group.experiment.validation.groupformation.SimilarMembers_OnlyNGroups;
import delfos.group.experiment.validation.predictionvalidation.NoPredictionProtocol;
import delfos.group.factories.GroupEvaluationMeasuresFactory;
import delfos.group.grs.GroupRecommenderSystem;
import delfos.group.grs.hesitant.HesitantKnnGroupUser;
import delfos.utils.hesitant.similarity.HesitantSimilarity;
import delfos.utils.hesitant.similarity.basic.HesitantMeanAggregation;
import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;
import org.junit.Test;

public class HesitantGRS_CaseStudy extends DelfosTest {

    public HesitantGRS_CaseStudy() {
    }

    public static final int numExec = 1;
    public static final long SEED_VALUE = 123456L;

    public static final List<Integer> groupSizes = Arrays.asList(1, 2, 3, 4, 5, 6, 7, 8, 9, 10);
    public static final int numGroups = 90;

    File experimentDirectory = new File(Constants.getTempDirectory().getAbsolutePath() + File.separator
            + "HesitantGRS.experiment0" + File.separator);

    private Collection<GroupFormationTechnique> getGroupFormationTechnique() {
        return groupSizes.stream()
                .map((groupSize) -> {
                    GroupFormationTechnique gft = new SimilarMembers_OnlyNGroups(
                            numGroups,
                            groupSize);
                    return gft;
                }).collect(Collectors.toList());
    }

    private Collection<ConfiguredDatasetLoader> getDatasetLoader() {
        return Arrays.asList(new ConfiguredDatasetLoader("ml-100k"));
    }

    private List<GroupRecommenderSystem> getGRSs() {
        int neighborhoodSize = 100;

        List<GroupRecommenderSystem> ret = new ArrayList<>();

        HesitantSimilarity hesitantSimilarity = new HesitantMeanAggregation();
        HesitantKnnGroupUser hesitantGRS = new HesitantKnnGroupUser();

        hesitantGRS.setAlias(hesitantSimilarity.getName());
        hesitantGRS.setParameterValue(HesitantKnnGroupUser.NEIGHBORHOOD_SIZE, neighborhoodSize);
        hesitantGRS.setParameterValue(HesitantKnnGroupUser.HESITANT_SIMILARITY_MEASURE, hesitantSimilarity);
        hesitantGRS.setParameterValue(HesitantKnnGroupUser.DELETE_REPEATED, false);

        ret.add(hesitantGRS);

        return ret;
    }

    public void createCaseStudyXML() {

        TuringPreparator turingPreparator = new TuringPreparator();

        List<GroupCaseStudy> groupCaseStudys = new ArrayList<>();

        for (GroupFormationTechnique groupFormationTechnique : getGroupFormationTechnique()) {
            for (GroupRecommenderSystem groupRecommenderSystem : getGRSs()) {
                GroupCaseStudy groupCaseStudy = new GroupCaseStudy(
                        null,
                        groupRecommenderSystem,
                        groupFormationTechnique,
                        new CrossFoldValidation_Ratings(),
                        new NoPredictionProtocol(),
                        GroupEvaluationMeasuresFactory.getInstance().getAllClasses(),
                        new RelevanceCriteria(4),
                        1,
                        SEED_VALUE
                );

                groupCaseStudy.setAlias(
                        "_dataValidation=" + groupCaseStudy.hashDataValidation()
                        + "_technique=" + groupCaseStudy.hashTechnique()
                        + "_" + groupRecommenderSystem.getAlias()
                        + "_allHash=" + groupCaseStudy.hashCode()
                );
                groupCaseStudys.add(groupCaseStudy);
            }
        }

        turingPreparator.prepareGroupExperiment(experimentDirectory,
                groupCaseStudys,
                getDatasetLoader().toArray(new DatasetLoader[0]));
    }

    @Test
    public void testExecute() throws Exception {
        FileUtilities.deleteDirectoryRecursive(experimentDirectory);
        createCaseStudyXML();
        Global.show("This case study has " + new TuringPreparator()
                .sizeOfAllExperimentsInDirectory(experimentDirectory)
                + " experiments");

        new TuringPreparator().executeAllExperimentsInDirectory(experimentDirectory, 1);
    }
}
