package edu.sc.seis.sod.subsetter;
import edu.iris.Fissures.IfNetwork.Channel;
import edu.sc.seis.sod.RunStatus;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import org.w3c.dom.Element;



public class ChannelGroupTemplate extends Template implements GenericTemplate{
    Map channelMap = new HashMap();
    
    public ChannelGroupTemplate(Element el){ parse(el);  }
    
    public String getResult() {
        StringBuffer buf = new StringBuffer();
        Iterator it = channelMap.keySet().iterator();
        while(it.hasNext()){
            Channel curChan = (Channel)it.next();
            Iterator templateIt = templates.iterator();
            while(templateIt.hasNext()){
                buf.append(((ChannelTemplate)templateIt.next()).getResult(curChan));
            }
        }
        return buf.toString();
    }
    
     protected Object textTemplate(final String text) {
        return new ChannelTemplate(){
            public String getResult(Channel chan) { return text; }
        };
    }
    
    public void change(Channel chan, RunStatus status){
        channelMap.put(chan, status);
    }
    
    public Object getTemplate(String tag, Element element){
        if(tag.equals("channel")) return new ChannelFormatter(element);
        return null;
    }
    
}

