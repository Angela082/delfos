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
package delfos.databaseconnections;

import java.sql.Connection;
import java.sql.SQLException;

/**
 * Interfaz que encapsula una conexión a base de datos.
 *
 * @author jcastro-inf ( https://github.com/jcastro-inf )
 *
 * @version 1.0 Unknown date
 * @version 1.1 (1 de Marzo de 2013)
 */
public interface DatabaseConection {

    /**
     * Devuelve el prefijo que se pone a las tablas que se utilizan para
     * almacenar los modelos generados por los sistemas de recomendación.
     *
     * @return Prefijo de las tablas.
     */
    public String getPrefix();

    /**
     * Devuelve el usuario que se utiliza para la conexión
     *
     * @return Usuario que se conecta a la base de datos
     */
    public String getUser();

    /**
     * Devuelve la contraseña
     *
     * @return Contraseña
     *
     * @deprecated No se debe almacenar la contraseña
     */
    @Deprecated
    public String getPass();

    /**
     * Devuelve la base de datos a la que se conecta
     *
     * @return Base de datos a la que se conecta
     */
    public String getDatabaseName();

    /**
     * Devuelve la ip o el nombre del servidor.
     *
     * @return IP o nombre del servidor
     */
    public String getHostName();

    /**
     * Devuelve el puerto de conexión con la base de datos
     *
     * @return Puerto de conexión con la base de datos
     */
    public int getPort();

    /**
     * Copia la conexión añadiendo una cadena detras del prefijo que ya existía.
     *
     * @param prefix Cadena que se añade.
     * @return Nuevo objeto para comunicarse usando la cadena incorporada.
     * @throws java.sql.SQLException
     */
    public DatabaseConection copyWithPrefix(String prefix) throws SQLException;

    public Connection doConnection() throws SQLException;

    /**
     * Commits and closes the connection.
     *
     * @throws java.sql.SQLException
     */
    public void close() throws SQLException;
}
