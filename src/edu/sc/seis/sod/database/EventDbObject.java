package edu.sc.seis.sod.database;

import edu.sc.seis.fissuresUtil.cache.CacheEvent;

/**
 * EventDbObject.java
 *
 *
 * Created: Wed Oct 23 09:58:20 2002
 *
 * @author <a href="mailto:">Srinivasa Telukutla</a>
 * @version
 */

public class EventDbObject extends DbObject{
    public EventDbObject (int dbid, CacheEvent eventAccess){
        super(dbid);
        this.event = eventAccess;
    }
    
    public CacheEvent getEvent() {
        return event;
    }
    
    private CacheEvent event;
    
}// EventDbObject
