package edu.sc.seis.sod.web;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

import edu.sc.seis.sod.Stage;
import edu.sc.seis.sod.Standing;
import edu.sc.seis.sod.Start;
import edu.sc.seis.sod.hibernate.SodDB;
import edu.sc.seis.sod.hibernate.StatefulEvent;
import edu.sc.seis.sod.hibernate.StatefulEventDB;
import edu.sc.seis.sod.status.eventArm.EventMonitor;

public class SuccessfulEventCache {

    public SuccessfulEventCache() {

        Timer t = new Timer("SuccessfulEventCache updater", true);
        t.schedule(new TimerTask() {

            public void run() {
                try {
                    updateSuccessfulEvents();
                } catch(Throwable t) {
                    logger.error("Trying to update seccessful events", t);
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
                if (event.getStatus().getStanding().equals(Standing.SUCCESS) 
                        && event.getStatus().getStage().equals(Stage.EVENT_CHANNEL_POPULATION)) {
                    updateInCache(event);
                }
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
        int numSuccess = SodDB.getSingleton().getNumSuccessful(statefulEvent);
        if (numSuccess > 0) {
            eventWithSuccessfulCache.add(statefulEvent);
        }
    }
    
    List<StatefulEvent> eventWithSuccessfulCache = new ArrayList<StatefulEvent>();
    
    HashMap<StatefulEvent, Integer> numSuccessful = new HashMap<StatefulEvent, Integer>();
    
    private static final org.slf4j.Logger logger = org.slf4j.LoggerFactory.getLogger(SuccessfulEventCache.class);
}
