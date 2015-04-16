package delfos.dataset.mockdatasets;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.TreeSet;
import delfos.ERROR_CODES;
import delfos.common.exceptions.dataset.entity.EntityAlreadyExists;
import delfos.common.exceptions.dataset.entity.EntityNotFound;
import delfos.common.exceptions.dataset.items.ItemNotFound;
import delfos.dataset.basic.item.ContentDataset;
import delfos.dataset.basic.item.Item;
import delfos.dataset.basic.features.Feature;
import delfos.dataset.basic.features.FeatureGenerator;
import delfos.dataset.basic.features.FeatureType;

/**
 *
 * @author Jorge
 * @version 10-Octubre-2013
 */
public final class MockContentDataset implements ContentDataset {

    private final Map<Integer, Item> items = new TreeMap<Integer, Item>();
    private final FeatureGenerator featureGenerator = new FeatureGenerator();
    private final Feature featurePriceNumerical;
    private final Feature featureClassNominal;

    /**
     * Comprueba que el producto no existe en el conjunto. Si existe lanza una
     * excepción {@link IllegalArgumentException}.
     *
     * @param idItem
     */
    private void checkItemNotExists(int idItem) {
        if (items.containsKey(idItem)) {
            throw new IllegalArgumentException("The item " + idItem + " already exists.");
        }
    }

    private void checkItem(int idItem) throws ItemNotFound {
        if (!items.containsKey(idItem)) {
            throw new ItemNotFound(idItem);
        }
    }

    public MockContentDataset() {

        featureGenerator.createFeature("class", FeatureType.Nominal);
        featureGenerator.createFeature("price", FeatureType.Numerical);

        featureClassNominal = featureGenerator.searchFeature("class");
        featurePriceNumerical = featureGenerator.searchFeature("price");

        Object[] featureValues;
        int idItem;
        Item item;

        idItem = 1;
        featureValues = new Object[2];
        featureValues[0] = "A";
        featureValues[1] = 1.0;
        item = new Item(idItem, "Item " + idItem, featureGenerator.getSortedFeatures().toArray(new Feature[0]), featureValues);
        try {
            this.add(item);
        } catch (EntityAlreadyExists ex) {
            ERROR_CODES.ITEM_ALREADY_EXISTS.exit(ex);
        }

        idItem = 2;
        featureValues = new Object[2];
        featureValues[0] = "B";
        featureValues[1] = 1.0;
        item = new Item(idItem, "Item " + idItem, featureGenerator.getSortedFeatures().toArray(new Feature[0]), featureValues);
        try {
            this.add(item);
        } catch (EntityAlreadyExists ex) {
            ERROR_CODES.ITEM_ALREADY_EXISTS.exit(ex);
        }

        idItem = 3;
        featureValues = new Object[2];
        featureValues[0] = "C";
        featureValues[1] = 1.0;
        item = new Item(idItem, "Item " + idItem, featureGenerator.getSortedFeatures().toArray(new Feature[0]), featureValues);

        try {
            this.add(item);
        } catch (EntityAlreadyExists ex) {
            ERROR_CODES.ITEM_ALREADY_EXISTS.exit(ex);
        }

        idItem = 4;
        featureValues = new Object[2];
        featureValues[0] = "A";
        featureValues[1] = 2.5;
        item = new Item(idItem, "Item " + idItem, featureGenerator.getSortedFeatures().toArray(new Feature[0]), featureValues);

        try {
            this.add(item);
        } catch (EntityAlreadyExists ex) {
            ERROR_CODES.ITEM_ALREADY_EXISTS.exit(ex);
        }

        idItem = 5;
        featureValues = new Object[2];
        featureValues[0] = "B";
        featureValues[1] = 2.5;
        item = new Item(idItem, "Item " + idItem, featureGenerator.getSortedFeatures().toArray(new Feature[0]), featureValues);

        try {
            this.add(item);
        } catch (EntityAlreadyExists ex) {
            ERROR_CODES.ITEM_ALREADY_EXISTS.exit(ex);
        }

        idItem = 6;
        featureValues = new Object[2];
        featureValues[0] = "C";
        featureValues[1] = 2.5;
        item = new Item(idItem, "Item " + idItem, featureGenerator.getSortedFeatures().toArray(new Feature[0]), featureValues);

        try {
            this.add(item);
        } catch (EntityAlreadyExists ex) {
            ERROR_CODES.ITEM_ALREADY_EXISTS.exit(ex);
        }

        idItem = 7;
        featureValues = new Object[2];
        featureValues[0] = "A";
        featureValues[1] = 3.0;
        item = new Item(idItem, "Item " + idItem, featureGenerator.getSortedFeatures().toArray(new Feature[0]), featureValues);

        try {
            this.add(item);
        } catch (EntityAlreadyExists ex) {
            ERROR_CODES.ITEM_ALREADY_EXISTS.exit(ex);
        }

        idItem = 8;
        featureValues = new Object[2];
        featureValues[0] = "B";
        featureValues[1] = 3.0;
        item = new Item(idItem, "Item " + idItem, featureGenerator.getSortedFeatures().toArray(new Feature[0]), featureValues);

        try {
            this.add(item);
        } catch (EntityAlreadyExists ex) {
            ERROR_CODES.ITEM_ALREADY_EXISTS.exit(ex);
        }

        idItem = 9;
        featureValues = new Object[2];
        featureValues[0] = "C";
        featureValues[1] = 3.0;
        item = new Item(idItem, "Item " + idItem, featureGenerator.getSortedFeatures().toArray(new Feature[0]), featureValues);

        try {
            this.add(item);
        } catch (EntityAlreadyExists ex) {
            ERROR_CODES.ITEM_ALREADY_EXISTS.exit(ex);
        }

        idItem = 10;
        featureValues = new Object[2];
        featureValues[0] = "A";
        featureValues[1] = 1.0;
        item = new Item(idItem, "Item " + idItem, featureGenerator.getSortedFeatures().toArray(new Feature[0]), featureValues);

        try {
            this.add(item);
        } catch (EntityAlreadyExists ex) {
            ERROR_CODES.ITEM_ALREADY_EXISTS.exit(ex);
        }

        idItem = 11;
        featureValues = new Object[2];
        featureValues[0] = "B";
        featureValues[1] = 2.5;
        item = new Item(idItem, "Item " + idItem, featureGenerator.getSortedFeatures().toArray(new Feature[0]), featureValues);

        try {
            this.add(item);
        } catch (EntityAlreadyExists ex) {
            ERROR_CODES.ITEM_ALREADY_EXISTS.exit(ex);
        }

        idItem = 12;
        featureValues = new Object[2];
        featureValues[0] = "C";
        featureValues[1] = 3.0;
        item = new Item(idItem, "Item " + idItem, featureGenerator.getSortedFeatures().toArray(new Feature[0]), featureValues);

        try {
            this.add(item);
        } catch (EntityAlreadyExists ex) {
            ERROR_CODES.ITEM_ALREADY_EXISTS.exit(ex);
        }

        idItem = 13;
        featureValues = new Object[2];
        featureValues[0] = "D";
        featureValues[1] = 4.0;
        item = new Item(idItem, "Item " + idItem, featureGenerator.getSortedFeatures().toArray(new Feature[0]), featureValues);

        try {
            this.add(item);
        } catch (EntityAlreadyExists ex) {
            ERROR_CODES.ITEM_ALREADY_EXISTS.exit(ex);
        }

    }

