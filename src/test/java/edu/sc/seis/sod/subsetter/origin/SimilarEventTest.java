package edu.sc.seis.sod.subsetter.origin;

import java.util.ArrayList;
import java.util.Iterator;

import junit.framework.TestCase;
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
        ArrayList events = similar.eventList;
        Iterator it = events.iterator();
        while (it.hasNext()) {
            CacheEvent e = (CacheEvent)it.next();
            assertTrue(""+e, similar.accept(e, e.get_attributes(), e.get_preferred_origin()).isSuccess());
        }
    }
    
    SimilarEvent similar;
}
