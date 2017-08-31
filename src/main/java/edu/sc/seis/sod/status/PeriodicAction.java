/**
 * PeriodicAction.java
 *
 * @author Created by Omnicore CodeGuide
 */

package edu.sc.seis.sod.status;

import java.time.Duration;
import java.time.Instant;
import java.util.Timer;
import java.util.TimerTask;

import edu.sc.seis.sod.util.exceptionHandler.GlobalExceptionHandler;
import edu.sc.seis.sod.util.time.ClockUtil;

public abstract class PeriodicAction{
    public abstract void act();

    public void actIfPeriodElapsed(){
        synchronized(schedulingLock) {
            if(ClockUtil.now().minus(lastAct).greaterThan(ACTION_INTERVAL)){
                actNow();
            }else if(!scheduled){
                t.schedule(new ScheduledActor(), ACTION_INTERVAL_MILLIS);
                scheduled = true;
            }
        }
    }

    private class ScheduledActor extends TimerTask{
        public void run() { actNow();  }
    }

    private void actNow(){
        synchronized(schedulingLock){
            lastAct = ClockUtil.now();
            scheduled = false;
        }
        try{
            act();
        }catch(Throwable t){
            GlobalExceptionHandler.handle("Trouble running periodic action", t);
        }
    }

    private boolean scheduled = false;
    private static final Duration ACTION_INTERVAL = Duration.ofMinutes(2);
    private static final long ACTION_INTERVAL_MILLIS = ACTION_INTERVAL.toMillis();
    private Instant lastAct = ClockUtil.now().minus(ACTION_INTERVAL);
    private Object schedulingLock = new Object();
    private static Timer t = new Timer();
}

