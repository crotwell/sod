/**
 * LogFactor5Status.java
 *
 * @author Created by Omnicore CodeGuide
 */

package edu.sc.seis.sod.subsetter.eventArm;

import edu.iris.Fissures.IfEvent.EventAccessOperations;
import edu.sc.seis.fissuresUtil.cache.CacheEvent;
import edu.sc.seis.sod.CommonAccess;
import edu.sc.seis.sod.EventStatus;
import edu.sc.seis.sod.RunStatus;
import org.apache.log4j.lf5.LogLevel;
import org.w3c.dom.Element;

public class LogFactor5EventStatus implements EventStatus{
    public LogFactor5EventStatus(Element config){}
    
    public void setArmStatus(String status) {
        log("Event Arm", RunStatus.GENERIC_STATUS.getLogLevel(), status);
    }
    
    public void change(EventAccessOperations event, RunStatus status) {
        log("Event Arm.Events", status.getLogLevel(), status + ": " + CacheEvent.getEventInfo(event));
    }
    
    private static void log(String category, LogLevel level, String message){
        CommonAccess.getCommonAccess().getLF5Adapter().log(category, level, message);
    }
}

