package delfos.main.managers.recommendation.nonpersonalised;

import delfos.ConsoleParameters;
import java.io.File;

/**
 *
 * @author Jorge Castro Gallardo (Universidad de Granada, sci2s)
 * <jorgecastrog@correo.ugr.es>
 */
public class ArgumentsNonPersonalisedTestSuite {

    public static ConsoleParameters buildRecommendationModel(File manageDatasetConfigFile) throws Exception {

        String[] consoleArguments = {
            "--non-personalised",
            "--build",
            "-config-file", manageDatasetConfigFile.getAbsolutePath()
        };

        return ConsoleParameters.parseArguments(consoleArguments);
    }

    public static ConsoleParameters recommendAnonymous(File manageDatasetConfigFile) throws Exception {
        String[] consoleArguments = {
            "--non-personalised",
            "--recommend",
            "-config-file", manageDatasetConfigFile.getAbsolutePath()
        };

        return ConsoleParameters.parseArguments(consoleArguments);
    }

    public static ConsoleParameters recommendToUser(File manageDatasetConfigFile, int idUser) throws Exception {
        String[] consoleArguments = {
            "--non-personalised",
            "--recommend",
            "-u", Integer.toString(idUser),
            "-config-file", manageDatasetConfigFile.getAbsolutePath()
        };

        return ConsoleParameters.parseArguments(consoleArguments);
    }

}
