/**
 * EventStatusjava.java
 *
 * @author Created by Omnicore CodeGuide
 */

package edu.sc.seis.sod.status.eventArm;
import edu.sc.seis.sod.*;

import edu.iris.Fissures.IfEvent.EventAccessOperations;
import edu.sc.seis.sod.database.event.EventCondition;

public interface EventArmMonitor extends SodElement {

    public void setArmStatus(String status) throws Exception;

    public void change(EventAccessOperations event, EventCondition status) throws Exception;
}

