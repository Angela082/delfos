package delfos.common.aggregationoperators.penalty.functions;

import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import delfos.common.aggregationoperators.AggregationOperator;
import delfos.common.aggregationoperators.Mean;
import delfos.common.aggregationoperators.Median;
import delfos.common.aggregationoperators.RMSMean;
import delfos.common.parameters.ParameterOwnerAdapter;
import delfos.common.parameters.ParameterOwnerType;

/**
 *
 * @version 02-jul-2014
* @author Jorge Castro Gallardo
 */
public abstract class PenaltyFunction extends ParameterOwnerAdapter {

    protected static List<AggregationOperator> allAggregationOperators;

    {
        LinkedList<AggregationOperator> aggregationOperators = new LinkedList<>();
        aggregationOperators.add(new Mean());
        aggregationOperators.add(new Median());
        aggregationOperators.add(new RMSMean());
        allAggregationOperators = Collections.unmodifiableList(aggregationOperators);
    }

    public abstract double penalty(Map<Integer, Map<Integer, Number>> referenceValues_byMember, Map<Integer, Number> aggregated_byItem);

    public abstract double penaltyThisItem(Number aggregatedValue, Iterable<Number> referenceValues);

    @Override
    public ParameterOwnerType getParameterOwnerType() {
        return ParameterOwnerType.PENALTY_FUNCION;
    }

    public List<AggregationOperator> getAllowedAggregations() {
        return allAggregationOperators;
    }
}