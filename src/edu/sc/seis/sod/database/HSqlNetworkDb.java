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
    }
    
    public void create() {
	try {

	    Statement stmt = connection.createStatement();
	    stmt.executeUpdate(" CREATE TABLE networkdatabase "+
			       " networkid int IDENTITY PRIMARY KEY, "+
			       " serverName VARCHAR, "+
			       " serverDNS VARCHAR, "+
			       " network_code VARCHAR, "+
			       " station_code VARCHAR, "+
			       " site_code VARCHAR, "+
			       " channel_code VARCHAR, "+
			       " network_time timestamp, "+
			       " channel_time timestamp, "+
			       " channelIdIOR VARCHAR");
	        
	} catch(SQLException sqle) {
	    sqle.printStackTrace();
	}

    }
    
}// HSqlNetworkDb
