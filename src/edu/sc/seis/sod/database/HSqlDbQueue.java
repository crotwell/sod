package edu.sc.seis.sod.database;

import edu.sc.seis.sod.*;

import edu.sc.seis.fissuresUtil.cache.*;
import edu.sc.seis.fissuresUtil.namingService.*;
import edu.iris.Fissures.IfEvent.*;

import java.util.Properties;
import java.sql.*;
import org.hsqldb.*;
import org.omg.CORBA.*;

/**
 * HSqlDbQueue.java
 *
 *
 * Created: Tue Sep 17 13:54:22 2002
 *
 * @author <a href="mailto:">Srinivasa Telukutla</a>
 * @version
 */

public class HSqlDbQueue implements Queue {
    public HSqlDbQueue (){
	Properties props = new Properties();
	this.props = props;
	eventDatabase = DatabaseManager.getDatabaseManager(props, "hsqldb").getEventDatabase();
	//	eventDatabase.updateStatus(Status.PROCESSING, Status.NEW);
	//	delete(Status.COMPLETE_SUCCESS);
    }

    public HSqlDbQueue(Properties props) {
	this.props = props;
	if(getDatabaseType(props) == 0) { 
		eventDatabase = DatabaseManager.getDatabaseManager(props, "hsqldb").getEventDatabase();
	}
	else { 
		eventDatabase =  DatabaseManager.getDatabaseManager(props, "postgres").getEventDatabase();
	}

    }

    public void updateEventDatabase() {
	if(getPersistanceType(props) == 0) {
	    eventDatabase.updateStatus(Status.PROCESSING, Status.NEW);
	    //delete(Status.COMPLETE_SUCCESS);
	} else {
	    //   delete(Status.COMPLETE_SUCCESS);
	    //delete(Status.PROCESSING);
	    eventDatabase.updateStatus(Status.PROCESSING, Status.COMPLETE_REJECT);
	}
    }

    private int getDatabaseType(Properties props) {
	String value = props.getProperty("edu.sc.seis.sod.databasetype");
	if(value == null) value = "hsqldb";
	if(value.equalsIgnoreCase("POSTGRES")) return 1;
	else return 0;
    }

    private int getPersistanceType(Properties props) {
	
	String value = props.getProperty("edu.sc.seis.sod.persistencetype");
	if(value == null) value = "ATLEASTONCE";
	if(value.equalsIgnoreCase("ATMOSTONCE")) return 1;
	else return 0;
    }
    
    public void push(java.lang.Object obj) {}
    /**
     * inserts the obj at the end of the queue.
     * 
     * @param obj a <code>java.lang.Object</code> value to be inserted into the queue.
     */
    public synchronized void push(String serverDNS, 
				  String serverName, 
				  edu.iris.Fissures.IfEvent.EventAccess obj,
				  edu.iris.Fissures.IfEvent.Origin originObj) {
	org.omg.CORBA.ORB orb = null;
	try {
	    orb = CommonAccess.getCommonAccess().getORB();
	} catch(edu.sc.seis.sod.ConfigurationException cfe) {
	    cfe.printStackTrace();
	}
	try {
	    //if(waitFlag){  
	    while(getLength() > 4) {
		System.out.println("&*********************&&&&&&&&&&&&&&&&&&& Waiting in push ");
		System.out.println("Before Wait push of queue");
		wait();
		System.out.println("After Wait push of queue");
	    }
	} catch(InterruptedException ie) {}



	System.out.println ("In the method push of the Queue the serverName "+serverName);
 	EventAccess eventAccess = obj;//(EventAccess) ((CacheEvent)obj).getEventAccess();
	Origin origin = originObj;
	String name = eventAccess.get_attributes().name;
	float lat = origin.my_location.latitude;
	float lon = origin.my_location.longitude;
	float depth = (float)origin.my_location.depth.value;

	System.out.println("Before the method object to String");
	String eventAccessIOR = null;
	try {
	    eventAccessIOR = orb.object_to_string(eventAccess);
	} catch(Exception e) {
	    e.printStackTrace();
	}
	System.out.println("THE IOR: "+eventAccessIOR);
	edu.iris.Fissures.Time origin_time = origin.origin_time;
	int dbid = eventDatabase.put(serverName,
				     serverDNS,
				     name,
				     lat,
				     lon,
				     depth,
				     origin_time,
				     eventAccessIOR);
	System.out.println("&&&&&&&&&&&&&&&&&    &&&&&&&&&&& &&&&&&   &&&&&&&&&The dbid that is obtained is "+dbid);
	notifyAll();
	

    }

