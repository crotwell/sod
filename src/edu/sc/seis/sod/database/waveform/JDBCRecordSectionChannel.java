package edu.sc.seis.sod.database.waveform;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;
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
        String createStmt = "CREATE TABLE "
                + tableName
                + " (recSecId int,channelid int,topLeftX double, topLeftY double,bottomRightX double,bottomRightY double)";
        String insertStmt = "INSERT INTO "
                + tableName
                + " (recSecId, channelid,topLeftX,topLeftY,bottomRightX,bottomRightY) VALUES (?, ?, ?, ?, ?, ?)";
        String updateRecordSectionStmt = "UPDATE "
                + tableName
                + " SET  recSecId=?, topLeftX=?, topLeftY=?, bottomRightX=?, bottomRightY=? WHERE recSecId=? and channelid=?";
        String getChannelsStmt = " SELECT channelid FROM " + tableName
                + " WHERE recSecId = ?";
        if(!DBUtil.tableExists(tableName, conn)) {
            Statement stmt = conn.createStatement();
            stmt.executeUpdate(createStmt);
        }
        insert = conn.prepareStatement(insertStmt);
        updateRecordSection = conn.prepareStatement(updateRecordSectionStmt);
        getChannels = conn.prepareStatement(getChannelsStmt);
    }

    public void insert(int recSecId, int channelid, double[] pixelInfo)
            throws SQLException {
        insert.setInt(1, recSecId);
        insert.setInt(2, channelid);
        insert.setDouble(3, pixelInfo[0]);
        insert.setDouble(4, pixelInfo[1]);
        insert.setDouble(5, pixelInfo[2]);
        insert.setDouble(6, pixelInfo[3]);
        insert.executeUpdate();
    }

    public void updateRecordSection(int newRecSecId,
                                    int eventId,
                                    int channelId,
                                    double[] pixelInfo) throws SQLException {
        int curRecSecId = getRecSecId(eventId, channelId);
        updateRecordSection.setInt(1, newRecSecId);
        updateRecordSection.setDouble(2, pixelInfo[0]);
        updateRecordSection.setDouble(3, pixelInfo[1]);
        updateRecordSection.setDouble(4, pixelInfo[2]);
        updateRecordSection.setDouble(5, pixelInfo[3]);
        updateRecordSection.setInt(6, curRecSecId);
        updateRecordSection.setInt(7, channelId);
        updateRecordSection.executeUpdate();
    }

    public double[] getPixelInfo(int eventId, int channelId)
            throws SQLException {
        if(getPixelInfo == null) {
            String getPixelInfoStmt = "select topLeftX,topLeftY,bottomRightX,bottomRightY from "
                    + tableName
                    + " recSecChannel,"
                    + eventRecSecTableName
                    + " eventRecSec WHERE"
                    + "eventRecSec.recSecId=recSecChannel.recSecId  AND eventid=? AND channelid=?";
            getPixelInfo = conn.prepareStatement(getPixelInfoStmt);
        }
        getPixelInfo.setInt(1, eventId);
        getPixelInfo.setInt(2, channelId);
        double[] pixelInfo = {-1, -1, -1, -1};
        ResultSet rs = getPixelInfo.executeQuery();
        if(rs.next()) {
            double topLeftX = rs.getInt(1);
            double topLeftY = rs.getInt(2);
            double bottomRightX = rs.getInt(3);
            double bottomRightY = rs.getInt(4);
            pixelInfo[0] = topLeftX;
            pixelInfo[1] = topLeftY;
            pixelInfo[2] = bottomRightX;
            pixelInfo[3] = bottomRightY;
        }
        return pixelInfo;
    }

    public int[] getChannels(int recSecId) throws SQLException {
        getChannels.setInt(1, recSecId);
        List results = new ArrayList();
        ResultSet rs = getChannels.executeQuery();
        while(rs.next()) {
            results.add(new Integer(rs.getInt("channelId")));
        }
        int[] ints = new int[results.size()];
        for(int i = 0; i < ints.length; i++) {
            ints[i] = ((Integer)results.get(i)).intValue();
        }
        return ints;
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
        ResultSet rs = getRecSecId.executeQuery();
        if(rs.next()) return rs.getInt(1);
        return -1;
    }

    PreparedStatement insert, updateRecordSection, getRecSecId, getPixelInfo,
            getChannels;

    Connection conn;

    String tableName;

    String eventRecSecTableName;
}