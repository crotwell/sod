/**
 * StatusPrintln.java
 *
 * @author Created by Omnicore CodeGuide
 */

package edu.sc.seis.sod.status.eventArm;

import org.w3c.dom.Element;

import edu.sc.seis.sod.model.event.StatefulEvent;
import edu.sc.seis.sod.util.display.EventUtil;

public class StatusPrintln implements EventMonitor{
    public StatusPrintln(Element config){}

    public void setArmStatus(String status) {
        System.out.println("Event Arm: " + status);
    }

    public void change(StatefulEvent event ) {
        System.out.println(event.getStatus() + ": " + EventUtil.getEventInfo(event));
    }
}
