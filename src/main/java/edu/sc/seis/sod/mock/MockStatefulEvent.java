package edu.sc.seis.sod.mock;

import edu.iris.Fissures.IfEvent.NoPreferredOrigin;
import edu.sc.seis.fissuresUtil.mockFissures.IfEvent.MockEventAccessOperations;
import edu.sc.seis.sod.Status;
import edu.sc.seis.sod.hibernate.StatefulEvent;


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
