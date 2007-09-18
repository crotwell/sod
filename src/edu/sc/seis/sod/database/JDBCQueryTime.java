package edu.sc.seis.sod.database;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import edu.iris.Fissures.model.MicroSecondDate;
import edu.sc.seis.fissuresUtil.database.ConnMgr;
import edu.sc.seis.fissuresUtil.database.DBUtil;
import edu.sc.seis.fissuresUtil.database.JDBCTable;
import edu.sc.seis.fissuresUtil.database.NotFound;
import edu.sc.seis.fissuresUtil.database.util.TableSetup;

public class JDBCQueryTime extends JDBCTable {

    public JDBCQueryTime() throws SQLException {
        super("querytimes", ConnMgr.createConnection());
        TableSetup.setup(this, "edu/sc/seis/sod/database/props/default.props");
        getQuery = conn.prepareStatement("SELECT query_time FROM querytimes WHERE server = ? AND dns = ?");
        putServerDNS = conn.prepareStatement("INSERT into querytimes ( server, dns, query_time) "
                + "VALUES (?, ?, ?)");
        serverDNSExist = conn.prepareStatement("SELECT query_time FROM querytimes WHERE server = ? AND dns = ?");
        setQuery = conn.prepareStatement("UPDATE querytimes SET query_time = ? WHERE server = ? AND dns = ?");
    }

    public void setQuery(String server, String dns, MicroSecondDate time)
            throws SQLException {
        if(!rowExists(server, dns)) {
            insert(server, dns, time);
        } else {
            update(server, dns, time);
        }
    }

    public MicroSecondDate getQuery(String server, String dns)
            throws SQLException, NotFound {
        getQuery.setString(1, server);
        getQuery.setString(2, dns);
        ResultSet rs = getQuery.executeQuery();
        if(rs.next()) {
            return new MicroSecondDate(rs.getTimestamp("query_time"));
        }
        throw new NotFound("no stored query time for " + server + " " + dns);
    }

    private boolean rowExists(String server, String dns) throws SQLException {
        synchronized(conn) {
            serverDNSExist.setString(1, server);
            serverDNSExist.setString(2, dns);
            ResultSet rs = serverDNSExist.executeQuery();
            return rs.next();
        }
    }

    private void update(String server, String dns, MicroSecondDate d)
            throws SQLException {
        synchronized(conn) {
            setQuery.setTimestamp(1, d.getTimestamp());
            setQuery.setString(2, server);
            setQuery.setString(3, dns);
            setQuery.executeUpdate();
        }
    }

    private void insert(String server, String dns, MicroSecondDate d)
            throws SQLException {
        synchronized(conn) {
            putServerDNS.setString(1, server);
            putServerDNS.setString(2, dns);
            putServerDNS.setTimestamp(3, d.getTimestamp());
            putServerDNS.executeUpdate();
        }
    }

    private PreparedStatement getQuery, setQuery, serverDNSExist, putServerDNS;

}
