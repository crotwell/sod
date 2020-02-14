package edu.sc.seis.sod.subsetter.origin;

import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.ArrayList;
import java.util.Iterator;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import edu.sc.seis.sod.mock.event.MockEventAccessOperations;
import edu.sc.seis.sod.model.event.CacheEvent;
import edu.sc.seis.sod.model.event.EventAttrImpl;


public class SimilarEventTest  {

	@BeforeEach
    protected void setUp() throws Exception {
        similar = new SimilarEvent(MockEventAccessOperations.createEvents());
    }

	@AfterEach
    protected void tearDown() throws Exception {
        similar = null;
    }
    
	@Test
    public void testSimilar() throws Exception {
        ArrayList<CacheEvent> events = similar.eventList;
        Iterator<CacheEvent> it = events.iterator();
        while (it.hasNext()) {
            CacheEvent e = (CacheEvent)it.next();
            assertTrue( similar.accept(e, (EventAttrImpl)e.get_attributes(), e.getPreferred()).isSuccess(), ""+e);
        }
    }
    
    SimilarEvent similar;
}