    @Override
    public Item get(int idItem) throws EntityNotFound {
        try {
            checkItem(idItem);
        } catch (ItemNotFound ex) {
            throw new EntityNotFound(Item.class, idItem, ex);
        }
        return items.get(idItem);
    }

    @Override
    public int size() {
        return items.size();
    }

    @Override
    public Collection<Integer> allID() {
        return new ArrayList<>(items.keySet());
    }

    @Override
    public Collection<Integer> getAvailableItems() {
        return allID();
    }

    @Override
    public void setItemAvailable(int idItem, boolean available) throws ItemNotFound {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public int compareTo(Object o) {
        return new Integer(this.hashCode()).compareTo(o.hashCode());
    }

    @Override
    public void add(Item entity) throws EntityAlreadyExists {
        checkItemNotExists(entity.getId());

        items.put(entity.getId(), entity);
    }

    @Override
    public Feature[] getFeatures() {
        return featureGenerator.getSortedFeatures().toArray(new Feature[0]);
    }

    @Override
    public Set<Object> getAllFeatureValues(Feature feature) {
        if (feature.equals(featureClassNominal)) {
            Set<Object> ret = new TreeSet<>();
            ret.add("A");
            ret.add("B");
            ret.add("C");
            ret.add("D");
            return ret;
        }

        if (feature.equals(featurePriceNumerical)) {
            Set<Object> ret = new TreeSet<>();
            ret.add(1.0);
            ret.add(2.5);
            ret.add(3.0);
            ret.add(4.0);
            return ret;
        }

        throw new IllegalArgumentException("Feature '" + feature + "'not known ");
    }

    @Override
    public double getMinValue(Feature feature) {
        if (feature.equals(featureClassNominal)) {
            throw new IllegalArgumentException("Not a numerical feature '" + feature + "'");
        }

        if (feature.equals(featurePriceNumerical)) {
            return 1;
        }

        throw new IllegalArgumentException("Feature '" + feature + "'not known ");
    }

    @Override
    public double getMaxValue(Feature feature) {
        if (feature.equals(featureClassNominal)) {
            throw new IllegalArgumentException("Not a numerical feature '" + feature + "'");
        }

        if (feature.equals(featurePriceNumerical)) {
            return 4;
        }

        throw new IllegalArgumentException("Feature '" + feature + "'not known ");
    }

    @Override
    public Feature searchFeature(String featureName) {
        if (featureName.equals(featureClassNominal.getName())) {
            return featureClassNominal;
        }

        if (featureName.equals(featurePriceNumerical.getName())) {
            return featurePriceNumerical;
        }
        return null;
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
        return new ArrayList<>(items.keySet());
    }

    @Override
    public Map<Feature, Object> parseEntityFeaturesAndAddToExisting(int idEntity, Map<String, String> features) throws EntityNotFound {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public Iterator<Item> iterator() {
        return new ArrayList<>(items.values()).iterator();
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
