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

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

/**
 *
 * @author jcastro-inf ( https://github.com/jcastro-inf )
 *
 * @version 04-mar-2014
 * @param <Node>
 */
public class PathBetweenNodes<Node> implements Comparable<PathBetweenNodes> {

    public static <Node> Comparator<PathBetweenNodes<Node>> CLOSEST_PATH(Class<Node> classOfNode) {
        return (path1, path2) -> {
            double diff = path1.getLength() - path2.getLength();
            return diff < 0 ? 1 : -1;
        };
    }

    public static <Node> Comparator<PathBetweenNodes<Node>> FURTHEST_PATH(Class<Node> classOfNode) {
        return (path1, path2) -> {
            double diff = path1.getLength() - path2.getLength();
            return diff > 0 ? 1 : -1;
        };
    }

    public static Comparator<PathBetweenNodes<?>> CLOSEST_PATH() {
        return (path1, path2) -> {
            double diff = path1.getLength() - path2.getLength();
            return diff < 0 ? 1 : -1;
        };
    }

    public static Comparator<PathBetweenNodes<?>> FURTHEST_PATH() {
        return (path1, path2) -> {
            double diff = path1.getLength() - path2.getLength();
            return diff > 0 ? 1 : -1;
        };
    }

    private final List<Node> _nodes;
    private final List<Double> _weights;
    private final double length;

    public double getLength() {
        return length;
    }

    public List<Node> getNodes() {
        return Collections.unmodifiableList(_nodes);
    }

    public static <Node> PathBetweenNodes<Node> buildEdge(Node from, Node to, double length) {

        if (length < 1 && !from.equals(to)) {
            throw new IllegalStateException("arg");
        }

        if (Double.isNaN(length)) {
            throw new IllegalStateException("arg");
        }
        if (length == 0) {
            throw new IllegalStateException("arg");
        }
        return new PathBetweenNodes<>(Arrays.asList(from, to), Arrays.asList(1 / length), length);
    }

    private PathBetweenNodes(List<Node> _nodes, List<Double> _weights, double length) {
        this._nodes = _nodes;
        this._weights = _weights;
        this.length = length;
    }

    public PathBetweenNodes(WeightedGraph<Node> graph, List<Node> nodes) {

        nodes = removeRepetitions(nodes);

        convertEdgeToPath(nodes);

        List<Double> weights = new ArrayList<>();

        double _length = 0;
        for (int i = 1; i < nodes.size(); i++) {
            Node previousNode = nodes.get(i - 1);
            Node thisNode = nodes.get(i);
            if (previousNode == thisNode) {
                weights.add(1.0);
                _length += 0.0;
            } else {
                double distance = graph.distance(previousNode, thisNode);
                _length += distance;
                weights.add(graph.connectionWeight(previousNode, thisNode).get());
            }
        }

        _nodes = Collections.unmodifiableList(new ArrayList<Node>(nodes));
        _weights = Collections.unmodifiableList(weights);
        this.length = _length;
    }

    private void convertEdgeToPath(List<Node> nodes) {
        if (nodes.size() == 1) {
            nodes.add(nodes.get(0));
        }
    }

    private List<Node> removeRepetitions(List<Node> nodes) {
        nodes = new ArrayList<>(nodes);
        for (int i = 1; i < nodes.size(); i++) {
            Node previous = nodes.get(i - 1);
            Node thisNode = nodes.get(i);

            if (previous.equals(thisNode)) {
                nodes.remove(i - 1);
            }
        }
        return nodes;
    }

    @Override
    public int compareTo(PathBetweenNodes o) {
        if (this.length == o.length) {
            return 0;
        } else if (this.length > o.length) {
            return 1;
        } else {
            return -1;
        }
    }

    public Node from() {
        return getFirst();
    }

    public Node to() {
        return getLast();
    }

    public Node getFirst() {
        return _nodes.get(0);
    }

    public Node getLast() {
        return _nodes.get(_nodes.size() - 1);
    }

    public boolean isPathBetween(Node n1, Node n2) {
        return getFirst().equals(n1) && getLast().equals(n2);
    }

    public int numJumps() {
        return _nodes.size();
    }

    public int numEdges() {
        return _nodes.size() - 1;
    }

    @Override
    public String toString() {
        return _nodes.toString() + " --> " + WeightedGraph.DECIMAL_FORMAT.format(length);
    }

    public double getFirstWeight() {
        return _weights.get(0);
    }

    public boolean isSelf() {
        return this.from().equals(this.to());
    }

    public boolean isEdge() {
        return this.numEdges() == 1;
    }
}
