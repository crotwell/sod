package edu.sc.seis.sod.database;

import java.sql.*;

import edu.sc.seis.fissuresUtil.database.ConnMgr;
import edu.sc.seis.fissuresUtil.database.DBUtil;
import edu.sc.seis.fissuresUtil.database.NotFound;
import edu.sc.seis.sod.database.SodJDBC;
import java.util.Date;

public class JDBCQueryTime extends SodJDBC{
    public JDBCQueryTime() throws SQLException{
        Connection conn = ConnMgr.createConnection();
        if(!DBUtil.tableExists("querytimes", conn)){
            conn.createStatement().executeUpdate(ConnMgr.getSQL("querytimes.create"));
        }
        getQuery = conn.prepareStatement("SELECT query_time FROM querytimes WHERE server = ? AND dns = ?");
        putServerDNS = conn.prepareStatement("INSERT into querytimes ( server, dns) " +
                                                 "VALUES (?, ?)");
        serverDNSExist = conn.prepareStatement("SELECT query_time FROM querytimes WHERE server = ? AND dns = ?");
        setQuery = conn.prepareStatement("UPDATE querytimes SET query_time = ? WHERE server = ? AND dns = ?");
    }


    public void setQuery(String server, String dns, Date time)
        throws SQLException{
        insert(server, dns);
        setQuery.setTimestamp(1, new Timestamp(time.getTime()));
        setQuery.setString(2, server);
        setQuery.setString(3, dns);
        setQuery.executeUpdate();
    }

    public Date getQuery(String server, String dns) throws SQLException, NotFound{
        getQuery.setString(1, server);
        getQuery.setString(2, dns);
        ResultSet rs = getQuery.executeQuery();
        if(rs.next())return rs.getTimestamp("query_time");
        throw new NotFound("no stored query time for " + server + " " + dns);
    }

    private boolean rowExists(String server, String dns) throws SQLException{
        serverDNSExist.setString(1, server);
        serverDNSExist.setString(2, dns);
        ResultSet rs = serverDNSExist.executeQuery();
        return rs.next();
    }

    private void insert(String server, String dns) throws SQLException{
        if(!rowExists(server, dns)) {
            putServerDNS.setString(1, server);
            putServerDNS.setString(2, dns);
            putServerDNS.executeUpdate();
        }
    }

    private PreparedStatement getQuery, setQuery, serverDNSExist,
        putServerDNS;
}
