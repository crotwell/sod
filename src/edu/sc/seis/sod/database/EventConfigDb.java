
package edu.sc.seis.sod.database;

import edu.iris.Fissures.model.*;

import java.io.*;
import java.util.*;
import java.sql.*;

import org.hsqldb.*;


/**
 * EventConfigDb.java
 *
 *
 * Created: Thu Sep 12 15:33:59 2002
 *
 * @author <a href="mailto:">Srinivasa Telukutla</a>
 * @version
 */

public class EventConfigDb {
    public EventConfigDb (){
	init();
    }
    
    private void init() {
	try {
	    String driverName = "org.hsqldb.jdbcDriver";
	    Class.forName(driverName).newInstance();
	    connection = DriverManager.getConnection("jdbc:hsqldb:myhsqldb", "sa", "");
	    Statement stmt = connection.createStatement();
	    try {
		stmt.executeUpdate("CREATE TABLE timeconfig "+
				   "( time timestamp)");
	    } catch(SQLException sqle) {
		System.out.println("Table timeconfig  is already created");
	    }
	    setTimeStmt = connection.prepareStatement(" INSERT into timeconfig "+
						      " VALUES(?) ");
	    updateTimeStmt = connection.prepareStatement(" UPDATE timeconfig set time = ? ");
	    
	    getTimeStmt = connection.prepareStatement(" SELECT time from timeconfig");
	} catch(Exception e) {
	    e.printStackTrace();
	}
	

    }
    public EventConfigDb(edu.iris.Fissures.Time time, 
			 String serverDNS,
			 String serverName) {
	

	this.serverDNS = serverDNS;
	this.serverName = serverName;
	setTime(time);
    }

    
    public void setTime(edu.iris.Fissures.Time time) {
	try {
	    if(getTime() == null) {
		MicroSecondDate ms = new MicroSecondDate(time);
		setTimeStmt.setTimestamp(1, ms.getTimestamp());
		setTimeStmt.executeUpdate();
	    } else {
		updateTime(time);
	    }
	} catch(SQLException sqle) {
	    sqle.printStackTrace();
	}
    }
    

    public void setServerDNS(String serverDNS) {
	this.serverDNS = serverDNS;
    }

    public void setServerName(String serverName) {

	this.serverName = serverName;
    }


    public edu.iris.Fissures.Time getTime() {
	try {
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

    public void incrementTime(int days) {
	edu.iris.Fissures.Time time = getTime();
	MicroSecondDate microSecondDate = new MicroSecondDate(time);
	Calendar calendar = Calendar.getInstance();
	calendar.setTime(microSecondDate);
	calendar.add(Calendar.DAY_OF_YEAR, days);
	microSecondDate = new MicroSecondDate(calendar.getTime());
	time = microSecondDate.getFissuresTime();
	updateTime(time);
    }

    private void updateTime(edu.iris.Fissures.Time time) {
	try {
	    MicroSecondDate ms = new MicroSecondDate(time);
	    updateTimeStmt.setTimestamp(1, ms.getTimestamp());
	    updateTimeStmt.executeUpdate();
	} catch(SQLException sqle) {
	    sqle.printStackTrace();
	}
    }
    
    public String getServerDNS() {
	return this.serverDNS;
    }

    public String getServerName() {
	return this.serverName;
    }


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

    private Connection connection;

    private String serverDNS;

    private String serverName;

    private PreparedStatement setTimeStmt;
    
    private PreparedStatement updateTimeStmt;

    private PreparedStatement getTimeStmt;
   
}// EventConfigDb
