package edu.sc.seis.sod.database;

import edu.iris.Fissures.IfEvent.*;
import edu.iris.Fissures.model.*;

import java.sql.*;
import java.util.*;
/**
 * AbstractDatabase.java
 *
 *
 * Created: Sat Sep 28 14:26:27 2002
 *
 * @author <a href="mailto:">Srinivasa Telukutla</a>
 * @version
 */

public abstract class AbstractDatabase implements EventDatabase{
    public AbstractDatabase (){
		init();
    }
    
    public AbstractDatabase (Connection connection) {
	this.connection = connection;
	//	processProperties();
	init();
    }

   public abstract ConfigDatabase getConfigDatabase();

    public abstract void create();
    
    public abstract String getTableName();

   
  
    private void init() {

	try {
		create();
	putStmt = connection.prepareStatement("INSERT INTO eventconfig(serverName, "+
													  " serverDNS, "+
													  " eventName, "+
													  " latitude, "+
													  " longitude, "+
													  " depth, "+
													  " origin_time, "+
													  " status, "+
													  " eventAccess)"+
													  " VALUES(?,?,?,?,?,?,?,?,?)");
	    //	    getMaxIdStmt = connection.prepareStatement("SELECT MAX(eventid) FROM eventconfig");
	    getIDStmt = connection.prepareStatement("SELECT eventid FROM eventconfig WHERE "+
						    // " serverName = ? AND "+
						    //" serverDNS = ? AND "+
						    " eventName = ? AND "+
						    " latitude = ? AND "+
						    " longitude = ? AND "+
						    " depth = ? AND "+
						    " origin_time = ? "
						  );
	    getIDArrayStmt = connection.prepareStatement("SELECT eventid FROM eventconfig WHERE "+
							 " status = ? ");
	    
	    getObjectStmt = connection.prepareStatement("SELECT eventAccess FROM eventconfig WHERE "+
							" eventid = ? ");

	    deleteStmt = connection.prepareStatement("DELETE FROM eventconfig WHERE eventid = ? ");
	    
	    updateStmt = connection.prepareStatement("UPDATE eventconfig SET status = ? WHERE eventid = ?");
	    
	    statusStmt = connection.prepareStatement("SELECT status from eventconfig WHERE "+
						     " eventid = ? ");

	    countStmt = connection.prepareStatement("SELECT count(eventid) from eventconfig WHERE status = ?");

	    getStmt = connection.prepareStatement("SELECT eventid, serverName, serverDNS, eventName, latitude, longitude, "+
						  " depth, origin_time, status, eventAccess FROM eventconfig "+
						  " WHERE eventid = ? ");
	    
	    iorUpdateStmt = connection.prepareStatement("UPDATE eventconfig SET eventAccess = ? WHERE eventid = ?");

	    statusUpdateStmt = connection.prepareStatement("UPDATE eventconfig SET status = ? WHERE status = ?");
	    
	} catch(SQLException sqle) {
	    sqle.printStackTrace();
	}
    }

      private int get(String eventName, 
		    float lat,
		    float lon,
		    float depth,
		    edu.iris.Fissures.Time origin_time) {
	 try {

	     int index = 1;
	     getIDStmt.setString(index++, eventName);
	     getIDStmt.setFloat(index++, lat);
	     getIDStmt.setFloat(index++, lon);
	     getIDStmt.setFloat(index++, depth);
	     MicroSecondDate ms = new MicroSecondDate(origin_time);
	     getIDStmt.setTimestamp(index++, ms.getTimestamp());
	     // getIDStmt.setString(index++, ior);
	     
	     ResultSet rs = getIDStmt.executeQuery();
	     if(rs.next()) {
		 return rs.getInt("eventid"); 
	     }
	     return -1;
	 } catch(SQLException sqle) {
	     sqle.printStackTrace();
	     return -1;
	 }
     }


    
    public int get(EventAccess eventAccess) {
	try {
	    Origin origin = eventAccess.get_preferred_origin();
	    String name = eventAccess.get_attributes().name;
	    float lat = origin.my_location.latitude;
	    float lon = origin.my_location.longitude;
	    float depth = (float)origin.my_location.depth.value;

	    String eventAccessIOR = null;
	    try {
		org.omg.CORBA.ORB orb = ((org.omg.CORBA.portable.ObjectImpl)eventAccess)._orb();
		eventAccessIOR = orb.object_to_string(eventAccess);
	    } catch(Exception e) {
		e.printStackTrace();
	    }

	    edu.iris.Fissures.Time origin_time = origin.origin_time;
	    return get(name,
		       lat,
		       lon,
		       depth,
		       origin_time);
	} catch(Exception e) {
	    return -1;
	}
    }


	public int put(String serverName,
				   String serverDNS,
				   String eventName,
				   float lat,
				   float lon,
				   float depth,
				   edu.iris.Fissures.Time origin_time,
				   String objectIOR) {
		try {
			// putStmt.setInt(1, 10);
			int dbid = get(eventName,
						   lat,
						   lon,
						   depth,
						   origin_time);
			if(dbid != -1) return dbid;
			int index = insert(putStmt,
							   1,
							   serverName,
							   serverDNS,
							   eventName,
							   lat,
							   lon,
							   depth,
							   origin_time);
			putStmt.setInt(index++, Status.NEW.getId());
			putStmt.setString(index, objectIOR);
			putStmt.executeUpdate();
			return get(eventName,
					   lat,
					   lon,
					   depth,
					   origin_time);

		} catch(SQLException sqle) {

			sqle.printStackTrace();
			return 0;
		}

	}

    public Status getStatus(int dbid) {
	return (Status)getField(9, dbid);
    }

    public String getServerName(int dbid) {
	return (String)getField(2, dbid);
    }

