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
import edu.sc.seis.sod.RunStatus;
import org.apache.log4j.lf5.LogLevel;
import org.w3c.dom.Element;

public class LogFactor5Status implements EventStatus{
    public LogFactor5Status(Element config){}
    
    public void setArmStatus(String status) {
        CommonAccess.getCommonAccess().getLF5Adapter().log("Event Arm", RunStatus.GENERIC_STATUS,
                                                           status);
    }
    
    public void change(EventAccessOperations event, RunStatus status) {
        CommonAccess.getCommonAccess().getLF5Adapter().log("Event Arm.Events", status,
                                                           status + ": " + DisplayUtils.getEventInfo(event));
    }
}

