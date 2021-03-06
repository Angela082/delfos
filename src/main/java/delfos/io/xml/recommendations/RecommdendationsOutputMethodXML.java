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
package delfos.io.xml.recommendations;

import org.jdom2.Element;
import delfos.ERROR_CODES;
import delfos.io.xml.parameterowner.parameter.ParameterXML;
import delfos.io.xml.parameterowner.ParameterOwnerXML;
import delfos.rs.output.RecommendationsOutputMethod;
import delfos.common.parameters.Parameter;
import delfos.common.parameters.ParameterOwner;
import delfos.common.parameters.ParameterOwnerType;

/**
 * Clase para almacenar y recuperar métodos de salida de las recomendaciones.
 *
 * @author jcastro-inf ( https://github.com/jcastro-inf )
 *
 * @version 28-oct-2013
 */
public class RecommdendationsOutputMethodXML {

    public static final String RECOMMENDATIONS_OUTPUT_METHOD_ELEMENT_NAME = "RecommendationOutput";

    /**
     * Devuelve el elemento que describe totalmente el método de salida de
     * recomendaciones, almacenando también los parámetros que posee y el valor
     * para cada uno.
     *
     * @param recommendationsOutputMethod Método de salida de recomendaciones a
     * almacenar.
     * @return Objeto XML que lo describe
     */
    public static Element getElement(RecommendationsOutputMethod recommendationsOutputMethod) {
        Element recommendationsOutputMethodElement = new Element(RECOMMENDATIONS_OUTPUT_METHOD_ELEMENT_NAME);

        recommendationsOutputMethodElement.setAttribute(
                ParameterOwnerXML.PARAMETER_OWNER_ATTRIBUTE_NAME,
                recommendationsOutputMethod.getName());

        recommendationsOutputMethodElement.setAttribute(
                ParameterOwnerXML.PARAMETER_OWNER_ATTRIBUTE_TYPE,
                ParameterOwnerType.RECOMMENDATIONS_OUTPUT_METHOD.name());

        for (Parameter p : recommendationsOutputMethod.getParameters()) {
            Element parameter = ParameterXML.getElement(recommendationsOutputMethod, p);
            recommendationsOutputMethodElement.addContent(parameter);
        }
        return recommendationsOutputMethodElement;
    }

    /**
     * Construye el objeto en memoria que representa el método de salida de
     * recomendaciones descrito en el elemento que se pasa por parámetro.
     *
     * @param recommdendationsOutputMethodElement Objeto XML que contienen la
     * descripción.
     * @return Método de salida de recomendaciones obtenido a partir del
     * elemento XML.
     */
    public static RecommendationsOutputMethod getRecommdendationsOutputMethod(Element recommdendationsOutputMethodElement) {
        String name = recommdendationsOutputMethodElement.getAttributeValue(ParameterOwnerXML.PARAMETER_OWNER_ATTRIBUTE_NAME);
        String parameterOwnerType = recommdendationsOutputMethodElement.getAttributeValue(ParameterOwnerXML.PARAMETER_OWNER_ATTRIBUTE_TYPE);
        if (parameterOwnerType == null) {
            recommdendationsOutputMethodElement.setAttribute(ParameterOwnerXML.PARAMETER_OWNER_ATTRIBUTE_TYPE, ParameterOwnerType.RECOMMENDATIONS_OUTPUT_METHOD.name());
        }
        ParameterOwner parameterOwner = ParameterOwnerXML.getParameterOwner(recommdendationsOutputMethodElement);
        if (parameterOwner instanceof RecommendationsOutputMethod) {
            RecommendationsOutputMethod recommendationsOutputMethod = (RecommendationsOutputMethod) parameterOwner;
            return recommendationsOutputMethod;
        } else {
            IllegalStateException ex = new IllegalStateException("The XML does not have the expected structure: The loaded parameter owner is not a recommendation output method [" + parameterOwner + "]");
            ERROR_CODES.CANNOT_LOAD_CONFIG_FILE.exit(ex);
            throw ex;
        }
    }
}
