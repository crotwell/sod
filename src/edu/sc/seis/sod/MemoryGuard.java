package edu.sc.seis.sod;

import java.util.ArrayList;
import java.util.Timer;
import java.util.TimerTask;
import com.yourkit.api.Controller;
import edu.iris.Fissures.model.MicroSecondDate;
import edu.sc.seis.fissuresUtil.exceptionHandler.GlobalExceptionHandler;

public class MemoryGuard extends TimerTask {

    public MemoryGuard() {
        Timer t = new Timer(true);
        MicroSecondDate startTime = new MicroSecondDate();
        t.schedule(this, startTime, FIFTEEN_SECONDS);
    }

    public void run() {
        logger.debug("running garbage collector");
        System.gc();
        logger.debug("checking memory usage");
        long memoryUsed = (Runtime.getRuntime().totalMemory() - Runtime.getRuntime()
                .freeMemory())
                / MB;
        logger.debug("memory used right now: " + memoryUsed + " MB");
        if(memoryUsed > 400) {
            takeSnapShot();
        } else {
            consecutiveSnaps = 0;
        }
    }

    private void takeSnapShot() {
        try {
            Controller controller = new Controller();
            String snapshotFileName;
            snapshotFileName = controller.captureMemorySnapshot();
            logger.debug("Captured memory snapshot to file " + snapshotFileName);
            try {
                Start.getResultMailer().mail("took memory snapshot",
                                             "snapshot is located at "
                                                     + snapshotFileName,
                                             new ArrayList());
            } catch(ConfigurationException confex) {
                GlobalExceptionHandler.handle("problem getting result mailer.  make sure it's configured",
                                              confex);
            }
            consecutiveSnaps++;
            if(consecutiveSnaps > 3) {
                logger.fatal("I've been nervous long enough.  Exiting now.");
                System.exit(0);
            }
        } catch(Exception ex) {
            GlobalExceptionHandler.handle("Cannot capture snapshot. Reason: "
                    + ex.getMessage(), ex);
        }
    }

    private static int consecutiveSnaps = 0;

    private static final int FIFTEEN_SECONDS = 15 * 1000;

    private static final long MB = 1024 * 1024;

    private static final org.apache.log4j.Logger logger = org.apache.log4j.Logger.getLogger(MemoryGuard.class);
}
