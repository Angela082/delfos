package delfos.group.results.groupevaluationmeasures;

import delfos.ERROR_CODES;
import delfos.common.exceptions.dataset.users.UserNotFound;
import delfos.common.statisticalfuncions.MeanIterative;
import delfos.dataset.basic.loader.types.DatasetLoader;
import delfos.dataset.basic.rating.Rating;
import delfos.dataset.basic.rating.RatingsDataset;
import delfos.dataset.basic.rating.RelevanceCriteria;
import delfos.group.groupsofusers.GroupOfUsers;
import delfos.group.results.grouprecomendationresults.GroupRecommenderSystemResult;
import delfos.rs.recommendation.Recommendation;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import org.jdom2.Element;

//TODO: revisar si las características e interpretación de la medida son correctas
/**
 * A la espera de respuesta de Ingrid A. Christensen desde argentina, la primera
 * autora de
 *
 * Entertainment recommender systems for group of users Expert Systems with
 * Applications 38 (2011) 14127–14135
 *
 * Corresponding author at: ISISTAN, Facultad de Ciencias Exactas, UNCPBA Campus
 * Universitario, Paraje Arroyo Seco, Tandil, Argentina. E-mail addresses:
 * ichriste@exa.unicen.edu.ar, christenseningrid@gmail.com (I.A.Christensen),
 * sschia@exa.unicen.edu.ar (S. Schiafﬁno)
 *
 * @author Jorge Castro Gallardo
 *
 * @version Unknown Date
 * @version 20-Noviembre-2013
 */
public class NormalizedIndividualSatisfaction extends GroupEvaluationMeasure {
    /*

     "Obtaining the normalized individual satisfaction by dividing the sum of
     ratings of the recommended items in the list by the maximal 'possible'
     sum for each individual member".

     Es decir

     En este trabajo obtuvimos una lista de 10 recomendaciones, con las
     valoraciones estimadas para el grupo. Utilizando la técnica de
     estimación usada en la agregación obtuvimos valoraciones individuales
     para cada miembro. Por cada miembro realizamos la suma de dichas
     valoraciones y las dividimos por la máxima suma posible derivada de las
     valoraciones reales (conocidas en el test data set).

     */

    public NormalizedIndividualSatisfaction() {
        super();
    }

    @Override
    public GroupEvaluationMeasureResult getMeasureResult(
            GroupRecommenderSystemResult groupRecommenderSystemResult,
            DatasetLoader<? extends Rating> originalDatasetLoader,
            RatingsDataset<? extends Rating> testDataset,
            RelevanceCriteria relevanceCriteria,
            DatasetLoader<? extends Rating> trainingDatasetLoader,
            DatasetLoader<? extends Rating> testDatasetLoader) {

        MeanIterative media = new MeanIterative();

        Element measureElement = new Element(getName());

        for (GroupOfUsers groupOfUsers : groupRecommenderSystemResult.getGroupsOfUsers()) {
            Collection<Recommendation> groupRecommendations = groupRecommenderSystemResult.getGroupOutput(groupOfUsers).getRecommendations();

            Element groupElement = new Element("Group");

            Map<Integer, Number> predicciones = new TreeMap<>();
            groupRecommendations.stream().forEach((r) -> {
                predicciones.put(r.getIdItem(), r.getPreference());
            });

            MeanIterative groupNIS = new MeanIterative();
            for (int idUser : groupOfUsers) {
                Element userElement = new Element("User");
                userElement.setAttribute("idUser", Integer.toString(idUser));

                try {

                    Map<Integer, ? extends Rating> userRated = testDataset.getUserRatingsRated(idUser);

                    double denominador = 0;
                    double numerador = 0;

                    List<Recommendation> recomendacionesAlGrupoParaUser = new ArrayList<>(predicciones.size());
                    List<Recommendation> recomendacionesOptimasAlUser = new ArrayList<>(userRated.size());
                    for (int idItem : userRated.keySet()) {
                        recomendacionesOptimasAlUser.add(new Recommendation(idItem, userRated.get(idItem).getRatingValue()));
                    }

                    int minLength = Math.min(recomendacionesAlGrupoParaUser.size(), recomendacionesOptimasAlUser.size());

                    Collections.sort(recomendacionesOptimasAlUser);
                    recomendacionesOptimasAlUser = recomendacionesOptimasAlUser.subList(0, minLength);

                    Collections.sort(recomendacionesAlGrupoParaUser);
                    recomendacionesAlGrupoParaUser = recomendacionesAlGrupoParaUser.subList(0, minLength);

                    for (Recommendation r : recomendacionesAlGrupoParaUser) {
                        numerador += r.getPreference().doubleValue();
                    }

                    for (Recommendation r : recomendacionesOptimasAlUser) {
                        //Suma del máximo posible para este usuario
                        denominador += r.getPreference().doubleValue();
                    }

                    if (denominador != 0) {
                        groupNIS.addValue(numerador / denominador);
                    }
                    userElement.setAttribute("normalizedIndividualSatisfaction", Double.toString(numerador / denominador));
                } catch (UserNotFound ex) {
                    ERROR_CODES.USER_NOT_FOUND.exit(ex);
                }
                groupElement.addContent(userElement);
            }
            groupElement.setAttribute("value", Double.toString(groupNIS.getMean()));
            media.addValue(groupNIS.getMean());
            measureElement.addContent(groupElement);
        }
        measureElement.setAttribute("value", Double.toString(media.getMean()));
        return new GroupEvaluationMeasureResult(this, (float) media.getMean(), measureElement);
    }

    @Override
    public boolean usesRatingPrediction() {
        return true;
    }
}
