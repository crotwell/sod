package edu.sc.seis.sod.database;

import edu.iris.Fissures.IfEvent.*;

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
    public EventDbObject (int dbid, EventAccessOperations eventAccess){
	super(dbid);
	this.eventAccess = eventAccess;
    }

    public EventAccessOperations getEventAccess() {
	return this.eventAccess;
    }

    private EventAccessOperations eventAccess;
    
}// EventDbObject
