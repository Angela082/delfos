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

import delfos.common.Global;
import delfos.dataset.basic.loader.types.DatasetLoader;
import delfos.dataset.basic.rating.Rating;
import delfos.dataset.basic.rating.RatingsDataset;
import delfos.dataset.basic.rating.RelevanceCriteria;
import delfos.group.groupsofusers.GroupOfUsers;
import delfos.group.results.grouprecomendationresults.GroupRecommenderSystemResult;
import delfos.io.xml.parameterowner.ParameterOwnerXML;
import delfos.results.evaluationmeasures.EvaluationMeasure;
import delfos.rs.recommendation.Recommendation;
import java.util.Collection;
import org.jdom2.Element;

/**
 * Medida de evaluación para calcular el número de solicitudes de predicción que
 * se hicieron al sistema.
 *
 * @author jcastro-inf ( https://github.com/jcastro-inf )
 *
 * @version 1.0 14-Mayo-2013
 * @see Coverage_ForGroups
 */
public class NumberOfRequests extends GroupEvaluationMeasure {

    @Override
    public GroupEvaluationMeasureResult getMeasureResult(
            GroupRecommenderSystemResult groupRecommenderSystemResult,
            DatasetLoader<? extends Rating> originalDatasetLoader,
            RatingsDataset<? extends Rating> testDataset,
            RelevanceCriteria relevanceCriteria,
            DatasetLoader<? extends Rating> trainingDatasetLoader,
            DatasetLoader<? extends Rating> testDatasetLoader) {

        Element ret = ParameterOwnerXML.getElement(this);
        long solicitadas = 0;

        for (GroupOfUsers groupOfUsers : groupRecommenderSystemResult.getGroupsOfUsers()) {
            Collection<Recommendation> groupRecommendations = groupRecommenderSystemResult.getGroupOutput(groupOfUsers).getRecommendations();

            Collection<Integer> groupRequests = groupRecommenderSystemResult.getGroupInput(groupOfUsers).getItemsRequested();

            Element groupRequestsElement = new Element("GroupRequests");
            groupRequestsElement.setAttribute("group", groupOfUsers.toString());
            if (groupRequests == null) {
                Global.showWarning("the group " + groupOfUsers + " has no requests (null)");
                groupRequestsElement.addContent("[]");
            } else {
                solicitadas += groupRequests.size();
                groupRequestsElement.addContent(groupRequests.toString());
            }
            ret.addContent(groupRequestsElement);
        }
        ret.setAttribute("value", Long.toString(solicitadas));
        return new GroupEvaluationMeasureResult(this, solicitadas, ret);
    }

    @Override
    public GroupEvaluationMeasureResult agregateResults(Collection<GroupEvaluationMeasureResult> results) {
        Element aggregatedElement = new Element(this.getName());

        long sumOfAggregated = 0;
        for (GroupEvaluationMeasureResult mr : results) {
            long value = (long) mr.getValue();
            if (Double.isNaN(value)) {
                Global.showWarning("The value for the measure " + this.getName() + " is NaN");
            } else {
                if (Double.isInfinite(value)) {
                    Global.showWarning("The value for the measure " + this.getName() + " is Infinite");
                } else {
                    sumOfAggregated += mr.getValue();
                }
            }
        }
        aggregatedElement.setAttribute(EvaluationMeasure.VALUE_ATTRIBUTE_NAME, Long.toString(sumOfAggregated));
        return new GroupEvaluationMeasureResult(this, sumOfAggregated, aggregatedElement);
    }

    @Override
    public boolean usesRatingPrediction() {
        return true;
    }
}
