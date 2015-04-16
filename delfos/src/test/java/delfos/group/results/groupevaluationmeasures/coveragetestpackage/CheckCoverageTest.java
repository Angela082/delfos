package delfos.group.results.groupevaluationmeasures.coveragetestpackage;

import java.io.File;
import java.util.Collection;
import org.junit.Test;
import delfos.common.DateCollapse;
import delfos.common.Global;
import delfos.common.exceptions.dataset.CannotLoadContentDataset;
import delfos.common.exceptions.dataset.CannotLoadRatingsDataset;
import delfos.common.exceptions.dataset.items.ItemNotFound;
import delfos.common.exceptions.dataset.users.UserNotFound;
import delfos.dataset.loaders.csv.CSVfileDatasetLoader;
import delfos.dataset.util.DatasetPrinterDeprecated;
import delfos.experiment.ExperimentListerner_default;
import delfos.experiment.casestudy.ExecutionProgressListener;
import delfos.experiment.casestudy.ExecutionProgressListener_default;
import delfos.group.casestudy.GroupCaseStudy;
import delfos.group.casestudy.defaultcase.DefaultGroupCaseStudy;
import delfos.group.factories.GroupEvaluationMeasuresFactory;
import delfos.group.groupsofusers.GroupOfUsers;
import delfos.group.grs.aggregation.AggregationOfIndividualRatings;
import delfos.group.grs.filtered.GroupRecommenderSystemWithPreFilter;
import delfos.group.grs.filtered.filters.NoFilter;
import delfos.group.grs.filtered.filters.OutliersRatingsFilter;
import delfos.group.grs.persistence.GroupRecommenderSystem_fixedFilePersistence;
import delfos.group.io.xml.casestudy.GroupCaseStudyXML;
import delfos.group.results.groupevaluationmeasures.GroupEvaluationMeasure;
import delfos.group.experiment.validation.validationtechniques.HoldOutGroupRatedItems;
import delfos.group.experiment.validation.groupformation.GivenGroups;
import delfos.group.experiment.validation.groupformation.GroupFormationTechnique;
import delfos.group.experiment.validation.predictionvalidation.NoPredictionProtocol;
import delfos.rs.RecommenderSystemBuildingProgressListener_default;
import delfos.rs.collaborativefiltering.knn.modelbased.KnnModelBasedCFRS;
import delfos.rs.persistence.FailureInPersistence;
import delfos.rs.persistence.FilePersistence;

/**
 * Test para comprobar la cobertura. Ahora mismo se utiliza como un main, pero
 * se hace así para no 'ensuciar' el código de la biblioteca.
 *
 * @author Jorge Castro Gallardo (Universidad de Jaén)
 * @version 1.0 07-May-2013
 */
public class CheckCoverageTest {

    private static CSVfileDatasetLoader datasetLoader;
    private static GroupFormationTechnique groupFormationTechnique;
    /**
     * ============== CONSTANTS ====================
     */
    private static final FilePersistence filePersistence = new FilePersistence("modeloParaEvaluarCoverage_forGroups", "dat");
    private static final int NUM_EJECUCIONES = 1;
    private static final long SEED = 6288;

    public CheckCoverageTest() {
    }

    @Test
    public void dummyTest() {
    }

    //@BeforeClass
    public static void setUpClass() throws FailureInPersistence, CannotLoadContentDataset, CannotLoadRatingsDataset {
        Global.setVerbose();
        datasetLoader = new CSVfileDatasetLoader("datasets" + File.separator + "SSII - ratings9.csv", "datasets" + File.separator + "SSII - peliculas.csv");

        DatasetPrinterDeprecated.printGeneralInformation(datasetLoader.getRatingsDataset());
        GroupOfUsers[] groups = new GroupOfUsers[1];
        groups[0] = new GroupOfUsers(1774684, 1887988, 2394147);
        groupFormationTechnique = new GivenGroups(groups);
        GroupRecommenderSystem_fixedFilePersistence grs = new GroupRecommenderSystem_fixedFilePersistence(
                new AggregationOfIndividualRatings(
                        new KnnModelBasedCFRS()), filePersistence);

        groupFormationTechnique.shuffle(datasetLoader);

        try {
            Object grsModel = grs.loadModel(filePersistence, datasetLoader.getRatingsDataset().allUsers(), datasetLoader.getContentDataset().allID());
        } catch (Exception ex) {
            Global.showError(ex);
            Global.showWarning("\n\nHay que generar el modelo \n");
            grs.addBuildingProgressListener(new RecommenderSystemBuildingProgressListener_default(System.out, 5000));
            Object build = grs.build(datasetLoader);
            grs.saveModel(filePersistence, build);
        }
    }

