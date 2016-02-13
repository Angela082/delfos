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
import delfos.common.parameters.ParameterListener;
import delfos.common.parameters.restriction.FloatParameter;

/**
 * Operador de agregación que ensure some degree of fairness for the
 * aggregation.
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
            new FloatParameter(-Float.MAX_VALUE, Float.MAX_VALUE, 1),
            "Minimum value, to perform the normalisation.");
    /**
     * Maximum value, to perform the normalisation.
     */
    public static final Parameter MAX_VALUE = new Parameter(
            "MAX_VALUE",
            new FloatParameter(-Float.MAX_VALUE, Float.MAX_VALUE, 5),
            "Maximum value, to perform the normalisation.");

    public MultiplicativeAggregation() {
        super();
        addParameter(MIN_VALUE);
        addParameter(MAX_VALUE);

        addParammeterListener(new ParameterListener() {
            @Override
            public void parameterChanged() {
                if (getMinValue() >= getMaxValue()) {
                    ERROR_CODES.PARAMETER_VIOLATION.exit(new ParammeterIncompatibleValues(MultiplicativeAggregation.this, MIN_VALUE, MAX_VALUE));
                }
            }
        });

    }

    @Override
    public float aggregateValues(Iterable<Number> values) {
        if (values == null) {
        }

        float retValue = 1;
        int n = 0;

        for (Number value : values) {

            if (value.floatValue() > getMaxValue()) {
                throw new IllegalArgumentException("El valor " + value + " excede el máximo.");
            }

            if (value.floatValue() < getMinValue()) {
                throw new IllegalArgumentException("El valor " + value + " excede el mínimo.");
            }

            float normalisedValue = (value.floatValue() - getMinValue()) / (getMaxValue() - getMinValue());

            retValue *= normalisedValue;
            n++;
        }

        if (n == 0) {
            throw new IllegalArgumentException("The collection of values for aggregate is empty.");
        }

        //deshago la normalización
        retValue = retValue * (getMaxValue() + getMinValue()) + getMinValue();

        return retValue;
    }

    private float getMinValue() {
        return (Float) getParameterValue(MIN_VALUE);
    }

    private float getMaxValue() {
        return (Float) getParameterValue(MAX_VALUE);
    }
}
