/**
 * JDBCEventChannelStatus.java
 *
 * @author Created by Omnicore CodeGuide
 */

package edu.sc.seis.sod.database.waveform;

import edu.sc.seis.fissuresUtil.database.ConnMgr;
import edu.sc.seis.fissuresUtil.database.DBUtil;
import edu.sc.seis.fissuresUtil.database.JDBCSequence;
import edu.sc.seis.fissuresUtil.database.NotFound;
import edu.sc.seis.sod.Status;
import edu.sc.seis.sod.database.SodJDBC;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

public class JDBCEventChannelStatus extends SodJDBC{
    public JDBCEventChannelStatus() throws SQLException{
        Connection conn = ConnMgr.createConnection();
        if(!DBUtil.tableExists("eventchannelstatus", conn)){
            Statement stmt = conn.createStatement();
            stmt.executeUpdate(ConnMgr.getSQL("eventchannelstatus.create"));
        }
        seq = new JDBCSequence(conn, "EventChannelSeq");
        insert = conn.prepareStatement("INSERT into eventchannelstatus (pairid, eventid, channelid) " +
                                           "VALUES (? , ?, ?)");
        getPairId = conn.prepareStatement("SELECT pairid FROM eventchannelstatus WHERE eventid = ? and channelid = ?");
        setStatus = conn.prepareStatement("UPDATE eventchannelstatus SET status = ? where pairid = ?");
        getEventAndChanId = conn.prepareStatement("SELECT eventid, channelid FROM eventchannelstatus WHERE pairid = ?");
    }

    public int getEventId(int pairId) throws NotFound, SQLException{
        return getEventAndChanIds(pairId)[0];
    }

    public int getChanId(int pairId) throws NotFound, SQLException{
        return getEventAndChanIds(pairId)[1];
    }

    /**
     * @return   an int[] with the event id in the 0th position and the chan id
     *  in the 1st position
     */
    public int[] getEventAndChanIds(int pairId) throws NotFound, SQLException{
        getEventAndChanId.setInt(1, pairId);
        ResultSet rs = getEventAndChanId.executeQuery();
        if(rs.next()){
            int[] evAndChanId = new int[2];
            evAndChanId[0] = rs.getInt("eventid");
            evAndChanId[1] = rs.getInt("channelid");
            return evAndChanId;
        }
        throw new NotFound("No such pair id " + pairId + " in the event channel pair db");
    }

    public int put(int eventId, int chanId, Status stat) throws SQLException{
        int pairId = insert(eventId, chanId);
        setStatus(pairId, stat);
        return pairId;
    }

    public int insert(int eventId, int chanId) throws SQLException{
        try {
            return getPairId(eventId, chanId);
        } catch (NotFound e) {
            int pairId = seq.next();
            insert.setInt(1, pairId);
            insert.setInt(2, eventId);
            insert.setInt(3, chanId);
            insert.executeUpdate();
            return pairId;
        }
    }

    private int getPairId(int eventId, int chanId) throws NotFound, SQLException{
        getPairId.setInt(1, eventId);
        getPairId.setInt(2, chanId);
        ResultSet rs = getPairId.executeQuery();
        if(rs.next())return rs.getInt("pairid");
        throw new NotFound("No event and channel pair");
    }

    public void setStatus(int pairId, Status status) throws SQLException{
        setStatus.setShort(1, status.getAsShort());
        setStatus.setInt(2, pairId);
        setStatus.executeUpdate();
    }

    private PreparedStatement insert, setStatus, getPairId,
        getEventAndChanId;

    private JDBCSequence seq;
}

