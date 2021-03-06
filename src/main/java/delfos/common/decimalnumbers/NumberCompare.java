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
package delfos.common.decimalnumbers;

import static delfos.Constants.COMPARE_NUM_DECIMALS;

/**
 *
 * @author jcastro-inf ( https://github.com/jcastro-inf )
 * @version 1.0 22-Mar-2013
 */
public class NumberCompare {

    /**
     * Compara dos valores, teniendo en cuenta sólo un determinado número de
     * decimales.
     *
     * @param n1 Número a comparar.
     * @param n2 Número a comparar.
     * @return true, si los valores son iguales hasta el decimal indicado, false
     * en otro caso.
     *
     * @throws IllegalArgumentException Si el número de decimales es negativo.
     */
    public static boolean equals(Number n1, Number n2) {

        if (COMPARE_NUM_DECIMALS < 0) {
            throw new IllegalArgumentException("Number of decimals can't be negative.");
        }

        double p1 = n1.doubleValue();
        double p2 = n2.doubleValue();

        double diff = p1 - p2;

        if (diff == 0) {
            return true;
        } else if (diff < Math.pow(10, -COMPARE_NUM_DECIMALS)) {
            return true;
        } else if (diff > Math.pow(10, -COMPARE_NUM_DECIMALS)) {
            final int value = (int) Math.pow(10, COMPARE_NUM_DECIMALS);

            p1 = (int) (p1 * value);
            p1 = p1 / value;

            p2 = (int) (p2 * value);
            p2 = p2 / value;

            return p1 == p2;
        } else if (Double.isNaN(n1.doubleValue()) && Double.isNaN(n2.doubleValue())) {
            return true;
        } else {
            throw new IllegalStateException("asdf");
        }
    }

    public static int compare(Number n1, Number n2) {
        if (COMPARE_NUM_DECIMALS < 0) {
            throw new IllegalArgumentException("Number of decimals can't be negative.");
        }

        double d1 = n1.doubleValue();
        double d2 = n2.doubleValue();

        double diff = d1 - d2;

        if (diff == 0) {
            return 0;
        } else if (diff < Math.pow(10, -COMPARE_NUM_DECIMALS)) {
            return 0;
        } else if (diff > Math.pow(10, -COMPARE_NUM_DECIMALS)) {
            final int value = (int) Math.pow(10, COMPARE_NUM_DECIMALS);

            int truncatedN1 = (int) (d1 * value);
            truncatedN1 = truncatedN1 / value;

            int truncatedN2 = (int) (d2 * value);
            truncatedN2 = truncatedN2 / value;

            return Integer.compare(truncatedN1, truncatedN2);
        } else if (Double.isNaN(n1.doubleValue()) && Double.isNaN(n2.doubleValue())) {
            return 0;
        } else {
            throw new IllegalStateException("asdf");
        }
    }
}
