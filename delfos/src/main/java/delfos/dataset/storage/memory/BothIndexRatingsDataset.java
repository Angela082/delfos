package delfos.dataset.storage.memory;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.TreeSet;
import java.util.logging.Level;
import java.util.logging.Logger;
import delfos.ERROR_CODES;
import delfos.common.exceptions.dataset.items.ItemNotFound;
import delfos.common.exceptions.dataset.users.UserNotFound;
import delfos.dataset.basic.rating.Rating;
import delfos.dataset.basic.rating.RatingsDataset;
import delfos.dataset.basic.rating.RatingsDatasetAdapter;
import delfos.dataset.basic.rating.domain.DecimalDomain;
import delfos.dataset.basic.rating.domain.Domain;

/**
 * Dataset que almacena las valoraciones doblemente indexadas, es decir, por
 * usuarios y por productos. De esta manera se gana en eficiencia temporal a
 * costa de utilizar una mayor cantidad de memoria ram
 *
* @author Jorge Castro Gallardo
 *
 * @version 1.1 (21-01-2013) Ahora implementa de {@link RatingsDatasetAdapter}
 * @version 1.0 Unknow date
 * @param <RatingType>
 */
public class BothIndexRatingsDataset<RatingType extends Rating> extends RatingsDatasetAdapter<RatingType> {

    protected Map<Integer, Map<Integer, RatingType>> userIndex = new TreeMap<>();
    protected Map<Integer, Map<Integer, RatingType>> itemIndex = new TreeMap<>();
    protected int numRatings = 0;

    /**
     * Crea un dataset doblemente indexado (por usuarios y por productos) para
     * ganar en eficiencia en tiempo utilizando una mayor cantidad de memoria.
     * El dataset está vacío iniciamlente
     */
    public BothIndexRatingsDataset() {
        //Se crea vacío
    }

    /**
     * Genera el dataset con las valoraciones de otro. Constructor por copia.
     *
     * @param ratingsDataset
     */
    public BothIndexRatingsDataset(RatingsDataset<RatingType> ratingsDataset) {
        for (int idUser : ratingsDataset.allUsers()) {
            try {
                Map<Integer, RatingType> userRatingsRated = ratingsDataset.getUserRatingsRated(idUser);
                for (int idItem : userRatingsRated.keySet()) {
                    RatingType rating = userRatingsRated.get(idItem);
                    addOneRating(rating);
                }
            } catch (UserNotFound ex) {
                ERROR_CODES.USER_NOT_FOUND.exit(ex);
            }
        }
    }

    /**
     * Crea un dataset doblemente indexado (por usuarios y por productos) para
     * ganar en eficiencia en tiempo utilizando una mayor cantidad de memoria.
     *
     * @param ratingsIndexedByUser Valoraciones de todos los usuarios.
     */
    public BothIndexRatingsDataset(Map<Integer, Map<Integer, RatingType>> ratingsIndexedByUser) {
        for (int idUser : ratingsIndexedByUser.keySet()) {
            for (int idItem : ratingsIndexedByUser.get(idUser).keySet()) {
                RatingType rating = ratingsIndexedByUser.get(idUser).get(idItem);
                addOneRating(rating);
            }
        }
    }

    /**
     * Genera el dataset añadiendo valoraciones a un dataset.
     *
     * @param ratingsDataset dataset de origen
     * @param ratingsIndexedByUser valoraciones que se añaden.
     */
    public BothIndexRatingsDataset(RatingsDataset<RatingType> ratingsDataset, Map<Integer, Map<Integer, RatingType>> ratingsIndexedByUser) {

        checkDatasetsAreDisjointInUsers(ratingsDataset, ratingsIndexedByUser);

        try {
            for (int idUser : ratingsDataset.allUsers()) {
                try {
                    Map<Integer, RatingType> userRatingsRated = ratingsDataset.getUserRatingsRated(idUser);
                    for (int idItem : userRatingsRated.keySet()) {
                        Rating rating = userRatingsRated.get(idItem);
                        addOneRating((RatingType) rating);
                    }
                } catch (UserNotFound ex) {
                    ERROR_CODES.USER_NOT_FOUND.exit(ex);
                }
            }

            for (int idUser : ratingsIndexedByUser.keySet()) {
                for (int idItem : ratingsIndexedByUser.get(idUser).keySet()) {

                    RatingType rating = ratingsIndexedByUser.get(idUser).get(idItem);
                    addOneRating((RatingType) rating.clone());

                }
            }
        } catch (CloneNotSupportedException ex) {
            Logger.getLogger(BothIndexRatingsDataset.class.getName()).log(Level.SEVERE, null, ex);
        }

    }

