package delfos.group.factories;

import delfos.factories.Factory;
import delfos.group.experiment.validation.validationtechniques.CrossFoldValidation_Items;
import delfos.group.experiment.validation.validationtechniques.CrossFoldValidation_Ratings;
import delfos.group.experiment.validation.validationtechniques.GroupValidationTechnique;
import delfos.group.experiment.validation.validationtechniques.HoldOutGroupMemberRatings;
import delfos.group.experiment.validation.validationtechniques.HoldOutGroupRatedItems;
import delfos.group.experiment.validation.validationtechniques.NoValidation;

/**
 * Conoce las técnicas de generación de grupos que se utilizarán en los casos de
 * estudio para evaluar sistemas de recomendación a grupos.
 *
 * @author Jorge Castro Gallardo
 *
 * @version 1.0 16-Jan-2013
 * @version 9-Enero-2013 Adaptado a la nueva declaración de las factorías.
 */
public class GroupValidationTechniquesFactory extends Factory<GroupValidationTechnique> {

    protected static final GroupValidationTechniquesFactory instance;

    public static GroupValidationTechniquesFactory getInstance() {
        return instance;
    }

    static {
        instance = new GroupValidationTechniquesFactory();

        instance.addClass(CrossFoldValidation_Items.class);
        instance.addClass(CrossFoldValidation_Ratings.class);
        instance.addClass(HoldOutGroupRatedItems.class);
        instance.addClass(HoldOutGroupMemberRatings.class);
        instance.addClass(NoValidation.class);
    }

    protected GroupValidationTechniquesFactory() {
    }
}