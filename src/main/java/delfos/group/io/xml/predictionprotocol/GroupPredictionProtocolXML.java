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
package delfos.group.io.xml.predictionprotocol;

import org.jdom2.Element;
import delfos.io.xml.parameterowner.ParameterOwnerXML;
import delfos.common.parameters.ParameterOwner;
import delfos.group.experiment.validation.predictionvalidation.GroupPredictionProtocol;

/**
 * Clase para realizar el almacenamiento/recuperación de protocolos de
 * predicción para grupos.
 *
 * @author jcastro-inf ( https://github.com/jcastro-inf )
 *
 * @version 9-Enero-2014
 */
public class GroupPredictionProtocolXML {

    public static final String ELEMENT_NAME = GroupPredictionProtocol.class.getSimpleName();

    public static Element getElement(GroupPredictionProtocol validationTechnique) {
        Element element = ParameterOwnerXML.getElement(validationTechnique);
        element.setName(ELEMENT_NAME);
        return element;
    }

    public static GroupPredictionProtocol getGroupPredictionProtocol(Element validationTechniqueElement) {
        ParameterOwner parameterOwner = ParameterOwnerXML.getParameterOwner(validationTechniqueElement);

        if (parameterOwner instanceof GroupPredictionProtocol) {
            return (GroupPredictionProtocol) parameterOwner;
        } else {
            throw new IllegalArgumentException("The object readed is not a validation technique.");
        }
    }

}
