package edu.sc.seis.sod.subsetter.waveFormArm;

import edu.iris.Fissures.IfEvent.EventAccessOperations;
import edu.sc.seis.sod.EventChannelPair;
import edu.sc.seis.sod.WaveFormStatus;
import edu.sc.seis.sod.database.Status;
import edu.sc.seis.sod.subsetter.eventArm.EventGroupTemplate;
import edu.sc.seis.sod.subsetter.eventArm.EventTemplate;
import java.util.HashMap;
import java.util.Map;
import org.w3c.dom.Element;

public class WaveformEventGroup extends EventGroupTemplate implements WaveFormStatus{
    public WaveformEventGroup(){ this(null); }
    
    public WaveformEventGroup(Element el){ super(el); }
    
    public Object getInterpreter(String tag, Element el){
        if(tag.equals("channelCount")) return new ChannelCount();
        return super.getInterpreter(tag, el);
    }
    
    public boolean isInterpreted(String tag){
        if(tag.equals("channelCount")) return true;
        return super.isInterpreted(tag);
    }
    
    public void update(EventChannelPair ecp) {
        if(ecp.getStatus().equals(Status.COMPLETE_SUCCESS)){
            Integer cur = new Integer(0);
            if(evChans.containsKey(ecp.getEvent())){
                cur = (Integer)evChans.get(ecp.getEvent());
            }
            cur = new Integer(cur.intValue() + 1);
            evChans.put(ecp.getEvent(), cur);
        }
        super.change(ecp.getEvent(), null);
    }
    
    public void useDefaultConfig(){
        pieces.add(new ChannelCount());
        pieces.add("\n");
    }
    
    private class ChannelCount implements EventTemplate, WaveFormStatus{
        
        public void update(EventChannelPair ecp) {
            // TODO
        }
        
        public String getResult(EventAccessOperations ev) {
            return getSuccessfulChannels(ev).toString();
        }
    }
    
    private Integer getSuccessfulChannels(EventAccessOperations ev) {
        return (Integer)evChans.get(ev);
    }
    
    private Map evChans = new HashMap();
}
