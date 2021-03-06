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
package delfos.rs.hybridtechniques;

import java.io.Serializable;

/**
 *
 * @author jcastro-inf ( https://github.com/jcastro-inf )
 * @version 1.0 28-May-2013
 */
public class HybridRecommendationModel implements Serializable {
    
    private static final long serialVersionUID = 114;

    private final Object[] model;

    protected HybridRecommendationModel() {
        model = null;
    }

    public HybridRecommendationModel(Object... model) {
        this.model = model;
    }

    public Object getModel(int index) {
        return model[index];
    }
}
