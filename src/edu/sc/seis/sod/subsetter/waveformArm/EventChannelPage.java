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
import edu.sc.seis.fissuresUtil.chooser.ClockUtil;
import edu.sc.seis.sod.database.waveform.EventChannelCondition;
import edu.sc.seis.sod.subsetter.PageSection;
import edu.sc.seis.sod.subsetter.SimpleHTMLPage;
import java.io.File;
import java.io.IOException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;

public class EventChannelPage extends SimpleHTMLPage{
    public EventChannelPage(String title, File location, EventAccessOperations event){
        super(title, location, false);
        String eventOutput = CacheEvent.getEventInfo(event, eventFormat);
        chanSec = new PageSection("Channels for " + eventOutput);
        sections.add(chanSec);
    }

    public void add(Channel c, EventChannelCondition string) throws IOException{
        add(c, string, false);
    }

    public void add(Channel c, EventChannelCondition string, boolean stampTime) throws IOException {
        String time = "";
        if(stampTime) time = df.format(ClockUtil.now())  + " ";
        chanSec.append(time + string + " " + ChannelIdUtil.toString(c.get_id()) + "<br>\n");
        write();
    }

    public static final DateFormat df = new SimpleDateFormat("HH:mm:ss.SSS");

    PageSection chanSec;

    private String eventFormat = CacheEvent.LOC + " " + CacheEvent.TIME + " Mag: " + CacheEvent.MAG + " Depth: " + CacheEvent.DEPTH + " " + CacheEvent.DEPTH_UNIT;
}
