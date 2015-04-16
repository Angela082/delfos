package delfos.rs.persistence.database;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Collection;
import java.util.TreeMap;
import java.util.logging.Level;
import java.util.logging.Logger;
import delfos.ERROR_CODES;
import delfos.common.Global;
import delfos.databaseconnections.DatabaseConection;
import delfos.rs.collaborativefiltering.svd.TryThisAtHomeSVD;
import delfos.rs.collaborativefiltering.svd.TryThisAtHomeSVDModel;
import delfos.rs.persistence.DatabasePersistence;
import delfos.rs.persistence.FailureInPersistence;

/**
 * Objeto para almacenar y recuperar en una base de datos mysql el modelo de
 * recomendación del sistema{@link TryThisAtHomeSVD}.
 *
 * @author Jorge Castro Gallardo (Universidad de Jaén, Sinbad2)
 *
 * @version 1.0 Unknown date
 * @version 1.1 1 de Marzo de 2013
 * @version 2.0 28-Mayo-2013 Adecuación a la refactorización de los sistemas de
 * recomendación.
 */
public class DAOTryThisAtHomeDatabaseModel implements RecommendationModelDatabasePersistence<TryThisAtHomeSVDModel> {

    /**
     * Prefijo que se añade a las tablas creadas con este objeto, para denotar
     * que pertenecen al modelo generado por el sistema de recomendación basado
     * en descomposición en valores singulares
     */
    private final String RECOMMENDER_PREFIX = "try_this_";
    /**
     * Nombre de la tabla destinada a almacenar los perfiles de usuario
     */
    private final String USER_PROFILES = "user_profiles";
    /**
     * Nombre de la tabla destinada a almacenar los perfiles de productos
     */
    private final String ITEM_PROFILES = "item_profiles";

    /**
     * Devuelve el nombre final de la tabla que se usa para almacenar/recuperar
     * los perfiles de usuario generados por el sistema de recomendación
     *
     * @return Nombre de la tabla en la base de datos
     */
    private String getUserProfilesTable(String prefix) {
        return prefix + RECOMMENDER_PREFIX + USER_PROFILES;
    }

    /**
     * Devuelve el nombre final de la tabla que se usa para almacenar/recuperar
     * los perfiles de productos generados por el sistema de recomendación
     *
     * @return Nombre de la tabla en la base de datos
     */
    private String getItemProfilesTable(String prefix) {
        return prefix + RECOMMENDER_PREFIX + ITEM_PROFILES;
    }

    private String getTemporalUserProfilesTable(String prefix) {
        return getUserProfilesTable(prefix) + "_temp";
    }

    private String getTemporalItemProfilesTable(String prefix) {
        return getItemProfilesTable(prefix) + "_temp";
    }

    public DAOTryThisAtHomeDatabaseModel() {
    }

    private void createStructures(DatabaseConection databaseConection) throws FailureInPersistence {
        try (
                Connection connection = databaseConection.doConnection();
                Statement st = connection.createStatement()) {

            String prefix = databaseConection.getPrefix();

            st.execute("DROP TABLE IF EXISTS " + getTemporalUserProfilesTable(prefix) + ";");
            st.execute("DROP TABLE IF EXISTS " + getTemporalItemProfilesTable(prefix) + ";");

            st.execute("CREATE TABLE  " + getTemporalUserProfilesTable(prefix) + " ("
                    + "idUser int(10) NOT NULL,"
                    + "idFeature int(10) unsigned NOT NULL,"
                    + "value float NOT NULL,"
                    + "PRIMARY KEY (idUser,idFeature)"
                    + ") ENGINE=InnoDB DEFAULT CHARSET=latin1;");

            st.execute("CREATE TABLE  " + getTemporalItemProfilesTable(prefix) + " ("
                    + "idItem int(10) unsigned NOT NULL,"
                    + "idFeature int(10) unsigned NOT NULL,"
                    + "value float NOT NULL,"
                    + "PRIMARY KEY (idItem,idFeature)"
                    + ") ENGINE=InnoDB DEFAULT CHARSET=latin1;");

            st.close();
        } catch (SQLException ex) {
            throw new FailureInPersistence(ex);
        }
    }

