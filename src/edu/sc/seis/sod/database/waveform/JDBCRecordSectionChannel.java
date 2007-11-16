package edu.sc.seis.sod.database.waveform;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import edu.iris.Fissures.network.StationImpl;
import edu.sc.seis.fissuresUtil.database.ConnMgr;
import edu.sc.seis.fissuresUtil.database.JDBCTable;
import edu.sc.seis.fissuresUtil.database.NotFound;
import edu.sc.seis.fissuresUtil.database.network.JDBCStation;
import edu.sc.seis.fissuresUtil.database.util.TableSetup;
import edu.sc.seis.sod.velocity.network.VelocityStation;

public class JDBCRecordSectionChannel extends JDBCTable {

    public JDBCRecordSectionChannel() throws SQLException {
        this(ConnMgr.createConnection());
    }

    public JDBCRecordSectionChannel(Connection conn) throws SQLException {
        super("recsecchannel", conn);

        TableSetup.setup(this, "edu/sc/seis/sod/database/props/default.props");
        String insertStmt = "INSERT INTO "
                + tableName
                + " (recSecId,eq_dbid,channelid,topLeftX,topLeftY,bottomRightX,bottomRightY,best,internalId) VALUES (?, ?, ?, ?, ?, ?,?,?,?)";
        String containsStmt = "SELECT channelid FROM " + tableName
                + " WHERE recSecId = ? AND eq_dbid = ? and channelid = ? and internalid = ?";
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
        insert = conn.prepareStatement(insertStmt);
        contains = conn.prepareStatement(containsStmt);
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

    public boolean contains(String recSecId,
                            int eq_dbid,
                            int channelid,
                            int internalId) throws SQLException {
        contains.setString(1, recSecId);
        contains.setInt(2, eq_dbid);
        contains.setInt(3, channelid);
        contains.setInt(4, internalId);
        ResultSet rs = contains.executeQuery();
        return rs.next();
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

    public List getStations(String recSecId,
                            int eq_dbid,
                            int best,
                            JDBCStation sta) throws SQLException, NotFound {
        getStations.setString(1, recSecId);
        getStations.setInt(2, eq_dbid);
        getStations.setInt(3, best);
        List results = new ArrayList();
        ResultSet rs = getStations.executeQuery();
        while(rs.next()) {
            int sta_dbId = rs.getInt("sta_id");
            results.add(new VelocityStation((StationImpl)sta.get(sta_dbId)));
        }
        return results;
    }

    public int[] getAllChannelDbIds(String recSecId, int eq_dbid)
            throws SQLException {
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
        int i = 0;
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
    public boolean updateChannels(String recSecId,
                               int eq_dbid,
                               int[] newChannelIds,
                               int internalId) throws SQLException {
        Set oldChannels = makeIntegerSet(getChannels(recSecId, eq_dbid, 1));
        Set added = makeIntegerSet(newChannelIds);
        added.removeAll(oldChannels);
        Set removed = oldChannels;
        oldChannels.removeAll(makeIntegerSet(newChannelIds));
        if(added.size() > 0) {
            updateChannelStatus(1, internalId, recSecId, eq_dbid, added);
        }
        if(removed.size() > 0) {
            updateChannelStatus(0, internalId, recSecId, eq_dbid, removed);
        }
        return added.size() > 0 || removed.size() > 0;
    }

    private void updateChannelStatus(int best,
                                     int internalId,
                                     String recSecId,
                                     int eq_dbid,
                                     Set channelIds) throws SQLException {
        StringBuffer sb = new StringBuffer();
        Iterator it = channelIds.iterator();
        while(it.hasNext()) {
            sb.append(it.next() + ",");
        }
        sb.deleteCharAt(sb.length() - 1);
        String updateChannelStmt = "UPDATE " + tableName + " set best = "
                + best + " where internalId = " + internalId
                + " AND recSecId='" + recSecId + "' AND eq_dbid=" + eq_dbid
                + " AND channelid IN (" + sb.toString() + ")";
        Statement st = conn.createStatement();
        st.executeUpdate(updateChannelStmt);
    }

    private Set makeIntegerSet(int[] newChannelIds) {
        Set newChannels = new HashSet(newChannelIds.length);
        for(int i = 0; i < newChannelIds.length; i++) {
            newChannels.add(new Integer(newChannelIds[i]));
        }
        return newChannels;
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

    PreparedStatement insert, contains, getPixelInfo, getChannels,
            getAllChannels, getStations, recSecExists, getInternalId,
            whichRecSecs;

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