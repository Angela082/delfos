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
package delfos.rs.trustbased;

import delfos.common.Global;
import delfos.dataset.util.DatasetPrinter;
import java.util.Map;
import java.util.TreeMap;

/**
 * Normaliza un grafo ponderado dado. Utiliza la normalización
 *
 * @author jcastro-inf ( https://github.com/jcastro-inf )
 */
public class WeightedGraphNormaliser {

    public static final <Node> WeightedGraph<Node> normalise(WeightedGraph<Node> source) {

        if (Global.isVerboseAnnoying()) {
            String printWeightedGraph = DatasetPrinter.printWeightedGraph(source);
            Global.showInfoMessage(printWeightedGraph);
        }

        double min = Double.POSITIVE_INFINITY;
        double max = Double.NEGATIVE_INFINITY;

        for (Node nodeSource : source.allNodes()) {
            for (Node nodeDestiny : source.allNodes()) {
                if (nodeSource.equals(nodeDestiny)) {
                    //Skip same node connections
                    continue;
                }
                double connectionValue = source.connectionWeight(nodeSource, nodeDestiny).orElse(0.0);
                min = Math.min(min, connectionValue);
                max = Math.max(max, connectionValue);
            }
        }

        Global.showInfoMessage("min = " + min);
        Global.showInfoMessage("max = " + max);

        if (Global.isVerboseAnnoying()) {
            Global.showInfoMessage("Original range       [" + min + "," + max + "]\n");
            Global.showInfoMessage("Normalising based on [0.0," + max + "]\n");
        }
        min = 0;
        if (max == 1 && min == 0) {
            Global.showWarning("Weighted graph normalisation isn't needed (Values were alrealdy normalised).");
        }

        Map<Node, Map<Node, Number>> connections = new TreeMap<>();

        for (Node nodeSource : source.allNodes()) {
            TreeMap<Node, Number> thisNodeConnections = new TreeMap<>();
            for (Node nodeDestiny : source.allNodes()) {
                if (nodeSource.equals(nodeDestiny)) {
                    //Skip same node connections by setting to 1.
                    thisNodeConnections.put(nodeDestiny, 1);
                } else {
                    //Do the normalisation.
                    final double originalConnection = source.connectionWeight(nodeSource, nodeDestiny).orElse(0.0);
                    final double normalisedConnection = (originalConnection - min) / (max - min);
                    thisNodeConnections.put(nodeDestiny, normalisedConnection);
                }
            }
            connections.put(nodeSource, thisNodeConnections);
        }

        return new WeightedGraph<>(connections);
    }
}
