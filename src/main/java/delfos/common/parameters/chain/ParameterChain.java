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
package delfos.common.parameters.chain;

import delfos.common.parameters.Parameter;
import delfos.common.parameters.ParameterOwner;
import delfos.experiment.SeedHolder;
import delfos.group.casestudy.defaultcase.GroupCaseStudy;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;
import java.util.function.Supplier;
import java.util.stream.Collectors;

/**
 * Represents a chain of parameters and values of a given parameter owner.
 *
 * @author jcastro-inf ( https://github.com/jcastro-inf )
 *
 */
public class ParameterChain implements Comparable<ParameterChain> {

    /**
     * Returns the parameter chains that are common to at least two groupCaseStudyResults and also have at least two
     * case study with different value for the terminal value.
     *
     * @param <ParameterOwnerType>
     * @param groupCaseStudys
     * @return
     */
    public static <ParameterOwnerType extends ParameterOwner> List<ParameterChain> obtainDifferentChains(List<ParameterOwnerType> groupCaseStudys) {

        if (groupCaseStudys.isEmpty()) {
            return Collections.EMPTY_LIST;
        }

        List<ParameterChain> allParameterChains = obtainAllParameterChains(
                groupCaseStudys.iterator().next());

        for (ParameterOwner groupCaseStudy : groupCaseStudys) {
            List<ParameterChain> thisCaseStudyParameterChains
                    = obtainAllParameterChains(groupCaseStudy);

            List<ParameterChain> notMatchedWithExisting = new ArrayList<>();

            for (ParameterChain parameterChain : thisCaseStudyParameterChains) {

                List<ParameterChain> matchesWith = allParameterChains.stream()
                        .filter(parameterChain2 -> parameterChain.isCompatible(parameterChain2))
                        .collect(Collectors.toList());

                if (matchesWith.isEmpty()) {
                    notMatchedWithExisting.add(parameterChain);
                }
            }

            if (!notMatchedWithExisting.isEmpty()) {
                allParameterChains.addAll(notMatchedWithExisting);
            }
        }

        //Delete chains applicable to only one groupCaseStudy
        List<ParameterChain> chainsApplicableToMoreThanOne = allParameterChains.stream()
                .filter(parameterChain -> {
                    List< ? extends ParameterOwner> applicableTo = groupCaseStudys.stream()
                            .filter(groupCaseStudy -> parameterChain.isApplicableTo(groupCaseStudy))
                            .collect(Collectors.toList());
                    boolean applicableToMoreThanOne = applicableTo.size() > 1;
                    return applicableToMoreThanOne;
                })
                .collect(Collectors.toList());

        //Delete chains with only one value across case studies
        List<ParameterChain> chainsWithMoreThanOneDifferentValue = chainsApplicableToMoreThanOne.stream()
                .filter(parameterChain -> {

                    Supplier<TreeSet<Object>> supplier = () -> new TreeSet<>(ParameterOwner.SAME_CLASS_COMPARATOR_OBJECT);

                    Set<Object> differentValues = groupCaseStudys.stream()
                            .filter(groupCaseStudy -> parameterChain.isApplicableTo(groupCaseStudy))
                            .map(groupCaseStudy -> parameterChain.getValueOn(groupCaseStudy)).collect(Collectors.toCollection(supplier));

                    if (differentValues.isEmpty()) {
                        throw new IllegalStateException("There must be at least one different value.");
                    }

                    boolean moreThanOneDifferentValue = differentValues.size() > 1;

                    return moreThanOneDifferentValue;
                }).collect(Collectors.toList());

        return chainsWithMoreThanOneDifferentValue;
    }

    public boolean isDataValidationParameter() {
        if (nodes.isEmpty()) {
            return !leaf.getParameter().equals(GroupCaseStudy.GROUP_RECOMMENDER_SYSTEM);
        } else {
            return nodes.get(0).getParameter() != GroupCaseStudy.GROUP_RECOMMENDER_SYSTEM;
        }
    }

