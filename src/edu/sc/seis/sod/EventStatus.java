/**
 * EventStatusjava.java
 *
 * @author Created by Omnicore CodeGuide
 */

package edu.sc.seis.sod;

import edu.iris.Fissures.IfEvent.EventAccessOperations;

public interface EventStatus extends SodElement {
    
    public void setArmStatus(String status);
    
    public void change(EventAccessOperations event, RunStatus status);
}

