package edu.sc.seis.sod.database.event;

import java.sql.*;

import edu.sc.seis.fissuresUtil.database.ConnMgr;
import edu.sc.seis.fissuresUtil.database.DBUtil;
import edu.sc.seis.sod.database.SodJDBC;
import edu.sc.seis.sod.subsetter.eventArm.EventTimeRange;
import java.util.Date;

public class JDBCEventQueryTime extends SodJDBC{
    public JDBCEventQueryTime() throws SQLException{
        Connection conn = ConnMgr.getConnection();
        if(!DBUtil.tableExists("eventquerytimes", conn)){
            Statement stmt = conn.createStatement();
            stmt.executeUpdate(ConnMgr.getSQL("eventquerytimes.create"));
        }
        getEnd = conn.prepareStatement("SELECT endtime FROM eventquerytimes WHERE server = ? AND dns = ?");
        getStart = conn.prepareStatement("SELECT starttime FROM eventquerytimes WHERE server = ? AND dns = ?");
        putServerDNS = conn.prepareStatement("INSERT into eventquerytimes ( server, dns) " +
                                                 "VALUES (?, ?)");
        serverDNSExist = conn.prepareStatement("SELECT * FROM eventquerytimes WHERE server = ? AND dns = ?");
        setEnd = conn.prepareStatement("UPDATE eventquerytimes SET endtime = ? WHERE server = ? AND dns = ?");
        setStart = conn.prepareStatement("UPDATE eventquerytimes SET starttime = ? WHERE server = ? AND dns = ?");
    }
    
    public Date getEnd(String server, String dns) throws SQLException{
        return getTime(server, dns, "endtime", getEnd);
    }
    
    public Date getStart(String server, String dns) throws SQLException{
        return getTime(server, dns, "starttime", getStart);
    }
    
    private static Timestamp getTime(String server, String dns, String columnName,
                                     PreparedStatement ps)throws SQLException{
        ps.setString(1, server);
        ps.setString(2, dns);
        ResultSet rs = ps.executeQuery();
        if(rs.next())return rs.getTimestamp(columnName);
        return null;
    }
    
    public synchronized void setStart(String server, String dns, Date start)
        throws SQLException{
        updateTimeColumn(setStart, server, dns, start);
    }
    
    public synchronized void setEnd(String server, String dns, Date end)
        throws SQLException{
        updateTimeColumn(setEnd, server, dns, end);
    }
    
    private void updateTimeColumn(PreparedStatement updateStmt, String server,
                                  String dns, Date time) throws SQLException{
        insert(server, dns);
        updateStmt.setTimestamp(1, new Timestamp(time.getTime()));
        updateStmt.setString(2, server);
        updateStmt.setString(3, dns);
        updateStmt.executeUpdate();
    }
    
    public synchronized void setTimes(String server, String dns, Date start,
                                      Date end) throws SQLException{
        setStart(server, dns, start);
        setEnd(server, dns, end);
    }
    
    public void setTimes(String server, String dns, EventTimeRange times) throws SQLException{
        setTimes(server, dns, times.getStartMSD(), times.getEndMSD());
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
    
    public PreparedStatement getEnd, getStart, setStart, setEnd, serverDNSExist,
        putServerDNS;
}
