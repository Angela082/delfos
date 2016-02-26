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
package delfos.group.groupsofusers.measuresovergroups;

import delfos.common.exceptions.dataset.CannotLoadRatingsDataset;
import delfos.common.parameters.Parameter;
import delfos.common.parameters.restriction.ParameterOwnerRestriction;
import delfos.dataset.basic.loader.types.DatasetLoader;
import delfos.dataset.basic.rating.Rating;
import delfos.factories.WeightedGraphCalculatorFactory;
import delfos.group.groupsofusers.GroupOfUsers;
import delfos.rs.trustbased.WeightedGraphAdapter;
import delfos.rs.trustbased.WeightedGraphCalculation;
import delfos.rs.trustbased.implicittrustcomputation.ShambourLu_UserBasedImplicitTrustComputation;

/**
 * Clase para la suma de todas las distancias entre todos los pares de usuarios.
 *
 * @author jcastro-inf ( https://github.com/jcastro-inf )
 * @version 1.0 03-May-2013
 */
public class SumDistanceInGraph extends GroupMeasureAdapter {

    public static final Parameter weightedGraphCalculation = new Parameter(
            "weightedGraphCalculation",
            new ParameterOwnerRestriction(WeightedGraphCalculatorFactory.getInstance(), new ShambourLu_UserBasedImplicitTrustComputation()));

    public SumDistanceInGraph() {
        super();
        addParameter(weightedGraphCalculation);
    }

    public SumDistanceInGraph(WeightedGraphCalculation weightedGraphCalculation) {
        this();
        setParameterValue(SumDistanceInGraph.weightedGraphCalculation, weightedGraphCalculation);
    }

    @Override
    public String getNameWithParameters() {
        return "SumD_" + getWeightedGraphCalculation().getShortName();
    }

    /**
     * Devuelve el grado con el que el grupo indicado es un clique.
     *
     * @param datasetLoader
     * @param group Grupo a comprobar.
     * @return Valor difuso con el que un grupo es un clique.
     */
    @Override
    public double getMeasure(DatasetLoader<? extends Rating> datasetLoader, GroupOfUsers group) throws CannotLoadRatingsDataset {

        WeightedGraphAdapter<Integer> trustNetwork = getWeightedGraphCalculation().computeTrustValues(datasetLoader, group.getIdMembers());

        double sumDistance = 0;

        for (int idMember1 : group.getIdMembers()) {
            for (int idMember2 : group.getIdMembers()) {
                double distance;
                distance = trustNetwork.geodesicDistance(idMember1, idMember2);
                sumDistance += distance;
            }
        }
        return sumDistance;

    }

    public WeightedGraphCalculation getWeightedGraphCalculation() {
        return (WeightedGraphCalculation) getParameterValue(weightedGraphCalculation);
    }

}
