package edu.sc.seis.sod.subsetter.waveFormArm;

import edu.iris.Fissures.IfEvent.EventAccessOperations;
import edu.sc.seis.sod.EventChannelPair;
import edu.sc.seis.sod.WaveFormStatus;
import edu.sc.seis.sod.subsetter.NameGenerator;
import edu.sc.seis.sod.subsetter.eventArm.EventTemplate;
import java.util.HashMap;
import java.util.Map;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;
import java.io.IOException;

public class EventChannelsTemplate implements EventTemplate, WaveFormStatus{
    public EventChannelsTemplate(){ this(null); }
    
    public EventChannelsTemplate(Element el){
        if(el != null){
            NodeList nl = el.getChildNodes();
            for (int i = 0; i < nl.getLength(); i++) {
                String tag = nl.item(i).getNodeName();
                if(tag.equals("eventLabel")){
                    gen = new NameGenerator((Element)nl.item(i), true);
                }else if(tag.equals("perEventTemplate")){
                    wetEl = (Element)nl.item(i);
                }
            }
        }
        if(gen == null) gen = new NameGenerator(true);
    }
    
    public String getResult(EventAccessOperations ev) {
        return gen.getResult(ev);
    }
    
    public void update(EventChannelPair ecp) {
        WaveformEventTemplate wet = (WaveformEventTemplate)eventsToTemplates.get(ecp.getEvent());
        if(wet == null){
            try {
                wet = new WaveformEventTemplate(makeSpecific(wetEl, ecp.getEvent()));
            } catch (IOException e) {
                e.printStackTrace();
            }
            eventsToTemplates.put(ecp.getEvent(), wet);
        }
        wet.update(ecp);
    }
    
    /**
     * makeSpecific changes the outputLocation attribute of the generic
     * Element passed in to be the filename returned by this template for the
     * passed in event
     */
    public Element makeSpecific(Element generic, EventAccessOperations ev){
        Element specific = (Element)generic.cloneNode(true);
        specific.setAttribute("outputLocation", gen.getResult(ev));
        return specific;
    }
    
    private Map eventsToTemplates = new HashMap();
    
    private NameGenerator gen;
    
    private Element wetEl;
}
