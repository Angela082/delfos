package delfos.experiment.casestudy.cluster;

import java.io.File;
import java.util.List;
import delfos.dataset.basic.rating.Rating;
import delfos.dataset.basic.loader.types.DatasetLoader;
import delfos.experiment.casestudy.CaseStudy;
import delfos.group.casestudy.GroupCaseStudy;

/**
 *
 * @version 19-jun-2014
* @author Jorge Castro Gallardo
 */
public interface ExperimentPreparator {

    public void prepareExperiment(File folder, List<CaseStudy> caseStudies, DatasetLoader< ? extends Rating> datasetLoader);

    public void prepareGroupExperiment(File experimentBaseFolder, List<GroupCaseStudy> groupCaseStudies, DatasetLoader<? extends Rating>... datasetLoader);
}