    public boolean isTechniqueParameter() {
        if (nodes.isEmpty()) {
            return leaf.getParameter().equals(GroupCaseStudy.GROUP_RECOMMENDER_SYSTEM);
        } else {
            return nodes.get(0).getParameter() == GroupCaseStudy.GROUP_RECOMMENDER_SYSTEM;
        }
    }

    public static List<ParameterChain> obtainDataValidationParameterChains(GroupCaseStudy groupCaseStudy) {
        List<ParameterChain> allParameterChains = obtainAllParameterChains(groupCaseStudy);

        List<ParameterChain> dataValidationParameterChains
                = allParameterChains.stream()
                .filter(chain -> chain.isDataValidationParameter())
                .collect(Collectors.toList());

        return dataValidationParameterChains;
    }

    public static List<ParameterChain> obtainTechniqueParameterChains(GroupCaseStudy groupCaseStudy) {
        List<ParameterChain> allParameterChains = obtainAllParameterChains(groupCaseStudy);

        List<ParameterChain> dataValidationParameterChains
                = allParameterChains.stream()
                .filter(chain -> chain.isTechniqueParameter())
                .collect(Collectors.toList());

        return dataValidationParameterChains;
    }

    public static <ParameterOwnerType extends ParameterOwner> List<ParameterChain> obtainAllParameterChains(ParameterOwnerType rootParameterOwner) {

        List<ParameterChain> allParameterChains = new ArrayList<>();

        ParameterChain rootChain = new ParameterChain(rootParameterOwner);

        Collection<Parameter> groupCaseStudyParameters = rootParameterOwner.getParameters();

        for (Parameter parameter : groupCaseStudyParameters) {
            Object parameterValue = rootParameterOwner.getParameterValue(parameter);
            allParameterChains.add(rootChain.createWithLeaf(parameter, parameterValue));

            if (parameterValue instanceof ParameterOwner) {
                ParameterOwner parameterValueParameterOwner = (ParameterOwner) parameterValue;

                List<ParameterChain> chains
                        = obtainAllParameterChains(parameterValueParameterOwner);

                for (ParameterChain chain : chains) {
                    ParameterChain newChain = rootChain.addChain(chain, parameter);
                    allParameterChains.add(newChain);
                }
            }
        }

        return allParameterChains;
    }

    private final Root root;
    private final List<Node> nodes;
    private final Leaf leaf;

    public ParameterChain(ParameterOwner parameterOwner) {
        this.root = new Root(parameterOwner);
        this.nodes = Collections.unmodifiableList(new ArrayList<>());
        this.leaf = null;
    }

    private ParameterChain(Root root, List<Node> nodes, Leaf leaf) {
        this.root = root;
        this.nodes = Collections.unmodifiableList(new ArrayList<>(nodes));
        this.leaf = leaf;
    }

    public ParameterChain createWithNode(Parameter parameter, ParameterOwner parameterOwner) {
        if (leaf != null) {
            throw new IllegalStateException("Cannot add node after adding leaf");
        }

        List<Node> newNodes = new ArrayList<>(nodes);
        newNodes.add(new Node(parameter, parameterOwner));

        newNodes = Collections.unmodifiableList(newNodes);

        return new ParameterChain(root, newNodes, leaf);
    }

    public ParameterChain createWithLeaf(Parameter parameter, Object parameterValue) {

        ParameterOwner lastParameterOwner;

        if (nodes.isEmpty()) {
            lastParameterOwner = root.getParameterOwner();
        } else {
            lastParameterOwner = nodes.get(nodes.size() - 1).getParameterOwner();
        }

        return new ParameterChain(root, nodes, new Leaf(lastParameterOwner, parameter, parameterValue));
    }

