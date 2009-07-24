package edu.sc.seis.sod.status;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

import org.apache.log4j.Logger;

import edu.iris.Fissures.model.TimeInterval;
import edu.iris.Fissures.model.UnitImpl;
import edu.sc.seis.fissuresUtil.database.ConnMgr;
import edu.sc.seis.fissuresUtil.exceptionHandler.GlobalExceptionHandler;
import edu.sc.seis.sod.Arm;
import edu.sc.seis.sod.ArmListener;
import edu.sc.seis.sod.ConfigurationException;
import edu.sc.seis.sod.NetworkArm;
import edu.sc.seis.sod.PeriodicCheckpointer;
import edu.sc.seis.sod.Start;

public class OutputScheduler extends Thread implements ArmListener {

    private OutputScheduler() {
        super("OutputScheduler");
        Start.add(this);
    }

    public void starting(Arm arm) {
        synchronized(arms) {
            arms.add(arm);
            // Wake up when we get our first arm
            if(arms.size() == 1) {
                synchronized(this) {
                    notify();
                }
            }
        }
        if(arm instanceof NetworkArm) {
            ((NetworkArm)arm).add(this);
        }
    }

    public void finished(Arm arm) {
        synchronized(this) {
            notifyAll();
        }
    }

    public void started() throws ConfigurationException {}

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
        // Waiting for the initial arm to come into starting and wake us
        try {
            synchronized(this) {
                wait();
            }
        } catch(InterruptedException e) {}
        while(true) {
            runAll(runnables);
            if(!anyArmsActive()) {
                runAll(runnables);
                runAll(onExitRunnables);
                if (Start.getRunProps().checkpointPeriodically()) {
                    new PeriodicCheckpointer().run();
                }
                logger.debug("Output Scheduler done.");
                logger.info("Lo!  I am weary of my wisdom, like the bee that hath gathered too much\n"
                            + "honey; I need hands outstretched to take it.");
                String lcOSName = System.getProperty("os.name").toLowerCase();
                boolean MAC_OS_X = lcOSName.startsWith("mac os x");
                if(MAC_OS_X) {
                    // hopefully everything is done!
                    try {
                        Connection conn = ConnMgr.createConnection();
                        conn.createStatement().execute("shutdown");
                        conn.close();
                    } catch(SQLException e) {
                        GlobalExceptionHandler.handle(e);
                    }
                    logger.debug("Using System.exit(0) only on the mac due to AWT thread not exiting.");
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

    private void runAll(Set toRun) {
        Runnable[] currentRunnables = new Runnable[0];
        synchronized(toRun) {
            currentRunnables = (Runnable[])toRun.toArray(currentRunnables);
            toRun.clear();
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
            if(curArms[i].isActive()) {
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

    private static OutputScheduler DEFAULT = null;

    private Set arms = Collections.synchronizedSet(new HashSet());

    private Set runnables = Collections.synchronizedSet(new HashSet());

    private Set onExitRunnables = Collections.synchronizedSet(new HashSet());

    private static final Logger logger = Logger.getLogger(OutputScheduler.class);
}
