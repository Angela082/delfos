/*
 * Copyright (C) 2016 jcastro
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package delfos.rs.collaborativefiltering.als;

import delfos.dataset.basic.item.Item;
import delfos.dataset.basic.loader.types.DatasetLoader;
import delfos.dataset.basic.rating.Rating;
import delfos.dataset.basic.user.User;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 *
 * @author jcastro
 */
public class Bias {

    private final double generalBias;
    private final Map<User, Double> usersBias;
    private final Map<Item, Double> itemsBias;

    public Function<Rating, Double> ELIMINATE_BIAS;

    public Bias(double generalBias,
            Map<User, Double> usersBias,
            Map<Item, Double> itemsBias) {

        this.generalBias = generalBias;
        this.usersBias = usersBias.entrySet().parallelStream().collect(Collectors.toMap(entry -> entry.getKey(), entry -> entry.getValue()));
        this.itemsBias = itemsBias.entrySet().parallelStream().collect(Collectors.toMap(entry -> entry.getKey(), entry -> entry.getValue()));

        this.ELIMINATE_BIAS = (rating
                -> rating.getRatingValue().doubleValue() - this.generalBias - this.usersBias.get(rating.getUser()) - this.itemsBias.get(rating.getItem()));
    }

    public Function<Rating, Rating> getBiasApplier() {
        return (rating
                -> {
            double userBias = usersBias.get(rating.getUser());
            double itemBias = itemsBias.get(rating.getItem());
            double unbiasedValue = rating.getRatingValue().doubleValue()
                    - this.generalBias
                    - userBias
                    - itemBias;

            return rating.copyWithRatingValue(unbiasedValue);
        });
    }

    public Function<Rating, Rating> getBiasRestorer() {
        return (rating
                -> {
            double userBias = usersBias.get(rating.getUser());
            double itemBias = itemsBias.get(rating.getItem());
            double unbiasedValue = rating.getRatingValue().doubleValue()
                    + this.generalBias
                    + userBias
                    + itemBias;

            return rating.copyWithRatingValue(unbiasedValue);
        });
    }

    public Bias(DatasetLoader<? extends Rating> datasetLoader) {

        generalBias = datasetLoader.getRatingsDataset().getMeanRating();

        usersBias = datasetLoader.getUsersDataset().parallelStream().collect(Collectors.toMap(user -> user, user -> {
            double userBias = datasetLoader.getRatingsDataset()
                    .getUserRatingsRated(user.getId()).values()
                    .parallelStream()
                    .mapToDouble(rating -> rating.getRatingValue().doubleValue() - generalBias)
                    .average()
                    .orElse(0);
            return userBias;
        }));

        itemsBias = datasetLoader.getContentDataset().parallelStream().collect(Collectors.toMap(item -> item, item -> {
            double itemBias = datasetLoader.getRatingsDataset()
                    .getItemRatingsRated(item.getId()).values()
                    .parallelStream()
                    .mapToDouble(rating -> rating.getRatingValue().doubleValue() - generalBias - usersBias.get(rating.getUser()))
                    .average()
                    .orElse(0);
            return itemBias;
        }));

        this.ELIMINATE_BIAS = (rating
                -> rating.getRatingValue().doubleValue() - this.generalBias - this.usersBias.get(rating.getUser()) - this.itemsBias.get(rating.getItem()));
    }

    public double restoreBias(User user, Item item, double value) {
        double userBias = usersBias.get(user);
        double itemBias = itemsBias.get(item);

        double originalRating = value + generalBias + userBias + itemBias;

        return originalRating;
    }

    public double restoreBias(int idUser, int idItem, double value) {

        User user = usersBias.keySet().parallelStream().filter(userInner -> userInner.getId() == idUser).findFirst().orElse(null);
        Item item = itemsBias.keySet().parallelStream().filter(itemInner -> itemInner.getId() == idItem).findFirst().orElse(null);

        return restoreBias(user, item, value);
    }

}
