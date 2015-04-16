package delfos.group.experiment.validation.groupformation;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Random;
import java.util.Set;
import java.util.TreeSet;
import delfos.common.exceptions.dataset.CannotLoadRatingsDataset;
import delfos.common.parameters.Parameter;
import delfos.common.parameters.restriction.IntegerParameter;
import delfos.common.parameters.restriction.ParameterOwnerRestriction;
import delfos.dataset.basic.rating.Rating;
import delfos.dataset.basic.loader.types.DatasetLoader;
import delfos.group.groupsofusers.GroupOfUsers;
import delfos.group.groupsofusers.GroupOfUsers;
import delfos.similaritymeasures.PearsonCorrelationCoefficient;
import delfos.similaritymeasures.useruser.UserUserSimilarity;
import delfos.similaritymeasures.useruser.UserUserSimilarityWrapper;
import delfos.similaritymeasures.useruser.UserUserSimilarityWrapper_relevanceFactor;

/**
 * Crea grupos buscando similitudes entre los miembros, excepto cierto numero de
 * miembros. Los grupos no tienen usuarios en común, es decir, la intersección
 * entre cualquier par de grupos generados (de una vez) es siempre el conjunto
 * vacío.
 *
 * @version 25-Junio-2014
* @author Jorge Castro Gallardo
 */
public class SimilarMembers_except extends GroupFormationTechnique {

    static {
        UserUserSimilarity defaultSimilarity = new UserUserSimilarityWrapper_relevanceFactor(new UserUserSimilarityWrapper(new PearsonCorrelationCoefficient()), 5);

        ParameterOwnerRestriction parameterOwnerRestriction = new ParameterOwnerRestriction(
                UserUserSimilarity.class,
                defaultSimilarity);
        SIMILARITY_MEASURE = new Parameter(
                "SIMILARITY_MEASURE",
                parameterOwnerRestriction);
    }

    /**
     * Parámetro para establecer el número de usuarios que tendrán los grupos
     * generados con esta validación de grupos
     */
    public static final Parameter NUM_GROUPS_PARAMETER = new Parameter("numGroups", new IntegerParameter(1, 1000000, 5));
    public static final Parameter GROUP_SIZE_PARAMETER = new Parameter("groupSize", new IntegerParameter(1, 10000, 5));
    public static final Parameter GROUP_SIZE_DIFFERENT_MEMBERS_PARAMETER = new Parameter("groupSize_differentMembers", new IntegerParameter(1, 10000, 1));
    public static final Parameter N_CANDIDATES_PARAMETER = new Parameter("numCandidates", new IntegerParameter(1, 1000000, 5));
    public static final Parameter SIMILARITY_MEASURE;

    /**
     * Genera una validación de usuarios que genera grupos de tamaño fijo. Por
     * defecto, el tamaño de los grupos es de cuatro miembros.
     */
    public SimilarMembers_except() {
        super();
        addParameter(GROUP_SIZE_PARAMETER);
        addParameter(GROUP_SIZE_DIFFERENT_MEMBERS_PARAMETER);
        addParameter(NUM_GROUPS_PARAMETER);
        addParameter(N_CANDIDATES_PARAMETER);
        addParameter(SIMILARITY_MEASURE);

        addParammeterListener(() -> {
            int numGroupsValue = (Integer) getParameterValue(NUM_GROUPS_PARAMETER);
            int groupSizeValue = (Integer) getParameterValue(GROUP_SIZE_PARAMETER);
            int groupSize_differentMembers = (Integer) getParameterValue(GROUP_SIZE_DIFFERENT_MEMBERS_PARAMETER);

            String oldAlias = getAlias();
            String newAlias = this.getClass().getSimpleName() + "(num=" + numGroupsValue + " size=" + groupSizeValue + " diff=" + groupSize_differentMembers + ")";

            if (!oldAlias.equals(newAlias)) {
                setAlias(newAlias);
            }
        });
    }

    /**
     * Genera una técnica de generación de grupos que genera
     * <b>numGroupsValue</b> de tamaño <b>groupSizeValue</b> y con
     *
     * @param groupSizeValue Tamaño de los grupos generados
     * @param numGroupsValue Número de grupos considerados
     * @param groupSize_differentMembers
     */
    public SimilarMembers_except(int numGroupsValue, int groupSizeValue, int groupSize_differentMembers) {
        this();

        if (groupSizeValue < groupSize_differentMembers + 2) {
            throw new IllegalStateException("There must be at least 2 members similar ( groupSizeValue <= groupSize_differentMembers+2 )");
        }

        setParameterValue(NUM_GROUPS_PARAMETER, numGroupsValue);
        setParameterValue(GROUP_SIZE_PARAMETER, groupSizeValue);
        setParameterValue(N_CANDIDATES_PARAMETER, 20);
        setParameterValue(GROUP_SIZE_DIFFERENT_MEMBERS_PARAMETER, groupSize_differentMembers);
    }

    @Override
    public Collection<GroupOfUsers> shuffle(DatasetLoader<? extends Rating> datasetLoader) throws CannotLoadRatingsDataset {
        if (datasetLoader == null) {
            throw new IllegalStateException("The datasetLoader is null.");
        }

        Random random = new Random(getSeedValue());

        final int numMembersCandidate = (Integer) getParameterValue(N_CANDIDATES_PARAMETER);
        final int groupSize = (Integer) getParameterValue(GROUP_SIZE_PARAMETER);
        final int numGroups = (Integer) getParameterValue(NUM_GROUPS_PARAMETER);

        final int numMembersDifferent = (Integer) getParameterValue(GROUP_SIZE_DIFFERENT_MEMBERS_PARAMETER);
        final int numMembersSimilar = groupSize - numMembersDifferent;

        final int maximumGroups = datasetLoader.getRatingsDataset().allUsers().size() / groupSize;

        if (maximumGroups < numGroups) {
            throw new IllegalArgumentException("The number of groups * groupSize exceed the number of users (" + numGroups + " * " + groupSize + " > " + datasetLoader.getRatingsDataset().allUsers().size());
        }

        if (datasetLoader == null) {
            throw new IllegalStateException("The datasetLoader is null.");
        }
        UserUserSimilarity similarityMeasure = (UserUserSimilarity) getParameterValue(SIMILARITY_MEASURE);

        SimilarMembers similarMembers = new SimilarMembers(numGroups, numMembersSimilar, numMembersCandidate);
        similarMembers.setSeedValue(getSeedValue());
        similarMembers.setParameterValue(SimilarMembers.SIMILARITY_MEASURE, similarityMeasure);
        similarMembers.addListener((String message, int progress) -> {
            progressChanged(message, progress);
        });

        Collection<GroupOfUsers> groupsOfSimilarMembers = similarMembers.shuffle(datasetLoader);

        ArrayList<Integer> usersRemainToSelect = new ArrayList<>(datasetLoader.getRatingsDataset().allUsers());
        groupsOfSimilarMembers.stream().forEach((group) -> {
            usersRemainToSelect.removeAll(group.getGroupMembers());
        });

        Collection<GroupOfUsers> ret = new ArrayList<>(numGroups);

        //A cada grupo le añado los usuarios no similares.
        for (GroupOfUsers group : groupsOfSimilarMembers) {
            Set<Integer> usersGrupoActual = new TreeSet<>(group.getGroupMembers());

            while (usersGrupoActual.size() < groupSize) {
                int index = random.nextInt(usersRemainToSelect.size());
                Integer idUser = usersRemainToSelect.remove(index);
                usersGrupoActual.add(idUser);
            }
            ret.add(new GroupOfUsers(usersGrupoActual));
        }

        return ret;
    }
}
