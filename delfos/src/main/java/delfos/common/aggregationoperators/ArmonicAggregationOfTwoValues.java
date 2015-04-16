package delfos.common.aggregationoperators;

/**
 * Operador de agregación F1-Score, que agrega los valores indicados según la
 * siguiente fórmula:
 *
 * <p>
 * <p>
 * aggregatedValue = (2*v1*v2)/(v1+v2)
 *
* @author Jorge Castro Gallardo
 *
 * @version 1.0 01-Mar-2013
 */
public class ArmonicAggregationOfTwoValues extends TwoValuesAggregator {

    private final static long serialVersionUID = 1L;

    @Override
    public float aggregateTwoValues(Number v1, Number v2) {
        double d1 = v1.doubleValue();
        double d2 = v2.doubleValue();

        if (d1 < 0) {
            throw new IllegalArgumentException("v1 es menor que cero.");
        }
        if (d1 > 1) {
            throw new IllegalArgumentException("v1 es mayor que uno.");
        }
        if (d2 < 0) {
            throw new IllegalArgumentException("v2 es menor que cero.");
        }
        if (d2 > 1) {
            throw new IllegalArgumentException("v2 es mayor que uno.");
        }
        return (float) ((2 * d1 * d2) / (d1 + d2));
    }
}