package edu.sc.seis.sod.database;

import edu.iris.Fissures.IfEvent.*;

/**
 * EventDatabase.java
 *
 *
 * Created: Wed Sep 18 11:18:10 2002
 *
 * @author <a href="mailto:">Srinivasa Telukutla</a>
 * @version
 */

public interface EventDatabase {

    public int get(EventAccessOperations eventAccess);
    /*public int get(String serverName,
		   String serverDNS,
		   float lat,
		   float lon,
		   float depth,
		   edu.iris.Fissures.Time origin_time);*/

    public int put(String serverName,
		   String serverDNS,
		   String eventName,
		   float lat,
		   float lon,
		   float depth,
		   edu.iris.Fissures.Time origin_time,
		   String objectIOR);

    public Status getStatus(int dbid);

    public String getServerName(int dbid);

    public String getServerDNS(int dbid);

    public String getEventName(int dbid);
        
    public int[] get(Status status);

    public int getFirst(Status status);
    
    public void updateStatus(int id, Status newStatus);

    public void updateStatus(Status oldStatus, Status newStatus);

    public void updateIOR(int dbid, String newIOR);
		   
    public void delete(int id);

    public void delete(EventAccessOperations eventAccess);
    
    public void delete(Status status);

    public int getCount(Status status);

    public String getObject(int eventid);

    public void setTime(String serverName, String serverDNS, edu.iris.Fissures.Time time);

    public edu.iris.Fissures.Time getTime(String serverName, String serverDNS);

    public void incrementTime(String serverName, String serverDNS, int numDays);

    public void clean();
    
    public void deleteTimeConfig();

    public void reOpenEvents();
}// EventDatabase
