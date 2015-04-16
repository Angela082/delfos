package delfos.rs.collaborativefiltering.svd;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import delfos.ERROR_CODES;
import delfos.common.exceptions.dataset.CannotLoadContentDataset;
import delfos.common.exceptions.dataset.CannotLoadRatingsDataset;
import delfos.common.exceptions.dataset.items.ItemAlreadyExists;
import delfos.dataset.basic.item.ContentDataset;
import delfos.dataset.basic.item.ContentDatasetDefault;
import delfos.dataset.basic.item.Item;
import delfos.dataset.basic.features.Feature;
import delfos.dataset.basic.features.FeatureGenerator;
import delfos.dataset.basic.features.FeatureType;
import delfos.dataset.basic.rating.Rating;
import delfos.dataset.basic.rating.RatingsDataset;
import delfos.dataset.basic.loader.types.DatasetLoaderAbstract;
import delfos.dataset.loaders.given.DatasetLoaderGivenRatingsContent;
import delfos.dataset.basic.loader.types.ContentDatasetLoader;
import delfos.dataset.storage.memory.BothIndexRatingsDataset;

/**
 *
 * @author Jorge Castro Gallardo (Sinbad2,Universidad de Jaén)
 * @version 1.0 22-Mar-2013
 */
public class MockDatasetLoader extends DatasetLoaderAbstract<Rating> implements ContentDatasetLoader {

    private final DatasetLoaderGivenRatingsContent<Rating> datasetLoader;

    public MockDatasetLoader() {

        //Ratings dataset
        List<Rating> ratings = new ArrayList<>(9);
        ratings.add(new Rating(1, 1, 1));
        ratings.add(new Rating(1, 2, 2));
        ratings.add(new Rating(1, 3, 3));
        ratings.add(new Rating(1, 4, 4));
        ratings.add(new Rating(1, 5, 5));
        ratings.add(new Rating(2, 1, 1));
        ratings.add(new Rating(2, 2, 2));
        ratings.add(new Rating(2, 3, 3));
        ratings.add(new Rating(2, 4, 4));
        ratings.add(new Rating(2, 5, 5));
        ratings.add(new Rating(3, 1, 1));
        ratings.add(new Rating(3, 5, 5));
        ratings.add(new Rating(3, 3, 3));
        BothIndexRatingsDataset<Rating> ratingsDataset = new BothIndexRatingsDataset<>(ratings);
        //Content dataset
        FeatureGenerator featureGenerator = new FeatureGenerator();
        LinkedList<Item> items = new LinkedList<>();
        Feature[] features = new Feature[4];
        featureGenerator.createFeature("feature1_float", FeatureType.Numerical);
        featureGenerator.createFeature("feature2_int", FeatureType.Numerical);
        featureGenerator.createFeature("feature3_class", FeatureType.Nominal);
        featureGenerator.createFeature("feature4_boolean", FeatureType.Nominal);
        features[0] = featureGenerator.searchFeature("feature1_float");
        features[1] = featureGenerator.searchFeature("feature2_int");
        features[2] = featureGenerator.searchFeature("feature3_class");
        features[3] = featureGenerator.searchFeature("feature4_boolean");
        Object[] item1_values = new Object[4];
        item1_values[0] = 5.4;
        item1_values[1] = 9;
        item1_values[2] = "class1";
        item1_values[3] = "true";
        Object[] item2_values = new Object[4];
        item2_values[0] = 5.5;
        item2_values[1] = 9;
        item2_values[2] = "class1";
        item2_values[3] = "false";
        Object[] item3_values = new Object[4];
        item3_values[0] = 5.6;
        item3_values[1] = 8;
        item3_values[2] = "class1";
        item3_values[3] = "false";
        Object[] item4_values = new Object[4];
        item4_values[0] = 5.7;
        item4_values[1] = 8;
        item4_values[2] = "class2";
        item4_values[3] = "false";
        items.add(new Item(1, "item1", features, item1_values));
        items.add(new Item(2, "item2", features, item2_values));
        items.add(new Item(3, "item1", features, item3_values));
        items.add(new Item(4, "item1", features, item4_values));
        ContentDataset contentDataset;
        try {
            contentDataset = new ContentDatasetDefault(items);
            datasetLoader = new DatasetLoaderGivenRatingsContent(ratingsDataset, contentDataset);
        } catch (ItemAlreadyExists ex) {
            IllegalStateException ise = new IllegalStateException("This exception should never be thrown in a regular execution, if the dataset data is properly added. If this exception is thrown, am item with duplicate idItem[" + ex.getIdItem() + "] is being added");
            ERROR_CODES.UNDEFINED_ERROR.exit(ise);
            throw ise;
        }
    }

    @Override
    public ContentDataset getContentDataset() throws CannotLoadContentDataset {
        return datasetLoader.getContentDataset();
    }

    @Override
    public RatingsDataset<Rating> getRatingsDataset() throws CannotLoadRatingsDataset {
        return datasetLoader.getRatingsDataset();
    }
}
