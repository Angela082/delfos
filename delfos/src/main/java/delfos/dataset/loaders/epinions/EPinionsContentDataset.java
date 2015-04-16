/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package delfos.dataset.loaders.epinions;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.Collection;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.Map;
import java.util.Set;
import delfos.ERROR_CODES;
import delfos.common.Chronometer;
import delfos.common.Global;
import delfos.common.datastructures.DoubleMapping;
import delfos.common.exceptions.dataset.entity.EntityNotFound;
import delfos.common.exceptions.dataset.items.ItemAlreadyExists;
import delfos.common.exceptions.dataset.items.ItemNotFound;
import delfos.dataset.basic.item.ContentDataset;
import delfos.dataset.basic.item.ContentDatasetDefault;
import delfos.dataset.basic.item.Item;
import delfos.dataset.basic.features.Feature;
import delfos.dataset.basic.features.FeatureGenerator;
import delfos.dataset.basic.features.FeatureType;

/**
 *
* @author Jorge Castro Gallardo
 */
public class EPinionsContentDataset implements ContentDataset {

    private final DoubleMapping<Long, Integer> productsIndex = new DoubleMapping<Long, Integer>();
    private final DoubleMapping<Long, Integer> authorsIndex = new DoubleMapping<Long, Integer>();
    private final DoubleMapping<Long, Integer> subjectsIndex = new DoubleMapping<Long, Integer>();

    private final ContentDatasetDefault contentDataset;

    private final FeatureGenerator featureGenerator = new FeatureGenerator();

    public EPinionsContentDataset(File contentFile) throws FileNotFoundException, IOException {

        featureGenerator.createFeature("author", FeatureType.Nominal);
        featureGenerator.createFeature("subject", FeatureType.Nominal);

        BufferedReader br = new BufferedReader(new FileReader(contentFile));
        String linea = br.readLine();

        int i = 1;

        Chronometer c = new Chronometer();

        LinkedList<Item> items = new LinkedList<Item>();

        while (linea != null) {

            String[] columns = linea.split("\\|");

            final long CONTENT_ID;
            final String AUTHOR_ID_STRING;
            final String SUBJECT_ID_STRING;

            CONTENT_ID = new Long(columns[0]);
            if (!productsIndex.containsType1Value(CONTENT_ID)) {
                productsIndex.add(CONTENT_ID, productsIndex.size() + 1);
            }

            AUTHOR_ID_STRING = columns[1];
            long AUTHOR_ID = new Long(AUTHOR_ID_STRING);
            if (!authorsIndex.containsType1Value(AUTHOR_ID)) {
                authorsIndex.add(AUTHOR_ID, authorsIndex.size() + 1);
            }

            Feature[] features;
            Object[] values;
            if (columns.length == 3) {
                SUBJECT_ID_STRING = columns[2];
                long SUBJECT_ID = new Long(SUBJECT_ID_STRING);

                if (!subjectsIndex.containsType1Value(SUBJECT_ID)) {
                    subjectsIndex.add(SUBJECT_ID, subjectsIndex.size() + 1);
                }

                features = new Feature[2];
                features[0] = featureGenerator.searchFeature("author");
                features[1] = featureGenerator.searchFeature("subject");

                values = new Object[2];
                values[0] = AUTHOR_ID_STRING;
                values[1] = SUBJECT_ID_STRING;
            } else {

                features = new Feature[1];
                features[0] = featureGenerator.searchFeature("author");

                values = new Object[1];
                values[0] = AUTHOR_ID_STRING;
            }

            int idItem = productsIndex.typeOneToTypeTwo(CONTENT_ID);

            Item item = new Item(idItem, "Item_" + idItem, features, values);
            items.add(item);

            linea = br.readLine();

            if (i % 100000 == 0) {
                Global.showMessage("Loading EPinions content --> " + i + " items " + c.printPartialElapsed() + " / " + c.printTotalElapsed() + "\n");
                c.setPartialEllapsedCheckpoint();
            }

            i++;
        }

        try {
            //Leo el archivo de contenido, construyo los items y los añado al dataset.
            contentDataset = new ContentDatasetDefault(items);
        } catch (ItemAlreadyExists ex) {
            ERROR_CODES.CANNOT_LOAD_CONTENT_DATASET.exit(ex);
            throw new IllegalArgumentException(ex);
        }
    }

    public DoubleMapping<Long, Integer> getProductsIndex() {
        return productsIndex;
    }

    @Override
    public Item get(int idItem) throws EntityNotFound {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public int size() {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public Collection<Integer> allID() {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public Collection<Integer> getAvailableItems() {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public void setItemAvailable(int idItem, boolean available) throws ItemNotFound {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public int compareTo(Object o) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public void add(Item entity) throws EntityNotFound {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public Feature[] getFeatures() {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public Set<Object> getAllFeatureValues(Feature feature) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public double getMinValue(Feature feature) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public double getMaxValue(Feature feature) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public Feature searchFeature(String featureName) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public Feature searchFeatureByExtendedName(String extendedName) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public Map<Feature, Object> parseEntityFeatures(Map<String, String> features) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public Collection<Integer> getAllID() {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public Map<Feature, Object> parseEntityFeaturesAndAddToExisting(int idEntity, Map<String, String> features) throws EntityNotFound {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public Iterator<Item> iterator() {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    public DoubleMapping<Long, Integer> getAuthorsIndex() {
        return authorsIndex;
    }

    public DoubleMapping<Long, Integer> getSubjectsIndex() {
        return subjectsIndex;
    }

    @Override
    public Item getItem(int idItem) throws ItemNotFound {
        try {
            return get(idItem);
        } catch (EntityNotFound ex) {
            ex.isA(ItemNotFound.class);
            throw new ItemNotFound(idItem, ex);
        }
    }
}