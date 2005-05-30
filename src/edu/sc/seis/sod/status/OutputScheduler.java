/**
 * TimerWrapper.java
 * 
 * @author Charles Groves
 */
package edu.sc.seis.sod.status;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;
import org.apache.log4j.Logger;
import edu.iris.Fissures.model.TimeInterval;
import edu.iris.Fissures.model.UnitImpl;
import edu.sc.seis.fissuresUtil.exceptionHandler.GlobalExceptionHandler;
import edu.sc.seis.sod.Arm;

public class OutputScheduler extends Thread {

    protected OutputScheduler() {}
    
    public void registerArm(Arm arm) {
        synchronized(arms) {
            arms.add(arm);
        }
    }

    public void schedule(Runnable a) {
        synchronized(runnables) {
            runnables.add(a);
        }
    }

    public void scheduleForExit(Runnable a) {
        synchronized(onExitRunnables) {
            onExitRunnables.add(a);
        }
    }

    public void run() {
        // initial sleep before first set of status pages
        try {
            Thread.sleep(INITIAL_SLEEP);
        } catch(InterruptedException e) {}
        while(true) {
            runAll(runnables);
            if(!anyArmsActive()) {
                runAll(runnables);
                runAll(onExitRunnables);
                logger.debug("Output Scheduler done.");
                String lcOSName = System.getProperty("os.name").toLowerCase();
                boolean MAC_OS_X = lcOSName.startsWith("mac os x");
                if(MAC_OS_X) {
                    // hopefully everything is done!
                    System.out.println("Using System.exit(0) only on the mac due to AWT thread not exiting.");
                    logger.info("Using System.exit(0) only on the mac due to AWT thread not exiting.");
                    System.exit(0);
                }
                return;
            }
            try {
                synchronized(this) {
                    wait(ACTION_INTERVAL_MILLIS);
                }
            } catch(InterruptedException e) {}
        }
    }

    private void runAll(Set runnables) {
        Runnable[] currentRunnables = new Runnable[0];
        synchronized(runnables) {
            currentRunnables = (Runnable[])runnables.toArray(currentRunnables);
            runnables.clear();
        }
        for(int i = 0; i < currentRunnables.length; i++) {
            try {
                currentRunnables[i].run();
            } catch(Throwable t) {
                GlobalExceptionHandler.handle(t);
            }
        }
    }

    private boolean anyArmsActive() {
        Arm[] curArms = new Arm[0];
        synchronized(arms) {
            curArms = (Arm[])arms.toArray(curArms);
        }
        boolean active = false;
        for(int i = 0; i < curArms.length; i++) {
            if (curArms[i].isActive()) {
                active = true;
                break;
            }
        }
        return active;
    }
    
    public synchronized static OutputScheduler getDefault() {
        if(DEFAULT == null) {
            DEFAULT = new OutputScheduler();
            DEFAULT.start();
        }
        return DEFAULT;
    }

    private static final TimeInterval ACTION_INTERVAL = new TimeInterval(2,
                                                                         UnitImpl.MINUTE);

    private static final long ACTION_INTERVAL_MILLIS = (long)ACTION_INTERVAL.convertTo(UnitImpl.MILLISECOND)
            .get_value();

    private static final long INITIAL_SLEEP = 10000l;

    private static OutputScheduler DEFAULT = null;
    
    private Set arms = Collections.synchronizedSet(new HashSet());

    private Set runnables = Collections.synchronizedSet(new HashSet());

    private Set onExitRunnables = Collections.synchronizedSet(new HashSet());

    private static final Logger logger = Logger.getLogger(OutputScheduler.class);
}
