package delfos.common.parameters.restriction;

import org.jdom2.Element;
import delfos.io.xml.parameterowner.parameter.FloatParameterXML;
import delfos.common.parameters.Parameter;
import delfos.common.parameters.ParameterOwner;

/**
 * Restricción de valores de un parámetro que permite que tome valores reales
 * entre un valor mínimo y un valor máximo.
 *
* @author Jorge Castro Gallardo
 * @version 1.0 (Unknow date)
 * @version 1.1 18-Jan-2013
 */
public class FloatParameter extends ParameterRestriction {

    private final static long serialVersionUID = 1L;
    /**
     * Valor mínimo del parámetro que tiene esta restricción.
     */
    private final float minValue;
    /**
     * Valor máximo del parámetro que tiene esta restricción.
     */
    private final float maxValue;

    /**
     * Crea la restricción para aceptar valores entre cero y uno, con valor por
     * defecto uno.
     */
    public FloatParameter() {
        this(0, 1, 1);
    }

    /**
     * Crea la restricción con los valores mínimo y máximo indicados. También se
     * especifica el valor por defecto.
     *
     * @param minValue Valor minimo.
     * @param maxValue Valor máximo.
     * @param defaultValue Valor por defecto.
     */
    public FloatParameter(float minValue, float maxValue, float defaultValue) {
        super(defaultValue);

        this.minValue = minValue;
        this.maxValue = maxValue;

        if (!isCorrect(defaultValue)) {
            throw new UnsupportedOperationException("Invalid default value");
        }
    }

    @Override
    public final boolean isCorrect(Object o) {
        if (o instanceof Number) {
            Number d = (Number) o;
            if (d.doubleValue() >= minValue && d.doubleValue() <= maxValue) {
                return true;
            } else {
                return false;
            }
        } else {
            return false;
        }
    }

    /**
     * Devuelve el valor mínimo para el parámetro que tiene esta restricción.
     *
     * @return Valor mínimo.
     */
    public float getMin() {
        return minValue;
    }

    /**
     * Devuelve el valor máximo para el parámetro que tiene esta restricción.
     *
     * @return Valor máximo.
     */
    public float getMax() {
        return maxValue;
    }

    @Override
    public Object parseString(String parameterValue) {
        Float d = Float.parseFloat(parameterValue);
        return d;
    }

    @Override
    public Object getValue(ParameterOwner parameterOwner, Element elementParameter) {
        return FloatParameterXML.getParameterValue(parameterOwner, elementParameter);
    }

    @Override
    public Element getXMLElement(ParameterOwner parameterOwner, Parameter parameter) {
        return FloatParameterXML.getFloatParameterElement(parameterOwner, parameter);
    }
}