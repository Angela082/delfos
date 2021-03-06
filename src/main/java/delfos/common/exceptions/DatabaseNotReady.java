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
package delfos.common.exceptions;

import java.sql.SQLException;

/**
 * Excepción que se lanza cuando no se pudo realizar la conexión a la base de
 * datos (usuario o pass erróneo, host no encontrado, puerto erróneo, etc)
 *
 * @author jcastro-inf ( https://github.com/jcastro-inf )
 *
 * @version 1.0 Unknown date
 * @version 1.1 20-Mar-2103
 */
public class DatabaseNotReady extends Exception {

    private static final long serialVersionUID = 1L;

    /**
     * Crea la excepción con el mensaje indicado.
     *
     * @param msg Mensaje a mostrar.
     */
    public DatabaseNotReady(String msg) {
        super(msg);
    }

    public DatabaseNotReady(SQLException ex) {
        super(ex);
    }
}
