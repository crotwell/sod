/**
 * TimerWrapper.java
 *
 * @author Charles Groves
 */

package edu.sc.seis.sod.status;

import edu.iris.Fissures.model.TimeInterval;
import edu.iris.Fissures.model.UnitImpl;
import edu.sc.seis.fissuresUtil.chooser.ClockUtil;
import edu.sc.seis.fissuresUtil.exceptionHandler.GlobalExceptionHandler;
import edu.sc.seis.sod.Start;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;
import java.util.Timer;
import java.util.TimerTask;
import org.apache.log4j.Logger;

public class OutputScheduler extends Thread{
    protected OutputScheduler(){
    }

    public void schedule(Runnable a){
        synchronized(runnables){
            runnables.add(a);
        }
    }

    public void scheduleForExit(Runnable a) {
        synchronized(onExitRunnables){
            onExitRunnables.add(a);
        }
    }

    public void run() {
        // initial sleep before first set of status pages
        try {
            Thread.sleep(INITIAL_SLEEP);
        } catch (InterruptedException e) {}

        while(true) {
            runAll(runnables);
            if(Start.getWaveformArm() != null && Start.getWaveformArm().isFinished()){
                runAll(runnables);
                runAll(onExitRunnables);
                logger.debug("Output Scheduler done.");


                String lcOSName = System.getProperty("os.name").toLowerCase();
                boolean MAC_OS_X = lcOSName.startsWith("mac os x");
                if (MAC_OS_X) {
                    // hopefully everything is done!
                    System.out.println("Using System.exit(0) only on the mac due to AWT thread not exiting.");
                    logger.info("Using System.exit(0) only on the mac due to AWT thread not exiting.");
                    System.exit(0);
                }
                return;
            }

            try {
                sleep(ACTION_INTERVAL_MILLIS);
            } catch (InterruptedException e) {}
        }
    }

    private void runAll(Set runnables){
        Runnable[] currentRunnables = new Runnable[0];
        synchronized(runnables){
            currentRunnables = (Runnable[])runnables.toArray(currentRunnables);
            runnables.clear();
        }
        for (int i = 0; i < currentRunnables.length; i++) {
            try{
                currentRunnables[i].run();
            }catch(Throwable t){
                GlobalExceptionHandler.handle(t);
            }
        }
    }

    public synchronized static OutputScheduler getDefault() {
        if (DEFAULT == null) {
            DEFAULT = new OutputScheduler();
            DEFAULT.start();
        }
        return DEFAULT;
    }

    private static final TimeInterval ACTION_INTERVAL = new TimeInterval(2, UnitImpl.MINUTE);
    private static final long ACTION_INTERVAL_MILLIS = (long)ACTION_INTERVAL.convertTo(UnitImpl.MILLISECOND).get_value();
    private static final long INITIAL_SLEEP = 10000l;
    private static OutputScheduler DEFAULT = null;

    private Set runnables = Collections.synchronizedSet(new HashSet());
    private Set onExitRunnables = Collections.synchronizedSet(new HashSet());
    private Timer t = new Timer();

    private static final Logger logger = Logger.getLogger(OutputScheduler.class);

}
