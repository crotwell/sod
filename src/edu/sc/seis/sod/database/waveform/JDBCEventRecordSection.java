package edu.sc.seis.sod.database.waveform;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import edu.sc.seis.fissuresUtil.database.ConnMgr;
import edu.sc.seis.fissuresUtil.database.DBUtil;
import edu.sc.seis.fissuresUtil.database.JDBCSequence;
import edu.sc.seis.sod.database.SodJDBC;

public class JDBCEventRecordSection extends SodJDBC {

    public JDBCEventRecordSection(String tableName, String recChannelTableName)
            throws SQLException {
        this(tableName, recChannelTableName, ConnMgr.createConnection());
    }

    public JDBCEventRecordSection(String tableName, String recChannelTableName,
            Connection conn) throws SQLException {
        String createStmt = "CREATE TABLE "
                + tableName
                + "( recSecId int,  eventid int,  imageName varchar, PRIMARY KEY (recSecId))";
        String insertStmt = "INSERT INTO " + tableName
                + "(recSecId, eventid, imageName) VALUES (?, ?, ?)";
        String getRecSecIdStmt = "SELECT recSecId FROM " + tableName
                + " WHERE eventid=? AND imageName=?";
        String getImageForEventStmt = "SELECT imageName, recSecId FROM "
                + tableName + " WHERE eventid=?";
        String getImageforEventChannelStmt = "SELECT imageName FROM "
                + tableName + " eventRecSec," + recChannelTableName
                + " recSecChannel"
                + " WHERE eventRecSec.recSecId=recSecChannel.recSecId AND "
                + "eventid=? AND channelid=? ";
        String imageNameExistsStmt = "SELECT count(imageName) FROM "
                + tableName + " WHERE eventid=? AND imageName=?";
        if(!DBUtil.tableExists(tableName, conn)) {
            Statement stmt = conn.createStatement();
            stmt.executeUpdate(createStmt);
        }
        insert = conn.prepareStatement(insertStmt);
        getRecSecId = conn.prepareStatement(getRecSecIdStmt);
        getImageForEvent = conn.prepareStatement(getImageForEventStmt);
        getImageforEventChannel = conn.prepareStatement(getImageforEventChannelStmt);
        imageNameExists = conn.prepareStatement(imageNameExistsStmt);
        seq = new JDBCSequence(conn, "RecordSectionSeq");
    }

    public int insert(int eventid, String imageName) throws SQLException {
        int recSecId = seq.next();
        insert.setInt(1, recSecId);
        insert.setInt(2, eventid);
        insert.setString(3, imageName);
        insert.executeUpdate();
        return recSecId;
    }

    public String getImageForEvent(int eventid) throws SQLException {
        return executeGetImageQuery(eventid).getString("imageName");
    }

    public int getRecSecId(int eventId) throws SQLException {
        return executeGetImageQuery(eventId).getInt("recSecId");
    }

    public ResultSet executeGetImageQuery(int eventid) throws SQLException {
        getImageForEvent.setInt(1, eventid);
        ResultSet rs = getImageForEvent.executeQuery();
        rs.next();
        return rs;
    }

    public String getImageforEventChannel(int eventid, int channelid)
            throws SQLException {
        getImageforEventChannel.setInt(1, eventid);
        getImageforEventChannel.setInt(2, channelid);
        ResultSet rs = getImageforEventChannel.executeQuery();
        rs.next();
        return rs.getString(1);
    }

    public int getRecSecId(int eventid, String fileName) throws SQLException {
        getRecSecId.setInt(1, eventid);
        getRecSecId.setString(2, fileName);
        ResultSet rs = getRecSecId.executeQuery();
        rs.next();
        return rs.getInt(1);
    }

    public boolean imageExists(int eventid, String imageName)
            throws SQLException {
        imageNameExists.setInt(1, eventid);
        imageNameExists.setString(2, imageName);
        ResultSet rs = imageNameExists.executeQuery();
        rs.next();
        int count = rs.getInt(1);
        if(count != 0) { return true; }
        return false;
    }

    private PreparedStatement getImageForEvent, insert,
            getImageforEventChannel, imageNameExists, getRecSecId;

    private JDBCSequence seq;
}