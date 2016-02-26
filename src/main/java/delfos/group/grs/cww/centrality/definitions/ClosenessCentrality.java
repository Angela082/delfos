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
package delfos.group.grs.cww.centrality.definitions;

import delfos.group.grs.cww.centrality.CentralityConceptDefinition;
import delfos.rs.trustbased.WeightedGraphAdapter;

/**
 *
 * @author jcastro-inf ( https://github.com/jcastro-inf )
 *
 * @version 04-mar-2014
 */
public class ClosenessCentrality extends CentralityConceptDefinition<Integer> {

    public ClosenessCentrality() {
    }

    @Override
    public double centrality(WeightedGraphAdapter<Integer> weightedGraph, Integer node) {

        double centrality, numerator = 0;
        int n = 0;
        for (Integer otherNode : weightedGraph.allNodes()) {
            if (node == otherNode) {
                // No se tiene en cuenta la confianza consigo mismo.
            } else {
                double distance = weightedGraph.distance(node, otherNode);

                if (Double.isInfinite(distance)) {

                } else {
                    if (distance == 0) {

                    } else {
                        numerator += 1 / distance;
                    }
                }

                n++;
            }
        }
        centrality = numerator / n;
        return centrality;
    }
}
