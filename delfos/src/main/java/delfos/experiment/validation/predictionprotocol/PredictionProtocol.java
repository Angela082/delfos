package delfos.experiment.validation.predictionprotocol;

import java.util.Collection;
import delfos.dataset.basic.rating.Rating;
import delfos.dataset.basic.rating.RatingsDataset;
import delfos.rs.RecommenderSystemAdapter;
import delfos.experiment.SeedHolder;
import delfos.common.exceptions.dataset.users.UserNotFound;
import delfos.common.Global;
import delfos.common.parameters.ParameterListener;
import delfos.common.parameters.ParameterOwnerAdapter;
import delfos.common.parameters.ParameterOwnerType;

/**
 * Interfaz que especifica los métodos que deberá tener una validación de
 * predicciones. Esta clase se encarga de separar los ratings que conoce el
 * sistema de recomendación al realizar la predicción de los que no. Sólo se
 * usan las {@link PredictionProtocol} en algoritmos de filtrado colaborativo.
 *
* @author Jorge Castro Gallardo
 * @version 1.0 (19 Octubre 2011)
 * @version 1.1 08-Mar-2013 Ahora implementa {@link SeedHolder}, para controlar
 * la generación de datos aleatorios.
 */
public abstract class PredictionProtocol extends ParameterOwnerAdapter implements SeedHolder {

    /**
     * Crea un protocolo de predicción con los valores dados por defecto.
     */
    public PredictionProtocol() {
        super();

        addParameter(SEED);

        init();
    }

    /**
     * Solicita a la técnica de validación que prepare los datasets para
     * realizar una solicitud de recomendaciones al usuario <code>idUser</code>
     * en la partición <code>split</code>
     *
     * La lista devuelta se interpreta de la siguiente manera. La lista externa
     * representa las solicitudes de recomendación que se debe hacer y la lista
     * interna almacena los items que se pueden recomendar en cada solicitud.
     * Por ejemplo, si el valor devuelto es: <code>{{1,2},{1},{2}}</code> se
     * deberán realizar las llamadas: <code>recommendOnly(idUser,{1,2});</code>
     * <code>recommendOnly(idUser,{1});</code>
     * <code>recommendOnly(idUser,{2});</code> Este mecanismo se utiliza para la
     * validación {@link GivenN} y {@link AllButOne}.
     *
     * NOTA: Antes de cada
     * {@link RecommenderSystemAdapter#recommendOnly(java.lang.Integer, java.util.Collection) }
     * hay que eliminar las valoraciones que se van a predecir
     *
     * Calcula la lista que define cuántas peticiones de recomendación se
     * realizarán al sistema de recomendación y qué items debe predecir en cada
     * una de las mismas. Para ello se devuelve una lista de listas de idItems
     *
     * @param testRatingsDataset
     * @param idUser Usuario para el que se calcula la lista de peticiones
     * @return Lista que define cuántas peticiones y con qué items se debe
     * solicitar al sistema de recomendación colaborativo que realice
     * recomendaciones para su validación
     * @throws UserNotFound Si el usuario <code>idUser<\code> no se encuentra en
     * el dataset original.
     */
    public abstract Collection<Collection<Integer>> getRecommendationRequests(RatingsDataset<? extends Rating> testRatingsDataset, int idUser) throws UserNotFound;

    @Override
    public void setSeedValue(long seedValue) {
        setParameterValue(SEED, seedValue);
    }

    @Override
    public long getSeedValue() {
        return (Long) getParameterValue(SEED);
    }

    /**
     * Realiza las inicializaciones de la instancia.
     */
    private void init() {
        addParammeterListener(new ParameterListener() {
            private long valorAnterior
                    = (Long) getParameterValue(SEED);

            @Override
            public void parameterChanged() {
                long newValue = (Long) getParameterValue(SEED);
                if (valorAnterior != newValue) {
                    if (Global.isVerboseAnnoying()) {
                        Global.showWarning("Reset " + getName() + " to seed = " + newValue + "\n");
                    }
                    valorAnterior = newValue;
                    setSeedValue(newValue);
                }
            }
        });
    }

    @Override
    public ParameterOwnerType getParameterOwnerType() {
        return ParameterOwnerType.PREDICTION_PROTOCOL_TECHNIQUE;
    }

}