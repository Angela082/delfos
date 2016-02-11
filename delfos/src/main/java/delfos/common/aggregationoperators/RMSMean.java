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

import java.math.BigDecimal;
import java.math.BigInteger;
import java.math.MathContext;
import java.math.RoundingMode;

/**
 * Operador de agregación de la media geométrica de los valores de entrada
 *
* @author Jorge Castro Gallardo
 *
 * @version 17-julio-2014
 */
public class RMSMean extends AggregationOperator {

    private final static long serialVersionUID = 1L;

    @Override
    public float aggregateValues(Iterable<Number> values) {

        MathContext mathContext = new MathContext(32, RoundingMode.HALF_UP);
        BigDecimal sum = new BigDecimal(BigInteger.ZERO, mathContext);
        int n = 0;

        for (Number value : values) {
            BigDecimal valueB = new BigDecimal(value.doubleValue(), mathContext);
            sum = sum.add(valueB.multiply(valueB, mathContext), mathContext);
            n++;
        }

        BigDecimal meanSquare = sum.multiply(BigDecimal.ONE.divide(new BigDecimal(n), mathContext));

        double rootMeanSquare = Math.sqrt(meanSquare.doubleValue());
        return (float) rootMeanSquare;
    }
}
