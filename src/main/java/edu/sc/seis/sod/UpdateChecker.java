/**
 * UpdateChecker.java
 *
 * @author Philip Crotwell
 */

package edu.sc.seis.sod;

import edu.sc.seis.sod.util.thread.UpdateCheckerJob;
import edu.sc.seis.sod.util.thread.WorkerThreadPool;

public class UpdateChecker  {

    public UpdateChecker(boolean gui) {
        UpdateCheckerJob job = new UpdateCheckerJob("SOD update checker",
                                                    "SOD",
                                                    VersionHistory.current().getVersion(),
                                                    updateURL,
                                                    gui,
                                                    forceCheck);
        job.setUsePreferencesForStorage(false); // sod doesn't ask, so check on each startup
        WorkerThreadPool.getDefaultPool().invokeLater(job);
    }

    public static final String updateURL = "http://www.seis.sc.edu/SOD/UpdateChecker.xml";

    public static boolean forceCheck = false;
}

