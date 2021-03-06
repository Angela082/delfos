package delfos.dataset.basic.ratingsdataset;

import delfos.dataset.basic.rating.Rating;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import org.junit.Test;

/**
 *
 * @version 01-jul-2014
 * @author jcastro-inf ( https://github.com/jcastro-inf )
 */
public class RatingTest {

    public RatingTest() {
    }

    /**
     * Test of compareTo method, of class Rating.
     */
    @Test
    public void testCompareTo() {
        Rating o = new Rating(1, 2, 3);
        Rating instance = new Rating(1, 2, 3.0);
        int expResult = 0;
        int result = instance.compareTo(o);
        assertEquals(expResult, result);
    }

    /**
     * Test of equals method, of class Rating.
     */
    @Test
    public void testEquals() {
        Object obj = new Rating(1, 2, 3);
        Rating instance = new Rating(1, 2, 3.0);
        boolean expResult = true;
        boolean result = instance.equals(obj);
        assertEquals(expResult, result);
    }

    /**
     * Test of hashCode method, of class Rating.
     */
    @Test
    public void testHashCode() {
        Rating instance = new Rating(1, 2, 3.0);
        int expResult = 1074315502;
        int result = instance.hashCode();
        assertEquals(expResult, result);
    }

    @Test
    public void testClone() throws CloneNotSupportedException {

        Rating instance = new Rating(1, 2, 3);
        Rating expResult = new Rating(1, 2, 3.0);

        Rating result = instance.clone();

        assertEquals(expResult, result);

        assertTrue(instance.clone().equals(instance));
        assertTrue(instance != instance.clone());
    }

    @Test
    public void testEqualsCompareHashCode() {

        Rating rating = new Rating(1, 2, 3);

        Rating rating_equal = new Rating(1, 2, 3);

        Rating rating_different = new Rating(1, 2, 4);

        {
            assertEquals(true, rating.equals(rating_equal));
            assertEquals(rating.hashCode(), rating_equal.hashCode());
            assertEquals(0, rating.compareTo(rating_equal));
        }

    }
}
