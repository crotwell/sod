package edu.sc.seis.sod.database.event;

import edu.iris.Fissures.IfEvent.EventAccessOperations;
import edu.iris.Fissures.IfEvent.NoPreferredOrigin;
import edu.sc.seis.fissuresUtil.cache.CacheEvent;
import edu.sc.seis.fissuresUtil.database.ConnMgr;
import edu.sc.seis.fissuresUtil.database.DBUtil;
import edu.sc.seis.fissuresUtil.database.NotFound;
import edu.sc.seis.fissuresUtil.database.event.JDBCEventAccess;
import edu.sc.seis.fissuresUtil.exceptionHandler.GlobalExceptionHandler;
import edu.sc.seis.sod.Stage;
import edu.sc.seis.sod.Standing;
import edu.sc.seis.sod.Status;
import edu.sc.seis.sod.database.SodJDBC;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

public class JDBCEventStatus extends SodJDBC{
    public JDBCEventStatus() throws SQLException{
        conn = ConnMgr.createConnection();
        eventAccessTable = new JDBCEventAccess(conn);
        if(!DBUtil.tableExists("eventstatus", conn)){
            Statement stmt = conn.createStatement();
            stmt.executeUpdate(ConnMgr.getSQL("eventstatus.create"));
        }
        getStatus = prepare("SELECT eventcondition FROM eventstatus WHERE eventid = ?");
        putEvent = prepare("INSERT into eventstatus (eventcondition, eventid) " +
                                             "VALUES (?, ?)");
        updateStatus = prepare("UPDATE eventstatus SET eventcondition = ? WHERE eventid = ?");
        getNextStmt = prepare("SELECT TOP 1 eventid FROM eventstatus WHERE eventcondition = " +
                                                Status.get(Stage.EVENT_CHANNEL_POPULATION,
                                                           Standing.IN_PROG).getAsShort());
        getAllOfEventArmStatus = prepare(" SELECT * FROM eventstatus WHERE eventcondition = ? " );
        getAll = prepare("SELECT eventid, eventcondition FROM eventstatus");
    }

    public void restartCompletedEvents() throws SQLException {
        CacheEvent[] events = getAll(Status.get(Stage.EVENT_CHANNEL_POPULATION,
                                                Standing.SUCCESS));
        for (int i = 0; i < events.length; i++) {
            setStatus(events[i], Status.get(Stage.EVENT_CHANNEL_POPULATION,
                                            Standing.IN_PROG));
        }
    }

    public CacheEvent[] getAll(Status status) throws SQLException {
        getAllOfEventArmStatus.setShort(1, status.getAsShort());
        return extractEvents(getAllOfEventArmStatus);
    }

    private CacheEvent[] extractEvents(PreparedStatement eventStatement)
        throws SQLException{
        ResultSet rs = eventStatement.executeQuery();
        List evs = new ArrayList();
        while(rs.next()){
            try {
                evs.add(eventAccessTable.getEvent(rs.getInt("eventid")));
            } catch (NotFound e) {
                throw new RuntimeException("this shouldn't happen, the id's in this table are foreign keys from the eventaccess table",
                                           e);
            }
        }
        return (CacheEvent[])evs.toArray(new CacheEvent[evs.size()]);
    }


    public StatefulEvent[] getAll() throws SQLException {
        return get(getAll);
    }

    public PreparedStatement prepare(String query) throws SQLException{
        return conn.prepareStatement(query);
    }

    public StatefulEvent[] get(PreparedStatement prep) throws SQLException{
        ResultSet rs = prep.executeQuery();
        List evs = new ArrayList();
        while(rs.next()){
            try {
                CacheEvent ev = eventAccessTable.getEvent(rs.getInt("eventid"));
                Status stat = Status.getFromShort(rs.getShort("eventcondition"));
                try {
                    evs.add(new StatefulEvent(ev, stat));
                } catch (NoPreferredOrigin e) {
                    GlobalExceptionHandler.handle("impossible, the cache event must have a preferred origin", e);
                }
            } catch (NotFound e) {
                GlobalExceptionHandler.handle("this shouldn't happen, the id's in this table are foreign keys from the eventaccess table",
                                              e);
            }
        }
        return (StatefulEvent[])evs.toArray(new StatefulEvent[evs.size()]);
    }

    public Status getStatus(int dbId) throws SQLException, NotFound{
        getStatus.setInt(1, dbId);
        ResultSet rs = getStatus.executeQuery();
        if(rs.next()) {
            short val = rs.getShort("eventcondition");
            return Status.getFromShort(val);
        }
        throw new NotFound("There is no status for that id");
    }

    public CacheEvent getEvent(int dbId) throws NotFound, SQLException{
        return eventAccessTable.getEvent(dbId);
    }

    public int setStatus(EventAccessOperations ev,  Status status)
        throws SQLException {
        int id = eventAccessTable.put(ev, null, null, null);
        setStatus(id, status);
        return id;
    }

    public void setStatus(int eventId, Status status) throws SQLException{
        if(tableContains(eventId)){
            insert(updateStatus, eventId, status);
        }else{
            insert(putEvent, eventId, status);
        }
    }

    public boolean tableContains(int dbId) throws SQLException{
        try{
            getStatus(dbId);
        }catch(NotFound e){ return false; }
        return true;
    }

    private void insert(PreparedStatement stmt, int id, Status eventArmStatus) throws SQLException{
        stmt.setShort(1, eventArmStatus.getAsShort());
        stmt.setInt(2, id);
        stmt.executeUpdate();
    }

    public int getNext() throws SQLException{
        ResultSet rs = getNextStmt.executeQuery();
        if(rs.next()) return rs.getInt("eventid");
        return -1;
    }

    private JDBCEventAccess eventAccessTable;

    private PreparedStatement getStatus, putEvent, updateStatus,
        getAllOfEventArmStatus, getAllOfWaveformArmStatus,
        getWaveformStatus, getAllOrderByDate, getNextStmt, getAll;

    private Connection conn;
}
