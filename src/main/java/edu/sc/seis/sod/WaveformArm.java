package edu.sc.seis.sod;

import java.util.ArrayList;
import java.util.List;

import edu.iris.Fissures.IfEvent.NoPreferredOrigin;
import edu.iris.Fissures.model.MicroSecondDate;
import edu.iris.Fissures.model.TimeInterval;
import edu.iris.Fissures.model.UnitImpl;
import edu.iris.Fissures.network.NetworkAttrImpl;
import edu.iris.Fissures.network.NetworkIdUtil;
import edu.sc.seis.fissuresUtil.cache.EventUtil;
import edu.sc.seis.fissuresUtil.chooser.ClockUtil;
import edu.sc.seis.fissuresUtil.database.NotFound;
import edu.sc.seis.fissuresUtil.exceptionHandler.GlobalExceptionHandler;
import edu.sc.seis.fissuresUtil.hibernate.NetworkDB;
import edu.sc.seis.sod.hibernate.SodDB;
import edu.sc.seis.sod.hibernate.StatefulEvent;
import edu.sc.seis.sod.hibernate.StatefulEventDB;
import edu.sc.seis.sod.status.OutputScheduler;
import edu.sc.seis.sod.subsetter.EventEffectiveTimeOverlap;

public class WaveformArm extends Thread implements Arm {

    public WaveformArm(int nextProcessorNum, AbstractWaveformRecipe waveformRecipe) {
        super("WaveformArm " + nextProcessorNum);
        this.recipe = waveformRecipe;
        this.processorNum = nextProcessorNum;
    }


    boolean possibleToContinue() {
        return (Start.getEventArm() == null || Start.getEventArm().isActive())&& ! Start.isArmFailure();
    }
    
    public void run() {
        int noWorkLoopCounter = 0;
        logger.info("Starting WaveformArm");
        // wait on Network arm startup
        while( ! Start.getNetworkArm().isInitialStartupFinished()) {
            try { Thread.sleep(10);  } catch(InterruptedException e) { }
        }
        try {
            // on startup pull any old existing ecp, esp or enp to work on first
            SodDB.getSingleton().populateECPToDo();
            SodDB.getSingleton().populateESPToDo();
            SodDB.getSingleton().populateENPToDo();
            while( ! threadShouldExit) {
                AbstractEventPair next = getNext();
                while(next == null
                        && (possibleToContinue() || SodDB.getSingleton().isENPTodo() || SodDB.getSingleton().isESPTodo()
                                || SodDB.getSingleton().getNumWorkUnits(Standing.RETRY) != 0 || SodDB.getSingleton()
                                .getNumWorkUnits(Standing.IN_PROG) != 0  || SodDB.getSingleton()
                                .getNumWorkUnits(Standing.INIT) != 0)) {
                    if (noWorkLoopCounter > 10) {
                        noWorkLoopCounter=0;
                        logger.info("Processor waiting for work unit to show up: ptc= "+
                                possibleToContinue() 
                                +" enp="+ (SodDB.getSingleton().isENPTodo())  
                                +" esp="+ (SodDB.getSingleton().isESPTodo()) 
                                +" retry="+ (SodDB.getSingleton().getNumWorkUnits(Standing.RETRY) != 0)  
                                +" prog="+ (SodDB.getSingleton().getNumWorkUnits(Standing.IN_PROG) != 0 )  
                                +" init="+ (SodDB.getSingleton().getNumWorkUnits(Standing.INIT) != 0));
                    } else {
                        logger.debug("Processor waiting for work unit to show up ");
                    }
                    try {
                        // sleep, but wake up if eventArm does notifyAll()
                        //logger.debug("waiting on event arm");
                        if (Start.getEventArm() != null) {
                            synchronized (Start.getEventArm()) {
                                Start.getEventArm().notifyAll();
                            }
                            if(possibleToContinue()) {
                                synchronized(Start.getEventArm().getWaveformArmSync()) {
                                    // close db connection as we don't need it for next 2 minutes
                                    SodDB.rollback();
                                    // wake up every 2 minutes in case there is retrys to process
                                    Start.getEventArm().getWaveformArmSync().wait(2*60*1000);
                                }
                            }
                        }
                    } catch(InterruptedException e) {}
                    //logger.debug("done waiting on event arm");
                    next = getNext();
                    if (next == null && SodDB.getSingleton().getNumWorkUnits(Standing.INIT) > 0) {
                        logger.debug("next null, so try get from DB");
                        next = SodDB.getSingleton().getNextECP();
                    } else {
                        logger.debug("next null, not try get from DB "+(next == null) +" && "+ SodDB.getSingleton().getNumWorkUnits(Standing.INIT)+" > 0");
                        synchronized(Start.getEventArm().getWaveformArmSync()) {
                            // wake up every 2 seconds in case there is something to process
                            Start.getEventArm().getWaveformArmSync().wait(2*1000);
                        }
                    }
                }
                if(next == null) {
                    // lets sleep for a couple of seconds just to make sure
                    // in case another thread has the  last/only event-network
                    // and we can help out once the stations are retrieved
                    for (int i = 0; i < 5; i++) {
                        next = getNext();
                        if (next != null) {
                            break;
                        }
                        Thread.sleep(1000);
                    }
                }
                if(next != null) {
                    noWorkLoopCounter = 0;
                    processorStartWork();
                    try {
                        next.run();
                    } catch(Throwable t) {
                        SodDB.rollback();
                        next.update(t, Status.get(Stage.EVENT_CHANNEL_POPULATION, Standing.SYSTEM_FAILURE));
                    }
                    SodDB.commit();
                    processorFinishWork();
                } else {
                    // nothing to do in db, not possible to continue
                    logger.info("No work to do, quiting processing: " + possibleToContinue()
                            + " " + (SodDB.getSingleton().getNumWorkUnits(Standing.RETRY) != 0) + " "
                            + (SodDB.getSingleton().getNumWorkUnits(Standing.IN_PROG) != 0));
                    return;
                }
            }
        } catch(Throwable t) {
            // just in case...
            GlobalExceptionHandler.handle(t);
            Start.armFailure(this, t);
        } finally {
            active = false;
            synchronized(OutputScheduler.getDefault()) {
                OutputScheduler.getDefault().notifyAll();
            }
        }
    }

    
    public boolean isActive() {
        return active;
    }
    
