/**
 * JDBCEventChannelCookieJar.java
 *
 * @author Created by Omnicore CodeGuide
 */

package edu.sc.seis.sod.database.waveform;

import edu.sc.seis.fissuresUtil.database.ConnMgr;
import edu.sc.seis.fissuresUtil.database.DBUtil;
import edu.sc.seis.fissuresUtil.database.JDBCTable;
import edu.sc.seis.fissuresUtil.database.util.TableSetup;
import edu.sc.seis.sod.CookieJarResult;
import edu.sc.seis.sod.database.SodJDBC;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.io.Serializable;

public class JDBCEventChannelCookieJar extends JDBCTable {

    public JDBCEventChannelCookieJar() throws SQLException{
        this(ConnMgr.createConnection());
    }

    public JDBCEventChannelCookieJar(Connection conn) throws SQLException{
        super("eventchannelcookiejar", conn);
        TableSetup.setup(this, "edu/sc/seis/sod/database/props/default.props");
        insertDouble = conn.prepareStatement("INSERT into eventchannelcookiejar (pairid, name, value) " +
                                                 "VALUES (? , ?, ?)");
        insertString = conn.prepareStatement("INSERT into eventchannelcookiejar (pairid, name, valuestring) " +
                                                 "VALUES (? , ?, ?)");
        insertObject = conn.prepareStatement("INSERT into eventchannelcookiejar (pairid, name, valueobject) " +
                                                 "VALUES (? , ?, ?)");
        updateDouble = conn.prepareStatement("UPDATE eventchannelcookiejar SET value = ? WHERE pairid = ? and name = ?");
        updateString = conn.prepareStatement("UPDATE eventchannelcookiejar SET valuestring = ? WHERE pairid = ? and name = ?");
        updateObject = conn.prepareStatement("UPDATE eventchannelcookiejar SET valueobject = ? WHERE pairid = ? and name = ?");
        get = conn.prepareStatement("SELECT * FROM eventchannelcookiejar WHERE pairid = ? and name = ?");
        getForPair = conn.prepareStatement("SELECT * FROM eventchannelcookiejar WHERE pairid = ?");
        getForName = conn.prepareStatement("SELECT * FROM eventchannelcookiejar WHERE name = ?");
        remove = conn.prepareStatement("DELETE from eventchannelcookiejar WHERE pairid = ? and name = ?");
    }

    /**
     * Method put
     *
     * @param    pairId              an int
     * @param    name                a  String
     * @param    value               a  Serializable
     *
     */
    public void put(int pairId, String name, Serializable value) throws SQLException {
        synchronized(conn) {
        if (get(pairId, name) == null) {
            insertObject.setInt(1, pairId);
            insertObject.setString(2, name);
            insertObject.setObject(3, value);
            insertObject.executeUpdate();
        } else {
            updateObject.setObject(1, value);
            updateObject.setInt(2, pairId);
            updateObject.setString(3, name);
            updateObject.executeUpdate();
        }
        }
    }

    public PreparedStatement prepareStatement(String stmt) throws SQLException {
        return conn.prepareStatement(stmt);
    }

    public CookieJarResult get(int pairId, String name) throws SQLException {
        synchronized(conn) {
        get.setInt(1, pairId);
        get.setString(2, name);
        ResultSet rs = get.executeQuery();
        if (rs.next()) {
            return extract(rs);
        } else {
            return null;
        }
        }
    }

    public List getAllForPair(int pairId) throws SQLException {
        synchronized(conn) {
        get.setInt(1, pairId);
        ResultSet rs = get.executeQuery();
        LinkedList out = new LinkedList();
        while (rs.next()) {
            out.add(extract(rs));
        }
        return out;
        }
    }

    public List getAllForName(String name) throws SQLException {
        synchronized(conn) {
        get.setString(1, name);
        ResultSet rs = get.executeQuery();
        LinkedList out = new LinkedList();
        while (rs.next()) {
            out.add(extract(rs));
        }
        return out;
        }
    }

    public void put(int pairId, String name, double value) throws SQLException{
        synchronized(conn) {
        if (get(pairId, name) == null) {
            insertDouble.setInt(1, pairId);
            insertDouble.setString(2, name);
            insertDouble.setDouble(3, value);
            insertDouble.executeUpdate();
        } else {
            updateDouble.setDouble(1, value);
            updateDouble.setInt(2, pairId);
            updateDouble.setString(3, name);
            updateDouble.executeUpdate();
        }
        }
    }

    public void put(int pairId, String name, String value) throws SQLException{
        synchronized(conn) {
        if (get(pairId, name) == null) {
            insertString.setInt(1, pairId);
            insertString.setString(2, name);
            insertString.setString(3, value);
            insertString.executeUpdate();
        } else {
            updateString.setString(1, value);
            updateString.setInt(2, pairId);
            updateString.setString(3, name);
            updateString.executeUpdate();
        }
        }
    }

    public CookieJarResult extract(ResultSet rs) throws SQLException {
        int pairId = rs.getInt("pairid");
        String name = rs.getString("name");
        String s = rs.getString("valuestring");
        Double d;
        if (s != null) {
            return new CookieJarResult(pairId, name, s);
        } else {
            Object o = rs.getObject("valueObject");
            if (o != null) {
                return new CookieJarResult(pairId, name, o);
            } else {
                return new CookieJarResult(pairId, name, rs.getDouble("value"));
            }
        }
    }

    public boolean containsKey(int pairId, String name) throws SQLException {
        if (get(pairId, name) != null) {
            return true;
        } else {
            return false;
        }
    }


    public String[] getKeys(int pairId) throws SQLException {
        List result = getAllForPair(pairId);
        String[] out = new String[result.size()];
        Iterator it = result.iterator();
        int i=0;
        while (it.hasNext()) {
            CookieJarResult cookie = (CookieJarResult)it.next();
            out[i] = cookie.getName();
            i++;
        }
        return out;
    }


    public CookieJarResult remove(int pairId, String name) throws SQLException {
        synchronized(conn) {
        CookieJarResult out = get(pairId, name);
        remove.setInt(1, pairId);
        remove.setString(2, name);
        remove.executeUpdate();
        return out;
        }
    }


    private PreparedStatement insertDouble, insertString, insertObject, updateDouble, updateString, updateObject, get, getForPair, getForName, remove;


}

