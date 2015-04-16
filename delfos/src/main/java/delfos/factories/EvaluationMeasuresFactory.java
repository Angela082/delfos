package delfos.factories;

import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import delfos.results.evaluationmeasures.Coverage;
import delfos.results.evaluationmeasures.EvaluationMeasure;
import delfos.results.evaluationmeasures.NDCG;
import delfos.results.evaluationmeasures.NumberOfRecommendations;
import delfos.results.evaluationmeasures.PRSpace;
import delfos.results.evaluationmeasures.prediction.PredicitonErrorHistogram;
import delfos.results.evaluationmeasures.prediction.list.HalfLifeUtility;
import delfos.results.evaluationmeasures.ratingprediction.FScoreCollaborative;
import delfos.results.evaluationmeasures.ratingprediction.MAE;
import delfos.results.evaluationmeasures.ratingprediction.NMAE;
import delfos.results.evaluationmeasures.ratingprediction.NRMSE;
import delfos.results.evaluationmeasures.ratingprediction.PrecisionCollaborative;
import delfos.results.evaluationmeasures.ratingprediction.RMSE;
import delfos.results.evaluationmeasures.ratingprediction.RecallCollaborative;
import delfos.results.evaluationmeasures.roccurve.AreaUnderROC;

/**
 * Clase que permite recuperar todas las medidas de evaluación que la biblioteca
 * conoce.
 *
* @author Jorge Castro Gallardo
 *
 * @version 1.0 Unknown date
 * @version 1.1 20-Mar-2013
 * @version 2.0 9-Mayo-2013 Ahora la clase hereda de {@link Factory}.
 */
public class EvaluationMeasuresFactory extends Factory<EvaluationMeasure> {

    private static final EvaluationMeasuresFactory instance;

    public static EvaluationMeasuresFactory getInstance() {
        return instance;
    }

    static {
        instance = new EvaluationMeasuresFactory();
        instance.addClass(AreaUnderROC.class);
        instance.addClass(Coverage.class);
        instance.addClass(NumberOfRecommendations.class);
        instance.addClass(PRSpace.class);
        instance.addClass(MAE.class);
        instance.addClass(RMSE.class);
        instance.addClass(FScoreCollaborative.class);
        instance.addClass(PrecisionCollaborative.class);
        instance.addClass(RecallCollaborative.class);
        instance.addClass(NDCG.class);

        instance.addClass(NMAE.class);
        instance.addClass(NRMSE.class);

        instance.addClass(HalfLifeUtility.class);
        instance.addClass(PredicitonErrorHistogram.class);
    }

    private EvaluationMeasuresFactory() {
    }

    /**
     * Devuelve todas las medidas de evaluación que no necesitan que los
     * valoraciones de preferencia indicados para las recomendaciones sean
     * predicciones de valoraciones.
     *
     * @return Colección de medidas de evaluación.
     */
    public Collection<EvaluationMeasure> getAllContentBasedEvaluationMeasures() {
        List<EvaluationMeasure> ret = getAllClasses();

        for (Iterator<EvaluationMeasure> it = ret.iterator(); it.hasNext();) {
            EvaluationMeasure evaluationMeasure = it.next();
            if (evaluationMeasure.usesRatingPrediction()) {
                it.remove();
            }
        }
        return ret;
    }
}
