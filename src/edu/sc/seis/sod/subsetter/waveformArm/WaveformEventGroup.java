package edu.sc.seis.sod.subsetter.waveFormArm;

import edu.iris.Fissures.IfEvent.EventAccessOperations;
import edu.sc.seis.sod.EventChannelPair;
import edu.sc.seis.sod.WaveFormStatus;
import edu.sc.seis.sod.database.Status;
import edu.sc.seis.sod.subsetter.eventArm.EventGroupTemplate;
import edu.sc.seis.sod.subsetter.eventArm.EventTemplate;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

public class WaveformEventGroup extends EventGroupTemplate implements WaveFormStatus{
    public WaveformEventGroup(){ this(null); }
    
    public WaveformEventGroup(Element el){ super(el); }
    
    public void setUp(){ ecpListeners = new ArrayList(); }
    
    public Object getInterpreter(String tag, Element el){
        if(tag.equals("channelCount")) return new ChannelCount(el);
        return super.getInterpreter(tag, el);
    }
    
    public boolean isInterpreted(String tag){
        if(tag.equals("channelCount")) return true;
        return super.isInterpreted(tag);
    }
    
    public void update(EventChannelPair ecp) {
        Iterator it = ecpListeners.iterator();
        while(it.hasNext())((WaveFormStatus)it.next()).update(ecp);
        super.change(ecp.getEvent(), null);
    }
    
    public void useDefaultConfig(){
        pieces.add(new ChannelCount(null));
        pieces.add("\n");
    }
    
    private class ChannelCount implements EventTemplate, WaveFormStatus{
        public ChannelCount(Element el){
            ecpListeners.add(this);
            if(el != null && el.getChildNodes().getLength() != 0){
                NodeList nl = el.getChildNodes();
                for (int i = 0; i < nl.getLength(); i++) {
                    String curNode = nl.item(i).getNodeName();
                    if(curNode.equals("COMPLETE_SUCCESS")){
                        monitoredStatus.add(Status.COMPLETE_SUCCESS);
                    }else if(curNode.equals("COMPLETE_REJECT")){
                        monitoredStatus.add(Status.COMPLETE_REJECT);
                    }else if(curNode.equals("NEW")){
                        monitoredStatus.add(Status.NEW);
                    }else if(curNode.equals("PROCESSING")){
                        monitoredStatus.add(Status.PROCESSING);
                    }else if(curNode.equals("RE_OPEN")){
                        monitoredStatus.add(Status.RE_OPEN);
                    }else if(curNode.equals("RE_OPEN_PROCESSING")){
                        monitoredStatus.add(Status.RE_OPEN_PROCESSING);
                    }else if(curNode.equals("RE_OPEN_SUCCESS")){
                        monitoredStatus.add(Status.RE_OPEN_SUCCESS);
                    }else if(curNode.equals("AWAITING_FINAL_STATUS")){
                        monitoredStatus.add(Status.AWAITING_FINAL_STATUS);
                    }else if(curNode.equals("SOD_FAILURE")){
                        monitoredStatus.add(Status.SOD_FAILURE);
                    }else if(curNode.equals("RE_OPEN_REJECT")){
                        monitoredStatus.add(Status.RE_OPEN_REJECT);
                    }
                }
            }else monitoredStatus.add(Status.COMPLETE_SUCCESS);
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
    
    private List ecpListeners;
}
