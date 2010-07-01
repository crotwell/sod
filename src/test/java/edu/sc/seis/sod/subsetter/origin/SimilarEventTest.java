package edu.sc.seis.sod.subsetter.origin;

import java.util.ArrayList;
import java.util.Iterator;

import junit.framework.TestCase;
import edu.iris.Fissures.event.EventAttrImpl;
import edu.sc.seis.fissuresUtil.cache.CacheEvent;
import edu.sc.seis.fissuresUtil.mockFissures.IfEvent.MockEventAccessOperations;


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