    private void checkDatasetsAreDisjointInUsers(RatingsDataset<RatingType> ratingsDataset, Map<Integer, Map<Integer, RatingType>> ratingsIndexedByUser) throws IllegalArgumentException {
        Set<Integer> intersection = new TreeSet<>(ratingsDataset.allUsers());
        intersection.retainAll(ratingsIndexedByUser.keySet());
        if (!intersection.isEmpty()) {
            throw new IllegalArgumentException("The datasets share users: " + intersection);
        }
    }

    public BothIndexRatingsDataset(RatingsDataset<RatingType> ratingsDataset, Iterable<RatingType> ratings) {
        for (int idUser : ratingsDataset.allUsers()) {
            try {
                Map<Integer, RatingType> userRatingsRated = ratingsDataset.getUserRatingsRated(idUser);
                for (int idItem : userRatingsRated.keySet()) {
                    RatingType rating = userRatingsRated.get(idItem);
                    addOneRating(rating);
                }
            } catch (UserNotFound ex) {
                ERROR_CODES.USER_NOT_FOUND.exit(ex);
            }
        }

        for (RatingType r : ratings) {
            addOneRating(r);
        }
    }

    public BothIndexRatingsDataset(Iterable<RatingType> ratings) {
        for (RatingType r : ratings) {
            addOneRating(r);
        }
    }

    @Override
    public RatingType getRating(int idUser, int idItem) throws ItemNotFound, UserNotFound {

        if (!itemIndex.containsKey(idItem)) {
            throw new ItemNotFound(idItem);
        }

        if (!userIndex.containsKey(idUser)) {
            throw new UserNotFound(idUser);
        }

        if (userIndex.get(idUser).containsKey(idItem)) {
            RatingType rating = userIndex.get(idUser).get(idItem);
            return rating;
        } else {
            return null;
        }
    }

    protected final void addOneRating(RatingType rating) {
        final int idUser = rating.idUser;
        final int idItem = rating.idItem;

        //Añado el producto a la lista de productos.
        if (!itemIndex.containsKey(idItem)) {
            itemIndex.put(idItem, new TreeMap<>());
        }

        //Añado el usuario a la lista de usuarios.
        if (!userIndex.containsKey(idUser)) {
            userIndex.put(idUser, new TreeMap<>());
        }

        if (userIndex.get(idUser).containsKey(idItem)) {
            throw new IllegalArgumentException("The rating was already in the dataset");
        } else {
            userIndex.get(idUser).put(idItem, rating);
        }

        if (itemIndex.get(idItem).containsKey(idUser)) {
            throw new IllegalArgumentException("The rating was already in the dataset");
        } else {
            itemIndex.get(idItem).put(idUser, rating);
        }

        if (!(userIndex.get(idUser).get(idItem) == itemIndex.get(idItem).get(idUser))) {
            throw new IllegalArgumentException("User index and item index is different!");
        }
        numRatings++;
    }

    @Override
    public Collection<Integer> allUsers() {
        return new ArrayList<>(userIndex.keySet());
    }

    @Override
    public Collection<Integer> allRatedItems() {
        return new ArrayList<>(itemIndex.keySet());
    }

    @Override
    public Collection<Integer> getUserRated(Integer idUser) throws UserNotFound {
        if (userIndex.containsKey(idUser)) {
            return Collections.unmodifiableCollection(getUserRatingsRated(idUser).keySet());
        } else {
            return Collections.EMPTY_LIST;
        }
    }

    @Override
    public Collection<Integer> getItemRated(Integer idItem) throws ItemNotFound {
        if (itemIndex.containsKey(idItem)) {
            return Collections.unmodifiableCollection(getItemRatingsRated(idItem).keySet());
        } else {
            return Collections.EMPTY_LIST;
        }
    }

    @Override
    public Map<Integer, RatingType> getUserRatingsRated(Integer idUser) throws UserNotFound {
        if (userIndex.containsKey(idUser)) {
            Map<Integer, RatingType> ret = userIndex.get(idUser);
            return Collections.unmodifiableMap(ret);
        } else {
            return Collections.EMPTY_MAP;
        }
    }

    @Override
    public Map<Integer, RatingType> getItemRatingsRated(Integer idItem) throws ItemNotFound {
        if (itemIndex.containsKey(idItem)) {
            Map<Integer, RatingType> ret = itemIndex.get(idItem);
            return Collections.unmodifiableMap(ret);
        } else {
            return Collections.EMPTY_MAP;
        }
    }

    @Override
    public Domain getRatingsDomain() {
        return new DecimalDomain(1, 5);
    }

    @Override
    public boolean isRatedItem(int idItem) throws ItemNotFound {
        if (itemIndex.containsKey(idItem)) {
            return itemIndex.get(idItem).isEmpty();
        } else {
            return false;
        }
    }

    @Override
    public boolean isRatedUser(int idUser) throws UserNotFound {
        if (userIndex.containsKey(idUser)) {
            return userIndex.get(idUser).isEmpty();
        } else {
            return false;
        }
    }

    @Override
    public int getNumRatings() {
        return numRatings;
    }

}