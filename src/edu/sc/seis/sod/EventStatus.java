/**
 * EventStatusjava.java
 *
 * @author Created by Omnicore CodeGuide
 */

package edu.sc.seis.sod;

import edu.iris.Fissures.IfEvent.EventAccessOperations;

public interface EventStatus {

    public void begin(EventAccessOperations event);

    public void fail(EventAccessOperations event, String reason);

    public void succeed(EventAccessOperations event);

}

