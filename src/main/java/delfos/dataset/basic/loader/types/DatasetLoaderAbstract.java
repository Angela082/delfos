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
package delfos.dataset.basic.loader.types;

import delfos.common.exceptions.dataset.CannotLoadContentDataset;
import delfos.common.exceptions.dataset.CannotLoadUsersDataset;
import delfos.common.parameters.ParameterOwnerAdapter;
import delfos.common.parameters.ParameterOwnerType;
import delfos.dataset.basic.item.ContentDataset;
import delfos.dataset.basic.item.ContentDatasetDefault;
import delfos.dataset.basic.item.Item;
import delfos.dataset.basic.rating.Rating;
import delfos.dataset.basic.rating.RatingsDataset;
import delfos.dataset.basic.rating.RelevanceCriteria;
import delfos.dataset.basic.user.User;
import delfos.dataset.basic.user.UsersDataset;
import delfos.dataset.basic.user.UsersDatasetAdapter;
import java.util.stream.Collectors;
import org.apache.commons.lang3.builder.HashCodeBuilder;

/**
 * Establece las operaciones que un <code>DatasetLoader</code> debe implementar.
 * Un <code>DatasetLoader</code> se encarga de cargar un dataset de
 * recomendación para su posterior uso con un sistema de recomendación.
 *
 * @author jcastro-inf ( https://github.com/jcastro-inf )
 * @version 1.0 Unknown date.
 * @version 1.0.1 26-Mayo-2013
 * @version 1.0.2 15-Noviembre-2013
 * @param <RatingType>
 */
public abstract class DatasetLoaderAbstract<RatingType extends Rating> extends ParameterOwnerAdapter implements Comparable<Object>, DatasetLoader<RatingType> {

    public static final void loadFullDataset(DatasetLoader<? extends Rating> datasetLoader) {
        datasetLoader.getRatingsDataset();

        if (datasetLoader instanceof ContentDatasetLoader) {
            ContentDatasetLoader contentDatasetLoader = (ContentDatasetLoader) datasetLoader;
            contentDatasetLoader.getContentDataset();

        }

        if (datasetLoader instanceof UsersDatasetLoader) {
            UsersDatasetLoader usersDatasetLoader = (UsersDatasetLoader) datasetLoader;
            usersDatasetLoader.getUsersDataset();
        }

        if (datasetLoader instanceof TrustDatasetLoader) {
            TrustDatasetLoader trustDatasetLoader = (TrustDatasetLoader) datasetLoader;
            trustDatasetLoader.getTrustDataset();
        }

    }

    @Override
    public int compareTo(Object o) {
        if (o instanceof DatasetLoader) {
            DatasetLoader parameterOwner = (DatasetLoader) o;
            return ParameterOwnerAdapter.compare(this, parameterOwner);
        }

        throw new IllegalArgumentException("The type is not valid, must be a " + DatasetLoader.class);
    }

    @Override
    public final ParameterOwnerType getParameterOwnerType() {
        return ParameterOwnerType.DATASET_LOADER;
    }

    @Override
    public RelevanceCriteria getDefaultRelevanceCriteria() {
        return new RelevanceCriteria(4);
    }

    @Override
    public UsersDataset getUsersDataset() throws CannotLoadUsersDataset {

        RatingsDataset<RatingType> ratingsDataset = getRatingsDataset();

        return new UsersDatasetAdapter(ratingsDataset
                .allUsers().stream()
                .map(idUser -> new User(idUser))
                .collect(Collectors.toSet()));

    }

    @Override
    public ContentDataset getContentDataset() throws CannotLoadContentDataset {

        RatingsDataset<RatingType> ratingsDataset = getRatingsDataset();

        return new ContentDatasetDefault(ratingsDataset
                .allRatedItems().stream()
                .map(idItem -> new Item(idItem))
                .collect(Collectors.toSet()));

    }

    @Override
    public int hashCode() {
        HashCodeBuilder hashCodeBuilder = new HashCodeBuilder(37, 11);

        hashCodeBuilder.append(getRatingsDataset().hashCode());
        hashCodeBuilder.append(getContentDataset().hashCode());
        hashCodeBuilder.append(getUsersDataset().hashCode());

        return hashCodeBuilder.hashCode();
    }

    @Override
    public String toString() {
        return getAlias();
    }

}
