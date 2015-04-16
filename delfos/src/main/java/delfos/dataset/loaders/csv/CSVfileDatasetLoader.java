package delfos.dataset.loaders.csv;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.Collection;
import delfos.common.Global;
import delfos.common.exceptions.dataset.CannotLoadContentDataset;
import delfos.common.exceptions.dataset.CannotLoadRatingsDataset;
import delfos.common.exceptions.dataset.CannotLoadUsersDataset;
import delfos.common.exceptions.dataset.users.UserAlreadyExists;
import delfos.common.filefilters.FileFilterByExtension;
import delfos.common.parameters.Parameter;
import delfos.common.parameters.restriction.FileParameter;
import delfos.common.parameters.restriction.ObjectParameter;
import delfos.dataset.basic.item.ContentDataset;
import delfos.dataset.basic.rating.Rating;
import delfos.dataset.basic.rating.RatingsDataset;
import delfos.dataset.basic.rating.RelevanceCriteria;
import delfos.dataset.basic.user.User;
import delfos.dataset.basic.user.UsersDataset;
import delfos.dataset.basic.user.UsersDatasetAdapter;
import delfos.dataset.basic.loader.types.DatasetLoaderAbstract;
import delfos.dataset.basic.loader.types.ContentDatasetLoader;
import delfos.dataset.basic.loader.types.RatingsDatasetLoader;
import delfos.dataset.basic.loader.types.UsersDatasetLoader;
import delfos.dataset.storage.memory.BothIndexRatingsDataset;
import delfos.dataset.storage.memory.DefaultMemoryRatingsDataset_ItemIndexed;
import delfos.dataset.storage.memory.DefaultMemoryRatingsDataset_ItemIndexed_withMaps;
import delfos.dataset.storage.memory.DefaultMemoryRatingsDataset_UserIndexed;
import delfos.dataset.storage.memory.DefaultMemoryRatingsDataset_UserIndexed_withMaps;
import delfos.io.csv.dataset.item.ContentDatasetToCSV;
import delfos.io.csv.dataset.item.DefaultContentDatasetToCSV;
import delfos.io.csv.dataset.rating.RatingsDatasetToCSV;
import delfos.io.csv.dataset.rating.RatingsDatasetToCSV_JavaCSV20;
import delfos.io.csv.dataset.user.DefaultUsersDatasetToCSV;
import delfos.io.csv.dataset.user.UsersDatasetToCSV;

/**
 * Construye el RatingsDataset<? extends Rating>y ContentDataset a partir de dos
 * archivos CSV, uno para cada dataset
 *
* @author Jorge Castro Gallardo
 *
 * @version 1.1 29-01-2013
 * @version 1.0 Unknown date
 */
public class CSVfileDatasetLoader extends DatasetLoaderAbstract<Rating> implements RatingsDatasetLoader<Rating>, ContentDatasetLoader, UsersDatasetLoader {

    private static final long serialVersionUID = -3387516993124229948L;
    public static final String INDEX_NONE = "INDEX_NONE";
    public static final String INDEX_USERS = "INDEX_USERS";
    public static final String INDEX_ITEMS = "INDEX_ITEMS";
    public static final String INDEX_USERS_MAPS = "INDEX_USERS_MAPS";
    public static final String INDEX_ITEMS_MAPS = "INDEX_ITEMS_MAPS";
    public static final String INDEX_BOTH = "INDEX_BOTH";
    public final static Parameter RATINGS_FILE = new Parameter("Ratings_file", new FileParameter(new File(".." + File.separator + ".." + File.separator + "datasets" + File.separator + "" + "ratings.csv"), new FileFilterByExtension("csv")));
    public final static Parameter CONTENT_FILE = new Parameter("Content_file", new FileParameter(new File(".." + File.separator + ".." + File.separator + "datasets" + File.separator + "" + "content.csv"), new FileFilterByExtension("csv")));
    public final static Parameter USERS_FILE = new Parameter("Users_file", new FileParameter(new File(".." + File.separator + ".." + File.separator + "datasets" + File.separator + "" + "users.csv"), new FileFilterByExtension("csv")));
    public final static Parameter INDEXATION;

    static {
        String indexOptions[] = {INDEX_NONE, INDEX_USERS, INDEX_ITEMS, INDEX_USERS_MAPS, INDEX_ITEMS_MAPS, INDEX_BOTH};

        INDEXATION = new Parameter("INDEXATION", new ObjectParameter(indexOptions, INDEX_BOTH), "Establece la indexación que se usará en el dataset de valoraciones una vez cargado en memoria.");
    }
    private RatingsDataset<Rating> ratingsDataset;
    private ContentDataset contentDataset;
    private UsersDataset usersDataset;
    public int tamañoDataset = -1;

    public void setTamañoDataset(int tamañoDataset) {
        this.tamañoDataset = tamañoDataset;
    }

    public CSVfileDatasetLoader() {
        addParameter(RATINGS_FILE);
        addParameter(CONTENT_FILE);
        addParameter(USERS_FILE);
        addParameter(INDEXATION);
        addParammeterListener(() -> {
            ratingsDataset = null;
            contentDataset = null;
            usersDataset = null;
        });
    }

