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
package delfos.common.parameters.restriction;

import delfos.common.parameters.Parameter;
import delfos.common.parameters.ParameterOwner;
import delfos.factories.Factory;
import delfos.io.xml.parameterowner.parameter.ParameterOwnerParameterXML;
import org.jdom2.Element;

/**
 * Encapsula el comportamiento de una restricción de valores de parámetro que
 * sólo permite seleccionar sistemas de recomendación que concuerden con los
 * tipos pasados por parámetro en el constructor. De esta manera, se pueden
 * asignar sistemas de recomendación del mismo tipo o que hereden del mismo.
 *
 * @author jcastro-inf ( https://github.com/jcastro-inf )
 * @param <ParameterOwnerType>
 */
public class ParameterOwnerRestriction<ParameterOwnerType extends ParameterOwner> extends ParameterRestriction {

    private static final long serialVersionUID = 1L;

    private final Factory<ParameterOwnerType> factory;

    /**
     * Constructor de una restricción de valores del parámetro para que sólo
     * permita objetos de tipo {@link ParameterOwner}. Si se tiene alguna
     * restricción más concreta sobre el tipo que deben implementar los valores,
     * se debe especificar mediante el parámetro <code>tiposPermitidos</code>.
     *
     * @param factory
     * @param defaultParameterOwner valor por defecto que se asigna al
     * parámetro. Debe ser de alguno de los tipos indicados en el parámetro.
     * <code>tiposPermitidos</code>.
     */
    public ParameterOwnerRestriction(
            Factory<ParameterOwnerType> factory,
            ParameterOwnerType defaultParameterOwner) {
        super(defaultParameterOwner);

        if (!isCorrect(defaultParameterOwner)) {
            throw new IllegalArgumentException("The default value isn't correct");
        }
        this.factory = factory;
    }

    /**
     * {@inheritDoc }
     *
     * Comprueba si el objeto indicado por parámetro es de alguno de los tipos
     * permitidos. Esto lo hace usando reflectividad, comprobando si las clases
     * que se indicaron como permitidas pueden almacenar el objeto, es decir,
     * comprueba si alguno de los tipos permitidos es compatible con el objeto.
     *
     * @param newValue Nuevo valor para este parámetro.
     * @return Devuelve true si el nuevo valor es compatible con alguno de los
     * tipos permitidos.
     */
    @Override
    public final boolean isCorrect(Object newValue) {
        return factory.containsObject(newValue);
    }

    @Override
    public Object parseString(String parameterValue) {
        ParameterOwner parameterOwnerParsedValue = factory.getClassByName(parameterValue);
        return parameterOwnerParsedValue;
    }

    @Override
    public Object getValue(ParameterOwner parameterOwner, Element elementParameter) {
        return ParameterOwnerParameterXML.getParameterOwnerParameterValue(parameterOwner, elementParameter);
    }

    @Override
    public Element getXMLElement(ParameterOwner parameterOwner, Parameter parameter) {
        return ParameterOwnerParameterXML.getParameterOwnerElement(parameterOwner, parameter);
    }

    public Factory<ParameterOwnerType> getFactory() {
        return factory;
    }

}
