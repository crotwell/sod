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
	    stmt.executeUpdate(" CREATE TABLE networkdatabase "+
			       " ( networkid int PRIMARY KEY DEFAULT nextval('networkconfigsequence'), "+
			       " serverName text, "+
			       " serverDNS text, "+
			       " network_code text, "+
			       " station_code text, "+
			       " site_code text, "+
			       " channel_code text, "+
			       " network_time timestamp, "+
			       " nleapseconds int, "+
			       " channel_time timestamp, "+
			       " cleapseconds int, "+
			       " channelIdIOR text)");
	} catch(SQLException sqle) {
	    sqle.printStackTrace();
	}
    }    
    
    public ConfigDatabase getConfigDatabase() {
	return this.configDatabase;
    }
    
    private ConfigDatabase configDatabase;
    
}// PostgresNetworkDb
