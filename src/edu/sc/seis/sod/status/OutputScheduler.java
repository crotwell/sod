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
        System.out.println("ACTION INTERVAL IS " + ACTION_INTERVAL_MILLIS);
        t.schedule(this, 10000, ACTION_INTERVAL_MILLIS);
    }

    public void schedule(Runnable a){ runnables.add(a); }

    public void run() {
        System.out.println("RUNNING OUTPUTSCHEDULER");
        runAll();
        if(Start.getWaveformArm() != null && Start.getWaveformArm().isFinished()){
            System.out.println("WAVEFORMARM IS DEAD");
            runAll();
            t.cancel();
            System.out.println("CANCELLDED!!");
        }
    }

    private void runAll(){
        Runnable[] currentRunnables = new Runnable[0];
        synchronized(runnables){
            currentRunnables = (Runnable[])runnables.toArray(currentRunnables);
            runnables.clear();
        }
        System.out.println("RUNNING ALL, OR " + currentRunnables.length + " RUNNABLES");
        for (int i = 0; i < currentRunnables.length; i++) {
            System.out.println(currentRunnables[i]);
            currentRunnables[i].run();
        }
        System.out.println("DONE");
    }

    private static final TimeInterval ACTION_INTERVAL = new TimeInterval(30, UnitImpl.SECOND);
    private static final long ACTION_INTERVAL_MILLIS = (long)ACTION_INTERVAL.convertTo(UnitImpl.MILLISECOND).get_value();
    public static OutputScheduler DEFAULT = new OutputScheduler();

    private Set runnables = Collections.synchronizedSet(new HashSet());
    private Timer t = new Timer();
}
