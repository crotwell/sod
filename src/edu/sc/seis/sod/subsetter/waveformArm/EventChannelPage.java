/**
 * EventPage.java
 *
 * @author Created by Charles Groves
 */

package edu.sc.seis.sod.subsetter.waveFormArm;

import edu.iris.Fissures.IfEvent.EventAccessOperations;
import edu.iris.Fissures.IfNetwork.Channel;
import edu.iris.Fissures.network.ChannelIdUtil;
import edu.sc.seis.fissuresUtil.cache.CacheEvent;
import edu.sc.seis.sod.subsetter.PageSection;
import edu.sc.seis.sod.subsetter.SimpleHTMLPage;
import java.io.File;

public class EventChannelPage extends SimpleHTMLPage{
    public EventChannelPage(String title, File location, EventAccessOperations event){
        super(title, location, false);
        String eventOutput = CacheEvent.getEventInfo(event, eventFormat);
        chanSec = new PageSection("Channels for " + eventOutput);
        sections.add(chanSec);
    }
    
    public void add(Channel c){
        chanSec.append(ChannelIdUtil.toString(c.get_id()) + "<br>\n");
        write();
    }
    
    PageSection chanSec;
    
    private String eventFormat = CacheEvent.LOC + " " + CacheEvent.TIME + " Mag: " + CacheEvent.MAG + " Depth: " + CacheEvent.DEPTH + " " + CacheEvent.DEPTH_UNIT;
}
