package edu.sc.seis.sod.status.waveformArm;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.w3c.dom.Element;

import edu.sc.seis.sod.ConfigurationException;
import edu.sc.seis.sod.EventChannelPair;
import edu.sc.seis.sod.EventNetworkPair;
import edu.sc.seis.sod.EventStationPair;
import edu.sc.seis.sod.EventVectorPair;
import edu.sc.seis.sod.status.eventArm.EventGroupTemplate;

public class WaveformEventGroup extends EventGroupTemplate implements WaveformMonitor{
    public WaveformEventGroup(){ useDefaultConfig(); }

    public WaveformEventGroup(Element el) throws ConfigurationException {
        parse(el);
    }

    public Object getTemplate(String tag, Element el) throws ConfigurationException {
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

    public void update(EventNetworkPair ecp) {
        Iterator it = ecpListeners.iterator();
        while(it.hasNext())((WaveformMonitor)it.next()).update(ecp);
    }

    public void update(EventStationPair ecp) {
        Iterator it = ecpListeners.iterator();
        while(it.hasNext())((WaveformMonitor)it.next()).update(ecp);
    }

    public void update(EventChannelPair ecp) {
        Iterator it = ecpListeners.iterator();
        while(it.hasNext())((WaveformMonitor)it.next()).update(ecp);
    }

    public void update(EventVectorPair ecp) {
        Iterator it = ecpListeners.iterator();
        while(it.hasNext())((WaveformMonitor)it.next()).update(ecp);
    }

    private List ecpListeners = new ArrayList();
}
