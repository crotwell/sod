package edu.sc.seis.sod;

import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Timer;
import java.util.TimerTask;
import edu.sc.seis.fissuresUtil.database.ConnMgr;
import edu.sc.seis.fissuresUtil.exceptionHandler.GlobalExceptionHandler;

/**
 * This task runs immediately on instantiation and then once a week after that.
 * It removes all events that were failed in the eventArm by a subsetter from
 * the database on each run.
 * 
 * @author groves
 * 
 * Created on Dec 28, 2006
 */
public class TotalLoserEventCleaner extends TimerTask {

    public TotalLoserEventCleaner() {
        Timer t = new Timer(true);
        t.schedule(this, 0, ONE_WEEK);
    }

    public void run() {
        Connection conn;
        Statement stmt;
        try {
            conn = ConnMgr.createConnection();
            conn.setAutoCommit(false);
            stmt = conn.createStatement();
        } catch(SQLException e) {
            GlobalExceptionHandler.handle(e);
            return;
        }
        try {
            logger.debug("Working");
            stmt.executeUpdate("DELETE FROM eventaccess WHERE event_id IN (SELECT eventid FROM eventstatus WHERE eventcondition = 258)");
            stmt.executeUpdate("DELETE FROM eventstatus WHERE eventid NOT IN (SELECT event_id FROM eventaccess)");
            stmt.executeUpdate("DELETE FROM eventattr WHERE eventattr_id NOT IN (SELECT eventattr_id FROM eventaccess)");
            stmt.executeUpdate("DELETE FROM origin WHERE origin_event_id NOT IN (SELECT event_id FROM eventaccess)");
            stmt.executeUpdate("DELETE FROM magnitude WHERE originid NOT IN (SELECT origin_id FROM origin)");
            stmt.executeUpdate("DELETE FROM time WHERE time_id NOT IN (SELECT chan_begin_id FROM channel) AND time_id NOT IN (SELECT chan_end_id FROM channel) AND time_id NOT IN (SELECT sta_begin_id FROM station) AND time_id NOT IN (SELECT sta_end_id FROM station) AND time_id NOT IN (SELECT net_begin_id FROM network) AND time_id NOT IN (SELECT net_end_id FROM network) AND time_id NOT IN (SELECT site_begin_id FROM site) AND time_id NOT IN (SELECT site_end_id FROM site) AND time_id NOT IN (SELECT origin_time_id FROM origin)");
            stmt.executeUpdate("DELETE FROM location WHERE loc_id NOT IN (SELECT loc_id FROM site) AND loc_id NOT IN (SELECT loc_id FROM station) AND loc_id NOT IN (SELECT origin_location_id FROM origin)");
            conn.commit();
            logger.debug("Done");
        } catch(SQLException e) {
            try {
                conn.rollback();
            } catch(SQLException e1) {
                GlobalExceptionHandler.handle(e1);
            }
            GlobalExceptionHandler.handle(e);
        } finally {
            try {
                conn.close();
            } catch(SQLException e) {
                GlobalExceptionHandler.handle(e);
            }
        }
    }

    private static final org.apache.log4j.Logger logger = org.apache.log4j.Logger.getLogger(TotalLoserEventCleaner.class);

    private static final long ONE_WEEK = 7 * 24 * 60 * 60 * 1000;
}
