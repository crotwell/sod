package edu.sc.seis.sod.database;

import java.sql.*;

/**
 * PostgresNetworkDb.java
 *
 *
 * Created: Wed Oct  9 10:14:05 2002
 *
 * @author <a href="mailto:">Srinivasa Telukutla</a>
 * @version
 */

public class PostgresNetworkDb extends AbstractNetworkDatabase{
    public PostgresNetworkDb (Connection connection){
	super(connection);
	this.configDatabase = new PostgresConfigDatabase(connection, getTimeConfigName());
    }
    
    public void create() {
	try {
	    Statement stmt = connection.createStatement();
	    stmt.executeUpdate(" CREATE SEQUENCE networkconfigsequence ");
	    stmt.executeUpdate(" CREATE SEQUENCE stationconfigsequence ");
	    stmt.executeUpdate(" CREATE SEQUENCE siteconfigsequence ");
	    stmt.executeUpdate(" CREATE SEQUENCE channelconfigsequence ");

	    stmt.executeUpdate(" CREATE TABLE networkdatabase "+
			       " ( networkid int PRIMARY KEY DEFAULT nextval('networkconfigsequence'), "+
			       " serverName text, "+
			       " serverDNS text, "+
			       " network_code text, "+
			       " network_time timestamp, "+
			       " nleapseconds int, "+
			       " nqtime timestamp, "+
			       " status int, "+
			       " networkAccessIOR text)");

	    stmt.executeUpdate("CREATE TABLE stationdatabase "+
			       " (stationid int PRIMARY KEY DEFAULT nextval('stationconfigsequence'), "+
			       " networkid int, "+
			       " station_code text, "+
			       " station_time timestamp, "+
			       " stleapseconds int, "+
			       " stqtime timestamp, "+
			       " status int)");
	    stmt.executeUpdate("CREATE TABLE sitedatabase "+
			       " (siteid int PRIMARY KEY DEFAULT nextval('siteconfigsequence'), "+
			       " stationid int, "+
			       " site_code text, "+
			       " site_time timestamp, "+
			       " sleapseconds int, "+
			       " sqtime timestamp, "+
			       " status int)");
	    stmt.executeUpdate("CREATE TABLE channeldatabase "+
			       " (channelid int PRIMARY KEY DEFAULT nextval('channelconfigsequence'), "+
			       " siteid int, "+
			       " channel_code text, "+
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
    
}// PostgresNetworkDb
