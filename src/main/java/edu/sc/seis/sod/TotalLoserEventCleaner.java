package edu.sc.seis.sod;

import java.util.Iterator;
import java.util.TimerTask;

import org.hibernate.Query;

import edu.iris.Fissures.model.MicroSecondDate;
import edu.iris.Fissures.model.TimeInterval;
import edu.iris.Fissures.model.UnitImpl;
import edu.sc.seis.fissuresUtil.chooser.ClockUtil;
import edu.sc.seis.fissuresUtil.exceptionHandler.GlobalExceptionHandler;
import edu.sc.seis.sod.hibernate.SodDB;
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
       // Timer t = new Timer(true);
       // t.schedule(this, 0, ONE_WEEK);
    }

    public void run() {
        try {
            logger.info("Working");
            MicroSecondDate ageAgo = ClockUtil.now().subtract(lagInterval);
            // stations will process slightly before channels, so give a little time
            // to avoid database forgein key violations
            TimeInterval littleSkip = new TimeInterval(10, UnitImpl.MINUTE);
            cleanEvents(ageAgo);
            StatefulEventDB.getSingleton().commit();
            logger.info("Cleaned events");
            ageAgo = ageAgo.add(littleSkip);
            
            cleanECP(ageAgo);
            SodDB.getSingleton().commit();
            logger.info("Cleaned event-channel pairs");
            cleanEVP(ageAgo);
            SodDB.getSingleton().commit();
            logger.info("Cleaned event-vector pairs");
            
            ageAgo = ageAgo.add(littleSkip);
            cleanESP(ageAgo);
            SodDB.getSingleton().commit();
            logger.info("Cleaned event-station pairs");
            
            ageAgo = ageAgo.add(littleSkip);
            cleanENP(ageAgo);
            SodDB.getSingleton().commit();
            logger.info("Cleaned event-network pairs");
        } catch(Throwable e) {
            try {
                StatefulEventDB.getSingleton().rollback();
            } catch(Throwable e1) {
                GlobalExceptionHandler.handle(e1);
            }
            GlobalExceptionHandler.handle(e);
        }
    }
    
    public static void cleanEvents(MicroSecondDate ageAgo) {
        Query q = StatefulEventDB.getSingleton().getSession().createQuery(" from "+StatefulEvent.class.getName()+
                                                                          " e  where e.status.standingInt = "+Standing.REJECT.getVal()+
                                                                          " and e.preferred.originTime.time < :ageAgo");
        q.setTimestamp("ageAgo", ageAgo.getTimestamp());
        Iterator<StatefulEvent> it = q.iterate();
        int counter=0;
        while(it.hasNext()) {
            StatefulEvent se = it.next();
            StatefulEventDB.getSingleton().getSession().delete(se);
            counter++;
        }
        StatefulEventDB.getSingleton().commit();
        logger.debug("Done, deleted "+counter+" events.");
    }
    
    public static void cleanESP(MicroSecondDate ageAgo) {
        clean(EventStationPair.class, ageAgo);
    }
    
    public static void cleanENP(MicroSecondDate ageAgo) {
        clean(EventNetworkPair.class, ageAgo);
    }
    
    public static void cleanECP(MicroSecondDate ageAgo) {
        clean(EventChannelPair.class, ageAgo);
    }

    public static void cleanEVP(MicroSecondDate ageAgo) {
        clean(EventVectorPair.class, ageAgo);
    }
    
    public static void clean(Class eventPairClass, MicroSecondDate ageAgo) {
        Query q = SodDB.getSingleton().getSession().createQuery("delete "+eventPairClass.getName()+
                                                                " ep where ep.status.standingInt = "+Standing.REJECT.getVal()+
                                                                " and ep.lastQuery < :ageAgo");
        q.setTimestamp("ageAgo", ageAgo.getTimestamp());
        int deleted = q.executeUpdate();
        SodDB.getSingleton().commit();
        logger.info("delete "+deleted+" old "+eventPairClass.getName());
    }
    
    TimeInterval lagInterval ;

    private static final org.slf4j.Logger logger = org.slf4j.LoggerFactory.getLogger(TotalLoserEventCleaner.class);

    private static final long ONE_WEEK = 7 * 24 * 60 * 60 * 1000;
}