    public boolean isCompatible(ParameterChain parameterChain) {

        boolean rootMatch = parameterChain.root.isCompatibleWith(this.root);
        if (!rootMatch) {
            return false;
        }

        int firstChainSize = this.nodes.size();
        boolean nodesSizesMatch = parameterChain.nodes.size() == firstChainSize;
        if (!nodesSizesMatch) {
            return false;
        }

        for (int i = 0; i < firstChainSize; i++) {
            final int thisNodeIndex = i;
            final Node firstChainNode = this.nodes.get(thisNodeIndex);

            boolean nodeIsCompatible = parameterChain.nodes.get(thisNodeIndex).isCompatibleWith(firstChainNode);
            if (!nodeIsCompatible) {
                return false;
            }
        }

        boolean leafMatch = parameterChain.leaf.isCompatibleWith(this.leaf);
        if (!leafMatch) {
            return false;
        }
        return true;
    }

    public static boolean areCompatible(ParameterChain... parameterChains) {
        return areCompatible(Arrays.asList(parameterChains));
    }

    public static boolean areCompatible(List<ParameterChain> parameterChains) {

        return parameterChains.stream()
                .allMatch(parameterChain
                        -> parameterChains.stream()
                        .allMatch(parameterChain2 -> parameterChain.isCompatible(parameterChain2)));

    }

    public static boolean areSame(ParameterChain... parameterChains) {
        return areSame(Arrays.asList(parameterChains));
    }

    public static boolean areSame(List<ParameterChain> parameterChains) {
        if (areCompatible(parameterChains)) {

            ParameterChain firstChain = parameterChains.iterator().next();
            Leaf firstLeaf = parameterChains.iterator().next().leaf;

            List<ParameterChain> parameterChainsThatMatchLeaf = parameterChains.stream()
                    .filter(parameterChain -> parameterChain.leaf.equals(firstLeaf))
                    .collect(Collectors.toList());

            return parameterChains.stream()
                    .allMatch(parameterChain -> parameterChain.leaf.equals(firstLeaf));
        } else {
            return false;
        }
    }

    public List<ParameterChain> createAllTerminalParameterChains(ParameterOwner parameterOwner) {
        List<ParameterChain> parameterChains = new ArrayList<>();

        parameterChains.add(new ParameterChain(parameterOwner));

        return parameterChains;
    }

    /**
     * Adds the specified chain to the current chain nodes. It moves the root element of the parameter chain to this
     * chain nodes as the first node.
     *
     * @param chain
     * @return
     */
    private ParameterChain addChain(ParameterChain chain, Parameter parameter) {

        Root newRoot = this.root;

        List<Node> newNodes = new ArrayList<>(chain.nodes);
        newNodes.add(0, new Node(parameter, chain.root.getParameterOwner()));

        Leaf newLeaf = chain.leaf;

        return new ParameterChain(newRoot, newNodes, newLeaf);
    }

    @Override
    public String toString() {
        StringBuilder str = new StringBuilder();

        str.append(this.root.getParameterOwner().getName()).append(" ==> ");

        if (!nodes.isEmpty()) {
            for (Node node : nodes) {
                str.append(node.getParameter().getName());
                str.append(" = ");

                if (node.getParameterOwner() != null) {
                    str.append(node.getParameterOwner().getName());
                } else {
                    str.append(node.getParameterOwner());
                }

                str.append(" -> ");
            }

            str.delete(str.length() - 4, str.length());
            str.append(" ==> ");
        }

        str.append(leaf.getParameter().getName());
        str.append(" = ");

        if (leaf.getParameterValue() != null) {
            str.append(leaf.getParameterValue().toString());
        } else {
            str.append(leaf.getParameterValue());
        }

        return str.toString();
    }

    protected Leaf getLeaf() {
        return leaf;
    }

    protected List<Node> getNodes() {
        return nodes;
    }

    protected Root getRoot() {
        return root;
    }

    public boolean isAlias() {
        return leaf.getParameter().equals(ParameterOwner.ALIAS);
    }

    public boolean isSeed() {
        return leaf.getParameter().equals(SeedHolder.SEED);
    }