    private void makePermanent(DatabaseConection databaseConection) throws FailureInPersistence {
        try (
                Connection connection = databaseConection.doConnection();
                Statement st = connection.createStatement()) {
            String prefix = databaseConection.getPrefix();

            st.execute("COMMIT");
            st.execute("DROP TABLE IF EXISTS " + getUserProfilesTable(prefix) + ";");
            st.execute("ALTER TABLE " + getTemporalUserProfilesTable(prefix) + " RENAME TO " + getUserProfilesTable(prefix) + ";");

            st.execute("DROP TABLE IF EXISTS " + getItemProfilesTable(prefix) + ";");
            st.execute("ALTER TABLE " + getTemporalItemProfilesTable(prefix) + " RENAME TO " + getItemProfilesTable(prefix) + ";");

            st.execute("COMMIT");
            st.close();
        } catch (SQLException ex) {
            throw new FailureInPersistence(ex);
        }
    }

    @Override
    public void saveModel(DatabasePersistence databasePersistence, TryThisAtHomeSVDModel model) throws FailureInPersistence {

        try {
            createStructures(databasePersistence.getConection());
        } catch (SQLException ex) {
            Logger.getLogger(DAOTryThisAtHomeDatabaseModel.class.getName()).log(Level.SEVERE, null, ex);
        } catch (ClassNotFoundException ex) {
            Logger.getLogger(DAOTryThisAtHomeDatabaseModel.class.getName()).log(Level.SEVERE, null, ex);
        }
        final String prefix = databasePersistence.getPrefix();

        try (
                Connection connection = databasePersistence.getConection().doConnection();
                Statement st = connection.createStatement()) {

            {
                //Guardo los usuarios
                ArrayList<ArrayList<Double>> userProfiles = model.getAllUserFeatures();
                TreeMap<Integer, Integer> usersIndex = model.getUsersIndex();

                for (int idUser : usersIndex.keySet()) {
                    int userIndex = usersIndex.get(idUser);

                    ArrayList<Double> features = userProfiles.get(userIndex);

                    StringBuilder sentence = new StringBuilder();
                    sentence.append("insert into ");
                    sentence.append(getTemporalUserProfilesTable(prefix));
                    sentence.append(" (idUser,idFeature,value) values ");

                    for (int idFeature = 0; idFeature < features.size(); idFeature++) {
                        sentence.append("(");
                        sentence.append(idUser);
                        sentence.append(",");
                        sentence.append(idFeature);
                        sentence.append(",");
                        sentence.append(features.get(idFeature));
                        sentence.append("),");

                    }
                    sentence.setCharAt(sentence.length() - 1, ';');
                    Global.showMessage("================================================\n");
                    Global.showMessage(sentence.toString() + "\n");
                    st.executeUpdate(sentence.toString());
                }
            }

            {
                //Guardo los productos
                ArrayList<ArrayList<Double>> itemProfiles = model.getAllItemFeatures();
                TreeMap<Integer, Integer> itemsIndex = model.getItemsIndex();

                for (int idItem : itemsIndex.keySet()) {
                    int itemIndex = itemsIndex.get(idItem);
                    ArrayList<Double> features = itemProfiles.get(itemIndex);
                    StringBuilder sentence = new StringBuilder();
                    sentence.append("insert into ");
                    sentence.append(getTemporalItemProfilesTable(prefix));
                    sentence.append(" (idItem,idFeature,value) values ");
                    for (int idFeature = 0; idFeature < features.size(); idFeature++) {
                        sentence.append("(");
                        sentence.append(idItem);
                        sentence.append(",");
                        sentence.append(idFeature);
                        sentence.append(",");
                        sentence.append(features.get(idFeature));
                        sentence.append("),");
                    }
                    sentence.setCharAt(sentence.length() - 1, ';');
                    Global.showMessage("================================================\n");
                    Global.showMessage(sentence.toString() + "\n");
                    st.executeUpdate(sentence.toString());
                }
            }

            makePermanent(databasePersistence.getConection());
        } catch (ClassNotFoundException ex) {
            throw new FailureInPersistence(ex);
        } catch (SQLException ex) {
            throw new FailureInPersistence(ex);
        }
    }

