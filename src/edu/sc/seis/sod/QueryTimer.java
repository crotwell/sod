/**
 * QueryTimer.java
 *
 * @author Created by Omnicore CodeGuide
 */

package edu.sc.seis.sod;

import edu.sc.seis.fissuresUtil.database.ConnMgr;
import edu.sc.seis.fissuresUtil.database.network.JDBCStation;
import edu.sc.seis.sod.database.waveform.JDBCEventChannelStatus;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

public class QueryTimer{
    public static final int QUERY_REPS = 20;
    private static Connection conn;
    private static Statement stmt;

    public static void main(String[] args) throws SQLException{
        new JDBCEventChannelStatus();
        conn = ConnMgr.createConnection();
        stmt = conn.createStatement();
        //String depth = "SELECT DISTINCT eventid, quantity_value, eventcondition  FROM origin, quantity, eventstatus " +
        //    "WHERE quantity_value = (SELECT quantity_value FROM quantity WHERE quantity_id = (SELECT loc_depth_id FROM location WHERE origin_location_id = loc_id)) " +
        //    "and eventid = origin_event_id " +
        //    " ORDER BY  quantity_value";
        String subquery = "SELECT DISTINCT " +  JDBCStation.getNeededForStation() + " FROM station, channel, site, eventchannelstatus  " +
            "WHERE eventid = 0 AND " +
            "station.sta_id = site.sta_id  AND " +
            "site.site_id = channel.site_id AND " +
            "chan_id = channelid AND " +
            "status = 2305";
        String query = "SELECT DISTINCT " + JDBCStation.getNeededForStation() + " FROM station, channel, site, eventchannelstatus WHERE " +
            "station.sta_id = site.sta_id  AND " +
            "site.site_id = channel.site_id AND " +
            "chan_id = channelid AND " +
            "eventid = 0 AND " +
            "status = 2305";

        System.out.println("THE WORKS========================");
        stmt.executeUpdate("CREATE INDEX evchanstatus_ev ON eventchannelstatus( eventid)");
        stmt.executeUpdate("CREATE INDEX evchanstatus_status ON eventchannelstatus (status)");
        stmt.executeUpdate("CREATE INDEX evchanstatus_chan ON eventchannelstatus( channelid)");
        stmt.executeUpdate("CREATE INDEX site_sta ON site( sta_id)");
        stmt.executeUpdate("CREATE INDEX chan_site ON channel( site_id)");
        timeQuery(subquery, "     sub");
        timeQuery(query, "     reg");
        stmt.executeUpdate("DROP INDEX evchanstatus_chan");
        stmt.executeUpdate("DROP INDEX site_sta");
        stmt.executeUpdate("DROP INDEX chan_site");
        stmt.executeUpdate("DROP INDEX evchanstatus_ev");
        stmt.executeUpdate("DROP INDEX evchanstatus_status");

        System.out.println("CHANNEL========================");
        stmt.executeUpdate("CREATE INDEX evchanstatus_chan ON eventchannelstatus( channelid)");
        timeQuery(subquery, "     sub");
        timeQuery(query, "     reg");
        stmt.executeUpdate("DROP INDEX evchanstatus_chan");

        System.out.println("EVENT========================");
        stmt.executeUpdate("CREATE INDEX evchanstatus_ev ON eventchannelstatus( eventid)");
        timeQuery(subquery, "     sub");
        timeQuery(query, "     reg");
        stmt.executeUpdate("DROP INDEX evchanstatus_ev");

        System.out.println("STATUS========================");
        stmt.executeUpdate("CREATE INDEX evchanstatus_status ON eventchannelstatus (status)");
        timeQuery(subquery, "     sub");
        timeQuery(query, "     reg");
        stmt.executeUpdate("DROP INDEX evchanstatus_status");

        System.out.println("PLAIN========================");
        timeQuery(subquery, "     sub");
        timeQuery(query, "     reg");
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

