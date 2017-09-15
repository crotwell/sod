package edu.sc.seis.sod.hibernate;

import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.util.List;

import org.junit.BeforeClass;
import org.junit.Test;

import edu.sc.seis.sod.mock.event.MockEventAccessOperations;
import edu.sc.seis.sod.model.event.CacheEvent;


public class EventDbTest {


    @BeforeClass
    public static void setUpBeforeClass() throws Exception {
        StationDbTest.setUpDB();
    }

    //@Test
    public void testGetEvent() {
        fail("Not yet implemented");
    }

    @Test
    public void testPut() {
        CacheEvent event = MockEventAccessOperations.createEvent();
        EventDB edb = EventDB.getSingleton();
        edb.put(event);
        edb.commit();
        List<CacheEvent> elist = edb.getAll();
        assertTrue("events size", elist.size() > 0);
;    }
}
