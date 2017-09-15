package edu.sc.seis.sod.hibernate;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

import java.time.Instant;

import org.junit.BeforeClass;
import org.junit.Test;

import edu.sc.seis.sod.QueryTime;
import edu.sc.seis.sod.VersionHistory;
import edu.sc.seis.sod.hibernate.eventpair.AbstractEventChannelPair;
import edu.sc.seis.sod.hibernate.eventpair.EventNetworkPair;
import edu.sc.seis.sod.hibernate.eventpair.EventStationPair;
import edu.sc.seis.sod.model.common.Version;

public class SodDBTest {

    @BeforeClass
    public static void setUpBeforeClass() throws Exception {
        StationDbTest.setUpDB();
    }

    @Test
    public void testGetNextENP() {
        SodDB soddb = SodDB.getSingleton();
        EventNetworkPair enp = soddb.getNextENP();
        assertNull("enp", enp);
    }

    @Test
    public void testGetNextESP() {
        SodDB soddb = SodDB.getSingleton();
        EventStationPair esp = soddb.getNextESP();
        assertNull("esp", esp);
    }

    @Test
    public void testGetNextECP() {
        SodDB soddb = SodDB.getSingleton();
        AbstractEventChannelPair ecp = soddb.getNextECP();
        assertNull("ecp", ecp);
    }

    @Test
    public void testGetQueryTime() {
        String serverName = "nowhere.example.com";
        Instant now = Instant.now();
        QueryTime qtime = new QueryTime(serverName, now);
        SodDB soddb = SodDB.getSingleton();
        soddb.putQueryTime(qtime);
        soddb.commit();
        QueryTime dbQTime = soddb.getQueryTime(serverName);
        assertEquals("name", serverName, dbQTime.getServerName());
        assertEquals("time",  now, dbQTime.getTime());
    }

    @Test
    public void testGetDBVersion() {
        SodDB soddb = SodDB.getSingleton();
        soddb.putDBVersion();
        soddb.commit();
        Version dbv = soddb.getDBVersion();
        assertEquals(VersionHistory.current(), dbv);
    }

}
