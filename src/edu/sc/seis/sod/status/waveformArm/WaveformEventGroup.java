package edu.sc.seis.sod.status.waveFormArm;

import edu.iris.Fissures.IfEvent.EventAccessOperations;
import edu.sc.seis.sod.EventChannelPair;
import edu.sc.seis.sod.WaveFormStatus;
import edu.sc.seis.sod.database.Status;
import edu.sc.seis.sod.database.waveform.EventChannelCondition;
import edu.sc.seis.sod.status.eventArm.EventGroupTemplate;
import edu.sc.seis.sod.status.eventArm.EventTemplate;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

public class WaveformEventGroup extends EventGroupTemplate implements WaveFormStatus{
    public WaveformEventGroup(){
        useDefaultConfig();
    }
    
    public WaveformEventGroup(Element el) {
        parse(el);
    }
    
    public Object getTemplate(String tag, Element el) {
        if(tag.equals("channelCount")) return new EventChannelStatus(el);
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
    
    public void update(EventChannelPair ecp) throws Exception {
        Iterator it = ecpListeners.iterator();
        while(it.hasNext())((WaveFormStatus)it.next()).update(ecp);
        super.change(ecp.getEvent(), null);
    }
    
    public void useDefaultConfig(){
        templates.add(new EventChannelStatus(null));
        templates.add("\n");
    }
    
    private class EventChannelStatus implements EventTemplate, WaveFormStatus{
        public EventChannelStatus(Element el){
            ecpListeners.add(this);
            if(el != null && el.getChildNodes().getLength() != 0){
                NodeList nl = el.getChildNodes();
                for (int i = 0; i < nl.getLength(); i++) {
                    String curNode = nl.item(i).getNodeName();
                    if(curNode.equals("SUCCESS")){
                        monitoredStatus.add(EventChannelCondition.SUCCESS);
                    }else if(curNode.equals("FAILURE")){
                        monitoredStatus.add(EventChannelCondition.FAILURE);
                    }else if(curNode.equals("NEW")){
                        monitoredStatus.add(EventChannelCondition.NEW);
                    }else if(curNode.equals("PROCESSING")){
                        monitoredStatus.add(EventChannelCondition.SUBSETTER_PASSED);
                    }
                }
            }else monitoredStatus.add(EventChannelCondition.SUCCESS);
        }
        
        public void update(EventChannelPair ecp) {
            if(monitoredStatus.contains(ecp.getStatus())){
                Integer cur = new Integer(0);
                if(evChans.containsKey(ecp.getEvent())){
                    cur = (Integer)evChans.get(ecp.getEvent());
                }
                cur = new Integer(cur.intValue() + 1);
                evChans.put(ecp.getEvent(), cur);
            }
        }
        
        public String getResult(EventAccessOperations ev) {
            if(evChans.containsKey(ev))
                return ((Integer)evChans.get(ev)).toString();
            else
                return "0";
        }
        
        private List monitoredStatus =  new ArrayList();
        
        private Map evChans = new HashMap();
    }
    
    private List ecpListeners = new ArrayList();
}
