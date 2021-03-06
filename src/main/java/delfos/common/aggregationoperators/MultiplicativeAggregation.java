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
package delfos.common.aggregationoperators;

import delfos.ERROR_CODES;
import delfos.common.exceptions.ParammeterIncompatibleValues;
import delfos.common.parameters.Parameter;
import delfos.common.parameters.restriction.DoubleParameter;
import delfos.dataset.basic.rating.domain.DecimalDomain;
import delfos.dataset.basic.rating.domain.Domain;
import java.util.Collection;

/**
 * Operador de agregación que ensure some degree of fairness for the aggregation.
 *
 * @author jcastro-inf ( https://github.com/jcastro-inf )
 *
 * @version 1.0 Unknown date
 * @version 1.1 (1 de Marzo de 2013)
 */
public class MultiplicativeAggregation extends AggregationOperator {

    private final static long serialVersionUID = 1L;
    /**
     * Minimum value, to perform the normalisation.
     */
    public static final Parameter MIN_VALUE = new Parameter(
            "MIN_VALUE",
            new DoubleParameter(-Double.MAX_VALUE, Double.MAX_VALUE, 1),
            "Minimum value, to perform the normalisation.");
    /**
     * Maximum value, to perform the normalisation.
     */
    public static final Parameter MAX_VALUE = new Parameter(
            "MAX_VALUE",
            new DoubleParameter(-Double.MAX_VALUE, Double.MAX_VALUE, 5),
            "Maximum value, to perform the normalisation.");

    public MultiplicativeAggregation() {
        super();
        addParameter(MIN_VALUE);
        addParameter(MAX_VALUE);

        addParammeterListener(() -> {
            if (getMinValue() >= getMaxValue()) {
                ERROR_CODES.PARAMETER_VIOLATION.exit(new ParammeterIncompatibleValues(MultiplicativeAggregation.this, MIN_VALUE, MAX_VALUE));
            }
        });

    }

    public MultiplicativeAggregation setDomain(Domain domain) {
        setParameterValue(MIN_VALUE, domain.min().doubleValue());
        setParameterValue(MAX_VALUE, domain.max().doubleValue());

        return this;
    }

    @Override
    public double aggregateValues(Collection<? extends Number> values) {
        if (values == null) {
        }

        double retValue = 1;
        int n = 0;

        DecimalDomain originalDomain = new DecimalDomain(getMinValue(), getMaxValue());

        for (Number value : values) {

            if (1 < value.doubleValue() && value.doubleValue() < 1 + 1e04) {
                value = 1;
            }

            if (value.doubleValue() > 1) {
                throw new IllegalArgumentException("El valor " + value + " excede el máximo.");
            }

            if (value.doubleValue() < getMinValue()) {
                throw new IllegalArgumentException("El valor " + value + " excede el mínimo.");
            }

            double normalisedValue = originalDomain.convertToDecimalDomain(value, DecimalDomain.ZERO_TO_ONE);

            retValue *= normalisedValue;
            n++;
        }

        if (n == 0) {
            throw new IllegalArgumentException("The collection of values for aggregate is empty.");
        }

        //deshago la normalización
        retValue = DecimalDomain.ZERO_TO_ONE.convertToDecimalDomain(retValue, originalDomain);

        return retValue;
    }

    private double getMinValue() {
        return (Double) getParameterValue(MIN_VALUE);
    }

    private double getMaxValue() {
        return (Double) getParameterValue(MAX_VALUE);
    }
}
