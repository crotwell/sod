/**
 * SimpleHTMlStatus.java
 *
 * @author Created by Omnicore CodeGuide
 */

package edu.sc.seis.sod.subsetter.eventArm;

import edu.iris.Fissures.IfEvent.EventAccessOperations;
import edu.sc.seis.fissuresUtil.display.DisplayUtils;
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
        page = new SimpleHTMLPage("Event Arm Status", "eventArm.html", htmlDir);
    }
    
    public void setArmStatus(String status) {
        page.clear("Status");
        page.append("Status", status);
        page.write();
    }
    
    public void change(EventAccessOperations event, RunStatus status) {
        if(status == RunStatus.FAILED){
            page.append("Failures", DisplayUtils.getEventInfo(event));
        }else if(status == RunStatus.PASSED){
            page.append("Successes", DisplayUtils.getEventInfo(event));
        }
        page.write();
    }
    
    SimpleHTMLPage page;
}

