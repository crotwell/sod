/**
 * UpdateChecker.java
 *
 * @author Philip Crotwell
 */

package edu.sc.seis.sod;

import edu.sc.seis.fissuresUtil.cache.JobTracker;
import edu.sc.seis.fissuresUtil.cache.WorkerThreadPool;
import edu.sc.seis.fissuresUtil.chooser.UpdateCheckerJob;

public class UpdateChecker  {

    public UpdateChecker(boolean gui) {
        UpdateCheckerJob job = new UpdateCheckerJob("SOD update checker",
                                                    "SOD",
                                                    "1.0",
                                                    updateURL,
                                                    gui,
                                                    forceCheck);
        JobTracker.getTracker().add(job);
        WorkerThreadPool.getDefaultPool().invokeLater(job);
    }

    public static final String updateURL = "http://www.seis.sc.edu/SOD/UpdateChecker.xml";

    public static boolean forceCheck = false;
}

