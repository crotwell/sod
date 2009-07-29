package edu.sc.seis.sod.hibernate;

import java.sql.SQLException;

import org.apache.log4j.BasicConfigurator;
import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import org.hibernate.LockMode;

import edu.iris.Fissures.model.TimeInterval;
import edu.iris.Fissures.model.UnitImpl;
import edu.iris.Fissures.network.StationImpl;
import edu.sc.seis.fissuresUtil.hibernate.HibernateUtil;
import edu.sc.seis.fissuresUtil.hibernate.NetworkDB;
import edu.sc.seis.fissuresUtil.simple.TimeOMatic;
import edu.sc.seis.sod.EventChannelPair;
import edu.sc.seis.sod.TotalLoserEventCleaner;
import edu.sc.seis.sod.mock.MockECP;
import edu.sc.seis.sod.mock.MockStatefulEvent;


public class Play extends edu.sc.seis.fissuresUtil.hibernate.Play {
    

    public static void main(String[] args) throws SQLException {
        try {
            BasicConfigurator.configure();
            Logger.getRootLogger().setLevel(Level.INFO);
            HibernateUtil.setUpFromConnMgr(System.getProperties());
            Play mgr = new Play();
            TimeOMatic.start();
            String todo = args[2];
            System.out.println("arg is: " + todo);
            if ( ! mgr.doIt(todo)) {
                System.err.println("Unknown arg: " + todo);
            }
            TimeOMatic.print("end");
        } catch (Throwable t) {
            logger.error("big problem!", t);
        }
    }
    protected boolean doIt(String todo) throws Exception {
        if (super.doIt(todo)) {
            return true;
        }
        if (todo.equals("storeecp")) {
            storeECP();
        } else if (todo.equals("storeevent")) {
            storeStatefulEvent();
        } else if (todo.equals("cookie")) {
            testCookie();
        } else if (todo.equals("delse")) {
            new TotalLoserEventCleaner(new TimeInterval(1, UnitImpl.WEEK)).run();
        } else {
            return false;
        }
        return true;
    }
    
    public void testCookie() {
        SodDB sodDb = SodDB.getSingleton();
        EventChannelPair ecp = MockECP.getECP();
        NetworkDB netdb = NetworkDB.getSingleton();
        netdb.put(ecp.getChannel());
        StatefulEventDB evtdb = StatefulEventDB.getSingleton();
        evtdb.put(ecp.getEvent());
        sodDb.put(ecp);
        SodDB.commit();
    }
    
    protected void storeECP() {
        SodDB sodDb = SodDB.getSingleton();
        EventChannelPair ecp = MockECP.getECP();
        NetworkDB netdb = NetworkDB.getSingleton();
        netdb.put(ecp.getChannel());
        StatefulEventDB evtdb = StatefulEventDB.getSingleton();
        evtdb.put(ecp.getEvent());
        sodDb.put(ecp);
        SodDB.commit();
        SodDB.getSession().lock(ecp, LockMode.NONE);
        sodDb.getNumFailed(ecp.getEvent());
        sodDb.getAll(ecp.getEvent());
        sodDb.getEventsForStation((StationImpl)ecp.getChannel().getSite().getStation());
        sodDb.getFailed(ecp.getEvent());
        sodDb.getFailed((StationImpl)ecp.getChannel().getSite().getStation());
        sodDb.getFailed(ecp.getEvent(), (StationImpl)ecp.getChannel().getSite().getStation());
        sodDb.getNumFailed(ecp.getEvent());
        sodDb.getNumFailed((StationImpl)ecp.getChannel().getSite().getStation());
        sodDb.getNumFailed(ecp.getEvent(), (StationImpl)ecp.getChannel().getSite().getStation());
        sodDb.getNumRetry(ecp.getEvent());
        sodDb.getNumRetry((StationImpl)ecp.getChannel().getSite().getStation());
        sodDb.getNumRetry(ecp.getEvent(), (StationImpl)ecp.getChannel().getSite().getStation());
        sodDb.getNumSuccessful();
        sodDb.getNumSuccessful(ecp.getEvent());
        sodDb.getNumSuccessful(ecp.getEvent(), (StationImpl)ecp.getChannel().getSite().getStation());
        sodDb.getRetry(ecp.getEvent());
        sodDb.getRetry((StationImpl)ecp.getChannel().getSite().getStation());
        sodDb.getRetry(ecp.getEvent(), (StationImpl)ecp.getChannel().getSite().getStation());
    }
    
    protected void storeStatefulEvent() {
        StatefulEvent s = MockStatefulEvent.create();
        StatefulEventDB db = StatefulEventDB.getSingleton();
        db.put(s);
        db.commit();
    }
    
    private static final org.apache.log4j.Logger logger = org.apache.log4j.Logger.getLogger(Play.class);
}