package edu.sc.seis.sod.database.event;

import edu.iris.Fissures.IfEvent.EventAccessOperations;
import edu.sc.seis.fissuresUtil.cache.CacheEvent;
import edu.sc.seis.fissuresUtil.database.NotFound;
import edu.sc.seis.mockFissures.IfEvent.MockEventAccessOperations;
import java.sql.SQLException;
import junit.framework.TestCase;

public class JDBCEventStatusTest extends TestCase{
    public JDBCEventStatusTest(String name) throws SQLException{
        super(name);
        jes = new JDBCEventStatus();
    }
    
    
    public void testSetStatus() throws SQLException, SQLException, NotFound{
        EventAccessOperations[] evs = MockEventAccessOperations.createEvents();
        for (int i = 0; i < evs.length; i++) {
            int dbId = jes.setStatus(evs[i], EventCondition.NEW);
            assertEquals(EventCondition.NEW, jes.getStatus(dbId));
        }
    }
    
    public void testGetAllPassed() throws SQLException{
        EventAccessOperations[] evs = MockEventAccessOperations.createEvents();
        for (int i = 0; i < evs.length; i++) {
            jes.setStatus(evs[i], EventCondition.PROCESSOR_PASSED);
        }
        CacheEvent[] gotten = jes.getAll(EventCondition.PROCESSOR_PASSED);
        for (int j = 0; j < evs.length; j++) {
            boolean found = false;
            for (int i = 0; i < gotten.length && !found; i++) {
                if(gotten[i].equals(evs[j])) found = true;
            }
            assertTrue(evs[j]+ " wasn't returned by get all", found);
        }
    }
    
    public void testGetAll() throws SQLException{
        EventAccessOperations[] evs = MockEventAccessOperations.createEvents();
        for (int i = 0; i < evs.length; i++) {
            jes.setStatus(evs[i], EventCondition.NEW);
        }
        CacheEvent[] gotten = jes.getAll();
        for (int j = 0; j < evs.length; j++) {
            boolean found = false;
            for (int i = 0; i < gotten.length && !found; i++) {
                if(gotten[i].equals(evs[j])) found = true;
            }
            assertTrue(evs[j]+ " wasn't returned by get all", found);
        }
    }
    
    private JDBCEventStatus jes;
}
