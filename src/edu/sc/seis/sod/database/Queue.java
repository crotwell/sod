package edu.sc.seis.sod.database;


import edu.iris.Fissures.IfEvent.*;
import edu.iris.Fissures.*;

import java.util.*;
import org.omg.CORBA.*;



/**
 * This is an implementation of the Queue DataStructure. 
 * This  class is thread safe.  
 *
 * @author <a href="mailto:">Srinivasa Telukutla</a>
 * @version 1.0
 */
public interface Queue {


   
    /**
     * inserts the obj at the end of the queue.
     * 
     * @param obj a <code>java.lang.Object</code> value to be inserted into the queue.
     */
    public void push(java.lang.Object obj);

    public void push(String serverName, 
		     String serverDNS, 
		     edu.iris.Fissures.IfEvent.EventAccess obj,
		     edu.iris.Fissures.IfEvent.Origin originObj);

    /**
     * pops the first element of the queue.
     * @return a <code>java.lang.Object</code> value
     */
    public int pop();


    public void setFinalStatus(EventAccessOperations eventAccess, Status status);


    public void delete(Status status);

    /**
     * returns the length of the queue.
     *
     * @return an <code>int</code> value
     */
    public int getLength();
    /**
     * sets if the source i.e., the thread which pushes objects into the queue
     * is alive 
     *
     * @param value a <code>boolean</code> value
     */
    public void setSourceAlive(boolean value);
    
    /**
     * returns true if the source i.e., the thread which pushes objects into the queue
     * is alive, else returns false.
     *
     * @return a <code>boolean</code> value
     */
    public boolean getSourceAlive();

    public EventAccessOperations getEventAccess(int dbid);


    public void waitForProcessing();

    public void closeDatabase();

    public int getEventId(EventAccessOperations eventAccess);
   
    public void setTime(String serverName,
			String serverDNS,
			edu.iris.Fissures.Time time);

    public edu.iris.Fissures.Time getTime(String serverName,
					  String serverDNS);

    public void incrementTime(String serverName,
			      String serverDNS,
			      int numDays);

    public void updateEventDatabase();
   
    public void clean();

    public void deleteTimeConfig();

    public Status getStatus(int eventid);
}
