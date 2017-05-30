package edu.sc.seis.sod.mock;

import edu.sc.seis.sod.mock.event.MockEventAccessOperations;
import edu.sc.seis.sod.model.event.NoPreferredOrigin;
import edu.sc.seis.sod.model.event.StatefulEvent;
import edu.sc.seis.sod.model.status.Status;


public class MockStatefulEvent {
    
    public static StatefulEvent create() {
        try {
            return new StatefulEvent(MockEventAccessOperations.createEvent(), Status.getFromShort((short)258));
        } catch(NoPreferredOrigin e) {
            // can't happen
            throw new RuntimeException(e);
        }
    }
}
