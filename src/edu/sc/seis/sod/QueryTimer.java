/**
 * QueryTimer.java
 *
 * @author Created by Omnicore CodeGuide
 */

package edu.sc.seis.sod;

import edu.sc.seis.fissuresUtil.database.ConnMgr;
import edu.sc.seis.sod.database.SodJDBC;
import edu.sc.seis.sod.database.waveform.JDBCEventChannelStatus;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Statement;

public class QueryTimer{
    public static final int QUERY_REPS = 10;
    private static Connection conn;
    private static Statement stmt;

    public static void main(String[] args) throws SQLException{
        new JDBCEventChannelStatus();
        conn = ConnMgr.createConnection();
        stmt = conn.createStatement();
        String time = "SELECT DISTINCT eventid, time_stamp, eventcondition FROM origin, time, eventstatus " +
            "WHERE origin_time_id = time_id"+
            " ORDER BY time_stamp";
        String magWithIn = "SELECT DISTINCT eventid, magnitudevalue, eventcondition FROM origin, magnitude, eventstatus " +
            "WHERE origin_id IN (SELECT origin_id FROM eventaccess) and  magnitudevalue IN (SELECT MAX(magnitudevalue) FROM magnitude WHERE origin_id = originid) " +
            " ORDER BY  magnitudevalue";
        String depthWithIn = "SELECT DISTINCT eventid, quantity_value, eventcondition  FROM origin, quantity, eventstatus " +
            "WHERE origin_id IN (SELECT origin_id FROM eventaccess) and " +
            "quantity_value IN (SELECT quantity_value FROM quantity WHERE quantity_id IN (SELECT loc_depth_id FROM location WHERE origin_location_id = loc_id)) " +
            " ORDER BY  quantity_value";
        String magWithoutIn = "SELECT DISTINCT eventid, magnitudevalue, eventcondition FROM origin, magnitude, eventstatus " +
            "WHERE magnitudevalue = (SELECT MAX(magnitudevalue) FROM magnitude WHERE origin_id = originid) " +
            " ORDER BY  magnitudevalue";
        String depthWithoutIn = "SELECT DISTINCT eventid, quantity_value, eventcondition  FROM origin, quantity, eventstatus " +
            "WHERE quantity_value = (SELECT quantity_value FROM quantity WHERE quantity_id = (SELECT loc_depth_id FROM location WHERE origin_location_id = loc_id)) " +
            " ORDER BY  quantity_value";
        timeQuery(time, "Time");
        timeQuery(magWithIn, "MagWithIn");
        timeQuery(magWithoutIn, "MagWithoutIn");
        timeQuery(depthWithIn, "DepthWithIn");
        timeQuery(depthWithoutIn, "DepthWithoutIn");
    }
    public static void timeQuery(String query, String name) throws SQLException{
        timeQuery(query, name, QUERY_REPS);
    }


    public static void timeQuery(String query, String name, int reps) throws SQLException{
        PreparedStatement prep = conn.prepareStatement(query);
        long total = 0;
        long min = Long.MAX_VALUE;
        long max = Long.MIN_VALUE;
        for (int i = 0; i < reps; i++) {
            long start = System.currentTimeMillis();
            stmt.executeQuery(query);
            long stop = System.currentTimeMillis();
            long elapsed = stop - start;
            if(elapsed < min){ min = elapsed; }
            if(elapsed > max){ max = elapsed; }
            total += elapsed;
        }
        System.out.println(name + " stmt.executeQuery  min: " + min + " max: " + max + " total: " + total + " avg: " + total/(double)reps);
        total = 0;
        min = Long.MAX_VALUE;
        max = Long.MIN_VALUE;
        for (int i = 0; i < reps; i++) {
            long start = System.currentTimeMillis();
            prep.executeQuery();
            long stop = System.currentTimeMillis();
            long elapsed = stop - start;
            if(elapsed < min){ min = elapsed; }
            if(elapsed > max){ max = elapsed; }
            total += elapsed;
        }
        System.out.println(name + " prepared statement min: " + min + " max: " + max + " total: " + total + " avg: " + total/(double)reps);
    }
}

