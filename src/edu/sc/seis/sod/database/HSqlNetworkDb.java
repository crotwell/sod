package edu.sc.seis.sod.database;

import java.sql.*;
import org.hsqldb.*;
/**
 * HSqlNetworkDb.java
 *
 *
 * Created: Wed Oct  9 10:33:06 2002
 *
 * @author <a href="mailto:">Srinivasa Telukutla</a>
 * @version
 */

public class HSqlNetworkDb extends AbstractNetworkDatabase{
    public HSqlNetworkDb (Connection connection){
	super(connection);
	this.configDatabase = new HSqlConfigDatabase(connection, getTimeConfigName());
    }
    
    public void create() {
	try {

	    Statement stmt = connection.createStatement();
	    stmt.executeUpdate(" CREATE TABLE networkdatabase "+
			       " (networkid int IDENTITY PRIMARY KEY, "+
			       " serverName VARCHAR, "+
			       " serverDNS VARCHAR, "+
			       " network_code VARCHAR, "+
			       " network_time timestamp, "+
			       " nleapseconds int, "+
			       " nqtime timestamp, "+
			       " status int, "+
			       " networkAccessIOR VARCHAR)");
	    stmt.executeUpdate("CREATE TABLE stationdatabase "+
			       " (stationid int IDENTITY PRIMARY KEY, "+
			       " networkid int, "+
			       " station_code VARCHAR, "+
			       " station_time timestamp, "+
			       " stleapseconds int, "+
			       " stqtime timestamp, "+
			       " status int)");
	    stmt.executeUpdate("CREATE TABLE sitedatabase "+
			       " (siteid int IDENTITY PRIMARY KEY, "+
			       " stationid int, "+
			       " site_code VARCHAR, "+
			       " site_time timestamp, "+
			       " sleapseconds int, "+
			       " sqtime timestamp, "+
			       " status int)");
	    stmt.executeUpdate("CREATE TABLE channeldatabase "+
			       " (channelid int IDENTITY PRIMARY KEY, "+
			       " siteid int, "+
			       " channel_code VARCHAR, "+
			       " channel_time timestamp, "+
			       " cleapseconds int, "+
			       " cqtime timestamp, "+
			       " status int)");
			       
	        
	} catch(SQLException sqle) {
	    sqle.printStackTrace();
	}

    }
    public ConfigDatabase getConfigDatabase() {
	return this.configDatabase;
    }
    private ConfigDatabase configDatabase;
    
}// HSqlNetworkDb
