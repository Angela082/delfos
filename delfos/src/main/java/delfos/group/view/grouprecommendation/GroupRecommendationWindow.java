package delfos.group.view.grouprecommendation;

import java.io.File;
import java.util.List;
import java.util.Random;
import java.util.Set;
import java.util.TreeSet;
import javax.swing.JFrame;
import delfos.common.exceptions.dataset.CannotLoadContentDataset;
import delfos.common.exceptions.dataset.CannotLoadRatingsDataset;
import delfos.common.exceptions.dataset.items.ItemNotFound;
import delfos.common.exceptions.dataset.users.UserNotFound;
import delfos.dataset.basic.loader.types.ContentDatasetLoader;
import delfos.dataset.basic.loader.types.DatasetLoader;
import delfos.dataset.basic.rating.Rating;
import delfos.dataset.basic.rating.RatingsDataset;
import delfos.dataset.loaders.csv.CSVfileDatasetLoader;
import delfos.group.experiment.validation.recommendableitems.NeverRatedItems;
import delfos.group.groupsofusers.GroupOfUsers;
import delfos.group.grs.SingleRecommenderSystemModel;
import delfos.group.grs.benchmark.polylens.PolyLens;
import delfos.group.grs.recommendations.GroupRecommendations;
import delfos.group.view.InitialFrame;
import delfos.rs.output.RecommendationsOutputStandardRaw;
import delfos.rs.recommendation.Recommendation;
import delfos.rs.recommendation.RecommendationComputationDetails;

/**
 *
 * @author Jorge Castro Gallardo
 */
public class GroupRecommendationWindow extends JFrame {

    private final InitialFrame initialFrame;
    private final InitialFrame aThis;

    public GroupRecommendationWindow(InitialFrame aThis) {
        if (1 == 1) {
            throw new UnsupportedOperationException("Not yet implemented");
        }
        this.aThis = aThis;
    }

    public void recomendar() throws CannotLoadRatingsDataset, CannotLoadContentDataset, UserNotFound, ItemNotFound {
        //Recomendar

        PolyLens groupRecommenderSystem = new PolyLens();
        DatasetLoader<? extends Rating> datasetLoader = new CSVfileDatasetLoader();
        datasetLoader.setParameterValue(CSVfileDatasetLoader.RATINGS_FILE, new File("datasets" + File.separator + "dummyRatings.csv"));
        datasetLoader.setParameterValue(CSVfileDatasetLoader.CONTENT_FILE, new File("datasets" + File.separator + "dummyMovies.csv"));
        datasetLoader.setParameterValue(CSVfileDatasetLoader.INDEXATION, CSVfileDatasetLoader.INDEX_BOTH);

        GroupOfUsers groupOfUsers = new GroupOfUsers();

        Random r = new Random(System.currentTimeMillis());
        RatingsDataset<? extends Rating> ratingsDataset = datasetLoader.getRatingsDataset();
        Integer[] allUsers = ratingsDataset.allUsers().toArray(new Integer[0]);
        while (groupOfUsers.size() < 4) {
            groupOfUsers.addUser(allUsers[r.nextInt(allUsers.length)]);
        }
        SingleRecommenderSystemModel build = groupRecommenderSystem.build(datasetLoader);
        GroupOfUsers buildGroupModel = groupRecommenderSystem.buildGroupModel(datasetLoader, build, groupOfUsers);

        NeverRatedItems nri = new NeverRatedItems();

        Set<Integer> allItems;
        if (datasetLoader instanceof ContentDatasetLoader) {
            ContentDatasetLoader contentDatasetLoader = (ContentDatasetLoader) datasetLoader;
            allItems = new TreeSet<>(contentDatasetLoader.getContentDataset().allID());
        } else {
            allItems = new TreeSet<>(datasetLoader.getRatingsDataset().allRatedItems());
        }

        List<Recommendation> recomm = groupRecommenderSystem.recommendOnly(datasetLoader, build, buildGroupModel, groupOfUsers, allItems);

        RecommendationsOutputStandardRaw output = new RecommendationsOutputStandardRaw();
        output.writeRecommendations(new GroupRecommendations(groupOfUsers, recomm, RecommendationComputationDetails.EMPTY_DETAILS));

    }
}
