package edu.sc.seis.sod.database.waveform;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import edu.sc.seis.fissuresUtil.database.ConnMgr;
import edu.sc.seis.fissuresUtil.database.DBUtil;
import edu.sc.seis.sod.database.SodJDBC;

public class JDBCRecordSectionChannel extends SodJDBC {

    public JDBCRecordSectionChannel(String tableName,
            String eventRecSecTableName) throws SQLException {
        this(tableName, eventRecSecTableName, ConnMgr.createConnection());
    }

    public JDBCRecordSectionChannel(String tableName,
            String eventRecSecTableName, Connection conn) throws SQLException {
        this.conn = conn;
        this.tableName = tableName;
        this.eventRecSecTableName = eventRecSecTableName;
        String createStmt = "CREATE TABLE " + tableName
                + "(recSecId int,channelid int)";
        String insertStmt = "INSERT INTO " + tableName
                + "(recSecId, channelid) VALUES (?, ?)";
        String updateRecordSectionStmt = "UPDATE " + tableName
                + " SET  recSecId=? WHERE recSecId=? and channelid=?";
        if(!DBUtil.tableExists(tableName, conn)) {
            Statement stmt = conn.createStatement();
            stmt.executeUpdate(createStmt);
        }
        insert = conn.prepareStatement(insertStmt);
        updateRecordSection = conn.prepareStatement(updateRecordSectionStmt);
    }

    public void insert(int recSecId, int channelid) throws SQLException {
        insert.setInt(1, recSecId);
        insert.setInt(2, channelid);
        insert.executeUpdate();
    }

    public void updateRecordSection(int newRecSecId, int eventId, int channelId)
            throws SQLException {
        try {
            int curRecSecId = getRecSecId(eventId, channelId);
            updateRecordSection.setInt(1, newRecSecId);
            updateRecordSection.setInt(2, curRecSecId);
            updateRecordSection.setInt(3, channelId);
            updateRecordSection.executeUpdate();
        } catch(SQLException e) {
            throw new RuntimeException("Running the SQL query "
                    + updateRecordSection.toString());
        }
    }

    public boolean channelExists(int eventId, int channelId)
            throws SQLException {
        if(getRecSecId(eventId, channelId) != -1) { return true; }
        return false;
    }

    public int getRecSecId(int eventId, int channelId) throws SQLException {
        if(getRecSecId == null) {
            String getRecSecIdStmt = "SELECT recSecId FROM "
                    + tableName
                    + " recSecChannel,"
                    + eventRecSecTableName
                    + " eventRecSec WHERE "
                    + "eventRecSec.recSecId=recSecChannel.recSecId AND eventid=? AND channelid=?";
            getRecSecId = conn.prepareStatement(getRecSecIdStmt);
        }
        getRecSecId.setInt(1, eventId);
        getRecSecId.setInt(2, channelId);
        try {
            ResultSet rs = getRecSecId.executeQuery();
            if(rs.next()) return rs.getInt(1);
            return -1;
        } catch(SQLException e) {
            throw new RuntimeException("Running the SQL query "
                    + getRecSecId.toString());
        }
    }

    PreparedStatement insert, updateRecordSection, getRecSecId;

    Connection conn;

    String tableName;

    String eventRecSecTableName;
}