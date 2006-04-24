package edu.sc.seis.sod.database.waveform;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;
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
import edu.sc.seis.fissuresUtil.database.util.SQLLoader;
import edu.sc.seis.fissuresUtil.display.MicroSecondTimeRange;
import edu.sc.seis.fissuresUtil.exceptionHandler.GlobalExceptionHandler;
import edu.sc.seis.sod.ChannelGroup;
import edu.sc.seis.sod.EventChannelPair;
import edu.sc.seis.sod.EventVectorPair;
import edu.sc.seis.sod.NetworkArm;
import edu.sc.seis.sod.RunProperties;
import edu.sc.seis.sod.SodUtil;
import edu.sc.seis.sod.Stage;
import edu.sc.seis.sod.Standing;
import edu.sc.seis.sod.Start;
import edu.sc.seis.sod.Status;
import edu.sc.seis.sod.WaveformArm;
import edu.sc.seis.sod.database.ChannelDbObject;
import edu.sc.seis.sod.database.EventDbObject;
import edu.sc.seis.sod.database.SodJDBC;

public class JDBCEventChannelStatus extends SodJDBC {

    public JDBCEventChannelStatus() throws SQLException {
        this(ConnMgr.createConnection());
    }

    public JDBCEventChannelStatus(Connection conn) throws SQLException {
        this.conn = conn;
        if(!DBUtil.tableExists("eventchannelstatus", conn)) {
            Statement stmt = conn.createStatement();
            stmt.executeUpdate(ConnMgr.getSQL("eventchannelstatus.create"));
            stmt.executeUpdate("CREATE INDEX evchanstatus_chan ON eventchannelstatus( channelid)");
        }
        seq = new JDBCSequence(conn, "EventChannelSeq");
        chanTable = new JDBCChannel(conn);
        eventTable = new JDBCEventAccess(conn);
        insert = conn.prepareStatement("INSERT into eventchannelstatus (pairid, eventid, channelid) "
                + "VALUES (? , ?, ?)");
        ofEventAndPair = conn.prepareStatement("SELECT pairid FROM eventchannelstatus WHERE eventid = ? and channelid = ?");
        setStatus = conn.prepareStatement("UPDATE eventchannelstatus SET status = ? where pairid = ?");
        all = conn.prepareStatement("SELECT * FROM eventchannelstatus");
        ofEvent = conn.prepareStatement("SELECT * FROM eventchannelstatus WHERE eventid = ?");
        ofPair = conn.prepareStatement("SELECT * FROM eventchannelstatus WHERE pairid = ?");
        ofStatus = conn.prepareStatement("SELECT COUNT(*) FROM eventchannelstatus WHERE status = ?");
        eventsOfStatus = conn.prepareStatement("SELECT COUNT(*) FROM eventchannelstatus WHERE status = ? AND eventid = ?");
        String chanJoin = "channelid = chan_id AND "
                + "channel.site_id = site.site_id AND "
                + "site.sta_id = station.sta_id";
        String stationsBase = "SELECT DISTINCT "
                + JDBCStation.getNeededForStation()
                + " FROM channel, site, eventchannelstatus, station ";
        stations = conn.prepareStatement(stationsBase + "WHERE " + chanJoin
                + " AND eventid = ?");
        String stationsOfStatusWhere = "WHERE " + chanJoin + " AND "
                + "eventid = ? AND " + "status = ?";
        stationsOfStatus = conn.prepareStatement(stationsBase
                + stationsOfStatusWhere);
        stationsNotOfStatus = conn.prepareStatement("SELECT "
                + JDBCStation.getNeededForStation()
                + " FROM station "
                + "WHERE sta_id NOT IN ("
                + "SELECT DISTINCT sta_id FROM channel, site, eventchannelstatus "
                + stationsOfStatusWhere + ")");
        SQLLoader networkSQLLoader = new SQLLoader("edu/sc/seis/fissuresUtil/database/props/network/default.props");
        channelsForPair = conn.prepareStatement("SELECT "
                + networkSQLLoader.getContext().get("channel_neededForChannel")
                + " FROM channel, eventchannelstatus "
                + "WHERE pairid = ? AND "
                + "site_id = (SELECT site_id FROM channel, eventchannelstatus WHERE chan_id = channelid AND pairid = ?)");
        dbIdForEventAndChan = conn.prepareStatement("SELECT pairid FROM eventchannelstatus "
                + "WHERE eventid = ? AND channelid = ?");
        ofStation = conn.prepareStatement("SELECT pairid, eventid, channelid, status "
                + "FROM channel, site, eventchannelstatus, station "
                + "WHERE "
                + chanJoin + " AND station.sta_id = ?");
        ofStationStatus = conn.prepareStatement("SELECT pairid, eventid, channelid, status "
                + "FROM channel, site, eventchannelstatus, station "
                + "WHERE "
                + chanJoin + " AND station.sta_id = ?" + " AND status = ?");
        // Since events come in in time order, sorting on reverse id order gives
        // us backwards in time
        ofStationStatusInTime = conn.prepareStatement("SELECT DISTINCT eventid FROM eventchannelstatus "
                + "JOIN channel ON (channelid = chan_id) "
                + "JOIN site ON (channel.site_id = site.site_id) "
                + "JOIN eventaccess ON (eventid = event_id) "
                + "JOIN origin ON (eventaccess.origin_id = origin.origin_id) "
                + "JOIN time ON (origin_time_id = time_id) "
                + "WHERE status = ? AND "
                + "time_stamp BETWEEN ?  AND ? AND "
                + "sta_id = ? ORDER BY eventid DESC");
        ofChannelStatusInTime = conn.prepareStatement("SELECT eventid FROM eventchannelstatus "
                + "JOIN eventaccess ON (eventid = event_id) "
                + "JOIN origin ON (eventaccess.origin_id = origin.origin_id) "
                + "JOIN time ON (origin_time_id = time_id) "
                + "WHERE status = ? AND "
                + "time_stamp BETWEEN ? AND ? AND "
                + "channelid = ? ORDER BY eventid DESC;");
    }

