/**
 * StatusPrintln.java
 *
 * @author Created by Omnicore CodeGuide
 */

package edu.sc.seis.sod.status.eventArm;

import edu.iris.Fissures.IfEvent.EventAccessOperations;
import edu.sc.seis.fissuresUtil.cache.CacheEvent;
import edu.sc.seis.sod.status.eventArm.EventStatus;
import edu.sc.seis.sod.database.event.EventCondition;
import org.w3c.dom.Element;

public class StatusPrintln implements EventStatus{
    public StatusPrintln(Element config){}
    
    public void setArmStatus(String status) {
        System.out.println("Event Arm: " + status);
    }
    
    public void change(EventAccessOperations event, EventCondition status) {
        System.out.println(status + ": " + CacheEvent.getEventInfo(event));
    }
}
