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
package delfos.common.aggregationoperators.weighted;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import delfos.common.aggregationoperators.AggregationOperator;

/**
 * Clase abstracta que encapsula el funcionamiento de un método de agregación
 * con valores ponderados.
 *
 * @author jcastro-inf ( https://github.com/jcastro-inf )
 *
 * @version 1.0 Unknown date
 * @version 1.1 (1 de Marzo de 2013)
 */
public abstract class WeightedAggregationOperator extends AggregationOperator {

    /**
     * Agrega los valores especificados en un único valor, aplicando la
     * ponderación indicada.
     *
     * @param values Conjunto de valores a agregar
     * @param weights
     *
     * @return valor agregado de los valores especificados
     *
     * @throws IllegalArgumentException Si no contiene ningún valor para agregar
     * o si es nulo. También si los tamaños de ambas listas son distintos.
     */
    public abstract float aggregateValues(List<? extends Number> values, List<? extends Number> weights);

    /**
     * Agrega los valores especificados en un único valor, aplicando la
     * ponderación indicada.
     *
     * @param values Conjunto de valores a agregar
     * @param weights
     * @return valor agregado de los valores especificados
     *
     * @throws IllegalArgumentException Si no contiene ningún valor para agregar
     * o si es nulo. También si los tamaños de ambas listas son distintos.
     */
    public final float aggregateValues(Number[] values, Number[] weights) {
        return aggregateValues(Arrays.asList(values), Arrays.asList(weights));
    }

    @Override
    public final float aggregateValues(Iterable<Number> values) {

        List<Number> valuesList = new ArrayList<Number>();

        for (Number v : values) {
            valuesList.add(v);
        }
        List<Number> weightsList = new ArrayList<Number>();

        double peso = 1.0 / valuesList.size();
        for (int i = 0; i < valuesList.size(); i++) {
            weightsList.add(peso);
        }

        return aggregateValues(valuesList, weightsList);
    }
}