    public Channel[] getAllChansForSite(int pairId) throws SQLException {
        channelsForPair.setInt(1, pairId);
        channelsForPair.setInt(2, pairId);
        try {
            return chanTable.extractAllChans(channelsForPair);
        } catch(NotFound e) {
            GlobalExceptionHandler.handle("Shouldn't be able to happen.  The ids are right in the statement",
                                          e);
            return new Channel[] {};
        }
    }

    public int[] getPairs(EventAccessOperations ev, ChannelGroup cg)
            throws NotFound, SQLException {
        int evDbid = eventTable.getDBId(ev);
        int[] channelDbIds = new int[cg.getChannels().length];
        for(int i = 0; i < cg.getChannels().length; i++) {
            channelDbIds[i] = chanTable.getDBId(cg.getChannels()[i].get_id());
        }
        int[] ids = new int[channelDbIds.length];
        for(int i = 0; i < ids.length; i++) {
            ids[i] = getDbId(evDbid, channelDbIds[i]);
        }
        return ids;
    }

    public int[] getPairs(EventVectorPair group) throws SQLException, NotFound {
        return getPairs(group.getEvent(), group.getChannelGroup());
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

    public Station[] getNotOfStatus(Status status, EventAccessOperations ev)
            throws SQLException {
        return executeGetStationsOfStatus(stationsNotOfStatus,
                                          status.getAsShort(),
                                          ev);
    }

    public Station[] getOfStatus(Status status, EventAccessOperations ev)
            throws SQLException {
        return executeGetStationsOfStatus(stationsOfStatus,
                                          status.getAsShort(),
                                          ev);
    }

    public Station[] getStations(EventAccessOperations ev) throws SQLException {
        int evDbId;
        try {
            evDbId = eventTable.getDBId(ev);
        } catch(NotFound e) {
            GlobalExceptionHandler.handle("Extracting a dbid for an event returned not found when the event is known to be in the db!  Zoinks!",
                                          e);
            return new Station[] {};
        }
        stations.setInt(1, evDbId);
        return chanTable.getSiteTable()
                .getStationTable()
                .extractAll(stations.executeQuery());
    }

    private Station[] executeGetStationsOfStatus(PreparedStatement stmt,
                                                 int status,
                                                 EventAccessOperations ev)
            throws SQLException {
        int evDbId;
        try {
            evDbId = eventTable.getDBId(ev);
        } catch(NotFound e) {
            GlobalExceptionHandler.handle("Extracting a dbid for an event returned not found when the event is known to be in the db!  Zoinks!",
                                          e);
            return new Station[] {};
        }
        stmt.setInt(1, evDbId);
        stmt.setInt(2, status);
        return chanTable.getSiteTable()
                .getStationTable()
                .extractAll(stmt.executeQuery());
    }

    public int put(EventChannelPair ecp) throws SQLException {
        return put(ecp.getEvent(), ecp.getChannel(), ecp.getStatus());
    }

    public int put(EventAccessOperations event, Channel chan, Status stat)
            throws SQLException {
        int eventId = eventTable.put(event, null, null, null);
        int chanId = chanTable.put(chan);
        return put(eventId, chanId, stat);
    }

    public int put(int eventId, int chanId, Status stat) throws SQLException {
        int pairId = insert(eventId, chanId);
        setStatus(pairId, stat);
        return pairId;
    }

    public int getNumOfStatus(Status s) throws SQLException {
        ofStatus.setInt(1, s.getAsShort());
        ResultSet rs = ofStatus.executeQuery();
        rs.next();
        return rs.getInt(1);
    }

    public EventChannelPair[] getAll() throws SQLException {
        return extractECPs(all.executeQuery());
    }

    public EventChannelPair[] getAll(int eventId) throws SQLException {
        ofEvent.setInt(1, eventId);
        return extractECPs(ofEvent.executeQuery());
    }

    public EventChannelPair[] getAllForStation(int stationId)
            throws SQLException {
        ofStation.setInt(1, stationId);
        return extractECPs(ofStation.executeQuery());
    }

    public EventChannelPair[] getSuccessfulForStation(int stationId)
            throws SQLException {
        ofStationStatus.setInt(1, stationId);
        ofStationStatus.setShort(2, Status.get(Stage.PROCESSOR,
                                               Standing.SUCCESS).getAsShort());
        return extractECPs(ofStationStatus.executeQuery());
    }

    public CacheEvent[] getSuccessfulForChannelForTime(int channelId,
                                                       MicroSecondTimeRange timeRange)
            throws SQLException, NotFound {
        ofChannelStatusInTime.setShort(1, Status.get(Stage.PROCESSOR,
                                                     Standing.SUCCESS)
                .getAsShort());
        ofChannelStatusInTime.setTimestamp(2, timeRange.getBeginTime()
                .getTimestamp());
        ofChannelStatusInTime.setTimestamp(3, timeRange.getEndTime()
                .getTimestamp());
        ofChannelStatusInTime.setInt(4, channelId);
        List events = new ArrayList();
        ResultSet rs = ofChannelStatusInTime.executeQuery();
        while(rs.next()) {
            events.add(eventTable.getEvent(rs.getInt("eventid")));
        }
        return (CacheEvent[])events.toArray(new CacheEvent[0]);
    }

    public EventChannelPair[] getAll(EventAccessOperations ev)
            throws SQLException, NotFound {
        return getAll(eventTable.getDBId(ev));
    }

    public int getNum(PreparedStatement stmt, EventAccessOperations ev)
            throws NotFound, SQLException {
        int evId = eventTable.getDBId(ev);
        stmt.setInt(1, evId);
        try {
            ResultSet rs = stmt.executeQuery();
            rs.next();
            return rs.getInt(1);
        } catch(SQLException e) {
            throw new RuntimeException("RUNNING THE SQL " + stmt.toString(), e);
        }
    }

    public int getNum(PreparedStatement stmt,
                      EventAccessOperations ev,
                      int stationDbId) throws NotFound, SQLException {
        int evId = eventTable.getDBId(ev);
        stmt.setInt(1, evId);
        stmt.setInt(2, stationDbId);
        ResultSet rs = stmt.executeQuery();
        rs.next();
        return rs.getInt(1);
    }

    private EventChannelPair[] extractECPs(ResultSet rs) throws SQLException {
        List pairs = new ArrayList();
        while(rs.next()) {
            try {
                pairs.add(extractECP(rs, null));
            } catch(NotFound e) {
                GlobalExceptionHandler.handle("This NotFound was thrown while extracting event channel pairs from a result set.  The ids in the result set should be accurate, so this means something is screwy.",
                                              e);
            }
        }
        return (EventChannelPair[])pairs.toArray(new EventChannelPair[pairs.size()]);
    }

    public EventChannelPair get(int pairId, WaveformArm owner) throws NotFound,
            SQLException {
        ofPair.setInt(1, pairId);
        ResultSet rs = ofPair.executeQuery();
        if(rs.next()) {
            return extractECP(rs, owner);
        }
        throw new NotFound("No such pairId: " + pairId
                + " in the event channel db");
    }

    private EventChannelPair extractECP(ResultSet rs, WaveformArm owner)
            throws SQLException, NotFound {
        Status s = Status.getFromShort((short)rs.getInt("status"));
        int eventId = rs.getInt("eventid");
        CacheEvent event = eventTable.getEvent(eventId);
        int chanId = rs.getInt("channelid");
        NetworkArm na = Start.getNetworkArm();
        Channel chan = null;
        if(na != null) {
            chan = na.getChannel(chanId);
        }
        if(chan == null) {
            chan = chanTable.get(chanId);
        }
        EventChannelPair cur = new EventChannelPair(new EventDbObject(eventId,
                                                                      event),
                                                    new ChannelDbObject(chanId,
                                                                        chan),
                                                    owner,
                                                    rs.getInt("pairid"),
                                                    s);
        return cur;
    }

    public int insert(int eventId, int chanId) throws SQLException {
        try {
            return getPairId(eventId, chanId);
        } catch(NotFound e) {
            int pairId = seq.next();
            insert.setInt(1, pairId);
            insert.setInt(2, eventId);
            insert.setInt(3, chanId);
            insert.executeUpdate();
            return pairId;
        }
    }

    public int getPairId(int eventId, int chanId) throws NotFound, SQLException {
        ofEventAndPair.setInt(1, eventId);
        ofEventAndPair.setInt(2, chanId);
        ResultSet rs = ofEventAndPair.executeQuery();
        if(rs.next())
            return rs.getInt("pairid");
        throw new NotFound("No event and channel pair");
    }

    public void setStatus(int pairId, Status status) throws SQLException {
        setStatus.setInt(1, status.getAsShort());
        setStatus.setInt(2, pairId);
        try {
            setStatus.executeUpdate();
        } catch(SQLException e) {
            throw new RuntimeException("RUNNING THE SQL "
                    + setStatus.toString(), e);
        }
    }

    public int[] getSuspendedEventChannelPairs(String processingRule)
            throws SQLException {
        Status eventStationInit = Status.get(Stage.EVENT_STATION_SUBSETTER,
                                             Standing.INIT);
        Status processorSuccess = Status.get(Stage.PROCESSOR, Standing.SUCCESS);
        Stage[] stages = {Stage.EVENT_STATION_SUBSETTER,
                          Stage.EVENT_CHANNEL_SUBSETTER,
                          Stage.REQUEST_SUBSETTER,
                          Stage.AVAILABLE_DATA_SUBSETTER,
                          Stage.DATA_RETRIEVAL,
                          Stage.PROCESSOR};
        Standing[] standings = {Standing.IN_PROG,
                                Standing.INIT,
                                Standing.SUCCESS};
        String query = "SELECT pairid FROM eventchannelstatus WHERE ";
        for(int i = 0; i < stages.length; i++) {
            for(int j = 0; j < standings.length; j++) {
                Status curStatus = Status.get(stages[i], standings[j]);
                if(!curStatus.equals(processorSuccess)) {
                    query += " status = " + curStatus.getAsShort();
                    query += " OR";
                }
            }
        }
        // get rid of last OR
        query = query.substring(0, query.length() - 2);
        Statement stmt = conn.createStatement();
        try {
            ResultSet rs = stmt.executeQuery(query);
            List pairs = new ArrayList();
            while(rs.next()) {
                int pairId = rs.getInt(1);
                if(processingRule.equals(RunProperties.AT_LEAST_ONCE)) {
                    pairs.add(new Integer(pairId));
                } else {
                    try {
                        Status curStatus = get(pairId, null).getStatus();
                        Stage currentStage = curStatus.getStage();
                        if(!curStatus.equals(eventStationInit)) {
                            setStatus(pairId,
                                      Status.get(currentStage,
                                                 Standing.SYSTEM_FAILURE));
                        } else {
                            pairs.add(new Integer(pairId));
                        }
                    } catch(NotFound e) {
                        GlobalExceptionHandler.handle("Could not find event-channel "
                                                              + "pair that was just purported "
                                                              + "to exist in the database",
                                                      e);
                    }
                }
            }
            return SodUtil.intArrayFromList(pairs);
        } catch(SQLException e) {
            logger.error(query);
            throw e;
        }
        // return new int[0];
    }

    public JDBCChannel getChannelTable() {
        return chanTable;
    }

    public JDBCEventAccess getEventTable() {
        return eventTable;
    }

    private PreparedStatement insert, setStatus, ofEventAndPair, all, ofEvent,
            ofPair, ofStatus, eventsOfStatus, stationsNotOfStatus,
            stationsOfStatus, channelsForPair, dbIdForEventAndChan, ofStation,
            ofStationStatus, stations, ofStationStatusInTime,
            ofChannelStatusInTime;

    private JDBCSequence seq;

    private JDBCEventAccess eventTable;

    private JDBCChannel chanTable;

    private Connection conn;

    public static String getRetryStatusRequest() {
        return getStatusRequest(RETRY_STATUS);
    }

    public static String getFailedStatusRequest() {
        return getStatusRequest(FAILED_STATUS);
    }

    public static String getStatusRequest(Status[] statii) {
        String request = "( status = " + statii[0].getAsShort();
        for(int i = 1; i < statii.length; i++) {
            request += " OR status = " + statii[i].getAsShort();
        }
        request += ")";
        return request;
    }

    public static final Status[] FAILED_STATUS = new Status[] {Status.get(Stage.EVENT_STATION_SUBSETTER,
                                                                          Standing.REJECT),
                                                               Status.get(Stage.EVENT_STATION_SUBSETTER,
                                                                          Standing.SYSTEM_FAILURE),
                                                               Status.get(Stage.EVENT_CHANNEL_SUBSETTER,
                                                                          Standing.REJECT),
                                                               Status.get(Stage.EVENT_CHANNEL_SUBSETTER,
                                                                          Standing.SYSTEM_FAILURE),
                                                               Status.get(Stage.REQUEST_SUBSETTER,
                                                                          Standing.REJECT),
                                                               Status.get(Stage.REQUEST_SUBSETTER,
                                                                          Standing.SYSTEM_FAILURE),
                                                               Status.get(Stage.AVAILABLE_DATA_SUBSETTER,
                                                                          Standing.SYSTEM_FAILURE),
                                                               Status.get(Stage.AVAILABLE_DATA_SUBSETTER,
                                                                          Standing.REJECT),
                                                               Status.get(Stage.DATA_RETRIEVAL,
                                                                          Standing.SYSTEM_FAILURE),
                                                               Status.get(Stage.DATA_RETRIEVAL,
                                                                          Standing.REJECT),
                                                               Status.get(Stage.PROCESSOR,
                                                                          Standing.SYSTEM_FAILURE),
                                                               Status.get(Stage.PROCESSOR,
                                                                          Standing.REJECT)};

    public static final Status[] RETRY_STATUS = new Status[] {Status.get(Stage.AVAILABLE_DATA_SUBSETTER,
                                                                         Standing.RETRY),
                                                              Status.get(Stage.AVAILABLE_DATA_SUBSETTER,
                                                                         Standing.CORBA_FAILURE),
                                                              Status.get(Stage.DATA_RETRIEVAL,
                                                                         Standing.CORBA_FAILURE),
                                                              Status.get(Stage.PROCESSOR,
                                                                         Standing.CORBA_FAILURE)};

    private static final org.apache.log4j.Logger logger = org.apache.log4j.Logger.getLogger(JDBCEventChannelStatus.class);
}