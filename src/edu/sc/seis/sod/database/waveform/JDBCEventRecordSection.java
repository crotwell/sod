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
        String insertStmt = "INSERT INTO "
                + tableName
                + "(recSecId, eventid, imageName) VALUES (?, ?, ?)";
        String getRecSecIdStmt = "SELECT recSecId FROM " + tableName
                + " WHERE eventid=? AND imageName=?";
        String getBestImageforEventStmt = "SELECT imageName FROM " + tableName
                + " WHERE eventid=?";
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
        getBestImageforEvent = conn.prepareStatement(getBestImageforEventStmt);
        getImageforEventChannel = conn.prepareStatement(getImageforEventChannelStmt);
        imageNameExists = conn.prepareStatement(imageNameExistsStmt);
        seq = new JDBCSequence(conn, "RecordSectionSeq");
    }

    public int insert(int eventid, String imageName)
            throws SQLException {
        int recSecId = seq.next();
        insert.setInt(1, recSecId);
        insert.setInt(2, eventid);
        insert.setString(3, imageName);
        insert.executeUpdate();
        return recSecId;
    }

    public String getBestImageforEvent(int eventid) throws SQLException {
        getBestImageforEvent.setInt(1, eventid);
        try {
            ResultSet rs = getBestImageforEvent.executeQuery();
            rs.next();
            return rs.getString(1);
        } catch(SQLException e) {
            throw new RuntimeException("Running the SQL statement"
                    + getBestImageforEvent.toString(), e);
        }
    }

    public String getImageforEventChannel(int eventid, int channelid)
            throws SQLException {
        getImageforEventChannel.setInt(1, eventid);
        getImageforEventChannel.setInt(2, channelid);
        try {
            ResultSet rs = getImageforEventChannel.executeQuery();
            rs.next();
            return rs.getString(1);
        } catch(SQLException e) {
            throw new RuntimeException("Running the SQL statement "
                    + getImageforEventChannel.toString(), e);
        }
    }

    public int getRecSecId(int eventid, String fileName) throws SQLException {
        getRecSecId.setInt(1, eventid);
        getRecSecId.setString(2, fileName);
        try {
            ResultSet rs = getRecSecId.executeQuery();
            rs.next();
            return rs.getInt(1);
        } catch(SQLException e) {
            throw new RuntimeException("Running the SQL statement"
                    + getRecSecId.toString(), e);
        }
    }

    public boolean imageExists(int eventid, String imageName)
            throws SQLException {
        imageNameExists.setInt(1, eventid);
        imageNameExists.setString(2, imageName);
        try {
            ResultSet rs = imageNameExists.executeQuery();
            rs.next();
            int count = rs.getInt(1);
            if(count != 0) { return true; }
        } catch(SQLException e) {
            throw new RuntimeException("Running the SQL statement "
                    + imageNameExists.toString(), e);
        }
        return false;
    }

    private PreparedStatement getBestImageforEvent, insert,
            getImageforEventChannel, imageNameExists, getRecSecId;

    private JDBCSequence seq;
}