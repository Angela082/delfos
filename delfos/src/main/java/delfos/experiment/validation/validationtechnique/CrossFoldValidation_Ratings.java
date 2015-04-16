package delfos.experiment.validation.validationtechnique;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Set;
import java.util.TreeMap;
import java.util.TreeSet;
import delfos.ERROR_CODES;
import delfos.common.exceptions.dataset.CannotLoadContentDataset;
import delfos.common.exceptions.dataset.CannotLoadRatingsDataset;
import delfos.common.exceptions.dataset.items.ItemNotFound;
import delfos.common.exceptions.dataset.users.UserNotFound;
import delfos.common.parameters.Parameter;
import delfos.common.parameters.restriction.IntegerParameter;
import delfos.dataset.basic.rating.Rating;
import delfos.dataset.basic.rating.RatingsDataset;
import delfos.dataset.basic.loader.types.DatasetLoader;
import delfos.dataset.storage.validationdatasets.PairOfTrainTestRatingsDataset;
import delfos.dataset.storage.validationdatasets.ValidationDatasets;

/**
 * Clase que implementa el método de partición de datasets Cross Fold Validation
 * tomando como entrada usuario-producto. De esta manera, cada dato individual
 * es una valoración por lo que al hacer las particiones se tienen en cuenta las
 * valoraciones (los usuarios o los items a los que pertenecen dichas
 * valoraciones no se tienen en cuenta)
 *
* @author Jorge Castro Gallardo
 * @version 1.0 (19 Octubre 2011)
 */
public class CrossFoldValidation_Ratings extends ValidationTechnique {

    private static final long serialVersionUID = 1L;
    /**
     * Parámetro para almacenar el número de particiones que se realizan sobre
     * el dataset original.
     */
    public static final Parameter NUM_PARTITIONS = new Parameter("NUM_PARTITIONS", new IntegerParameter(2, Integer.MAX_VALUE, 5), "Número de particiones que se realizan sobre el dataset original.");

    /**
     * Constructor de la clase que genera los conjuntos de validación cruzada.
     * Por defecto tiene cinco particiones y la semilla utilizada será la fecha
     * actual {@link System#currentTimeMillis()}
     */
    public CrossFoldValidation_Ratings() {
        super();

        addParameter(NUM_PARTITIONS);
    }

    @Override
    public PairOfTrainTestRatingsDataset[] shuffle(DatasetLoader<? extends Rating> datasetLoader) throws CannotLoadRatingsDataset, CannotLoadContentDataset {
        Random random = new Random(getSeedValue());
        int numSplit = getNumberOfPartitions();

        PairOfTrainTestRatingsDataset[] ret = new PairOfTrainTestRatingsDataset[numSplit];

        //cross validationDatasets initialization
        ArrayList<Map<Integer, Set<Integer>>> todosConjuntosTest = new ArrayList<>(numSplit);
        for (int i = 0; i < numSplit; i++) {
            todosConjuntosTest.add(new TreeMap<>());
        }

        //composicion de los conjuntos de training y test
        float numUserFinished = 0;
        final Collection<Integer> allUsers = datasetLoader.getRatingsDataset().allUsers();

        for (int idUser : allUsers) {
            //creo las listas para almacenar temporalmente los ids de los items de cada particion
            List<Set<Integer>> testLocal = new ArrayList<>(numSplit);
            for (int i = 0; i < numSplit; i++) {
                testLocal.add(new TreeSet<>());
            }

            //creo una lista con todos los idItem para ir quitando cuando se elige la partición en la que estará
            List<Integer> todosItemsUsuActual = new ArrayList<>();

            RatingsDataset<? extends Rating> ratingsDataset = datasetLoader.getRatingsDataset();
            try {
                for (int idItem : ratingsDataset.getUserRated(idUser)) {
                    todosItemsUsuActual.add(idItem);
                }
            } catch (UserNotFound ex) {
                ERROR_CODES.USER_NOT_FOUND.exit(ex);
            }

            //Realizo la elección de la particion a la que pertenece cada item
            //sacando uno aleatoriamente y lo meto en la lista que toca
            int particionActual = 0;
            while (!todosItemsUsuActual.isEmpty()) {
                int indiceElegido = random.nextInt(todosItemsUsuActual.size());
                testLocal.get(particionActual).add(todosItemsUsuActual.remove(indiceElegido));
                particionActual = (particionActual + 1) % numSplit;
            }

            //compongo los conjuntos de validación completos de cada ejecución
            for (int numTest = 0; numTest < testLocal.size(); numTest++) {
                todosConjuntosTest.get(numTest).put(idUser, testLocal.get(numTest));
            }

            double percent = numUserFinished * 100 / allUsers.size();
            progressChanged("Building partitions", (int) percent);
            numUserFinished++;
        }

        for (int i = 0; i < getNumberOfPartitions(); i++) {
            try {
                ret[i] = new PairOfTrainTestRatingsDataset(
                        datasetLoader,
                        ValidationDatasets.getInstance().createTrainingDataset(datasetLoader.getRatingsDataset(), todosConjuntosTest.get(i)),
                        ValidationDatasets.getInstance().createTestDataset(datasetLoader.getRatingsDataset(), todosConjuntosTest.get(i)));
            } catch (UserNotFound ex) {
                ERROR_CODES.USER_NOT_FOUND.exit(ex);
            } catch (ItemNotFound ex) {
                ERROR_CODES.ITEM_NOT_FOUND.exit(ex);
            }
            progressChanged("Copying partitions", (int) i * 100 / getNumberOfPartitions());
        }
        return ret;
    }

    public int getNumberOfPartitions() {
        return (Integer) getParameterValue(CrossFoldValidation_Ratings.NUM_PARTITIONS);
    }

    @Override
    public int getNumberOfSplits() {
        return getNumberOfPartitions();
    }
}
