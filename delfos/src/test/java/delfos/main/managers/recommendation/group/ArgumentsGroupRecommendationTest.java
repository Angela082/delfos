package delfos.main.managers.recommendation.group;

import delfos.main.managers.recommendation.group.BuildRecommendationModel;
import delfos.main.managers.recommendation.group.Recommend;
import java.io.File;
import org.junit.Before;
import org.junit.Test;
import delfos.ConsoleParameters;
import delfos.common.FileUtilities;
import delfos.common.aggregationoperators.Mean;
import delfos.configfile.rs.single.RecommenderSystemConfigurationFileParser;
import delfos.configureddatasets.ConfiguredDatasetsFactory;
import delfos.constants.DelfosTest;
import delfos.dataset.basic.rating.Rating;
import delfos.dataset.basic.loader.types.DatasetLoader;
import delfos.group.grs.GroupRecommenderSystem;
import delfos.group.grs.aggregation.AggregationOfIndividualRatings;
import delfos.main.Main;
import delfos.main.managers.CaseUseManagerTest;
import delfos.recommendationcandidates.OnlyNewItems;
import delfos.rs.collaborativefiltering.knn.memorybased.nwr.KnnMemoryBasedNWR;
import delfos.rs.output.RecommendationsOutputStandardRaw;
import delfos.rs.output.sort.SortBy;
import delfos.rs.persistence.FilePersistence;

/**
 *
 * @author Jorge Castro Gallardo
 */
public class ArgumentsGroupRecommendationTest extends DelfosTest {

    public ArgumentsGroupRecommendationTest() {
    }

    /**
     * Directorio en el que se almacenan los ficheros relacionados con los tests
     * del manejo de un dataset.
     */
    private final static String TEST_DIRECTORY = DelfosTest.getTemporalDirectoryForTest(ArgumentsGroupRecommendationTest.class).getPath() + File.separator;
    /**
     * Nombre del fichero que almacena la configuración del dataset manejado por
     * la biblioteca.
     */
    private final static String GROUP_RECOMMENDER_SYSTEM_CONFIG_XML = TEST_DIRECTORY + "groupRecommenderSystemConfiguration.xml";

    @Before
    public void beforeTest() {
        FileUtilities.cleanDirectory(new File(TEST_DIRECTORY));
    }

    @Test
    public void test_Group_BuildRecommendationModel_manageCaseUse() throws Exception {
        System.out.println("test_Group_BuildRecommendationModel_manageCaseUse");

        createConfigurationFile();
        String[] consoleArguments = {
            "-group-recommendation",
            "--build",
            "-config-file",
            GROUP_RECOMMENDER_SYSTEM_CONFIG_XML
        };

        BuildRecommendationModel.getInstance().manageCaseUse(new ConsoleParameters(consoleArguments));
    }

    @Test
    public void test_Group_Recommend_manageCaseUse() throws Exception {
        System.out.println("test_Group_Recommend_manageCaseUse");

        createConfigurationFile();

        test_Group_BuildRecommendationModel_manageCaseUse();

        String[] consoleArguments = {
            "-group-recommendation",
            "-group-members",
            "1", "65", "89", "54", "256",
            "-config-file",
            GROUP_RECOMMENDER_SYSTEM_CONFIG_XML

        };

        Recommend.getInstance()
                .manageCaseUse(new ConsoleParameters(consoleArguments));
    }

    @Test(expected = NumberFormatException.class)
    public void test_Group_Recommend_manageCaseUse_illegalUserIdMustThrowNumberFormatException() throws Exception {
        System.out.println("test_Group_Recommend_manageCaseUse");

        createConfigurationFile();

        test_Group_BuildRecommendationModel_manageCaseUse();

        String[] consoleArguments = {
            "-group-recommendation",
            "-group-members",
            "65", "89", "ABC", "54", "256",
            "-config-file",
            GROUP_RECOMMENDER_SYSTEM_CONFIG_XML

        };

        Recommend.getInstance()
                .manageCaseUse(new ConsoleParameters(consoleArguments));
    }

    @Test
    public void test_Group_BuildRecommendationModel_callFromCommandLine() throws Exception {
        System.out.println("test_Group_BuildRecommendationModel_manageCaseUse");

        createConfigurationFile();
        String[] consoleArguments = {
            "-group-recommendation",
            "--build",
            "-config-file",
            GROUP_RECOMMENDER_SYSTEM_CONFIG_XML
        };

        CaseUseManagerTest.testCaseUse(BuildRecommendationModel.getInstance(), consoleArguments);
        Main.mainWithExceptions(consoleArguments);
    }

    @Test
    public void test_Group_Recommend_callFromCommandLine() throws Exception {
        System.out.println("test_Group_Recommend_callFromCommandLine");

        createConfigurationFile();

        test_Group_BuildRecommendationModel_manageCaseUse();

        String[] consoleArguments = {
            "-group-recommendation",
            "-group-members",
            "1", "65", "89", "54", "256",
            "-config-file",
            GROUP_RECOMMENDER_SYSTEM_CONFIG_XML

        };

        CaseUseManagerTest.testCaseUse(Recommend.getInstance(), consoleArguments);
        Main.mainWithExceptions(consoleArguments);
    }

    private void createConfigurationFile() {

        File directory = new File(TEST_DIRECTORY);

        GroupRecommenderSystem groupRecommenderSystem
                = new AggregationOfIndividualRatings(new KnnMemoryBasedNWR(), new Mean());
        groupRecommenderSystem.setAlias("RatingAggregationGRS");

        DatasetLoader<? extends Rating> datasetLoader
                = ConfiguredDatasetsFactory.getInstance().getDatasetLoader("ml-100k");

        FilePersistence filePersistence = new FilePersistence(
                "recommendation-model-" + groupRecommenderSystem.getAlias().toLowerCase(),
                "data",
                directory);

        RecommenderSystemConfigurationFileParser.saveConfigFile(
                GROUP_RECOMMENDER_SYSTEM_CONFIG_XML,
                groupRecommenderSystem,
                datasetLoader,
                filePersistence,
                new OnlyNewItems(),
                new RecommendationsOutputStandardRaw(SortBy.SORT_BY_PREFERENCE));

    }
}