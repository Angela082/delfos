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

/**
 * Operador de agregación de la media geométrica de los valores de entrada
 *
 * @author jcastro-inf ( https://github.com/jcastro-inf )
 *
 * @version 16-julio-2014
 */
public class GeometricMean extends AggregationOperator {

    private final static long serialVersionUID = 1L;

    @Override
    public float aggregateValues(Iterable<Number> values) {

        double multiplication = 1;
        int n = 0;

        for (Number value : values) {
            multiplication *= value.doubleValue();
            n++;
        }

        double geometricMean = Math.pow(multiplication, 1.0 / n);

        return (float) geometricMean;
    }
}