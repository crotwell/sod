/**
 * TimerWrapper.java
 *
 * @author Charles Groves
 */

package edu.sc.seis.sod.status;

import edu.iris.Fissures.model.TimeInterval;
import edu.iris.Fissures.model.UnitImpl;
import edu.sc.seis.sod.Start;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;
import java.util.Timer;
import java.util.TimerTask;

public class OutputScheduler extends TimerTask{
    public OutputScheduler(){
        t.schedule(this, 10000, ACTION_INTERVAL_MILLIS);
    }

    public void schedule(Runnable a){ runnables.add(a); }

    public void run() {
        runAll();
        if(Start.getWaveformArm() != null && Start.getWaveformArm().isFinished()){
            runAll();
            t.cancel();
        }
    }

    private void runAll(){
        Runnable[] currentRunnables = new Runnable[0];
        synchronized(runnables){
            currentRunnables = (Runnable[])runnables.toArray(currentRunnables);
            runnables.clear();
        }
        for (int i = 0; i < currentRunnables.length; i++) {
            currentRunnables[i].run();
        }
    }

    private static final TimeInterval ACTION_INTERVAL = new TimeInterval(2, UnitImpl.MINUTE);
    private static final long ACTION_INTERVAL_MILLIS = (long)ACTION_INTERVAL.convertTo(UnitImpl.MILLISECOND).get_value();
    public static OutputScheduler DEFAULT = new OutputScheduler();

    private Set runnables = Collections.synchronizedSet(new HashSet());
    private Timer t = new Timer();
}