    @Override
    public TryThisAtHomeSVDModel loadModel(DatabasePersistence databasePersistence, Collection<Integer> users, Collection<Integer> items) throws FailureInPersistence {

        try {
            final String prefix = databasePersistence.getPrefix();

            final int numFeatures;
            final int numUsers;
            final int numItems;

            try (
                    Connection connection = databasePersistence.getConection().doConnection();
                    Statement statement = connection.createStatement()) {

                ResultSet executeQuery;

                //Calculo el número de características para los usuarios.
                executeQuery = statement.executeQuery(
                        "select count(distinct idFeature) "
                        + "from " + getUserProfilesTable(prefix) + ";");
                executeQuery.next();
                int numUserFeatures = executeQuery.getInt(1);
                executeQuery.close();

                //NumUsers
                executeQuery = statement.executeQuery(
                        "select count(distinct idUser) "
                        + "from " + getUserProfilesTable(prefix) + ";");
                executeQuery.next();
                numUsers = executeQuery.getInt(1);
                executeQuery.close();

                //Calculo el número de características para los productos.
                executeQuery = statement.executeQuery(
                        "select count(distinct idFeature) "
                        + "from " + getItemProfilesTable(prefix) + ";");
                executeQuery.next();
                int numItemFeatures = executeQuery.getInt(1);
                executeQuery.close();

                //NumItems
                executeQuery = statement.executeQuery(
                        "select count(distinct idItem) "
                        + "from " + getItemProfilesTable(prefix) + ";");
                executeQuery.next();
                numItems = executeQuery.getInt(1);
                executeQuery.close();

                if (numUserFeatures == numItemFeatures) {
                    numFeatures = numUserFeatures;
                } else {
                    throw new FailureInPersistence("The number of features for users and items is different (" + numUserFeatures + "," + numItemFeatures + ")");
                }
            } catch (SQLException ex) {
                throw new FailureInPersistence(ex);
            }

            ArrayList<ArrayList<Double>> usersFeatures = new ArrayList<ArrayList<Double>>(numUsers);
            TreeMap<Integer, Integer> usersIndex = new TreeMap<Integer, Integer>();

            {
                StringBuilder sentence = new StringBuilder();

                sentence.append("select idUser,idFeature,value from ");
                sentence.append(getUserProfilesTable(prefix));
                sentence.append(";");

//            sentence.append(" where ");
//            for (int idUser : users1) {
//                sentence.append(" idUser = ").append(idUser);
//
//                sentence.append(" or ");
//            }
//            sentence.replace(sentence.length() - 4, sentence.length(), ";");
                try (
                        Connection connection = databasePersistence.getConection().doConnection();
                        Statement statement = connection.createStatement()) {
                    ResultSet rstUsers = statement.executeQuery(sentence.toString());
                    while (rstUsers.next()) {
                        int idUser = rstUsers.getInt("idUser");
                        int idFeature = rstUsers.getInt("idFeature");
                        double featureValue = rstUsers.getDouble("value");

                        if (!usersIndex.containsKey(idUser)) {
                            usersIndex.put(idUser, usersIndex.size());
                            ArrayList<Double> arrayList = new ArrayList<Double>(numFeatures);
                            for (int i = 0; i < numFeatures; i++) {
                                arrayList.add(null);
                            }
                            usersFeatures.add(arrayList);
                        }
                        usersFeatures.get(usersIndex.get(idUser)).set(idFeature, featureValue);
                    }
                    rstUsers.close();
                } catch (SQLException ex) {
                    throw new FailureInPersistence(ex);
                }
            }

            ArrayList<ArrayList<Double>> itemsFeatures = new ArrayList<ArrayList<Double>>(numItems);
            TreeMap<Integer, Integer> itemsIndex = new TreeMap<Integer, Integer>();

            {
                StringBuilder sentence = new StringBuilder();
                sentence.append("select idItem,idFeature,value from ");
                sentence.append(getItemProfilesTable(prefix));
                sentence.append(";");

//            sentence.append(" where ");
//            for (int idItem : item) {
//                sentence.append(" idItem = ").append(idItem);
//                sentence.append(" or ");
//            }
//            sentence.replace(sentence.length() - 4, sentence.length(), ";");
                try (
                        Connection connection = databasePersistence.getConection().doConnection();
                        Statement statement = connection.createStatement()) {
                    ResultSet rstItems = statement.executeQuery(sentence.toString());
                    while (rstItems.next()) {
                        int idItem = rstItems.getInt("idItem");
                        int idFeature = rstItems.getInt("idFeature");
                        double featureValue = rstItems.getDouble("value");

                        if (!itemsIndex.containsKey(idItem)) {
                            itemsIndex.put(idItem, itemsIndex.size());
                            ArrayList<Double> arrayList = new ArrayList<Double>(numFeatures);
                            for (int i = 0; i < numFeatures; i++) {
                                arrayList.add(null);
                            }
                            itemsFeatures.add(arrayList);
                        }

                        itemsFeatures.get(itemsIndex.get(idItem)).set(idFeature, featureValue);
                    }
                    rstItems.close();
                    statement.close();
                } catch (SQLException ex) {
                    throw new FailureInPersistence(ex);
                }
            }

            return new TryThisAtHomeSVDModel(usersFeatures, usersIndex, itemsFeatures, itemsIndex);
        } catch (ClassNotFoundException ex) {
            ERROR_CODES.DEPENDENCY_NOT_FOUND.exit(ex);
            throw new IllegalArgumentException(ex);
        }
    }
}