    //@Test
    public void testWithFilter() throws CannotLoadContentDataset, CannotLoadRatingsDataset, UserNotFound, UserNotFound, ItemNotFound {
        Global.setVerbose();

        GroupRecommenderSystem_fixedFilePersistence rs = new GroupRecommenderSystem_fixedFilePersistence(new GroupRecommenderSystemWithPreFilter(
                new AggregationOfIndividualRatings(new KnnModelBasedCFRS()),
                new OutliersRatingsFilter(0.5, 0.2, true)), filePersistence);

        Collection<GroupEvaluationMeasure> evaluationMeasures = GroupEvaluationMeasuresFactory.getInstance().getAllClasses();

        GroupCaseStudy caseStudy = new DefaultGroupCaseStudy(
                datasetLoader,
                rs,
                groupFormationTechnique, new HoldOutGroupRatedItems(SEED), new NoPredictionProtocol(),
                evaluationMeasures,
                datasetLoader.getDefaultRelevanceCriteria(), NUM_EJECUCIONES);
        caseStudy.addExperimentListener(new ExperimentListerner_default(System.out, 10000));
        caseStudy.addExecutionProgressListener(new ExecutionProgressListener_default(System.out, 10000));

        String defaultFileName = GroupCaseStudyXML.getDefaultFileName(caseStudy);
        String prefix = "CheckCoverageTest_";

        caseStudy.setSeedValue(SEED);
        caseStudy.execute();

        GroupCaseStudyXML.saveCaseResults(caseStudy, prefix, defaultFileName);
        Global.showMessage("================ FIN CON FILTRO=================== \n");
    }

    //@Test
    public void testWithoutFilter() throws CannotLoadContentDataset, CannotLoadRatingsDataset, UserNotFound, UserNotFound, ItemNotFound {
        Global.setVerbose();

        Global.showMessage("================ INIT SIN FILTRO =================== \n");

        GroupRecommenderSystem_fixedFilePersistence rs = new GroupRecommenderSystem_fixedFilePersistence(new GroupRecommenderSystemWithPreFilter(
                new AggregationOfIndividualRatings(new KnnModelBasedCFRS()),
                new NoFilter()), filePersistence);

        Collection<GroupEvaluationMeasure> evaluationMeasures = GroupEvaluationMeasuresFactory.getInstance().getAllClasses();

        GroupCaseStudy caseStudy = new DefaultGroupCaseStudy(
                datasetLoader,
                rs,
                groupFormationTechnique, new HoldOutGroupRatedItems(SEED), new NoPredictionProtocol(),
                evaluationMeasures,
                datasetLoader.getDefaultRelevanceCriteria(), NUM_EJECUCIONES);
        caseStudy.addExperimentListener(new ExperimentListerner_default(System.out, 10000));
        caseStudy.addExecutionProgressListener(new ExecutionProgressListener_default(System.out, 10000));

        caseStudy.addExecutionProgressListener(new ExecutionProgressListener() {
            @Override
            public void executionProgressChanged(String proceso, int percent, long remainingMiliSeconds) {
                System.out.println(proceso + " --> " + percent + "% (" + DateCollapse.collapse(remainingMiliSeconds) + ")");
            }
        });

        String defaultFileName = GroupCaseStudyXML.getDefaultFileName(caseStudy);
        String prefix = "CheckCoverageTest_";

        caseStudy.setSeedValue(SEED);
        caseStudy.execute();

        GroupCaseStudyXML.saveCaseResults(caseStudy, prefix, defaultFileName);

        Global.showMessage("================ FIN SIN FILTRO=================== \n");
    }
}
