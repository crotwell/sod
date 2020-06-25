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
        }, 1000, 90 * 1000);

        if (Start.getEventArm() != null) {
        	Start.getEventArm().add(new EventMonitor() {
        		@Override
        		public void setArmStatus(String status) throws Exception {
        			// TODO Auto-generated method stub
        		}
        		@Override
        		public void change(StatefulEvent event) {
        			if (event.getStatus().getStanding().equals(Standing.SUCCESS) 
        					&& event.getStatus().getStage().equals(Stage.EVENT_CHANNEL_POPULATION)) {
        				updateInCache(event);
        			}
        		}

        	});
        }
        if (Start.getWaveformRecipe() != null) {
        	Start.getWaveformRecipe().addStatusMonitor(new WaveformMonitor() {
                
                @Override
                public void update(EventVectorPair evp) {
                    logger.info("SuccessfulEventCache update: "+evp.getStatus()+" ");
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

                    logger.info("SuccessfulEventCache update: "+ecp.getStatus()+" ");
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
                	logger.info("SuccessfulEventCache EventStationPair update: "+ecp);  
                }
                
                @Override
                public void update(EventNetworkPair ecp) {
                	logger.info("SuccessfulEventCache EventNetworkPair update: "+ecp);  
                    
                }
            });
        }
        
    }
    
    public List<StatefulEvent> getEventWithSuccessful() {
        return eventWithSuccessfulCache;
    }
    
    public int getNumSuccessful(StatefulEvent statefulEvent) {
    	if (statefulEvent == null) {
    		throw new RuntimeException("statefulEvent should not be null");
    	}
        Integer out = this.numSuccessful.get(statefulEvent);
        if (out == null) {
        	return 0;
        } else {
        	return out;
        }
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
            logger.info("SuccessfulEventCache.updateInCache("+statefulEvent+" "+numSuccess+" successful");
            eventWithSuccessfulCache.add(statefulEvent);
        } else {

            logger.info("SuccessfulEventCache.updateInCache("+statefulEvent+" none successful");
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
