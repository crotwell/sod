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
import edu.sc.seis.sod.subsetter.TabularSection;
import java.io.File;
import java.io.IOException;
import org.w3c.dom.Element;

public class SimpleHTMLEventStatus implements EventStatus{
    public SimpleHTMLEventStatus(Element config) throws ConfigurationException, IOException {
        File htmlDir = SodUtil.makeOutputDirectory(config);
        page = new SimpleHTMLPage("Event Arm Status",
                                  new File(htmlDir, "eventArm.html"));
        page.append("Status", "Starting up",0);
        page.add(successes);
        page.add(failures);
    }

    public void setArmStatus(String status) throws IOException {
        page.clear("Status");
        page.append("Status", status, 0);
        page.write();
    }

    public void change(EventAccessOperations event, RunStatus status) throws IOException {
        if(status == RunStatus.FAILED){
            failures.append(CacheEvent.getEventInfo(event), parse(event));
        }else if(status == RunStatus.PASSED){
            successes.append(CacheEvent.getEventInfo(event), parse(event));
        }
        page.write();
    }

    public static String[] parse(EventAccessOperations e){
        String[] items = new String[4];
        items[0] = CacheEvent.getEventInfo(e, CacheEvent.LOC);
        items[1] = CacheEvent.getEventInfo(e, CacheEvent.TIME);
        items[2] = CacheEvent.getEventInfo(e, CacheEvent.MAG);
        items[3] = CacheEvent.getEventInfo(e, CacheEvent.DEPTH + " " + CacheEvent.DEPTH_UNIT);
        return items;
    }

    private String[] eventColumns = { "Location", "Time", "Magnitude", "Depth"};

    private TabularSection successes = new TabularSection("Successes", eventColumns);

    private TabularSection failures = new TabularSection("Failures", eventColumns);

    SimpleHTMLPage page;
}

