/**
 * WaveformStatusTemplate.java
 *
 * @author Created by Charles Groves
 */

package edu.sc.seis.sod.status.waveformArm;

import edu.sc.seis.sod.EventChannelPair;
import edu.sc.seis.sod.status.waveformArm.WaveformArmMonitor;
import edu.sc.seis.sod.status.FileWritingTemplate;
import edu.sc.seis.sod.status.eventArm.EventGroupTemplate;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import org.w3c.dom.Element;

public class WaveformStatusTemplate extends FileWritingTemplate implements WaveformArmMonitor{
    public WaveformStatusTemplate(Element el)throws IOException {
        super(el.getAttribute("filDir"), el.getAttribute("outputLocation"));
        //TODO get parsing setup
    }

    protected Object getTemplate(String tag, Element el) {
        if(tag.equals("events")){
            WaveformEventGroup ect = new WaveformEventGroup(el);
            eventTemplates.add(ect);
            return ect;
        }
        return super.getTemplate(tag, el);
    }

    public void update(EventChannelPair ecp) {
        Iterator it = eventTemplates.iterator();
        while(it.hasNext()) ((WaveformArmMonitor)it.next()).update(ecp);
        write();
    }

    private List eventTemplates = new ArrayList();
}