    public boolean isNumExecutions() {
        return leaf.getParameter().equals(GroupCaseStudy.NUM_EXECUTIONS);
    }

    public void validateThatIsApplicableTo(ParameterOwner parameterOwner) {

        if (!root.getParameterOwner().getClass().equals(parameterOwner.getClass())) {
            String message = "Root classes do not match: "
                    + root.getParameterOwner().getClass() + " and "
                    + parameterOwner.getClass() + " "
                    + "[" + this.toString() + "]";
            throw new IllegalArgumentException(message);
        }

        ParameterOwner parameterOwnerToGetValue = parameterOwner;

        int i = 0;

        for (Node node : nodes) {
            if (!parameterOwnerToGetValue.haveParameter(node.getParameter())) {

                String message = "Node[" + i + "] "
                        + "parameter owner '" + parameterOwnerToGetValue + "' does not have the "
                        + "parameter '" + node.getParameter() + "' "
                        + "[" + this.toString() + "]";
                throw new IllegalArgumentException("ParameterOwner is not compatible: " + root.getParameterOwner().getClass() + " != " + parameterOwner.getClass());
            }
            parameterOwnerToGetValue = (ParameterOwner) parameterOwnerToGetValue.getParameterValue(
                    node.getParameter());
            i++;
        }

        if (leaf.getParameterOwner() == null) {
            //Any parameter owner is valid
        } else if (!parameterOwnerToGetValue.getClass().equals(leaf.getParameterOwner().getClass())) {
            String message = "Leaf "
                    + "parameter owner class '" + parameterOwnerToGetValue.getClass()
                    + "' is not the same '"
                    + leaf.getParameterOwner().getClass() + "' "
                    + "[" + this.toString() + "]";
            throw new IllegalArgumentException(message);
        } else {
            //Parameter owners have the same class.
        }

        if (!parameterOwnerToGetValue.haveParameter(leaf.getParameter())) {

            String message = "Leaf "
                    + "parameter owner '" + parameterOwnerToGetValue + "' does not have the "
                    + "parameter '" + leaf.getParameter() + "' "
                    + "[" + this.toString() + "]";
            throw new IllegalArgumentException(message);
        }
    }

    public Object getValueOn(ParameterOwner parameterOwner) {
        validateThatIsApplicableTo(parameterOwner);

        ParameterOwner parameterOwnerToGetValue = parameterOwner;

        for (Node node : nodes) {
            parameterOwnerToGetValue = (ParameterOwner) parameterOwnerToGetValue.getParameterValue(
                    node.getParameter());
        }

        return parameterOwnerToGetValue.getParameterValue(leaf.getParameter());
    }

    public static String printListOfChains(Collection<ParameterChain> allChains) {

        StringBuilder str = new StringBuilder();

        ArrayList<ParameterChain> allParameterChains = new ArrayList<>(allChains);

        allParameterChains.sort((ParameterChain o1, ParameterChain o2)
                -> o1.toString().compareTo(o2.toString()));

        str.append("=====================================================\n");
        str.append("all chains for now\n");
        for (ParameterChain chain : allParameterChains) {
            str.append(chain.toString()).append("\n");
        }
        str.append("=====================================================\n");

        return str.toString();
    }

    <ParameterOwnerType extends ParameterOwner> boolean isApplicableTo(ParameterOwnerType parameterOwner) {
        try {
            validateThatIsApplicableTo(parameterOwner);
            return true;
        } catch (IllegalArgumentException ex) {
            return false;
        }
    }

    @Override
    public int compareTo(ParameterChain o) {
        return this.toString().compareToIgnoreCase(o.toString());
    }

    public String getParameterName() {
        return leaf.getParameter().getName();
    }

    @Override
    public int hashCode() {
        return this.toString().toLowerCase().hashCode();
    }

    @Override
    public boolean equals(Object obj) {
        if (obj instanceof ParameterChain) {
            return this.toString().equalsIgnoreCase(obj.toString());
        } else {
            return false;
        }
    }

}
