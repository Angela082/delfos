package delfos.group.factories;

import delfos.factories.Factory;
import delfos.group.grs.filtered.filters.GroupRatingsFilter;
import delfos.group.grs.filtered.filters.NoFilter;
import delfos.group.grs.filtered.filters.OutliersItemsStandardDeviationThresholdFilter;
import delfos.group.grs.filtered.filters.OutliersItemsStandardDeviationTopPercentFilter;
import delfos.group.grs.filtered.filters.OutliersRatingsFilter;
import delfos.group.grs.filtered.filters.OutliersRatingsStandardDeviationFilter;

/**
 * Conoce las técnicas de filtrado de valoraciones para la recomendación a
 * grupos y las proveee.
 *
* @author Jorge Castro Gallardo
 *
 * @version 1.0 09-Mayo-2013
 */
public class GroupRatingsFilterFactory extends Factory<GroupRatingsFilter> {

    private static final GroupRatingsFilterFactory instance;

    static {
        instance = new GroupRatingsFilterFactory();

        instance.addClass(NoFilter.class);

        instance.addClass(OutliersRatingsFilter.class);
        instance.addClass(OutliersRatingsStandardDeviationFilter.class);

        instance.addClass(OutliersItemsStandardDeviationThresholdFilter.class);
        instance.addClass(OutliersItemsStandardDeviationTopPercentFilter.class);
    }

    public static GroupRatingsFilterFactory getInstance() {
        return instance;
    }
}