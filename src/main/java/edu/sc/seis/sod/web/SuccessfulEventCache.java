package edu.sc.seis.sod.web;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

import edu.sc.seis.sod.Start;
import edu.sc.seis.sod.WaveformArm;
import edu.sc.seis.sod.hibernate.SodDB;
import edu.sc.seis.sod.hibernate.StatefulEventDB;
import edu.sc.seis.sod.hibernate.eventpair.EventChannelPair;
import edu.sc.seis.sod.hibernate.eventpair.EventNetworkPair;
import edu.sc.seis.sod.hibernate.eventpair.EventStationPair;
import edu.sc.seis.sod.hibernate.eventpair.EventVectorPair;
import edu.sc.seis.sod.model.event.StatefulEvent;
import edu.sc.seis.sod.model.status.Stage;
import edu.sc.seis.sod.model.status.Standing;
import edu.sc.seis.sod.status.eventArm.EventMonitor;
import edu.sc.seis.sod.status.waveformArm.WaveformMonitor;

public class SuccessfulEventCache {

    public SuccessfulEventCache() {

        Timer t = new Timer("SuccessfulEventCache updater", true);
        t.schedule(new TimerTask() {

            public void run() {
                try {
                    updateSuccessfulEvents();
                } catch(Throwable t) {
                    logger.error("SuccessfulEventCache Trying to update seccessful events", t);
                }
            }
        }, 0, 900 * 1000);
        Start.getEventArm().add(new EventMonitor() {
            @Override
            public void setArmStatus(String status) throws Exception {
                // TODO Auto-generated method stub
            }
            @Override
            public void change(StatefulEvent event) {
            	System.out.println("change: "+event);
                if (event.getStatus().getStanding().equals(Standing.SUCCESS) 
                        && event.getStatus().getStage().equals(Stage.EVENT_CHANNEL_POPULATION)) {
                    updateInCache(event);
                }
            }
            
        });
            Start.getWaveformRecipe().addStatusMonitor(new WaveformMonitor() {
                
                @Override
                public void update(EventVectorPair evp) {
                    if (evp.getStatus().getStanding().equals(Standing.SUCCESS)) { 
                        if ( ! eventInList(evp.getEvent())) {
                            eventWithSuccessfulCache.add(evp.getEvent());
                        } else {
                            logger.info("SuccessfulEventCache Not adding as already added: "+evp);
                        }
                    } else {
                     logger.info("SuccessfulEventCache Not adding as not SUCCESS: "+evp);   
                    }
                }
                
                @Override
                public void update(EventChannelPair ecp) {
                    if (ecp.getStatus().getStanding().equals(Standing.SUCCESS)) { 
                        if ( ! eventInList(ecp.getEvent())) {
                            eventWithSuccessfulCache.add(ecp.getEvent());
                        } else {
                            logger.info("SuccessfulEventCache Not adding as already added: "+ecp);
                        }
                    } else {
                     logger.info("SuccessfulEventCache Not adding as not SUCCESS: "+ecp);   
                    }
                }
                
                @Override
                public void update(EventStationPair ecp) {
                    // TODO Auto-generated method stub
                    
                }
                
                @Override
                public void update(EventNetworkPair ecp) {
                    // TODO Auto-generated method stub
                    
                }
            });
        
    }
    
    public List<StatefulEvent> getEventWithSuccessful() {
        return eventWithSuccessfulCache;
    }
    
    public int getNumSuccessful(StatefulEvent statefulEvent) {
        return numSuccessful.get(statefulEvent);
    }
    
    void updateSuccessfulEvents() {
        logger.info("SuccessfulEventCache.updateSuccessfulEvents()");
        StatefulEventDB db = StatefulEventDB.getSingleton();
        List<StatefulEvent> events = db.getAll();
        for (Iterator iterator = events.iterator(); iterator.hasNext();) {
            StatefulEvent statefulEvent = (StatefulEvent)iterator.next();
            int numSuccess = SodDB.getSingleton().getNumSuccessful(statefulEvent);
            if (numSuccess == 0) {
                iterator.remove();
            } else {
                numSuccessful.put(statefulEvent, numSuccess);
            }
        }
        eventWithSuccessfulCache = events;
    }
    
    void updateInCache(StatefulEvent statefulEvent) {
        logger.info("SuccessfulEventCache.updateInCache("+statefulEvent);
        int numSuccess = SodDB.getSingleton().getNumSuccessful(statefulEvent);
        if (numSuccess > 0) {
            eventWithSuccessfulCache.add(statefulEvent);
        }
    }
    
    boolean eventInList(StatefulEvent e) {
        for (StatefulEvent statefulEvent : eventWithSuccessfulCache) {
            if (statefulEvent.getDbid() == e.getDbid()) {
                return true;
            }
        }
        return false;
    }
    
    List<StatefulEvent> eventWithSuccessfulCache = new ArrayList<StatefulEvent>();
    
    HashMap<StatefulEvent, Integer> numSuccessful = new HashMap<StatefulEvent, Integer>();
    
    private static final org.slf4j.Logger logger = org.slf4j.LoggerFactory.getLogger(SuccessfulEventCache.class);
}
