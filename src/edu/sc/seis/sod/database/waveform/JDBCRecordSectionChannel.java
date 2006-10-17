package edu.sc.seis.sod.database.waveform;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import edu.sc.seis.fissuresUtil.database.ConnMgr;
import edu.sc.seis.fissuresUtil.database.DBUtil;
import edu.sc.seis.fissuresUtil.database.NotFound;
import edu.sc.seis.fissuresUtil.database.network.JDBCStation;
import edu.sc.seis.sod.database.SodJDBC;
import edu.sc.seis.sod.velocity.network.VelocityStation;

public class JDBCRecordSectionChannel extends SodJDBC {

    public JDBCRecordSectionChannel() throws SQLException {
        this(ConnMgr.createConnection());
    }

    public JDBCRecordSectionChannel(Connection conn) throws SQLException {
        this.conn = conn;
        String createStmt = "CREATE TABLE "
                + tableName
                + " (recSecId varchar, eq_dbid int, channelid int,topLeftX double, topLeftY double,bottomRightX double,"
                + "bottomRightY double, best int, internalId int)";
        String insertStmt = "INSERT INTO "
                + tableName
                + " (recSecId,eq_dbid,channelid,topLeftX,topLeftY,bottomRightX,bottomRightY,best,internalId) VALUES (?, ?, ?, ?, ?, ?,?,?,?)";
        String getChannelsStmt = " SELECT channelid FROM " + tableName
                + " WHERE recSecId=? and eq_dbid=? and best=?";
        String getAllChannelsStmt = " SELECT channelid FROM " + tableName
        + " WHERE recSecId=? and eq_dbid=?";
        String getStationsStmt = " SELECT sta_id FROM "
                + tableName
                + " JOIN channel ON (channelid = chan_id) JOIN site ON (channel.site_id = site.site_id)"
                + " WHERE recSecId=? and eq_dbid=? and best=?";
        String recSecExistsStmt = "SELECT TOP 1 recSecId from " + tableName
                + " where  recSecId=? AND eq_dbid=? ";
        String getInternalIdStmt = "SELECT internalId from " + tableName
                + " where  recSecId=? AND eq_dbid=? ";
        String whichRecSecsStmt = "SELECT DISTINCT recsecid FROM " + tableName
                + " WHERE eq_dbid = ?";
        if(!DBUtil.tableExists(tableName, conn)) {
            Statement stmt = conn.createStatement();
            stmt.executeUpdate(createStmt);
            stmt.executeUpdate("CREATE INDEX rec_eq_index ON " + tableName
                    + " (eq_dbid)");
            stmt.executeUpdate("CREATE INDEX rec_best_eq ON " + tableName
                    + " (recsecid, eq_dbid, best)");
        }
        insert = conn.prepareStatement(insertStmt);
        getChannels = conn.prepareStatement(getChannelsStmt);
        getAllChannels = conn.prepareStatement(getAllChannelsStmt);
        getStations = conn.prepareStatement(getStationsStmt);
        recSecExists = conn.prepareStatement(recSecExistsStmt);
        whichRecSecs = conn.prepareStatement(whichRecSecsStmt);
        getInternalId = conn.prepareStatement(getInternalIdStmt);
    }

    public void insert(String recSecId,
                       int eq_dbid,
                       int channelid,
                       double[] pixelInfo,
                       int best,
                       int internalId) throws SQLException {
        insert.setString(1, recSecId);
        insert.setInt(2, eq_dbid);
        insert.setInt(3, channelid);
        insert.setDouble(4, pixelInfo[0]);
        insert.setDouble(5, pixelInfo[1]);
        insert.setDouble(6, pixelInfo[2]);
        insert.setDouble(7, pixelInfo[3]);
        insert.setInt(8, best);
        insert.setInt(9, internalId);
        insert.executeUpdate();
    }

    public double[] getPixelInfo(String recSecId, int eq_dbid, int channelId)
            throws SQLException {
        throw new UnsupportedOperationException("Pixel information isn't being inserted at the moment because JD's implementation is ridiculously memory intensive");
        // if(getPixelInfo == null) {
        // String getPixelInfoStmt = "select
        // topLeftX,topLeftY,bottomRightX,bottomRightY from "
        // + tableName
        // + " WHERE"
        // + " recSecId=? AND eq_dbid=? AND channelid=?";
        // getPixelInfo = conn.prepareStatement(getPixelInfoStmt);
        // }
        // getPixelInfo.setString(1, recSecId);
        // getPixelInfo.setInt(2, eq_dbid);
        // getPixelInfo.setInt(3, channelId);
        // double[] pixelInfo = {-1, -1, -1, -1};
        // ResultSet rs = getPixelInfo.executeQuery();
        // if(rs.next()) {
        // double topLeftX = rs.getInt(1);
        // double topLeftY = rs.getInt(2);
        // double bottomRightX = rs.getInt(3);
        // double bottomRightY = rs.getInt(4);
        // pixelInfo[0] = topLeftX;
        // pixelInfo[1] = topLeftY;
        // pixelInfo[2] = bottomRightX;
        // pixelInfo[3] = bottomRightY;
        // }
        // return pixelInfo;
    }

