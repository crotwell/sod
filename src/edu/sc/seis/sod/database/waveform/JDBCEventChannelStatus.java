package edu.sc.seis.sod.database.waveform;

import edu.iris.Fissures.IfEvent.EventAccessOperations;
import edu.iris.Fissures.IfNetwork.Channel;
import edu.sc.seis.fissuresUtil.cache.CacheEvent;
import edu.sc.seis.fissuresUtil.database.ConnMgr;
import edu.sc.seis.fissuresUtil.database.DBUtil;
import edu.sc.seis.fissuresUtil.database.JDBCSequence;
import edu.sc.seis.fissuresUtil.database.NotFound;
import edu.sc.seis.fissuresUtil.database.event.JDBCEventAccess;
import edu.sc.seis.fissuresUtil.database.network.JDBCChannel;
import edu.sc.seis.fissuresUtil.exceptionHandler.GlobalExceptionHandler;
import edu.sc.seis.sod.EventChannelPair;
import edu.sc.seis.sod.NetworkArm;
import edu.sc.seis.sod.Start;
import edu.sc.seis.sod.Status;
import edu.sc.seis.sod.WaveformArm;
import edu.sc.seis.sod.database.ChannelDbObject;
import edu.sc.seis.sod.database.EventDbObject;
import edu.sc.seis.sod.database.SodJDBC;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

public class JDBCEventChannelStatus extends SodJDBC{
    public JDBCEventChannelStatus() throws SQLException{
        Connection conn = ConnMgr.createConnection();
        if(!DBUtil.tableExists("eventchannelstatus", conn)){
            Statement stmt = conn.createStatement();
            stmt.executeUpdate(ConnMgr.getSQL("eventchannelstatus.create"));
        }
        seq = new JDBCSequence(conn, "EventChannelSeq");
        chanTable = new JDBCChannel(conn);
        eventTable = new JDBCEventAccess(conn);
        insert = conn.prepareStatement("INSERT into eventchannelstatus (pairid, eventid, channelid) " +
                                           "VALUES (? , ?, ?)");
        getPairId = conn.prepareStatement("SELECT pairid FROM eventchannelstatus WHERE eventid = ? and channelid = ?");
        setStatus = conn.prepareStatement("UPDATE eventchannelstatus SET status = ? where pairid = ?");
        getAll = conn.prepareStatement("SELECT * FROM eventchannelstatus");
        getForEvent = conn.prepareStatement("SELECT * FROM eventchannelstatus WHERE eventid = ?");
        getPair = conn.prepareStatement("SELECT * FROM eventchannelstatus WHERE pairid = ?");
        getForStatus = conn.prepareStatement("SELECT COUNT(*) FROM eventchannelstatus WHERE status = ?");
        getEventForStatus = conn.prepareStatement("SELECT * FROM eventchannelstatus WHERE status = ? AND eventid = ?");
    }

    public int put(EventChannelPair ecp) throws SQLException{
        return put(ecp.getEvent(), ecp.getChannel(), ecp.getStatus());
    }

    public int put(EventAccessOperations event, Channel chan, Status stat) throws SQLException{
        int eventId = eventTable.put(event, null, null, null);
        int chanId = chanTable.put(chan);
        return put(eventId, chanId, stat);
    }

    public int put(int eventId, int chanId, Status stat) throws SQLException{
        int pairId = insert(eventId, chanId);
        setStatus(pairId, stat);
        return pairId;
    }

    public int getNumOfStatus(Status s) throws SQLException{
        getForStatus.setInt(1, s.getAsShort());
        ResultSet rs = getForStatus.executeQuery();
        rs.next();
        return rs.getInt(1);
    }

    public EventChannelPair[] getAll()throws SQLException{
        return extractECPs(getAll.executeQuery());
    }

    public EventChannelPair[] getAll(int eventId)throws SQLException{
        getForEvent.setInt(1, eventId);
        return extractECPs(getForEvent.executeQuery());
    }

    public EventChannelPair[] getAll(CacheEvent ev)throws SQLException, NotFound{
        return getAll(eventTable.getDBId(ev));
    }

    public EventChannelPair[] getAll(EventAccessOperations ev, Status status) throws NotFound, SQLException {
        int evId = eventTable.getDBId(ev);
        getEventForStatus.setInt(1, status.getAsShort());
        getEventForStatus.setInt(2, evId);
        return extractECPs(getEventForStatus.executeQuery());
    }

    private EventChannelPair[] extractECPs(ResultSet rs) throws SQLException{
        List pairs = new ArrayList();
        while(rs.next()){
            try {
                pairs.add(extractECP(rs, null));
            } catch (NotFound e) {
                GlobalExceptionHandler.handle("This NotFound was thrown while extracting event channel pairs from a result set.  The ids in the result set should be accurate, so this means something is screwy.",
                                              e);
            }
        }
        return (EventChannelPair[])pairs.toArray(new EventChannelPair[0]);
    }

    public EventChannelPair get(int pairId, WaveformArm owner)throws NotFound,
        SQLException{
        getPair.setInt(1, pairId);
        ResultSet rs = getPair.executeQuery();
        if(rs.next()) {return extractECP(rs, owner); }
        throw new NotFound("No such pairId: " + pairId + " in the event channel db");
    }

    private EventChannelPair extractECP(ResultSet rs, WaveformArm owner)
        throws SQLException, NotFound{
        Status s = Status.getFromShort((short)rs.getInt("status"));
        int eventId = rs.getInt("eventid");
        CacheEvent event = eventTable.getEvent(eventId);
        int chanId = rs.getInt("channelid");
        NetworkArm na = Start.getNetworkArm();

        Channel chan = null;
        if(na != null){ chan = na.getChannel(chanId); }
        if(chan == null){ chan = chanTable.get(chanId); }
        EventChannelPair cur = new EventChannelPair(new EventDbObject(eventId, event),
                                                    new ChannelDbObject(chanId, chan),
                                                    owner,
                                                    rs.getInt("pairid"),
                                                    s);
        return cur;
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
        setStatus.setInt(1, status.getAsShort());
        setStatus.setInt(2, pairId);
        setStatus.executeUpdate();
    }

    private PreparedStatement insert, setStatus, getPairId, getAll, getForEvent,
        getPair, getForStatus, getEventForStatus;

    private JDBCSequence seq;
    private JDBCEventAccess eventTable;
    private JDBCChannel chanTable;
}

