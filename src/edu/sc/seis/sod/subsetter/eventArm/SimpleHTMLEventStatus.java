/**
 * SimpleHTMlStatus.java
 *
 * @author Created by Omnicore CodeGuide
 */

package edu.sc.seis.sod.subsetter.eventArm;

import edu.iris.Fissures.IfEvent.EventAccessOperations;
import edu.sc.seis.fissuresUtil.cache.CacheEvent;
import edu.sc.seis.sod.ConfigurationException;
import edu.sc.seis.sod.EventStatus;
import edu.sc.seis.sod.RunStatus;
import edu.sc.seis.sod.SodUtil;
import edu.sc.seis.sod.subsetter.SimpleHTMLPage;
import java.io.File;
import org.w3c.dom.Element;

public class SimpleHTMLEventStatus implements EventStatus{
    public SimpleHTMLEventStatus(Element config) throws ConfigurationException{
        File htmlDir = SodUtil.makeOutputDirectory(config);
        page = new SimpleHTMLPage("Event Arm Status",
                                  new File(htmlDir, "eventArm.html"));
        page.append("Status", "Starting up",0);
        page.append("Successes", "",1);
        page.append("Failures", "",2);
    }
    
    public void setArmStatus(String status) {
        page.clear("Status");
        page.append("Status", status, 0);
        page.write();
    }
    
    public void change(EventAccessOperations event, RunStatus status) {
        if(status == RunStatus.FAILED){
            page.append("Failures", CacheEvent.getEventInfo(event), 2);
        }else if(status == RunStatus.PASSED){
            page.append("Successes", CacheEvent.getEventInfo(event), 1);
        }
        page.write();
    }
    
    SimpleHTMLPage page;
}

