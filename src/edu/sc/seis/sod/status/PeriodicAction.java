/**
 * PeriodicAction.java
 *
 * @author Created by Omnicore CodeGuide
 */

package edu.sc.seis.sod.status;

import edu.iris.Fissures.model.MicroSecondDate;
import edu.iris.Fissures.model.TimeInterval;
import edu.iris.Fissures.model.UnitImpl;
import edu.sc.seis.fissuresUtil.chooser.ClockUtil;
import edu.sc.seis.fissuresUtil.exceptionHandler.GlobalExceptionHandler;
import java.util.Timer;
import java.util.TimerTask;

public abstract class PeriodicAction{
    public abstract void act();

    public void actIfPeriodElapsed(){
        synchronized(schedulingLock) {
            if(!ClockUtil.now().subtract(lastAct).lessThan(TWO_MINUTES)){
                actNow();
            }else if(!scheduled){
                t.schedule(new ScheduledActor(), 120000);
                scheduled = true;
            }
        }
    }

    private class ScheduledActor extends TimerTask{
        public void run() { actNow();  }
    }

    private void actNow(){
        try{
            act();
        }catch(Throwable t){
            GlobalExceptionHandler.handle("Trouble running periodic action", t);
        }
        synchronized(schedulingLock){
            lastAct = ClockUtil.now();
            scheduled = false;
        }
    }

    private boolean scheduled = false;
    protected boolean v = false;
    private static final TimeInterval TWO_MINUTES = new TimeInterval(2, UnitImpl.MINUTE);
    private MicroSecondDate lastAct = ClockUtil.now().subtract(TWO_MINUTES);
    private Object schedulingLock = new Object();
    private static Timer t = new Timer();
}

