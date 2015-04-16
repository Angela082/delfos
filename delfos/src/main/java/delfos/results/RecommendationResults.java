package delfos.results;

import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.TreeSet;
import delfos.dataset.basic.user.User;
import delfos.rs.recommendation.Recommendation;
import delfos.rs.recommendation.Recommendations;
import delfos.rs.recommendation.SingleUserRecommendations;

/**
 * Guarda los resultados de una ejecución, es decir, las recomendaciones que se
 * hacen a todos los usuarios con el mismo conjunto de datos de training
 *
 * @author Jorge Castro Gallardo
 * @version 1.0 (19 Octubre 2011)
 */
public class RecommendationResults {

    private final Map<Integer, List<Recommendation>> recommendationResults;

    public Set<Integer> usersWithRecommendations() {
        return new TreeSet<>(recommendationResults.keySet());
    }

    /**
     * Añade las recomendaciones de un usuario a los resultados
     *
     * @param idUser id del usuario para el que se añaden las recomendaciones
     * @param recommendations lista de recomendaciones que se le dan ordenadas
     * por relevancia (similitud o valoración predicha)
     */
    public void add(int idUser, List<Recommendation> recommendations) {
        if (!recommendationResults.containsKey(idUser)) {
            recommendationResults.put(idUser, recommendations);
        } else {
            recommendationResults.get(idUser).addAll(recommendations);
            Collections.sort(recommendationResults.get(idUser));
        }
    }

    /**
     * Constructor de la clase, que crea unos resultados de ejecución vacíos
     */
    public RecommendationResults() {
        this.recommendationResults = new TreeMap<>();
    }

    public RecommendationResults(List<SingleUserRecommendations> allRecommendations) {

        this.recommendationResults = new TreeMap<>();
        for (Recommendations recommendations : allRecommendations) {
            Integer idUser = User.parseIdTarget(recommendations.getTargetIdentifier()).getId();

            add(idUser, recommendations.getRecommendations());
        }
    }

    /**
     * Comprueba si a un usuario se le ha recomendado un item determinado
     *
     * @param idUser id del usuario para el que se realiza la comprobación
     * @param idItem id del item que se desea comprobar si se le ha recomendado
     * @return true si al usuario con id <code>idUser</code> se le ha
     * recomendado el item con id  <code>idItem</code>, false si no.
     */
    public boolean containsRecommendation(int idUser, int idItem) {
        if (recommendationResults.containsKey(idUser)) {
            boolean found = false;
            List<Recommendation> get = recommendationResults.get(idUser);
            int i = 0;
            while (i < get.size() && !found) {
                if (get.get(i).getIdItem() == idItem) {
                    found = true;
                }
                i++;
            }
            return found;
        } else {
            return false;
        }
    }

    public List<Recommendation> getRecommendationsForUser(int idUser) {
        return recommendationResults.get(idUser);
    }

    public void clear() {
        for (Iterator<Map.Entry<Integer, List<Recommendation>>> it = recommendationResults.entrySet().iterator(); it.hasNext();) {
            Map.Entry<Integer, List<Recommendation>> list = it.next();
            list.getValue().clear();
            it.remove();
        }
        recommendationResults.clear();
    }
}