/**
 * LogFactor5Status.java
 *
 * @author Created by Omnicore CodeGuide
 */

package edu.sc.seis.sod.subsetter.eventArm;

import edu.iris.Fissures.IfEvent.EventAccessOperations;
import edu.sc.seis.fissuresUtil.display.DisplayUtils;
import edu.sc.seis.sod.CommonAccess;
import edu.sc.seis.sod.EventStatus;
import org.apache.log4j.lf5.LogLevel;
import org.w3c.dom.Element;

public class LogFactor5Status implements EventStatus{
    public LogFactor5Status(Element config){}
    
    public void fail(EventAccessOperations event, String reason) {
        CommonAccess.getCommonAccess().getLF5Adapter().log("Event Arm", LogLevel.WARN,
                                                           "Failed: " + DisplayUtils.getEventInfo(event) + " because " + reason);
    }
    
    public void begin(EventAccessOperations event) {
        CommonAccess.getCommonAccess().getLF5Adapter().log("Event Arm", LogLevel.INFO,
                                                           "Began: " + DisplayUtils.getEventInfo(event));
    }
    
    public void pass(EventAccessOperations event) {
        CommonAccess.getCommonAccess().getLF5Adapter().log("Event Arm", LogLevel.INFO,
                                                           "Passed: " + DisplayUtils.getEventInfo(event));
    }
}