    public synchronized void setFinalStatus(EventAccess eventAccess, Status status) {
	System.out.println("UPDATING THE STATUS");
	int dbid = eventDatabase.get(eventAccess);
	eventDatabase.updateStatus(dbid, status);
	System.out.println("Notifying in  updateStatus ");

	notifyAll();

	getLength();
	
	
    }

    private void open() {

    }

    /**
     * pops the first element of the queue.
     * @return a <code>java.lang.Object</code> value
     */
    public synchronized int pop() {
	int dbid = eventDatabase.getFirst(Status.NEW);
	
	while((getLength() == 0 && sourceAlive == true)  || 
	    (dbid == -1 && sourceAlive == true)){
	    try {
		wait();
		if(dbid == -1) {
		    dbid = eventDatabase.getFirst(Status.NEW);
		}
	    } catch(InterruptedException ie) { 
		ie.printStackTrace();
	    }

	}
	eventDatabase.updateStatus(dbid, Status.PROCESSING);
	return dbid;
// 	EventAccess eventAccess = getEventAccess(dbid);
// 	if(getLength() <= 4){ 
// 	    waitFlag = false;
// 	    notifyAll();
// 	}

// 	return new CacheEvent(eventAccess);
    }

    private org.omg.CORBA.Object reValidate(int dbid, org.omg.CORBA.Object obj) {

	if(obj._non_existent()) {
	    //here query from the server again.
	    EventDC eventDC = getEventDC(dbid);
	    edu.iris.Fissures.IfEvent.EventFinder finder = eventDC.a_finder();
	    EventAccess[] eventAccess = finder.get_by_name(eventDatabase.getEventName(dbid));
	    System.out.println("THE OBJECT DOESNOT EXIST !!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!");
	    System.out.println("######################################################################################");
	    System.out.println("%%%%%%%%%%%%%%%%%%%%%%%% Object doesnot exist the length is "+eventAccess.length);
	    for(int counter = 0; counter < eventAccess.length; counter++) {
		int checkdbid = -1;
		if((checkdbid = eventDatabase.get(eventAccess[counter])) != -1) {
		    if(checkdbid == dbid) {
			//update the ior corresponding to the dbid and return the object.
			System.out.println("The dbid that matched is "+checkdbid);
			org.omg.CORBA.ORB orb = null;
			try {
			    orb = CommonAccess.getCommonAccess().getORB();
			} catch(edu.sc.seis.sod.ConfigurationException cfe) {
			    cfe.printStackTrace();
			}
			String newIOR = orb.object_to_string(eventAccess[counter]);
			eventDatabase.updateIOR(dbid, newIOR);
			return eventAccess[counter];
		    }
		}
	    }
	    return null;
	} else {
	    return obj;
	}

    }
   
    public int getEventId(EventAccess eventAccess) {
	return eventDatabase.get(eventAccess);
    }

  
    /**
     * returns the length of the queue.
     *
     * @return an <code>int</code> value
     */
    public synchronized int getLength() {
	int numNew = eventDatabase.getCount(Status.NEW);
	int numProcessing = eventDatabase.getCount(Status.PROCESSING);
	int numSuccessful = eventDatabase.getCount(Status.COMPLETE_SUCCESS);
	System.out.println("THE NUMBER OF EVENTS THAT ARE NEW :::::::::::::::::::: "+numNew);
	System.out.println("THE NUMBER OF EVENTS THAT ARE PROCESSED :::::::::::::: "+numProcessing);
	System.out.println("THE NUMBER OF SUCCESSFUL EVENTS ARE :::::::::::::::::: "+numSuccessful);
	return (numNew + numProcessing);
    }

