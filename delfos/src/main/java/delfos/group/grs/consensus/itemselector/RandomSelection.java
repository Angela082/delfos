package delfos.group.grs.consensus.itemselector;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Set;
import java.util.TreeSet;
import delfos.experiment.SeedHolder;
import delfos.rs.recommendation.Recommendation;

public class RandomSelection extends GroupRecommendationsSelector implements SeedHolder {

    public RandomSelection() {
        super();

        addParameter(NUMBER_OF_ITEM_SELECTED);
        addParameter(SEED);
    }

    @Override
    public Set<Integer> getRecommendationSelection(Map<Integer, List<Recommendation>> membersRecommendations) {

        long groupSeed = getGroupSeed(membersRecommendations.keySet());
        long numItems = getNumItemsSelect();
        Random random = new Random(groupSeed);

        Set<Integer> itemsToSelect = new TreeSet<>();
        Set<Integer> itemsSelected = new TreeSet<>();

        int idUser = membersRecommendations.keySet().iterator().next();
        membersRecommendations.get(idUser).stream().forEach((r) -> {
            itemsToSelect.add(r.getIdItem());
        });

        while (itemsSelected.size() < numItems) {
            int nextRandom = random.nextInt(itemsToSelect.size());
            int idItem = itemsToSelect.toArray(new Integer[0])[nextRandom];
            itemsSelected.add(idItem);
        }

        itemsSelected = Collections.unmodifiableSet(itemsSelected);

        return itemsSelected;
    }

    @Override
    public void setSeedValue(long seedValue) {
        setParameterValue(SEED, seedValue);
    }

    @Override
    public long getSeedValue() {
        return (Long) getParameterValue(SEED);
    }

    private long getGroupSeed(Set<Integer> keySet) {
        long seedValue = getSeedValue();

        for (int idUser : keySet) {
            seedValue += idUser;
        }
        return seedValue;
    }

    public int getNumItemsSelect() {
        return (Integer) getParameterValue(NUMBER_OF_ITEM_SELECTED);
    }
}