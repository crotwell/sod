/**
 * StatusPrintln.java
 *
 * @author Created by Omnicore CodeGuide
 */

package edu.sc.seis.sod.subsetter.eventArm;

import edu.iris.Fissures.IfEvent.EventAccessOperations;
import edu.sc.seis.fissuresUtil.display.DisplayUtils;
import edu.sc.seis.sod.EventStatus;
import org.w3c.dom.Element;
import edu.sc.seis.sod.RunStatus;

public class StatusPrintln implements EventStatus{
    public StatusPrintln(Element config){}
    
    public void setArmStatus(String status) {
        System.out.println("Event Arm: " + status);
    }
    
    public void change(EventAccessOperations event, RunStatus status) {
        System.out.println(status + ": " + DisplayUtils.getEventInfo(event));
    }
}