    private int getNew() {
	int numNew = eventDatabase.getCount(Status.NEW);
	return numNew;
    }
    /**
     * sets if the source i.e., the thread which pushes objects into the queue
     * is alive 
     *
     * @param value a <code>boolean</code> value
     */
    public synchronized void setSourceAlive(boolean value) {
	this.sourceAlive = value;
	System.out.println("@@@@@@@@@@@@@@@@@@@@@@@@@@@@@#@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@ SET SOURCE ALIVE = "+sourceAlive);
	notifyAll();
    }
    
    /**
     * returns true if the source i.e., the thread which pushes objects into the queue
     * is alive, else returns false.
     *
     * @return a <code>boolean</code> value
     */
    public boolean getSourceAlive() {
	return sourceAlive;
    }

  
    public  synchronized void waitForProcessing() {
	if(getLength() > 4) {
	    waitFlag = true;
	    System.out.println("Wait flag is set to be true");
	}
    }

    public synchronized void delete(Status status) {
	getLength();
	int numSuccessful = eventDatabase.getCount(status);
	if(numSuccessful > 0) {
	    eventDatabase.delete(status);
	    notifyAll();
	    //getLength();
	    //System.exit(0);
	}
	System.out.println("DELETING THE RECORD FROM THE DATABSE");

	//System.exit(0);
    }

    private edu.iris.Fissures.IfEvent.EventDC getEventDC(int dbid) {
	try {
	    
	
	    FissuresNamingServiceImpl fissuresNamingServiceImpl = 
		new FissuresNamingServiceImpl(CommonAccess.getCommonAccess().getORB());
	    EventDC eventDC = fissuresNamingServiceImpl.getEventDC(eventDatabase.getServerDNS(dbid),
								   eventDatabase.getServerName(dbid));
	    return eventDC;
	} catch(Exception e) {
	    e.printStackTrace();
	    return null;
	}
    }

    public EventAccessOperations getEventAccess(int dbid) {
	org.omg.CORBA.ORB orb = null;
	try {
	    orb = CommonAccess.getCommonAccess().getORB();
	} catch(edu.sc.seis.sod.ConfigurationException cfe) {
	    cfe.printStackTrace();
	}
	String ior = eventDatabase.getObject(dbid);
	if(ior == null)  {
		System.out.println("As the IOR is null returning NULL "+dbid);
		System.exit(0);
		return null;
	}
	org.omg.CORBA.Object obj = orb.string_to_object(ior);
	obj = reValidate(dbid, obj);
	EventAccess eventAccess = EventAccessHelper.narrow(obj);
	return new CacheEvent(eventAccess);
    }

    public void closeDatabase() {
	if(getDatabaseType(props) == 0) DatabaseManager.getDatabaseManager(props, "hsqldb").close();
	else DatabaseManager.getDatabaseManager(props, "postgres").close();
    }

    public void setTime(String serverName,
			String serverDNS,
			edu.iris.Fissures.Time time) {
	eventDatabase.setTime(serverName,
			      serverDNS,
			      time);
    }

    public edu.iris.Fissures.Time getTime(String serverName,
					  String serverDNS) {
	return eventDatabase.getTime(serverName,
				     serverDNS);
    }

    public void incrementTime(String serverName,
			      String serverDNS,
			      int days) {
	eventDatabase.incrementTime(serverName,
				    serverDNS,
				    days);	
    }
    
    Properties props;
    
    private boolean sourceAlive = true;
   
    EventDatabase eventDatabase;

    private boolean waitFlag = false;
    
    private PreparedStatement putStmt;

    private PreparedStatement getStmt;

    private PreparedStatement getMaxIdStmt;
  
}// HSqlDbQueue