    /**
     * Constructor sin fichero de usuarios.
     *
     * @param ratingsFile
     * @param contentFile
     *
     * @deprecated Todos los datasets deben tener un fichero de usuarios.
     */
    @Deprecated
    public CSVfileDatasetLoader(String ratingsFile, String contentFile) {
        this();
        setParameterValue(RATINGS_FILE, ratingsFile);
        setParameterValue(CONTENT_FILE, contentFile);
    }

    public CSVfileDatasetLoader(String ratingsFile, String contentFile, String usersFile) {
        this();
        setParameterValue(RATINGS_FILE, ratingsFile);
        setParameterValue(CONTENT_FILE, contentFile);
        setParameterValue(USERS_FILE, usersFile);
    }

    public CSVfileDatasetLoader(String ratingsFile, String contentFile, String usersFile, String indexationMode) {
        this(ratingsFile, contentFile, usersFile);

        setParameterValue(INDEXATION, indexationMode);
    }

    @Override
    public RatingsDataset<Rating> getRatingsDataset() throws CannotLoadRatingsDataset {
        if (ratingsDataset == null) {
            try {
                RatingsDatasetToCSV ratingsDatasetToCSV = new RatingsDatasetToCSV_JavaCSV20();
                Collection<Rating> ratings = ratingsDatasetToCSV.readRatingsDataset(getRatingsDatasetFile());

                String indexationMode = getIndexationMode();
                if (indexationMode.equals(INDEX_NONE)) {
                    throw new CannotLoadRatingsDataset("Indexation method INDEX_NONE not supported yet.");
                }
                if (indexationMode.equals(INDEX_USERS)) {
                    ratingsDataset = new DefaultMemoryRatingsDataset_UserIndexed(ratings);
                }
                if (indexationMode.equals(INDEX_ITEMS)) {
                    ratingsDataset = new DefaultMemoryRatingsDataset_ItemIndexed(ratings);
                }
                if (indexationMode.equals(INDEX_BOTH)) {
                    ratingsDataset = new BothIndexRatingsDataset(ratings);
                }
                if (indexationMode.equals(INDEX_USERS_MAPS)) {
                    ratingsDataset = new DefaultMemoryRatingsDataset_UserIndexed_withMaps(ratings);
                }
                if (indexationMode.equals(INDEX_ITEMS_MAPS)) {
                    ratingsDataset = new DefaultMemoryRatingsDataset_ItemIndexed_withMaps(ratings);
                }
                if (ratingsDataset == null) {
                    throw new IllegalStateException("The indexation mode is unknown: " + indexationMode);
                }
            } catch (FileNotFoundException ex) {
                throw new CannotLoadRatingsDataset(ex);
            }
        }
        return ratingsDataset;
    }

    @Override
    public ContentDataset getContentDataset() throws CannotLoadContentDataset {

        File contentCSV = (File) getParameterValue(CONTENT_FILE);

        if (contentDataset == null) {
            ContentDatasetToCSV contentDatasetToCSV = new DefaultContentDatasetToCSV();
            try {
                contentDataset = contentDatasetToCSV.readContentDataset(contentCSV);
            } catch (FileNotFoundException ex) {
                throw new CannotLoadContentDataset(ex);
            }
        }
        return contentDataset;
    }

    @Override
    public synchronized UsersDataset getUsersDataset() throws CannotLoadUsersDataset {

        if (usersDataset == null) {
            UsersDatasetToCSV usersDatasetToCSV = new DefaultUsersDatasetToCSV();

            try {
                usersDataset = usersDatasetToCSV.readUsersDataset(getUsersDatasetFile().getAbsoluteFile());
            } catch (CannotLoadUsersDataset | FileNotFoundException ex) {
                Global.showWarning("Fail at loading users CSV, generating usersDataset from ratingsDataset");
                try {
                    RatingsDataset<Rating> ratingsDataset1 = getRatingsDataset();

                    Collection<User> users = new ArrayList<>(ratingsDataset1.allUsers().size());
                    ratingsDataset1.allUsers().stream().forEach((idUser) -> {
                        users.add(new User(idUser));
                    });

                    usersDataset = new UsersDatasetAdapter(users);

                } catch (UserAlreadyExists | CannotLoadRatingsDataset | CannotLoadUsersDataset ex1) {
                    throw new CannotLoadUsersDataset(ex1);
                }

            } catch (UserAlreadyExists ex) {
                throw new CannotLoadUsersDataset(ex);
            }
        }
        return usersDataset;
    }

    @Override
    public RelevanceCriteria getDefaultRelevanceCriteria() {
        return new RelevanceCriteria(4);
    }

    private String getIndexationMode() {
        return (String) getParameterValue(INDEXATION);
    }

    /**
     * Devuelve el nombre del archivo en que se almacena el dataset de usuarios.
     *
     * @return
     */
    public File getUsersDatasetFile() {
        return (File) getParameterValue(USERS_FILE);
    }

    /**
     * Devuelve el nombre del archivo en que se almacena el dataset de
     * valoraciones.
     *
     * @return
     */
    public File getRatingsDatasetFile() {
        return (File) getParameterValue(RATINGS_FILE);
    }

    /**
     * Devuelve el nombre del archivo en que se almacena el dataset de contenido
     * de los productos.
     *
     * @return
     */
    public File getContentDatasetFile() {
        return (File) getParameterValue(CONTENT_FILE);
    }
}
