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
package delfos.common.parallelwork;

import delfos.common.parallelwork.Parallelisation.Worker;

/**
 *
 * @author jcastro-inf ( https://github.com/jcastro-inf )
 * @version 1.0 31-May-2013
 *
 * @deprecated The parallel execution should be done using
 * {@link java.util.function.Function}, by iterating over the list of the
 * objects with the data of the task. Also the objects that perform the
 * execution should be refactored to implement
 * {@link java.util.function.Function} and execute the code over the data
 * object.
 */
public interface ThreadOwner {

    public void notifyDeath(Worker workerDead);

}
