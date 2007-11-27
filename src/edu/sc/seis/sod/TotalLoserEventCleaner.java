package edu.sc.seis.sod;

import java.util.Timer;
import java.util.TimerTask;

import org.hibernate.Query;

import edu.iris.Fissures.model.MicroSecondDate;
import edu.iris.Fissures.model.TimeInterval;
import edu.sc.seis.fissuresUtil.cache.CacheEvent;
import edu.sc.seis.fissuresUtil.chooser.ClockUtil;
import edu.sc.seis.fissuresUtil.exceptionHandler.GlobalExceptionHandler;
import edu.sc.seis.sod.hibernate.StatefulEvent;
import edu.sc.seis.sod.hibernate.StatefulEventDB;

/**
 * This task runs immediately on instantiation and then once a week after that.
 * It removes all events that were failed in the eventArm by a subsetter from
 * the database on each run.
 * 
 * @author groves
 * 
 * Created on Dec 28, 2006
 */
public class TotalLoserEventCleaner extends TimerTask {

    public TotalLoserEventCleaner(TimeInterval lag) {
        this.lagInterval = lag;
        Timer t = new Timer(true);
        t.schedule(this, 0, ONE_WEEK);
        eventdb = new StatefulEventDB();
    }

    public void run() {
        try {
            logger.debug("Working");
            MicroSecondDate ageAgo = ClockUtil.now().subtract(lagInterval);
            Query q = eventdb.getSession().createQuery("delete from "+StatefulEvent.class.getName()+" where statusAsShort = 258 and preferred.originTime.time < :ageAgo");
            q.setTimestamp("ageAgo", ageAgo.getTimestamp());
            int num = q.executeUpdate();
            eventdb.commit();
            logger.debug("Done, deleted "+num+" events.");
        } catch(Throwable e) {
            try {
                eventdb.rollback();
            } catch(Throwable e1) {
                GlobalExceptionHandler.handle(e1);
            }
            GlobalExceptionHandler.handle(e);
        }
    }
    
    TimeInterval lagInterval ;
    StatefulEventDB eventdb;

    private static final org.apache.log4j.Logger logger = org.apache.log4j.Logger.getLogger(TotalLoserEventCleaner.class);

    private static final long ONE_WEEK = 7 * 24 * 60 * 60 * 1000;
}
