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
	eventDatabase = new HSqlDatabase(props);
	eventDatabase.updateStatus(Status.PROCESSING, Status.NEW);
	delete(Status.COMPLETE_SUCCESS);
    }

    public HSqlDbQueue(Properties props) {
	if(getDatabaseType(props) == 0) eventDatabase = new HSqlDatabase(props);
	else eventDatabase =  new PostgresDatabase(props);
	if(getPersistanceType(props) == 0) {
	    eventDatabase.updateStatus(Status.PROCESSING, Status.NEW);
	    delete(Status.COMPLETE_SUCCESS);
	} else {
	    delete(Status.COMPLETE_SUCCESS);
	    delete(Status.PROCESSING);
	}
    }

    private int getDatabaseType(Properties props) {
	String value = props.getProperty("edu.sc.seis.sod.databasetype");
	if(value.equalsIgnoreCase("POSTGRES")) return 1;
	else return 0;
    }

    private int getPersistanceType(Properties props) {
	
	String value = props.getProperty("edu.sc.seis.sod.persistencetype");
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
	    if(waitFlag){  
		System.out.println("&*********************&&&&&&&&&&&&&&&&&&& Waiting in push ");
		wait();
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
	notifyAll();
	
	System.out.println("The dbid that is obtained is "+dbid);
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
    public synchronized java.lang.Object pop() {
	int dbid = eventDatabase.getFirst(Status.NEW);
	System.out.println("%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%% In the method pop the value of sourceAlive is "+sourceAlive);
	while((getLength() == 0 && sourceAlive == true)  || 
	    (dbid == -1 && sourceAlive == true)){
	    try {
		System.out.println("Waiting in POP())))))))))))))))()()()()()()()()()()()()()()()()()(");
		wait();
		dbid = eventDatabase.getFirst(Status.NEW);
	    } catch(InterruptedException ie) { ie.printStackTrace();}

	}
	org.omg.CORBA.ORB orb = null;
	try {
	    orb = CommonAccess.getCommonAccess().getORB();
	} catch(edu.sc.seis.sod.ConfigurationException cfe) {
	    cfe.printStackTrace();
	}
	//int dbid = eventDatabase.getFirst(Status.NEW);
	System.out.println("The dbid while popping is "+dbid);
	eventDatabase.updateStatus(dbid, Status.PROCESSING);
	String ior = eventDatabase.getObject(dbid);
	System.out.println("The ior for the dbid is "+ior);
	if(ior == null) return null;
	org.omg.CORBA.Object obj = orb.string_to_object(ior);
	obj = reValidate(dbid, obj);
	EventAccess eventAccess = EventAccessHelper.narrow(obj);
	if(getLength() <= 4){ 
	    System.out.println("NOtifying the OTHER THREADS AS EVENTS ARE <= 4 #################################################################");
	    waitFlag = false;
	    notifyAll();
	}
	if(eventAccess == null) {
	    System.out.println("The event Access is null ");
	    System.exit(0);
	}
	return new CacheEvent(eventAccess);
	//return eventAccess;
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
	    EventDC eventDC = getEventDC(dbid);
	    return obj;
	}

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
	try {
	    Thread.sleep(5000);
	} catch(Exception e) {
	    e.printStackTrace();
	}
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
    
    private boolean sourceAlive = true;
   
    EventDatabase eventDatabase;

    private boolean waitFlag = false;
    
    private PreparedStatement putStmt;

    private PreparedStatement getStmt;

    private PreparedStatement getMaxIdStmt;

  
  
}// HSqlDbQueue