    public int[] getChannels(String recSecId, int eq_dbid, int best)
            throws SQLException {
        getChannels.setString(1, recSecId);
        getChannels.setInt(2, eq_dbid);
        getChannels.setInt(3, best);
        List results = new ArrayList();
        ResultSet rs = getChannels.executeQuery();
        while(rs.next()) {
            results.add(new Integer(rs.getInt("channelId")));
        }
        int[] channels = new int[results.size()];
        for(int i = 0; i < channels.length; i++) {
            channels[i] = ((Integer)results.get(i)).intValue();
        }
        return channels;
    }

    public List getStations(String recSecId, int eq_dbid, int best, JDBCStation sta)
            throws SQLException, NotFound {
        getStations.setString(1, recSecId);
        getStations.setInt(2, eq_dbid);
        getStations.setInt(3, best);
        List results = new ArrayList();
        ResultSet rs = getStations.executeQuery();
        while(rs.next()) {
            int sta_dbId = rs.getInt("sta_id");
            results.add(new VelocityStation(sta.get(sta_dbId), sta_dbId));
        }
        return results;
    }
    
    public int[] getAllChannelDbIds(String recSecId, int eq_dbid) throws SQLException {
        int index = 1;
        getAllChannels.setString(index++, recSecId);
        getAllChannels.setInt(index++, eq_dbid);
        ResultSet rs = getAllChannels.executeQuery();
        ArrayList out = new ArrayList();
        while(rs.next()) {
            out.add(new Integer(rs.getInt(1)));
        }
        Iterator it = out.iterator();
        int[] intOut = new int[out.size()];
        int i=0;
        while(it.hasNext()) {
            intOut[i] = ((Integer)it.next()).intValue();
            i++;
        }
        return intOut;
    }

    /*
     * Set the value in best column to 1 for channels that go into the best
     * recordsection and 0 for other channels
     */
    public void updateChannels(String recSecId,
                               int eq_dbid,
                               int[] channelIds,
                               int internalId) throws SQLException {
        String updateChannelStmt = "UPDATE " + tableName
                + " set best=0 where internalId = " + internalId
                + " AND recSecId='" + recSecId + "' AND eq_dbid=" + eq_dbid
                + " AND channelid NOT IN (";
        String updateBestChannelStmt = "UPDATE " + tableName
                + " set best=1 where internalId = " + internalId
                + " AND recSecId='" + recSecId + "' AND eq_dbid=" + eq_dbid
                + " AND channelid IN (";
        StringBuffer sb = new StringBuffer();
        for(int i = 0; i < channelIds.length; i++) {
            sb.append(channelIds[i] + ",");
            if(i == channelIds.length - 1) {
                sb.deleteCharAt(sb.length() - 1);
            }
        }
        String channelsString = sb.toString();
        updateChannelStmt += channelsString + ")";
        updateBestChannelStmt += channelsString + ")";
        Statement st = conn.createStatement();
        st.executeUpdate(updateChannelStmt);
        st.executeUpdate(updateBestChannelStmt);
    }

    public boolean recSecExists(int eventDbId, String recSecId)
            throws SQLException {
        recSecExists.setString(1, recSecId);
        recSecExists.setInt(2, eventDbId);
        return recSecExists.executeQuery().next();
    }

    public String[] whichReqSecs(int eventDbId) throws SQLException {
        whichRecSecs.setInt(1, eventDbId);
        ResultSet rs = whichRecSecs.executeQuery();
        List results = new ArrayList(3);
        while(rs.next()) {
            results.add(rs.getString("recsecid"));
        }
        return (String[])results.toArray(new String[results.size()]);
    }

    public Connection getConnection() {
        return conn;
    }

    PreparedStatement insert, getPixelInfo, getChannels, getAllChannels, getStations,
            recSecExists, getInternalId, whichRecSecs;

    Connection conn;

    String tableName = "recsecchannel";

    public int getInternalId(int eventDbId, String recSecId)
            throws SQLException, NotFound {
        getInternalId.setString(1, recSecId);
        getInternalId.setInt(2, eventDbId);
        ResultSet rs = getInternalId.executeQuery();
        if(rs.next()) {
            return rs.getInt("internalId");
        }
        throw new NotFound("No event " + eventDbId + " with recSecId "
                + recSecId);
    }
}