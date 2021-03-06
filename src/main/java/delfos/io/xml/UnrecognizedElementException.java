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
package delfos.io.xml;

import org.jdom2.Element;

/**
 * Excepción que se lanza para informar que el objeto {@link Element} que se 
 * deseaba convertir a un objeto concreto no tiene el formato correcto.
 * @author jcastro-inf ( https://github.com/jcastro-inf )
 */
public class UnrecognizedElementException extends Exception{

    private static final long serialVersionUID = 1L;
    public UnrecognizedElementException(String message) {
        super(message);
    }
    
}