    boolean active = true;
    
    protected static synchronized AbstractEventPair getNext() {
        double retryRandom = Math.random();
        AbstractEventChannelPair ecp = null;
        if(retryRandom < getRetryPercentage() ) {
            // try a retry
            ecp = SodDB.getSingleton().getNextRetryECPFromCache();
            if (ecp != null) {
                return ecp;
            }
        }
        if (retryRandom > ecpPercentage) {
            // random not in small, so try memory esp or enp first

            if (SodDB.getSingleton().isECPTodo()) {
                ecp = SodDB.getSingleton().getNextECPFromCache();
                if(ecp != null) {
                    ecp.update(Status.get(Stage.EVENT_CHANNEL_POPULATION, Standing.IN_PROG));
                    SodDB.commit();
                    ecp  = (AbstractEventChannelPair)SodDB.getSession().merge(ecp);
                    return ecp;
                }
            }
            if (SodDB.getSingleton().isESPTodo()) {
                EventStationPair esp = SodDB.getSingleton().getNextESPFromCache();
                if(esp != null) {
                    esp.update(Status.get(Stage.EVENT_CHANNEL_POPULATION, Standing.IN_PROG));
                    SodDB.commit();
                    esp  = (EventStationPair)SodDB.getSession().merge(esp);
                    return esp;
                }
            }
            // no e-station try e-network
            if (SodDB.getSingleton().isENPTodo()) {
                EventNetworkPair enp = SodDB.getSingleton().getNextENPFromCache();
                if(enp != null) {
                    enp.update(Status.get(Stage.EVENT_CHANNEL_POPULATION, Standing.IN_PROG));
                    SodDB.commit();
                    // reattach to new session
                    enp = (EventNetworkPair)SodDB.getSession().merge(enp);
                    return enp;
                }
            }
        }
        // normal operation is to get esp from the db, then process all ecp within the station
        // instead of going to the database for each ecp. So only need to check for ecps occassionally
        // mainly to pick up orphans in the event of a crash
        // we do this  by trying if the random is less than the ecpPercentage or if we have
        // have found an ecp in the db within the last ECP_WINDOW
        // this cuts down on useless db acccesses
        if(retryRandom < ecpPercentage ) {
            if ( ! SodDB.getSingleton().isECPTodo()) {
                SodDB.getSingleton().populateECPToDo();
            }
            if (SodDB.getSingleton().isECPTodo()) {
                ecp = SodDB.getSingleton().getNextECPFromCache();
                if(ecp != null) {
                    ecp.update(Status.get(Stage.EVENT_CHANNEL_POPULATION, Standing.IN_PROG));
                    SodDB.commit();
                    ecp  = (AbstractEventChannelPair)SodDB.getSession().merge(ecp);
                    return ecp;
                }
            }
        }
        // no ecp/evp try e-station
        EventStationPair esp = SodDB.getSingleton().getNextESP();
        if(esp != null) {
            esp.update(Status.get(Stage.EVENT_CHANNEL_POPULATION, Standing.IN_PROG));
            SodDB.commit();
            SodDB.getSession().update(esp);
            return esp;
        }
        // no e-station try e-network
        EventNetworkPair enp = SodDB.getSingleton().getNextENP();
        if(enp != null) {
            enp.update(Status.get(Stage.EVENT_CHANNEL_POPULATION, Standing.IN_PROG));
            SodDB.commit();
            // reattach to new session
            SodDB.getSession().update(enp);
            return enp;
        }
        // go get more events to make e-net pairs
        StatefulEvent ev = StatefulEventDB.getSingleton().getNext(Standing.INIT);
        if (ev != null) {
            createEventNetworkPairs(ev);
            enp = SodDB.getSingleton().getNextENP();
            if(enp != null) {
                enp.update(Status.get(Stage.EVENT_CHANNEL_POPULATION, Standing.IN_PROG));
                SodDB.commit();
                // reattach to new session
                SodDB.getSession().update(enp);
                return enp;
            }
        }
        // really nothing doing, might as well work on a retry if one is ready?
        return SodDB.getSingleton().getNextRetryECPFromCache();
    }

