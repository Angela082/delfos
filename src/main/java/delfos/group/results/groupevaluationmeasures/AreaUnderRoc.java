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
package delfos.group.results.groupevaluationmeasures;

import delfos.ERROR_CODES;
import delfos.common.Global;
import delfos.common.exceptions.dataset.users.UserNotFound;
import delfos.common.statisticalfuncions.MeanIterative;
import delfos.dataset.basic.loader.types.DatasetLoader;
import delfos.dataset.basic.rating.Rating;
import delfos.dataset.basic.rating.RatingsDataset;
import delfos.dataset.basic.rating.RelevanceCriteria;
import delfos.group.groupsofusers.GroupOfUsers;
import delfos.group.results.groupevaluationmeasures.precisionrecall.PRSpaceGroups;
import delfos.group.results.grouprecomendationresults.GroupRecommenderSystemResult;
import delfos.io.xml.UnrecognizedElementException;
import delfos.io.xml.evaluationmeasures.confusionmatricescurve.ConfusionMatricesCurveXML;
import delfos.io.xml.parameterowner.ParameterOwnerXML;
import delfos.results.evaluationmeasures.confusionmatrix.ConfusionMatricesCurve;
import delfos.results.evaluationmeasures.roccurve.AreaUnderROC;
import delfos.rs.recommendation.Recommendation;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import org.jdom2.Element;

/**
 * Medida de evaluación para sistemas de recomendación a grupos que calcula la
 * curva ROC (Receiver Operator Characteristic) y a partir de ella calcula el
 * area bajo la misma. A mayor área mejor es el sistema, siendo 1 el valor
 * máximo si el sistema de recomendación es perfecto y 0.5 si realiza
 * recomendaciones de manera totalmente aleatoria.
 *
 * <p>
 * Esta medida de evaluación es una generalización del
 *
 * @version 1.0 (10-01-2012)
 * @version 1.1 (11-01-2012)
 *
 * @author jcastro-inf ( https://github.com/jcastro-inf )
 *
 * @see AreaUnderROC
 */
public class AreaUnderRoc extends GroupEvaluationMeasure {

    /**
     * Nombre del elemento que almacena la información de un grupo de usuarios
     */
    public static final String GROUP_OF_USERS_ELEMENT = "GroupOfUsers";
    /**
     * Nombre del atributo que almacena la lista de usuarios que forman el grupo
     */
    public static final String USERS_ATTRIBUTE = "users";

    @Override
    public boolean usesRatingPrediction() {
        return false;
    }

    @Override
    public GroupEvaluationMeasureResult getMeasureResult(
            GroupRecommenderSystemResult groupRecommenderSystemResult,
            DatasetLoader<? extends Rating> originalDatasetLoader,
            RatingsDataset<? extends Rating> testDataset,
            RelevanceCriteria relevanceCriteria,
            DatasetLoader<? extends Rating> trainingDatasetLoader,
            DatasetLoader<? extends Rating> testDatasetLoader) {

        Map<GroupOfUsers, ConfusionMatricesCurve> prCurves = new TreeMap<>();

        int gruposSinMatriz = 0;
        for (GroupOfUsers group : groupRecommenderSystemResult.getGroupsOfUsers()) {
            Collection<Recommendation> groupRecommendations = groupRecommenderSystemResult.getGroupOutput(group).getRecommendations();

            List<Boolean> recommendacionesGrupo = new ArrayList<>(groupRecommendations.size());
            for (Recommendation r : groupRecommendations) {
                int idItem = r.getIdItem();

                MeanIterative mean = new MeanIterative();
                for (int idUser : group.getIdMembers()) {
                    try {
                        Map<Integer, ? extends Rating> userRatings = testDataset.getUserRatingsRated(idUser);
                        if (userRatings.containsKey(idItem)) {
                            mean.addValue(testDataset.getUserRatingsRated(idUser).get(idItem).getRatingValue().doubleValue());
                        }
                    } catch (UserNotFound ex) {
                        ERROR_CODES.USER_NOT_FOUND.exit(ex);
                    }
                }
                recommendacionesGrupo.add(relevanceCriteria.isRelevant(mean.getMean()));
            }

            try {
                prCurves.put(group, new ConfusionMatricesCurve(recommendacionesGrupo));
            } catch (IllegalArgumentException iae) {
                gruposSinMatriz++;
            }
        }

        ConfusionMatricesCurve curva = ConfusionMatricesCurve.mergeCurves(prCurves.values());

        float areaUnderRoc;
        if (curva.size() >= 2) {
            areaUnderRoc = curva.getAreaUnderROC();
        } else {
            areaUnderRoc = Float.NaN;
        }

        if (gruposSinMatriz != 0) {
            Global.showWarning("Grupos sin Matriz en " + PRSpaceGroups.class + " --> " + gruposSinMatriz + " \n");
        }

        Element areaUnderRocElement = ParameterOwnerXML.getElement(this);
        areaUnderRocElement.setAttribute("value", Float.toString(areaUnderRoc));

        areaUnderRocElement.addContent(ConfusionMatricesCurveXML.getElement(curva));

        return new GroupEvaluationMeasureResult(this, areaUnderRoc, areaUnderRocElement);
    }

    @Override
    public GroupEvaluationMeasureResult agregateResults(Collection<GroupEvaluationMeasureResult> results) {
        ArrayList<ConfusionMatricesCurve> curves = new ArrayList<>();

        for (GroupEvaluationMeasureResult r : results) {
            Element e = r.getXMLElement();
            try {
                curves.add(ConfusionMatricesCurveXML.getConfusionMatricesCurve(e.getChild(ConfusionMatricesCurveXML.CURVE_ELEMENT)));
            } catch (UnrecognizedElementException ex) {
                ERROR_CODES.UNRECOGNIZED_XML_ELEMENT.exit(ex);
            }
        }

        ConfusionMatricesCurve mergeCurves = ConfusionMatricesCurve.mergeCurves(curves);

        float areaUnderRoc = mergeCurves.getAreaUnderROC();
        Element areaUnderRocElement = new Element(this.getName());
        areaUnderRocElement.setAttribute("value", Float.toString(areaUnderRoc));

        areaUnderRocElement.addContent(ConfusionMatricesCurveXML.getElement(mergeCurves));

        return new GroupEvaluationMeasureResult(this, areaUnderRoc, areaUnderRocElement);
    }
}