
package edu.sc.seis.sod.database;

import edu.iris.Fissures.model.*;

import java.io.*;
import java.util.*;
import java.sql.*;




/**
 * AbstractConfigDatabase.java
 *
 *
 * Created: Thu Sep 12 15:33:59 2002
 *
 * @author <a href="mailto:">Srinivasa Telukutla</a>
 * @version
 */

public abstract class AbstractConfigDatabase implements ConfigDatabase{
    public AbstractConfigDatabase (Connection connection){
	this.connection = connection;
	init();
    }
    
    public abstract void create();

    private void init() {
	try {
	    create();
	    Statement stmt = connection.createStatement();
	    
	    setTimeStmt = connection.prepareStatement(" INSERT into timeconfig "+
						      " VALUES(?, ? , ? ) ");
	    updateTimeStmt = connection.prepareStatement(" UPDATE timeconfig set time = ? "+
							 " WHERE serverName = ? AND "+
							 " serverDNS = ? ");
	    
	    getTimeStmt = connection.prepareStatement(" SELECT time from timeconfig "+
						      " WHERE serverName = ? AND "+
						      " serverDNS = ? ");
	} catch(Exception e) {
	    e.printStackTrace();
	}
	

    }
    public AbstractConfigDatabase(edu.iris.Fissures.Time time, 
			 String serverDNS,
			 String serverName) {
	
	setTime(serverName,
		serverDNS,
		time);
    }

    
    public void setTime(String serverName,
			String serverDNS,
			edu.iris.Fissures.Time time) {
	try {
	    if(getTime(serverName, serverDNS) == null) {
		MicroSecondDate ms = new MicroSecondDate(time);
		setTimeStmt.setString(1, serverName);
		setTimeStmt.setString(2, serverDNS);
		setTimeStmt.setTimestamp(3, ms.getTimestamp());
		setTimeStmt.executeUpdate();
	    } else {
		updateTime(serverName, serverDNS, time);
	    }
	} catch(SQLException sqle) {
	    sqle.printStackTrace();
	}
    }
    

   //  public void setServerDNS(String serverDNS) {
// 	this.serverDNS = serverDNS;
//     }

//     public void setServerName(String serverName) {

// 	this.serverName = serverName;
//     }


    public edu.iris.Fissures.Time getTime(String serverName,
					  String serverDNS) {
	try {
	    getTimeStmt.setString(1, serverName);
	    getTimeStmt.setString(2, serverDNS);
	    ResultSet rs = getTimeStmt.executeQuery();
	    if(rs.next()) {
		MicroSecondDate ms = new MicroSecondDate(rs.getTimestamp("time"));
		return ms.getFissuresTime();
	    }
	    return null;
	} catch(SQLException sqle) {
	    sqle.printStackTrace();
	    return null;
	}
    }

    public void incrementTime(String serverName,
			      String serverDNS,
			      int days) {
	edu.iris.Fissures.Time time = getTime(serverName, serverDNS);
	MicroSecondDate microSecondDate = new MicroSecondDate(time);
	Calendar calendar = Calendar.getInstance();
	calendar.setTime(microSecondDate);
	calendar.roll(Calendar.DAY_OF_YEAR, days);
	microSecondDate = new MicroSecondDate(calendar.getTime());
	time = microSecondDate.getFissuresTime();
	updateTime(serverName,
		   serverDNS,
		   time);
    }

    private void updateTime(String serverName,
			    String serverDNS,
			    edu.iris.Fissures.Time time) {
	try {
	    MicroSecondDate ms = new MicroSecondDate(time);
	    
	    updateTimeStmt.setTimestamp(1, ms.getTimestamp());
	    updateTimeStmt.setString(2, serverName);
	    updateTimeStmt.setString(3, serverDNS);
	    updateTimeStmt.executeUpdate();
	} catch(SQLException sqle) {
	    sqle.printStackTrace();
	}
    }
    
  //   public String getServerDNS() {
// 	return this.serverDNS;
//     }

//     public String getServerName() {
// 	return this.serverName;
//     }


    private byte[] getBytes(Object obj) {
	try {
	    ByteArrayOutputStream baos = new ByteArrayOutputStream();
	    ObjectOutputStream oos = new ObjectOutputStream(baos);
	    oos.writeObject(obj);
	    oos.close();
	    byte[] bytes = baos.toByteArray();
	    baos.close();
	    return bytes;
	} catch(Exception e) {
	    e.printStackTrace();
	    return new byte[0];
	}
    }

    private Object getObject(byte[] bytes) {
	try {
	    if(bytes == null) return null;
	    ByteArrayInputStream bais = new ByteArrayInputStream(bytes);
	    ObjectInputStream ois = new ObjectInputStream(bais);
	    Object obj = ois.readObject();
	    bais.close();
	    ois.close();
	    return obj;
	} catch(Exception e) {
	    e.printStackTrace();
	    return null;
	}
    }

    public void close() {
	try {
	    connection.close();
	} catch(SQLException sqle) {
	    sqle.printStackTrace();
	}
				    
    }

    protected  Connection connection;

  //   private String serverDNS;

//     private String serverName;

    private PreparedStatement setTimeStmt;
    
    private PreparedStatement updateTimeStmt;

    private PreparedStatement getTimeStmt;
   
}// AbstractConfigDatabase

