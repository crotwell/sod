/**
 * EventStatusjava.java
 *
 * @author Created by Omnicore CodeGuide
 */

package edu.sc.seis.sod.status.eventArm;
import edu.iris.Fissures.IfEvent.EventAccessOperations;
import edu.sc.seis.sod.SodElement;
import edu.sc.seis.sod.Status;

public interface EventArmMonitor extends SodElement {

    public void setArmStatus(String status) throws Exception;

    public void change(EventAccessOperations event, Status status);
}

