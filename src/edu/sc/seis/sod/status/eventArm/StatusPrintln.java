/**
 * StatusPrintln.java
 *
 * @author Created by Omnicore CodeGuide
 */

package edu.sc.seis.sod.status.eventArm;

import org.w3c.dom.Element;

import edu.iris.Fissures.IfEvent.EventAccessOperations;
import edu.sc.seis.fissuresUtil.cache.EventUtil;
import edu.sc.seis.sod.Status;

public class StatusPrintln implements EventArmMonitor{
    public StatusPrintln(Element config){}

    public void setArmStatus(String status) {
        System.out.println("Event Arm: " + status);
    }

    public void change(EventAccessOperations event, Status status) {
        System.out.println(status + ": " + EventUtil.getEventInfo(event));
    }
}
