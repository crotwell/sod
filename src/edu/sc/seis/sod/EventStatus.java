/**
 * EventStatusjava.java
 *
 * @author Created by Omnicore CodeGuide
 */

package edu.sc.seis.sod;

import edu.iris.Fissures.IfEvent.EventAccessOperations;
import edu.sc.seis.sod.database.Status;

public interface EventStatus extends SodElement {
    
    public void setArmStatus(String status);
    
    public void change(EventAccessOperations event, RunStatus status);
}

