/**
 * WaveformStatusTemplate.java
 *
 * @author Created by Charles Groves
 */

package edu.sc.seis.sod.subsetter.waveFormArm;

import edu.sc.seis.sod.EventChannelPair;
import edu.sc.seis.sod.WaveFormStatus;
import edu.sc.seis.sod.subsetter.ExternalFileTemplate;
import edu.sc.seis.sod.subsetter.eventArm.EventGroupTemplate;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import org.w3c.dom.Element;

public class WaveformStatusTemplate extends ExternalFileTemplate implements WaveFormStatus{
    public WaveformStatusTemplate(Element el)throws IOException { super(el); }
    
    protected void setUp(){ eventTemplates = new ArrayList(); }
    
    protected Object getTemplate(String tag, Element el) {
        if(tag.equals("events")){
            WaveformEventGroup ect = new WaveformEventGroup(el);
            eventTemplates.add(ect);
            return ect;
        }
        return null;
    }
    
    public void update(EventChannelPair ecp) {
        Iterator it = eventTemplates.iterator();
        while(it.hasNext()) ((WaveformEventGroup)it.next()).update(ecp);
        update();
    }
    
    private List eventTemplates;
}
