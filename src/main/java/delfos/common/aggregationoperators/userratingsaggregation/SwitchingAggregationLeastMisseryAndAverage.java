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
package delfos.common.aggregationoperators.userratingsaggregation;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import delfos.dataset.basic.rating.Rating;
import delfos.dataset.basic.rating.RatingsDataset;
import delfos.common.aggregationoperators.AggregationOperator;
import delfos.common.aggregationoperators.Mean;
import delfos.common.aggregationoperators.MinimumValue;
import delfos.common.exceptions.dataset.items.ItemNotFound;
import delfos.common.exceptions.dataset.users.UserNotFound;
import delfos.common.parameters.Parameter;
import delfos.common.parameters.ParameterOwnerAdapter;
import delfos.common.parameters.ParameterOwnerType;
import delfos.common.parameters.restriction.DoubleParameter;

/**
 * Agrega las valoraciones de un grupo de usuarios sobre un producto indicado.
 * Esta técnica utiliza una técnica u otra dependiendo de la diferencia entre el
 * rating máximo y mínimo sobre el producto sobre el que se agrega, utilizando
 * leasy missery cuando la diferencia es mayor que el umbral establecido y
 * average cuando es menor.
 *
 * <p>
 * <p>
 * Xun Hu, Xiangwu Meng, Licai Wang: SVD-based group recommendation approaches:
 * an experimental study of Moviepilot. CAMRa '11 Proceedings of the 2nd
 * Challenge on Context-Aware Movie Recommendation Pages 23-28 ACM New York, NY,
 * USA ©2011
 *
 * @author jcastro-inf ( https://github.com/jcastro-inf )
 * @version 1.0 05-Julio-2013
 */
public class SwitchingAggregationLeastMisseryAndAverage extends ParameterOwnerAdapter implements UserRatingsAggregation {

    private final static long serialVersionUID = 1L;
    public static final Parameter threshold = new Parameter(
            "threshold",
            new DoubleParameter(0, Double.MAX_VALUE, 2.0f));
    private final AggregationOperator leastMissery = new MinimumValue();
    private final AggregationOperator average = new Mean();

    @Override
    public Number aggregateRatings(RatingsDataset<? extends Rating> rd, Collection<Integer> users, int idItem)
            throws UserNotFound, ItemNotFound {

        List<Number> values = new ArrayList<Number>(users.size());

        Double max = null;
        Double min = null;

        for (int idUser : users) {
            Double rating = rd.getRating(idUser, idItem).getRatingValue().doubleValue();

            if (max == null) {
                max = rating.doubleValue();
            }
            if (min == null) {
                min = rating;
            }

            if (rating < min) {
                min = rating;
            }

            if (rating > max) {
                max = rating;
            }
            values.add(rating);
        }

        if (values.isEmpty()) {
            throw new IllegalArgumentException("The users do not have ratings over item " + idItem);
        }

        double thresholdValue = getThreshold();
        if (max - min >= thresholdValue) {
            return leastMissery.aggregateValues(values);
        } else {
            return average.aggregateValues(values);
        }
    }

    private double getThreshold() {
        return (Double) getParameterValue(threshold);
    }

    @Override
    public ParameterOwnerType getParameterOwnerType() {
        throw new UnsupportedOperationException("Not supported yet.");
    }
}
