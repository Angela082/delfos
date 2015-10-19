package delfos.dataset.loaders.csv.changeable;

import delfos.ERROR_CODES;
import delfos.common.exceptions.dataset.CannotSaveUsersDataset;
import delfos.common.exceptions.dataset.entity.EntityNotFound;
import delfos.common.exceptions.dataset.users.UserNotFound;
import delfos.common.parameters.ParameterListener;
import delfos.dataset.basic.features.CollectionOfEntitiesWithFeaturesDefault;
import delfos.dataset.basic.user.User;
import delfos.dataset.changeable.ChangeableUsersDataset;
import delfos.io.csv.dataset.user.DefaultUsersDatasetToCSV;
import delfos.io.csv.dataset.user.UsersDatasetToCSV;
import java.io.File;
import java.io.IOException;
import java.util.Set;

/**
 * Implementa un dataset de contenido con persistencia sobre fichero CSV con la
 * posibilidad de modificar los productos del mismo.
 *
 * @author Jorge Castro Gallardo
 *
 * @version 16-sep-2013
 */
public class ChangeableUsersDatasetCSV extends CollectionOfEntitiesWithFeaturesDefault<User> implements ChangeableUsersDataset {

    private final ChangeableCSVFileDatasetLoader parent;

    public ChangeableUsersDatasetCSV(final ChangeableCSVFileDatasetLoader parent, Set<User> users) {
        super();
        this.parent = parent;
        parent.addParammeterListener(new ParameterListener() {
            private File usersDatasetFile = null;

            @Override
            public void parameterChanged() {
                if (usersDatasetFile == null) {
                    usersDatasetFile = parent.getUsersDatasetFile();
                } else {
                    if (!usersDatasetFile.equals(parent.getUsersDatasetFile())) {
                        try {
                            commitChangesInPersistence();
                        } catch (CannotSaveUsersDataset ex) {
                            ERROR_CODES.CANNOT_WRITE_USERS_DATASET.exit(ex);
                            throw new IllegalStateException(ex);
                        }
                    }
                }
            }
        });

        for (User user : users) {
            addUser(user);
        }
    }

    protected ChangeableUsersDatasetCSV(final ChangeableCSVFileDatasetLoader parent) {
        super();
        this.parent = parent;

        parent.addParammeterListener(new ParameterListener() {
            private File usersDatasetFile = null;

            @Override
            public void parameterChanged() {
                if (usersDatasetFile == null) {
                    usersDatasetFile = parent.getUsersDatasetFile();
                } else {
                    if (!usersDatasetFile.equals(parent.getUsersDatasetFile())) {
                        try {
                            commitChangesInPersistence();
                        } catch (CannotSaveUsersDataset ex) {
                            ERROR_CODES.CANNOT_WRITE_USERS_DATASET.exit(ex);
                            throw new IllegalStateException(ex);
                        }
                    }
                }
            }
        });
    }

    @Override
    public final void addUser(User user) {
        super.add(user);
    }

    @Override
    public User getUser(int idUser) throws UserNotFound {
        try {
            return get(idUser);
        } catch (EntityNotFound ex) {
            throw new UserNotFound(idUser, ex);
        }
    }

    @Override
    public void commitChangesInPersistence() throws CannotSaveUsersDataset {
        try {
            UsersDatasetToCSV usersDatasetToCSV = new DefaultUsersDatasetToCSV();
            usersDatasetToCSV.writeDataset(this, parent.getUsersDatasetFile().getAbsolutePath());
        } catch (IOException ex) {
            throw new CannotSaveUsersDataset(ex);
        }
    }

    @Override
    public User get(int idUser) throws EntityNotFound {
        if (entitiesById.containsKey(idUser)) {
            return entitiesById.get(idUser);
        } else {
            throw new UserNotFound(idUser);
        }
    }
}
