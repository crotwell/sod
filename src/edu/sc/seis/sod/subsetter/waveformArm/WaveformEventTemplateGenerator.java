package edu.sc.seis.sod.subsetter.waveFormArm;

import edu.sc.seis.sod.*;

import edu.iris.Fissures.IfEvent.EventAccessOperations;
import edu.sc.seis.sod.subsetter.EventFormatter;
import edu.sc.seis.sod.subsetter.TemplateFileLoader;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

public class WaveformEventTemplateGenerator implements EventStatus, WaveFormStatus{
    public WaveformEventTemplateGenerator(Element el){
        if(Start.getEventArm() != null) Start.getEventArm().add(this);
        NodeList nl = el.getChildNodes();
        for (int i = 0; i < nl.getLength(); i++) {
            Node n = nl.item(i);
            if(n.getNodeName().equals("fileLoc")){
                formatter = new EventFormatter((Element)n);
            }else if(n.getNodeName().equals("externalConfig")){
                try {
                    config = TemplateFileLoader.getTemplate((Element)n);
                } catch (IOException e) {
                    CommonAccess.handleException(e, "trouble getting config template");
                }
            }
        }
        if(formatter == null  || config == null)
            throw new IllegalArgumentException("The configuration element must contain a fileLoc and a externalConfig");
    }
    
    public void change(EventAccessOperations event, RunStatus status) {
        getTemplate(event);
    }
    
    public WaveformEventTemplate getTemplate(EventAccessOperations ev){
        if(!eventTemplates.containsKey(ev)){
            eventTemplates.put(ev, new WaveformEventTemplate(config, formatter.getResult(ev), ev));
        }
        return (WaveformEventTemplate)eventTemplates.get(ev);
    }
    
    public boolean contains(EventAccessOperations ev){
        return eventTemplates.containsKey(ev);
    }
    
    public void setArmStatus(String status) {}
    
    public void update(EventChannelPair ecp) {}
    
    
    private Element config;
    
    private EventFormatter formatter;
    
    private Map eventTemplates = new HashMap();
}
