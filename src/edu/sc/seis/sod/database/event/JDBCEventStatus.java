/**
 * JDBCEventStatus.java
 *
 * @author Created by Omnicore CodeGuide
 */

package edu.sc.seis.sod.database.event;

import edu.iris.Fissures.IfEvent.EventAccessOperations;
import edu.iris.Fissures.IfEvent.NoPreferredOrigin;
import edu.sc.seis.fissuresUtil.cache.CacheEvent;
import edu.sc.seis.fissuresUtil.database.ConnMgr;
import edu.sc.seis.fissuresUtil.database.DBUtil;
import edu.sc.seis.fissuresUtil.database.NotFound;
import edu.sc.seis.fissuresUtil.database.event.JDBCEventAccess;
import edu.sc.seis.fissuresUtil.exceptionHandler.GlobalExceptionHandler;
import edu.sc.seis.sod.CommonAccess;
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
        conn = ConnMgr.getConnection();
        ea = new JDBCEventAccess(conn);
        if(!DBUtil.tableExists("eventstatus", conn)){
            Statement stmt = conn.createStatement();
            stmt.executeUpdate(ConnMgr.getSQL("eventstatus.create"));
        }
        getStatus = conn.prepareStatement("SELECT eventcondition FROM eventstatus WHERE eventid = ?");
        putEvent = conn.prepareStatement("INSERT into eventstatus ( eventcondition, eventid ) " +
                                             "VALUES (?, ?)");
        updateStatus = conn.prepareStatement("UPDATE eventstatus SET eventcondition = ? WHERE eventid = ?");
        getAllOfEventArmStatus = conn.prepareStatement(" SELECT * FROM eventstatus WHERE eventcondition = ? " );
        getAllOrderByDate  = conn.prepareStatement("SELECT DISTINCT originid, origin_time, origineventid FROM origin ORDER BY origin_time DESC");
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
        getAllOfEventArmStatus.setInt(1, status.getAsByte());
        return extractEvents(getAllOfEventArmStatus);
    }

    private CacheEvent[] extractEvents(PreparedStatement eventStatement)
        throws SQLException{
        ResultSet rs = eventStatement.executeQuery();
        List evs = new ArrayList();
        while(rs.next()){
            try {
                evs.add(ea.getEvent(rs.getInt("eventid")));
            } catch (NotFound e) {
                throw new RuntimeException("this shouldn't happen, the id's in this table are foreign keys from the eventaccess table",
                                           e);
            }
        }
        return (CacheEvent[])evs.toArray(new CacheEvent[evs.size()]);
    }


    public StatefulEvent[] getAll() throws SQLException {
        return get("SELECT eventid FROM eventaccess", "eventid");
    }

    public StatefulEvent[] get(String query, String idLoc) throws SQLException{
        Statement stmt = conn.createStatement();
        ResultSet rs = stmt.executeQuery(query);
        List evs = new ArrayList();
        while(rs.next()){
            try {
                CacheEvent ev = ea.getEvent(rs.getInt(idLoc));
                Status stat = getStatus(rs.getInt(idLoc));
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

    public CacheEvent[] getAllOrderedByDate() throws SQLException{
        List events = new ArrayList();
        ResultSet rs = getAllOrderByDate.executeQuery();
        while(rs.next()){
            try {
                events.add(ea.getEvent(rs.getInt("origineventid")));
            } catch (NotFound e) {
                //shouldn't happen, I just got this id
                CommonAccess.handleException(e, "trouble getting event from id");
            }
        }
        CacheEvent[] evs = new CacheEvent[events.size()];
        return (CacheEvent[])events.toArray(evs);
    }

    public Status getStatus(int dbId) throws SQLException, NotFound{
        getStatus.setInt(1, dbId);
        ResultSet rs = getStatus.executeQuery();
        int val = rs.getInt("eventcondition");
        if(rs.next())return Status.get(Stage.getFromInt(val>>4), Standing.getFromInt(val&0x0F));
        throw new NotFound("There is no status for that id");
    }

    public CacheEvent getEvent(int dbId) throws NotFound, SQLException{
        return ea.getEvent(dbId);
    }

    public int setStatus(EventAccessOperations ev,  Status status)
        throws SQLException {
        int id = ea.put(ev, null, null, null);
        setStatus(id, status);
        return id;
    }

    public void setStatus(int eventId, Status status) throws SQLException{
        if(tableContains(eventId)){
            insert(updateStatus, eventId, status.getAsByte());
        }else{
            insert(putEvent, eventId, status.getAsByte());
        }
    }

    public boolean tableContains(int dbId) throws SQLException{
        try{
            getStatus(dbId);
        }catch(NotFound e){ return false; }
        return true;
    }

    public void insert(PreparedStatement stmt, int id, int eventArmStatus) throws SQLException{
        stmt.setInt(1, eventArmStatus);
        stmt.setInt(2, id);
        stmt.executeUpdate();
    }

    public int getNext() throws SQLException{
        Statement stmt = ConnMgr.getConnection().createStatement();
        ResultSet rs = stmt.executeQuery("SELECT * FROM eventstatus WHERE eventcondition = " +Status.get(Stage.EVENT_CHANNEL_POPULATION,
                                                                                                         Standing.IN_PROG).getAsByte());
        if(rs.next()) return rs.getInt("eventid");
        return -1;
    }

    private JDBCEventAccess ea;

    private PreparedStatement getStatus, putEvent, updateStatus,
        getAllOfEventArmStatus, getAllOfWaveformArmStatus,
        getWaveformStatus, getAllOrderByDate;

    private Connection conn;
}
