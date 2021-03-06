/* 
 * Copyright (C) 2016 jcastro
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package delfos.main.managers.database.submanagers;

import delfos.ConsoleParameters;
import delfos.dataset.changeable.ChangeableDatasetLoader;
import delfos.main.managers.CaseUseSubManager;
import delfos.main.managers.database.DatabaseManager;
import static delfos.main.managers.database.DatabaseManager.extractChangeableDatasetHandler;

/**
 *
 * @version 21-oct-2014
 * @author jcastro-inf ( https://github.com/jcastro-inf )
 */
public abstract class DatabaseCaseUseSubManager extends CaseUseSubManager {

    public DatabaseCaseUseSubManager() {
        super(DatabaseManager.getInstance());
    }

    /**
     * Comprueba si este es el manager correcto para la linea de comandos
     * especificada.
     *
     * @param consoleParameters Parámetros de la línea de comandos.
     * @return true si es el manager adecuado.
     */
    @Override
    public abstract boolean isRightManager(ConsoleParameters consoleParameters);

    @Override
    public final void manageCaseUse(ConsoleParameters consoleParameters) {
        ChangeableDatasetLoader changeableDatasetLoader = extractChangeableDatasetHandler(consoleParameters);
        this.manageCaseUse(consoleParameters, changeableDatasetLoader);
        changeableDatasetLoader.commitChangesInPersistence();
    }

    /**
     * Ejecuta las tareas necesarias de este caso de uso.
     *
     * @param consoleParameters Parámetros de la línea de comandos.
     * @param changeableDatasetLoader changeable dataset loader.
     */
    public abstract void manageCaseUse(ConsoleParameters consoleParameters, ChangeableDatasetLoader changeableDatasetLoader);

}
