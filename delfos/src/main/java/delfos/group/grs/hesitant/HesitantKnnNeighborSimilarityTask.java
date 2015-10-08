package delfos.group.grs.hesitant;

import delfos.common.exceptions.dataset.users.UserNotFound;
import delfos.common.parallelwork.Task;
import delfos.dataset.basic.item.Item;
import delfos.dataset.basic.loader.types.DatasetLoader;
import delfos.dataset.basic.rating.Rating;
import delfos.group.groupsofusers.GroupOfUsers;
import delfos.rs.collaborativefiltering.profile.Neighbor;
import es.jcastro.hesitant.HesitantValuation;
import es.jcastro.hesitant.similarity.HesitantSimilarity;

/**
 * Clase que almacena los datos necesarios para ejecutar paralelamente el
 * cálculo de la similitud con un vecino.
 *
 * @author Jorge Castro Gallardo
 *
 * @version 14-Noviembre-2013
 */
public class HesitantKnnNeighborSimilarityTask extends Task {

    public final int idNeighbor;
    public DatasetLoader<? extends Rating> datasetLoader;
    public Neighbor neighbor = null;
    public final GroupOfUsers groupOfUsers;
    public final HesitantValuation<Item, Double> groupModel;
    public HesitantSimilarity hesitantSimilarity;

    public HesitantKnnNeighborSimilarityTask(
            DatasetLoader<? extends Rating> datasetLoader,
            GroupOfUsers groupOfUsers, HesitantValuation<Item, Double> groupModel,
            int idNeighbor, HesitantSimilarity similarity) throws UserNotFound {
        this.datasetLoader = datasetLoader;
        this.groupModel = groupModel;

        this.groupOfUsers = groupOfUsers;
        this.idNeighbor = idNeighbor;
        this.hesitantSimilarity = similarity;
    }

    @Override
    public String toString() {
        StringBuilder str = new StringBuilder();

        str.append("group ----------> ").append(groupOfUsers.getGroupMembers()).append("\n");
        str.append("idNeighbor -----> ").append(idNeighbor).append("\n");
        str.append("rs -------------> ").append(hesitantSimilarity.getClass().getSimpleName()).append("\n");

        return str.toString();
    }

    public void setNeighbor(Neighbor neighbor) {
        this.neighbor = neighbor;
        hesitantSimilarity = null;
        datasetLoader = null;
    }

    public Neighbor getNeighbor() {
        return neighbor;
    }
}