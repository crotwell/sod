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

public class StatusPrintln implements EventStatus{
    public StatusPrintln(Element config){}
    
    public void fail(EventAccessOperations event, String reason) {
        System.out.println(DisplayUtils.getEventInfo(event) + " failed because " + reason);
    }
    
    public void begin(EventAccessOperations event) {
        System.out.print("began processing" + DisplayUtils.getEventInfo(event));
    }
    
    public void pass(EventAccessOperations event) {
        System.out.println(DisplayUtils.getEventInfo(event) + " passed through the event layer");
    }
    
    
}
