package edu.sc.seis.sod.database.waveform;

import edu.sc.seis.sod.*;

import edu.iris.Fissures.IfEvent.EventAccessOperations;
import edu.iris.Fissures.IfNetwork.Channel;
import edu.iris.Fissures.IfNetwork.Station;
import edu.sc.seis.fissuresUtil.cache.CacheEvent;
import edu.sc.seis.fissuresUtil.database.ConnMgr;
import edu.sc.seis.fissuresUtil.database.DBUtil;
import edu.sc.seis.fissuresUtil.database.JDBCSequence;
import edu.sc.seis.fissuresUtil.database.NotFound;
import edu.sc.seis.fissuresUtil.database.event.JDBCEventAccess;
import edu.sc.seis.fissuresUtil.database.network.JDBCChannel;
import edu.sc.seis.fissuresUtil.database.network.JDBCStation;
import edu.sc.seis.fissuresUtil.exceptionHandler.GlobalExceptionHandler;
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
        this(ConnMgr.createConnection());
    }

    public JDBCEventChannelStatus(Connection conn) throws SQLException{
        this.conn = conn;
        if(!DBUtil.tableExists("eventchannelstatus", conn)){
            Statement stmt = conn.createStatement();
            stmt.executeUpdate(ConnMgr.getSQL("eventchannelstatus.create"));
        }
        seq = new JDBCSequence(conn, "EventChannelSeq");
        chanTable = new JDBCChannel(conn);
        eventTable = new JDBCEventAccess(conn);
        insert = conn.prepareStatement("INSERT into eventchannelstatus (pairid, eventid, channelid) " +
                                           "VALUES (? , ?, ?)");
        ofEventAndPair = conn.prepareStatement("SELECT pairid FROM eventchannelstatus WHERE eventid = ? and channelid = ?");
        setStatus = conn.prepareStatement("UPDATE eventchannelstatus SET status = ? where pairid = ?");
        all = conn.prepareStatement("SELECT * FROM eventchannelstatus");
        ofEvent = conn.prepareStatement("SELECT * FROM eventchannelstatus WHERE eventid = ?");
        ofPair = conn.prepareStatement("SELECT * FROM eventchannelstatus WHERE pairid = ?");
        ofStatus = conn.prepareStatement("SELECT COUNT(*) FROM eventchannelstatus WHERE status = ?");
        eventsOfStatus = conn.prepareStatement("SELECT COUNT(*) FROM eventchannelstatus WHERE status = ? AND eventid = ?");
        stationsOfStatus = conn.prepareStatement("SELECT DISTINCT " + JDBCStation.getNeededForStation() + " FROM channel, site, eventchannelstatus, station " +
                                                     "WHERE eventid = ? AND " +
                                                     "status = ? AND " +
                                                     "channelid = chan_id AND " +
                                                     "channel.site_id = site.site_id AND " +
                                                     "site.sta_id = station.sta_id");
        stationsNotOfStatus = conn.prepareStatement("SELECT DISTINCT " + JDBCStation.getNeededForStation() + " FROM channel, site, eventchannelstatus, station " +
                                                        "WHERE eventid = ? AND " +
                                                        "status != ? AND " +
                                                        "channelid = chan_id AND " +
                                                        "channel.site_id = site.site_id AND " +
                                                        "site.sta_id = station.sta_id");
        channelsForPair = conn.prepareStatement("SELECT " + JDBCChannel.getNeededForChannel() + " FROM channel, eventchannelstatus " +
                                                    "WHERE pairid = ? AND " +
                                                    "site_id = (SELECT site_id FROM channel, eventchannelstatus WHERE chan_id = channelid AND pairid = ?)");
        dbIdForEventAndChan = conn.prepareStatement("SELECT pairid FROM eventchannelstatus " +
                                                        "WHERE eventid = ? AND channelid = ?");

    }

    public Channel[] getAllChansForSite(int pairId) throws SQLException{
        channelsForPair.setInt(1, pairId);
        channelsForPair.setInt(2, pairId);
        try {
            return chanTable.extractAllChans(channelsForPair);
        } catch (NotFound e) {
            GlobalExceptionHandler.handle("Shouldn't be able to happen.  The ids are right in the statement",
                                          e);
            return new Channel[]{};
        }
    }

    public int[] getPairs(EventAccessOperations ev, ChannelGroup cg) throws NotFound, SQLException{
        int evDbid = eventTable.getDBId(ev);
        int[] channelDbIds = new int[cg.getChannels().length];
        for (int i = 0; i < cg.getChannels().length; i++) {
            channelDbIds[i] = chanTable.getDBId(cg.getChannels()[i].get_id(),
                                                cg.getChannels()[i].my_site);
        }
        int[] ids = new int[channelDbIds.length];
        for (int i = 0; i < ids.length; i++) {
            ids[i] = getDbId(evDbid, channelDbIds[i]);
        }
        return ids;
    }

    private int getDbId(int evDbid, int channelDbId) throws SQLException {
        dbIdForEventAndChan.setInt(1, evDbid);
        dbIdForEventAndChan.setInt(2, channelDbId);
        ResultSet rs = dbIdForEventAndChan.executeQuery();
        rs.next();
        return rs.getInt("pairid");
    }

    public PreparedStatement prepareStatement(String stmt) throws SQLException {
        return conn.prepareStatement(stmt);
    }

    public Station[] getNotOfStatus(Status status, EventAccessOperations ev) throws SQLException {
        return executeGetStationsOfStatus(stationsNotOfStatus,
                                          status.getAsShort(),
                                          ev);
    }

    public Station[] getOfStatus(Status status, EventAccessOperations ev) throws SQLException{
        return executeGetStationsOfStatus(stationsOfStatus,
                                          status.getAsShort(),
                                          ev);
    }

    private Station[] executeGetStationsOfStatus(PreparedStatement stmt,
                                                 int status,
                                                 EventAccessOperations ev) throws SQLException{
        int evDbId;
        try {
            evDbId = eventTable.getDBId(ev);
        } catch (NotFound e) {
            GlobalExceptionHandler.handle("Extracting a dbid for an event returned not found when the event is known to be in the db!  Zoinks!",
                                          e);
            return new Station[]{};
        }
        stmt.setInt(1, evDbId);
        stmt.setInt(2, status);
        return chanTable.getSiteTable().getStationTable().extractAll(stmt.executeQuery());
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
        ofStatus.setInt(1, s.getAsShort());
        ResultSet rs = ofStatus.executeQuery();
        rs.next();
        return rs.getInt(1);
    }

    public EventChannelPair[] getAll()throws SQLException{
        return extractECPs(all.executeQuery());
    }

    public EventChannelPair[] getAll(int eventId)throws SQLException{
        ofEvent.setInt(1, eventId);
        return extractECPs(ofEvent.executeQuery());
    }

    public EventChannelPair[] getAll(EventAccessOperations ev)throws SQLException, NotFound{
        return getAll(eventTable.getDBId(ev));
    }

    public int getNum(EventAccessOperations ev, Status status) throws NotFound, SQLException {
        int evId = eventTable.getDBId(ev);
        eventsOfStatus.setInt(1, status.getAsShort());
        eventsOfStatus.setInt(2, evId);
        ResultSet rs = eventsOfStatus.executeQuery();
        rs.next();
        return rs.getInt(1);
    }

    public int getNum(PreparedStatement stmt, EventAccessOperations ev, int stationDbId) throws NotFound, SQLException {
        int evId = eventTable.getDBId(ev);
        stmt.setInt(1, evId);
        stmt.setInt(2, stationDbId);
        ResultSet rs = stmt.executeQuery();
        rs.next();
        return rs.getInt(1);
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
        return (EventChannelPair[])pairs.toArray(new EventChannelPair[pairs.size()]);
    }

    public EventChannelPair get(int pairId, WaveformArm owner)throws NotFound,
        SQLException{
        ofPair.setInt(1, pairId);
        ResultSet rs = ofPair.executeQuery();
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
        ofEventAndPair.setInt(1, eventId);
        ofEventAndPair.setInt(2, chanId);
        ResultSet rs = ofEventAndPair.executeQuery();
        if(rs.next())return rs.getInt("pairid");
        throw new NotFound("No event and channel pair");
    }

    public void setStatus(int pairId, Status status) throws SQLException{
        setStatus.setInt(1, status.getAsShort());
        setStatus.setInt(2, pairId);
        setStatus.executeUpdate();
    }

    private PreparedStatement insert, setStatus, ofEventAndPair, all, ofEvent,
        ofPair, ofStatus, eventsOfStatus,
        stationsNotOfStatus, stationsOfStatus,
        channelsForPair,
        dbIdForEventAndChan;

    private JDBCSequence seq;
    private JDBCEventAccess eventTable;
    private JDBCChannel chanTable;
    private Connection conn;
}

