package delfos.group.grs.consensus.itemselector;

import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.TreeSet;
import delfos.rs.recommendation.Recommendation;

public class TopNOfEach extends GroupRecommendationsSelector {

    public TopNOfEach() {
        super();

        addParameter(NUMBER_OF_ITEM_SELECTED);
    }

    @Override
    public Set<Integer> getRecommendationSelection(Map<Integer, List<Recommendation>> membersRecommendations) {

        Set<Integer> itemsSelected = new TreeSet<>();
        Map<Integer, LinkedList<Recommendation>> removableRecommendations = new TreeMap<>();
        int numItemsToSelect = getNumItemsSelect();

        membersRecommendations.entrySet().stream().forEach((entry) -> {
            removableRecommendations.put(entry.getKey(), new LinkedList<>(entry.getValue()));
        });

        while (itemsSelected.size() != numItemsToSelect) {
            for (int idUser : removableRecommendations.keySet()) {
                LinkedList<Recommendation> removableRecommendationsThisUser = removableRecommendations.get(idUser);

                Recommendation firstRecommendation = null;
                do {
                    firstRecommendation = removableRecommendationsThisUser.removeFirst();
                } while (!itemsSelected.add(firstRecommendation.getIdItem()));

                if (itemsSelected.size() == numItemsToSelect) {
                    break;
                }
            }
        }

        return itemsSelected;
    }

    public int getNumItemsSelect() {
        return (Integer) getParameterValue(NUMBER_OF_ITEM_SELECTED);
    }
}