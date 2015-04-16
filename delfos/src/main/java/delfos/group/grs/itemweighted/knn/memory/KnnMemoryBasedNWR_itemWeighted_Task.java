package delfos.group.grs.itemweighted.knn.memory;

import java.util.Map;
import delfos.common.exceptions.dataset.users.UserNotFound;
import delfos.common.parallelwork.Task;
import delfos.dataset.basic.rating.Rating;
import delfos.dataset.basic.rating.RatingsDataset;
import delfos.rs.collaborativefiltering.profile.Neighbor;

/**
 * Clase que almacena los datos necesarios para ejecutar paralelamente el
 * cálculo de la similitud con un vecino.
 *
* @author Jorge Castro Gallardo
 *
 * @version 14-Noviembre-2013
 */
public class KnnMemoryBasedNWR_itemWeighted_Task extends Task {

    public final int idUser;
    public final Map<Integer, Double> itemWeights;
    public final int idNeighbor;
    public KnnMemoryBasedNWR_itemWeighted rs;
    public RatingsDataset<? extends Rating> ratingsDataset;
    public Neighbor neighbor = null;

    public KnnMemoryBasedNWR_itemWeighted_Task(
            RatingsDataset<? extends Rating> ratingsDataset,
            int idUser, Map<Integer, Double> itemWeights,
            int idNeighbor,
            KnnMemoryBasedNWR_itemWeighted rs) throws UserNotFound {
        this.ratingsDataset = ratingsDataset;
        this.idUser = idUser;
        this.itemWeights = itemWeights;
        this.idNeighbor = idNeighbor;
        this.rs = rs;
    }

    @Override
    public String toString() {
        StringBuilder str = new StringBuilder();

        str.append("idUser ---------> ").append(idUser).append("\n");
        str.append("itemWeights ----> ").append(itemWeights.toString()).append("\n");
        str.append("idNeighbor -----> ").append(idNeighbor).append("\n");
        str.append("rs -------------> ").append(rs.getAlias()).append("\n");
        str.append("\n").append(rs.getNameWithParameters());

        return str.toString();
    }

    public void setNeighbor(Neighbor neighbor) {
        this.neighbor = neighbor;
        rs = null;
        ratingsDataset = null;
    }

    public Neighbor getNeighbor() {
        return neighbor;
    }

    public Map<Integer, Double> getItemWeights() {
        return itemWeights;
    }
}
