
package edu.sc.seis.sod.database;

import edu.iris.Fissures.model.*;

import com.sleepycat.db.*;
import java.io.*;
import java.util.*;


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
	
    }
    
    public EventConfigDb(edu.iris.Fissures.Time time, 
			 String serverDNS,
			 String serverName) {
	

	this.serverDNS = serverDNS;
	this.serverName = serverName;
	setTime(time);
    }

    
    public void setTime(edu.iris.Fissures.Time time) {
	open();
	try {
	    Dbt key = new Dbt();
	    String keyStr = new String("time");
	    key.set_data(keyStr.getBytes());
	    key.set_size(keyStr.length());

	    Dbt data = new Dbt();
	    byte[] bytes = getBytes(time);
	    data.set_data(bytes);
	    data.set_size(bytes.length);
	
	    table.put(null, key, data, Db.DB_APPEND);
	    table.close(0);
	} catch(Exception e) {
	    e.printStackTrace();
	}
    }

    public void setServerDNS(String serverDNS) {
	this.serverDNS = serverDNS;
    }

    public void setServerName(String serverName) {

	this.serverName = serverName;
    }


    public edu.iris.Fissures.Time getTime() {
	open();
	try {
	    Dbt data = new Dbt();
	    Dbt key = new Dbt();
	    Dbc dbc = table.cursor(null, 0);
	    dbc.get(key, data, Db.DB_NEXT);
	    Object obj = getObject(data.get_data());
	    dbc.close();
	    table.close(0);
	    
	    return (edu.iris.Fissures.Time)obj;
	} catch(Exception e) {
	    e.printStackTrace();
	    return null;
	}
    }

    public void incrementTime(int days) {
	edu.iris.Fissures.Time time = getTime();
	MicroSecondDate microSecondDate = new MicroSecondDate(time);
	Calendar calendar = Calendar.getInstance();
	calendar.setTime(microSecondDate);
	calendar.roll(Calendar.DAY_OF_MONTH, 1);
	microSecondDate = new MicroSecondDate(calendar.getTime());
	time = microSecondDate.getFissuresTime();
	open();
	try {
	    Dbt data = new Dbt();
	    Dbt key = new Dbt();
	    Dbc dbc = table.cursor(null, 0);
	    dbc.get(key, data, Db.DB_NEXT);
	    dbc.del(0);
	    dbc.close();
	    table.close(0);
	} catch(Exception e) {
	    e.printStackTrace();
	}

	setTime(time);
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

    
    private void open() {

	try {
	    String fileName = "eventconfig.db";
	    table = new Db(null, 0);
	    table.set_error_stream(System.err);
	    table.set_errpfx("EVENT CONFIG");
	    table.open(fileName, null, Db.DB_RECNO, Db.DB_CREATE, 0644);
	} catch(Exception e) {
	    e.printStackTrace();
	}
    }

    private Db table = null;

    private String serverDNS;

    private String serverName;

   
}// EventConfigDb
