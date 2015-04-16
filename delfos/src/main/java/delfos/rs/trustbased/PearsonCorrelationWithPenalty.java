package delfos.rs.trustbased;

import java.util.Collection;
import java.util.Map;
import java.util.TreeMap;
import delfos.dataset.basic.rating.Rating;
import delfos.dataset.basic.rating.RatingsDataset;
import delfos.dataset.basic.loader.types.DatasetLoader;
import delfos.ERROR_CODES;
import delfos.similaritymeasures.JaccardForUsers;
import delfos.common.Chronometer;
import delfos.common.exceptions.dataset.CannotLoadRatingsDataset;
import delfos.common.exceptions.dataset.users.UserNotFound;
import delfos.common.parameters.Parameter;
import delfos.common.parameters.restriction.IntegerParameter;
import delfos.common.statisticalfuncions.MeanIterative;
import delfos.group.casestudy.definedcases.jrs2014.CouldNotComputeTrust;
import delfos.group.casestudy.definedcases.jrs2014.PearsonWithPenalty;

/**
 * Calcula el grafo entre usuarios utilizando la medida Jaccard, con el número
 * de valoraciones en común de los usuarios.
 *
 * @author Jorge Castro Gallardo (Sinbad2,Universidad de Jaén)
 * @version 1.0 19-Julio-2013
 */
public class PearsonCorrelationWithPenalty extends WeightedGraphCalculation<Integer> {

    private static final long serialVersionUID = -3387516993124229948L;

    public static final Parameter PENALTY = new Parameter("commonRatingPenalty", new IntegerParameter(1, 10000, 30));

    public PearsonCorrelationWithPenalty() {
        super();
        addParameter(PENALTY);
    }

    public PearsonCorrelationWithPenalty(int commonRatingPenalty) {
        this();
        setParameterValue(PENALTY, commonRatingPenalty);
    }

    @Override
    public WeightedGraphAdapter<Integer> computeTrustValues(DatasetLoader<? extends Rating> datasetLoader, Collection<Integer> users) throws CannotLoadRatingsDataset {
        boolean printPartialResults;

        final RatingsDataset<? extends Rating> ratingsDataset = datasetLoader.getRatingsDataset();

        Map<Integer, Map<Integer, Number>> pccMatrix = new TreeMap<Integer, Map<Integer, Number>>();

        PearsonWithPenalty pairwisePcc = new PearsonWithPenalty((Integer) getParameterValue(PENALTY));

        for (Integer idUser : users) {

            pccMatrix.put(idUser, new TreeMap<Integer, Number>());
        }

        JaccardForUsers jaccardForUsers = new JaccardForUsers();

        int numLoops = users.size() * users.size();
        int thisLoop = 1;

        Chronometer c = new Chronometer();
        MeanIterative meanTimePerLoop = new MeanIterative();

        for (Integer idUser1 : users) {
            for (Integer idUser2 : users) {
                if (!pccMatrix.get(idUser1).containsKey(idUser2)) {
                    c.reset();
                    double similarity;
                    try {

                        similarity = pairwisePcc.getTrust(datasetLoader, idUser1, idUser2);
                        pccMatrix.get(idUser1).put(idUser2, similarity);
                        pccMatrix.get(idUser2).put(idUser1, similarity);
                    } catch (UserNotFound ex) {
                        ERROR_CODES.USER_NOT_FOUND.exit(ex);
                    } catch (CouldNotComputeTrust ex) {
                        /*Situación completamente normal, porque no se puede
                         calcular ningun valor de confianza por los datos que
                         hay en los perfiles del par de usuarios a comprobar
                         */
                    }
                    meanTimePerLoop.addValue(c.getTotalElapsed());

                    long remainingTime = (long) ((numLoops - thisLoop) * meanTimePerLoop.getMean());

                    fireProgressChanged("Building PccWithPenalty Graph", (thisLoop * 100) / numLoops, remainingTime);
                }
                thisLoop++;
            }
        }
        WeightedGraphAdapter<Integer> pccGraph = new WeightedGraphAdapter<Integer>(pccMatrix);

        return pccGraph;
    }
}