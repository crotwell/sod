package edu.sc.seis.sod.database;

import java.sql.*;
import org.hsqldb.*;
import org.apache.log4j.*;
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
            stmt.executeUpdate(" CREATE CACHED TABLE networkdatabase "+
                                   " (networkid int IDENTITY PRIMARY KEY, "+
                                   " serverName VARCHAR, "+
                                   " serverDNS VARCHAR, "+
                                   " network_code VARCHAR, "+
                                   " network_time timestamp, "+
                                   " nleapseconds int, "+
                                   " nqtime timestamp, "+
                                   " status int, "+
                                   " networkAccessIOR VARCHAR)");
            stmt.executeUpdate("CREATE CACHED TABLE stationdatabase "+
                                   " (stationid int IDENTITY PRIMARY KEY, "+
                                   " networkid int, "+
                                   " station_code VARCHAR, "+
                                   " station_time timestamp, "+
                                   " stleapseconds int, "+
                                   " stqtime timestamp, "+
                                   " status int)");
            stmt.executeUpdate("CREATE CACHED TABLE sitedatabase "+
                                   " (siteid int IDENTITY PRIMARY KEY, "+
                                   " stationid int, "+
                                   " site_code VARCHAR, "+
                                   " site_time timestamp, "+
                                   " sleapseconds int, "+
                                   " sqtime timestamp, "+
                                   " status int)");
            stmt.executeUpdate("CREATE CACHED TABLE channeldatabase "+
                                   " (channelid int IDENTITY PRIMARY KEY, "+
                                   " siteid int, "+
                                   " channel_code VARCHAR, "+
                                   " channel_time timestamp, "+
                                   " cleapseconds int, "+
                                   " cqtime timestamp, "+
                                   " status int)");
            
            
        } catch(SQLException sqle) {
            logger.debug("one or more network database tables are already created");
        }
        
    }
    public ConfigDatabase getConfigDatabase() {
        return this.configDatabase;
    }
    private ConfigDatabase configDatabase;
    
    static Category logger =
        Category.getInstance(HSqlNetworkDb.class.getName());
}// HSqlNetworkDb
