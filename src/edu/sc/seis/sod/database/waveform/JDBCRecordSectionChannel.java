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

    public JDBCRecordSectionChannel() throws SQLException {
        this(ConnMgr.createConnection());
    }

    public JDBCRecordSectionChannel(Connection conn)
            throws SQLException {
        this.conn = conn;
        String createStmt = "CREATE TABLE "
            + tableName
            + " (recSecId varchar, eq_dbid int, channelid int,topLeftX double, topLeftY double,bottomRightX double,"
            + "bottomRightY double, best int)";
        String insertStmt = "INSERT INTO "
                + tableName
                + " (recSecId,eq_dbid,channelid,topLeftX,topLeftY,bottomRightX,bottomRightY,best) VALUES (?, ?, ?, ?, ?, ?,?,?)";
        String getChannelsStmt = " SELECT channelid FROM " + tableName
                + " WHERE recSecId ='"+"?"+"' and eq_dbid=? and best=?";
        String channelExistsStmt = "SELECT count(channelid) from " + tableName + " where  recSecId ='"+"?"+"' AND eq_dbid=? AND channelid=?";
        if(!DBUtil.tableExists(tableName, conn)) {
            Statement stmt = conn.createStatement();
            stmt.executeUpdate(createStmt);
        }
        insert = conn.prepareStatement(insertStmt);
        getChannels = conn.prepareStatement(getChannelsStmt);
        channelExists = conn.prepareStatement(channelExistsStmt);
    }

    public void insert(String recSecId,
                       int eq_dbid,
                       int channelid,
                       double[] pixelInfo,
                       int best) throws SQLException {
        insert.setString(1, recSecId);
        insert.setInt(2, eq_dbid);
        insert.setInt(3, channelid);
        insert.setDouble(4, pixelInfo[0]);
        insert.setDouble(5, pixelInfo[1]);
        insert.setDouble(6, pixelInfo[2]);
        insert.setDouble(7, pixelInfo[3]);
        insert.setInt(8, best);
        insert.executeUpdate();
    }

    public double[] getPixelInfo(String recSecId, int eq_dbid, int channelId)
            throws SQLException {
        if(getPixelInfo == null) {
            String getPixelInfoStmt = "select topLeftX,topLeftY,bottomRightX,bottomRightY from "
                    + tableName
                    + " WHERE"
                    + " recSecId=? AND eq_dbid=? AND channelid=?";
            getPixelInfo = conn.prepareStatement(getPixelInfoStmt);
        }
        getPixelInfo.setString(1, recSecId);
        getPixelInfo.setInt(2, eq_dbid);
        getPixelInfo.setInt(3, channelId);
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

    public int[] getChannels(String recSecId,int eq_dbid, int best) throws SQLException {
        getChannels.setString(1, recSecId);
        getChannels.setInt(2,eq_dbid);
        getChannels.setInt(3,best);
        List results = new ArrayList();
        ResultSet rs = getChannels.executeQuery();
        while(rs.next()) {
            results.add(new Integer(rs.getInt("channelId")));
        }
        int[] channels = new int[results.size()];
        for(int i = 0; i < channels .length; i++) {
            channels[i] = ((Integer)results.get(i)).intValue();
        }
        return channels;
    }
    
    public boolean channelExists(String recSecId, int eq_dbid, int channelid) throws SQLException{
        channelExists.setString(1, recSecId);
        channelExists.setInt(2, eq_dbid);
        channelExists.setInt(3, channelid);
        ResultSet rs = channelExists.executeQuery();
        while(rs.next()){
            if(rs.getInt(1) != 0){
                return true;
            }
        }
        return false;
    }
    
   public void updateChannels(String recSecId, int eq_dbid, int[] channelIds) throws SQLException{
    String updateChannelStmt = "UPDATE " + tableName + " set best=0 where recSecId='"+recSecId+"' AND eq_dbid=" +eq_dbid+
    		" AND channelid NOT IN (";
    String updateBestChannelStmt = "UPDATE " + tableName + " set best=1 where recSecId='"+recSecId+"' AND eq_dbid=" +eq_dbid+
	" AND channelid IN (";
    StringBuffer sb = new StringBuffer();
    for(int i=0;i<channelIds.length;i++){
        sb.append(channelIds[i] +",");
        if(i == channelIds.length-1){
            sb.deleteCharAt(sb.length()-1);
        }
    }
    String channelsString = sb.toString();
    updateChannelStmt += channelsString+")";
    updateBestChannelStmt += channelsString+")";
    Statement st = conn.createStatement();
    st.executeUpdate(updateChannelStmt);
    st.executeUpdate(updateBestChannelStmt);
   }

    PreparedStatement insert, getPixelInfo, getChannels, channelExists;

    Connection conn;
    
    String tableName = "recsecchannel";
}