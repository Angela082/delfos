package delfos.dataset.loaders.movilens.ml1m;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.Collection;
import delfos.common.exceptions.dataset.CannotLoadContentDataset;
import delfos.common.exceptions.dataset.CannotLoadRatingsDataset;
import delfos.common.exceptions.dataset.CannotLoadUsersDataset;
import delfos.common.exceptions.dataset.users.UserAlreadyExists;
import delfos.common.parameters.Parameter;
import delfos.common.parameters.ParameterListener;
import delfos.common.parameters.restriction.DirectoryParameter;
import delfos.common.parameters.restriction.StringParameter;
import delfos.dataset.basic.item.ContentDataset;
import delfos.dataset.basic.rating.RatingWithTimestamp;
import delfos.dataset.basic.rating.RatingsDataset;
import delfos.dataset.basic.user.UsersDataset;
import delfos.dataset.basic.loader.types.DatasetLoaderAbstract;
import delfos.dataset.basic.loader.types.ContentDatasetLoader;
import delfos.dataset.basic.loader.types.UsersDatasetLoader;
import delfos.dataset.storage.memory.BothIndexRatingsDataset;
import delfos.io.csv.dataset.item.ContentDatasetToCSV;
import delfos.io.csv.dataset.user.UsersDatasetToCSV;

/**
 * Lee el dataset de MovieLens de 1 millón de ratings ml-1m.
 *
* @author Jorge Castro Gallardo
 *
 * @version 12-mar-2014
 */
public class MovieLens1Million extends DatasetLoaderAbstract<RatingWithTimestamp> implements ContentDatasetLoader, UsersDatasetLoader {

    private static final long serialVersionUID = 1L;

    private RatingsDataset<RatingWithTimestamp> ratingsDataset;
    private ContentDataset contentDataset;
    private UsersDataset usersDataset;

    static {
        File directory_Generic = new File(
                ".." + File.separator
                + ".." + File.separator
                + ".." + File.separator
                + "Datasets" + File.separator
                + "MovieLens" + File.separator
                + "1 - MovieLens-1M ratings" + File.separator
                + "ml-1m" + File.separator);

        File directory_C6108 = new File("C:\\Dropbox\\Datasets\\MovieLens\\1 - MovieLens-1M ratings\\ml-1m\\");

        DirectoryOfDataset = new Parameter("Directory", new DirectoryParameter(directory_C6108));
    }

    public final static Parameter DirectoryOfDataset;

    public final static Parameter ratingsDatasetParameter = new Parameter("File_ratingsDataset", new StringParameter("ratings.dat"));
    public final static Parameter contentDatasetParameter = new Parameter("File_contentDataset", new StringParameter("movies.dat"));
    public final static Parameter usersDatasetParameter = new Parameter("File_usersDataset", new StringParameter("users.dat"));

    public MovieLens1Million() {
        addParameter(DirectoryOfDataset);
        addParameter(ratingsDatasetParameter);
        addParameter(contentDatasetParameter);
        addParameter(usersDatasetParameter);

        addParammeterListener(new ParameterListener() {
            @Override
            public void parameterChanged() {
                ratingsDataset = null;
                contentDataset = null;
                usersDataset = null;
            }
        });
    }

    public MovieLens1Million(File directory) {
        this();

        setParameterValue(DirectoryOfDataset, directory);

    }

    @Override
    public synchronized RatingsDataset<RatingWithTimestamp> getRatingsDataset() throws CannotLoadRatingsDataset {
        if (ratingsDataset == null) {
            try {
                File ratingsDatasetFile = getRatingsFile();

                MovieLens1MillionRatingsDatasetToCSV ratingsDatasetToCSV = new MovieLens1MillionRatingsDatasetToCSV();
                Collection<RatingWithTimestamp> readContentDataset = ratingsDatasetToCSV.readRatingsDataset(ratingsDatasetFile);
                ratingsDataset = new BothIndexRatingsDataset<RatingWithTimestamp>(readContentDataset);
            } catch (FileNotFoundException ex) {
                throw new CannotLoadRatingsDataset(ex);
            }
        }
        return ratingsDataset;
    }

    @Override
    public synchronized ContentDataset getContentDataset() throws CannotLoadContentDataset {
        if (contentDataset == null) {
            try {
                File contentDatasetFile = getContentFile();

                ContentDatasetToCSV contentDatasetToCSV = new MovieLens1MillionContentDatasetToCSV();
                contentDataset = contentDatasetToCSV.readContentDataset(contentDatasetFile);
            } catch (FileNotFoundException ex) {
                throw new CannotLoadContentDataset(ex);
            }
        }
        return contentDataset;
    }

    @Override
    public synchronized UsersDataset getUsersDataset() throws CannotLoadUsersDataset {
        if (usersDataset == null) {
            try {
                File usersDatasetFile = getUsersFile();
                UsersDatasetToCSV usersDatasetToCSV = new MovieLens1MillionUsersDatasetToCSV();
                usersDataset = usersDatasetToCSV.readUsersDataset(usersDatasetFile);
            } catch (FileNotFoundException ex) {
                throw new CannotLoadUsersDataset(ex);
            } catch (UserAlreadyExists ex) {
                throw new CannotLoadUsersDataset(ex);
            }
        }
        return usersDataset;
    }

    public File getUsersFile() {

        File directory = (File) getParameterValue(DirectoryOfDataset);
        String usersDatasetFileName = (String) getParameterValue(usersDatasetParameter);
        File usersDatasetFile = new File(directory.getAbsolutePath() + File.separator + usersDatasetFileName);

        return usersDatasetFile;
    }

    public File getContentFile() {

        File directory = (File) getParameterValue(DirectoryOfDataset);
        String contentDatasetFileName = (String) getParameterValue(contentDatasetParameter);
        File contentDatasetFile = new File(directory.getAbsolutePath() + File.separator + contentDatasetFileName);

        return contentDatasetFile;
    }

    public File getRatingsFile() {

        File directory = (File) getParameterValue(DirectoryOfDataset);
        String ratingsDatasetFileName = (String) getParameterValue(ratingsDatasetParameter);
        File ratingsDatasetFile = new File(directory.getAbsolutePath() + File.separator + ratingsDatasetFileName);

        return ratingsDatasetFile;
    }

}
