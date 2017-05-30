package edu.sc.seis.sod.subsetter.origin;

import java.util.ArrayList;
import java.util.Iterator;

import edu.sc.seis.sod.mock.event.MockEventAccessOperations;
import edu.sc.seis.sod.model.event.CacheEvent;
import edu.sc.seis.sod.model.event.EventAttrImpl;
import junit.framework.TestCase;


public class SimilarEventTest extends TestCase {

    protected void setUp() throws Exception {
        similar = new SimilarEvent(MockEventAccessOperations.createEvents());
    }

    protected void tearDown() throws Exception {
        similar = null;
    }
    
    public void testSimilar() throws Exception {
        ArrayList<CacheEvent> events = similar.eventList;
        Iterator<CacheEvent> it = events.iterator();
        while (it.hasNext()) {
            CacheEvent e = (CacheEvent)it.next();
            assertTrue(""+e, similar.accept(e, (EventAttrImpl)e.get_attributes(), e.getPreferred()).isSuccess());
        }
    }
    
    SimilarEvent similar;
}
