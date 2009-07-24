package edu.sc.seis.sod;

import java.util.Iterator;
import java.util.TimerTask;

import org.hibernate.Query;

import edu.iris.Fissures.model.MicroSecondDate;
import edu.iris.Fissures.model.TimeInterval;
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
        eventdb = StatefulEventDB.getSingleton();
       // Timer t = new Timer(true);
       // t.schedule(this, 0, ONE_WEEK);
    }

    public void run() {
        try {
            logger.debug("Working");
            MicroSecondDate ageAgo = ClockUtil.now().subtract(lagInterval);
            Query q = eventdb.getSession().createQuery(" from "+StatefulEvent.class.getName()+" e  where e.statusAsShort = 258 and e.preferred.originTime.time < :ageAgo");
            q.setTimestamp("ageAgo", ageAgo.getTimestamp());
            Iterator<StatefulEvent> it = q.iterate();
            int counter=0;
            while(it.hasNext()) {
                StatefulEvent se = it.next();
                eventdb.getSession().delete(se);
                counter++;
            }
            eventdb.commit();
            logger.debug("Done, deleted "+counter+" events.");
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