    protected static MicroSecondDate lastECP = ClockUtil.now(); 
    
    protected static void createEventNetworkPairs(StatefulEvent ev) {
        logger.debug("Work on event: " + ev.getDbid() + " "
                + EventUtil.getEventInfo(ev));
        StatefulEventDB eventDb = StatefulEventDB.getSingleton();
        SodDB sodDb = SodDB.getSingleton();
        ev.setStatus(Status.get(Stage.EVENT_CHANNEL_POPULATION,
                                Standing.IN_PROG));
        eventDb.getSession().saveOrUpdate(ev);
        eventDb.commit();
        // refresh event to put back in new session
        eventDb.getSession().load(ev, ev.getDbid());
        EventEffectiveTimeOverlap overlap;
        try {
            if(ev.get_preferred_origin().getOriginTime() == null) {
                throw new RuntimeException("otime is null "
                        + ev.get_preferred_origin().getLocation());
            }
            overlap = new EventEffectiveTimeOverlap(ev);
        } catch(NoPreferredOrigin e) {
            throw new RuntimeException("Should never happen...", e);
        }
        List<NetworkAttrImpl> networks = Start.getNetworkArm().getSuccessfulNetworks();
        if (networks.size() == 0 && ! Start.isArmFailure()) {
             throw new RuntimeException("No successful networks!");
        }
        int numENP = 0;
        List<EventNetworkPair> enpList = new ArrayList<EventNetworkPair>();
        for (NetworkAttrImpl net : networks) {
            if(overlap.overlaps(net)) {
                EventNetworkPair p;
                try {
                    p = new EventNetworkPair(ev, NetworkDB.getSingleton().getNetwork(net.getDbid()));
                } catch(NotFound e) {
                    throw new RuntimeException("Should never happen, but I guess it just did!", e);
                }
                enpList.add(p);
                numENP++;
                logger.debug("Put EventNetworkPair: "+p);
            } else {
                failLogger.info("Network "
                        + NetworkIdUtil.toStringNoDates(net)
                        + " does not overlap event " + ev);
            }
        }
        logger.debug("Insert "+numENP+" EventNetworkPairs for "+ev);
        // set the status of the event to be SUCCESS implying that
        // that all the network information for this particular event is
        // inserted
        // in the waveformDatabase.
        synchronized(WaveformArm.class) {
            ev.setStatus(Status.get(Stage.EVENT_CHANNEL_POPULATION,
                                    Standing.SUCCESS));
            for (EventNetworkPair pair : enpList) {
                SodDB.getSession().save(pair);
            }
            eventDb.commit();
            sodDb.offerEventNetworkPairs(enpList);
        }
        Start.getEventArm().change(ev);
        int numWaiting = eventDb.getNumWaiting();
        if(numWaiting < EventArm.MIN_WAIT_EVENTS) {
            logger.debug("There are less than "
                    + EventArm.MIN_WAIT_EVENTS
                    + " waiting events.  Telling the eventArm to start up again");
            synchronized(Start.getEventArm()) {
                Start.getEventArm().notifyAll();
            }
        }
    }
    
    public int getProcessorNum() {
        return processorNum;
    }
    
    AbstractWaveformRecipe recipe;

    private int processorNum;
    
    boolean lastWorkWasEvent = false;
    
    public static int getProcessorsWorking() {
        return processorsWorking;
    }

    static int processorsWorking = 0;

    static void processorStartWork() {
        processorsWorking++;
    }

    static void processorFinishWork() {
        processorsWorking--;
    }

    static int usedProcessorNum = 0;

    static int nextProcessorNum() {
        return usedProcessorNum++;
    }

    private static double getRetryPercentage() {
        return retryPercentage;
    }
    
    public boolean threadShouldExit = false;
    
    /** percent of the pool that will be retries */
    private static double retryPercentage = .01; 
    
    private static double ecpPercentage = .001; // most processing uses esp from db, only use ecp if crash

    private static TimeInterval ECP_WINDOW = new TimeInterval(5, UnitImpl.MINUTE);
    
    private static final org.slf4j.Logger logger = org.slf4j.LoggerFactory.getLogger(WaveformArm.class);
    
    private static final org.slf4j.Logger failLogger = org.slf4j.LoggerFactory.getLogger("Fail.WaveformArm");

}
