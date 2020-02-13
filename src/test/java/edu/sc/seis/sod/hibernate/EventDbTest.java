package edu.sc.seis.sod.hibernate;


import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import edu.sc.seis.sod.mock.event.MockEventAccessOperations;
import edu.sc.seis.sod.model.event.CacheEvent;


public class EventDbTest {


    @BeforeAll
    public static void setUpBeforeClass() throws Exception {
        StationDbTest.setUpDB();
    }


    @Test
    public void testPut() {
        CacheEvent event = MockEventAccessOperations.createEvent();
        EventDB edb = EventDB.getSingleton();
        edb.put(event);
        edb.commit();
        List<CacheEvent> elist = edb.getAll();
        assertTrue( elist.size() > 0);
;    }
}