    public String getServerDNS(int dbid) {
	return (String)getField(3, dbid);
    }

    public String getEventName(int dbid) {
	return (String)getField(4, dbid);
    }
        
    public int[] get(Status status) {
	try {
	    getIDArrayStmt.setInt(1, status.getId());
	    ResultSet rs = getIDArrayStmt.executeQuery();
	    ArrayList arrayList = new ArrayList();
	    while(rs.next()) {
		int dbid = rs.getInt("eventid");
		arrayList.add(new Integer(dbid));
	    }
	    Integer[] ids = new Integer[arrayList.size()];
	    ids = (Integer[]) arrayList.toArray(ids);
	    int[] rtnValues = new int[ids.length];
	    for(int counter = 0; counter < ids.length; counter++) {
		rtnValues[counter] = ids[counter].intValue();
	    }
	    return rtnValues;
	} catch(SQLException sqle) {
	    sqle.printStackTrace();
	    return new int[0];
	}
    }

    public int getFirst(Status status) {

	int[] rtnValues = get(status);
	System.out.println("THE NUMBER OF RTNVALUES IN GETFIRST(STATUS) ARE "+rtnValues.length);
	if(rtnValues.length > 0) return rtnValues[0];
	else return -1;
    }
    
    public void updateStatus(int id, Status newStatus) {
	try {
	    updateStmt.setInt(1, newStatus.getId());
	    updateStmt.setInt(2, id);
	    updateStmt.executeUpdate();
	  	} catch(SQLException sqle) {

	    sqle.printStackTrace();
	    
	}
    }

    public void updateStatus(Status oldStatus, Status newStatus) {
	try {
	    statusUpdateStmt.setInt(1, newStatus.getId());
	    statusUpdateStmt.setInt(2, oldStatus.getId());
	    statusUpdateStmt.executeUpdate();
	} catch(SQLException sqle) {
	    sqle.printStackTrace();
	}
    }

    public void updateIOR(int dbid, String newIOR) {
	try {
	    iorUpdateStmt.setString(1, newIOR);
	    iorUpdateStmt.setInt(2, dbid);
	    updateStmt.executeUpdate();
	} catch(SQLException sqle) {
	    sqle.printStackTrace();
	}
    }
		   
    public void delete(int id) {
	try {
	    deleteStmt.setInt(1, id);
	    deleteStmt.executeUpdate();
	} catch(SQLException sqle) {
	    sqle.printStackTrace();
	}
    }

    public void delete(EventAccess eventAccess) {
	int dbid = get(eventAccess);
	delete(dbid);
    }
    
    public void delete(Status status) {
	int[] dbids = get(status);
	for(int counter = 0; counter < dbids.length; counter++) {
	    delete(dbids[counter]);
	}
    }

    public int getCount(Status status) {
	try {
	    countStmt.setInt(1, status.getId());
	    ResultSet rs = countStmt.executeQuery();
	    if(rs.next()) {
		return rs.getInt(1);
	    }
	    return 0;
	} catch(SQLException sqle) {
	    sqle.printStackTrace();
	    return 0;
	}
    }

    public String getObject(int eventid) {

	try {
	    getObjectStmt.setInt(1, eventid);
	    ResultSet rs = getObjectStmt.executeQuery();
	    if(rs.next()) {
		return rs.getString("eventAccess");
	    }
	    return null;
	} catch(SQLException sqle) {
	    sqle.printStackTrace();
	    return null;
	}    
	
    }

   

    public java.lang.Object getField(int index, int dbid) {
	try {
	    getStmt.setInt(1, dbid);
	    ResultSet rs = getStmt.executeQuery();
	    if(rs.next()) {
		switch(index) {
		case 1: return new Integer(dbid);
		case 2:
		case 3:
		case 4:
		case 10: return rs.getString(index);
		    //	    case 3: return rs.getString(index);
		    //case 4: return rs.getString(index);
		case 5:
		case 6: 
		case 7: return new Float(rs.getFloat(index));
		case 8: return null;
		case 9: return Status.getById(rs.getInt(index));
		default: return null;    
		}
	    }
	    return null;
	} catch(Exception e) {
	    e.printStackTrace();
	    return null;
	}
    }
	
	 
    private int  insert(PreparedStatement stmt,
                       int index,
                       String serverName,
                       String serverDNS,
                       String eventName,
                       float lat,
                       float lon,
                       float depth,
                       edu.iris.Fissures.Time origin_time) {
        try {
            stmt.setString(index++, serverName);
            stmt.setString(index++, serverDNS);
            stmt.setString(index++, eventName);
            stmt.setFloat(index++, lat);
            stmt.setFloat(index++, lon);
            stmt.setFloat(index++, depth);
            MicroSecondDate ms = new MicroSecondDate(origin_time);
            stmt.setTimestamp(index++, ms.getTimestamp());
            return index;
        } catch(SQLException sqle) {

            sqle.printStackTrace();
            return index;
        }

    }

    public void setTime(edu.iris.Fissures.Time time) {
	getConfigDatabase().setTime(time);
    }

    public edu.iris.Fissures.Time getTime() {
	return getConfigDatabase().getTime();
    }

    public void incrementTime(int numDays) {
	getConfigDatabase().incrementTime(numDays);
    }
    
    protected Connection connection;

    private PreparedStatement putStmt;
    
    private PreparedStatement getIDStmt;

    private PreparedStatement getIDArrayStmt;

    private PreparedStatement getObjectStmt;

    private PreparedStatement updateStmt;
    
    private PreparedStatement statusStmt;

    private PreparedStatement statusUpdateStmt;

    private PreparedStatement deleteStmt;

    private PreparedStatement countStmt;

    private PreparedStatement getStmt;
    
    private PreparedStatement iorUpdateStmt;

}// AbstractDatabase
