package edu.sc.seis.sod.status.waveFormArm;

import edu.iris.Fissures.IfEvent.EventAccessOperations;
import edu.sc.seis.sod.EventChannelPair;
import edu.sc.seis.sod.status.eventArm.EventGroupTemplate;
import edu.sc.seis.sod.status.eventArm.EventTemplate;
import edu.sc.seis.sod.status.waveFormArm.WaveformArmMonitor;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

public class WaveformEventGroup extends EventGroupTemplate implements WaveformArmMonitor{
    public WaveformEventGroup(){ useDefaultConfig(); }

    public WaveformEventGroup(Element el) { parse(el); }

    public Object getTemplate(String tag, Element el) {
        try {
            // this is a weird template due to dependence on external an xml template
            // maybe this should be changed later...
            // TODO
            if(tag.equals("waveformEvents")) return new WaveformEventTemplateGenerator(el);
        } catch (Exception e) {
            throw new RuntimeException("Problem getting template in WaveformEventGroup", e);
        }
        return super.getTemplate(tag, el);
    }

    public void update(EventChannelPair ecp) {
        Iterator it = ecpListeners.iterator();
        while(it.hasNext())((WaveformArmMonitor)it.next()).update(ecp);
    }

    private List ecpListeners = new ArrayList();
}
