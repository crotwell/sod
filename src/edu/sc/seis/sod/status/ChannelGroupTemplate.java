package edu.sc.seis.sod.status;


import edu.iris.Fissures.IfNetwork.Channel;
import edu.sc.seis.sod.SodUtil;
import edu.sc.seis.sod.Status;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.Map;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

public class ChannelGroupTemplate extends Template implements GenericTemplate{
    private Map channelMap = new HashMap();

    public ChannelGroupTemplate(Element el){ parse(el);  }

    public String getResult() {
        StringBuffer buf = new StringBuffer();
        synchronized(channelMap){
            Iterator it = channelMap.keySet().iterator();
            while(it.hasNext()){
                Channel curChan = (Channel)it.next();
                Status status = (Status)channelMap.get(curChan);
                if ((useStatus.size()==0 && ! notUseStatus.contains(status)) || useStatus.contains(status) ) {
                    Iterator templateIt = templates.iterator();
                    while(templateIt.hasNext()){
                        buf.append(((ChannelTemplate)templateIt.next()).getResult(curChan));
                    }
                } // don't do anything otherwise
            }
        }
        return buf.toString();
    }

    protected Object textTemplate(final String text) {
        return new ChannelTemplate(){
            public String getResult(Channel chan) { return text; }
        };
    }

    public void change(Channel chan, Status status){
        synchronized(channelMap) {
            channelMap.put(chan, status);
        }
    }

    public String getStatus(Channel chan){
        return channelMap.get(chan).toString();
    }

    public Object getTemplate(String tag, Element element){
        if(tag.equals("channel")) {
            return new ChannelFormatter(element, this);
        } else if (tag.equals("statusFilter")) {
            NodeList nl = element.getChildNodes();
            for (int i = 0; i < nl.getLength(); i++) {
                if (nl.item(i) instanceof Element) {
                    Element child = (Element)nl.item(i);
                    Status[] statusArr = Status.getAllForType(SodUtil.getNestedText(child));
                    if (child.getTagName().equals("status")) {
                        for (int j = 0; j < statusArr.length; j++) {
                            useStatus.add(statusArr[j]);
                        }
                    } else if (child.getTagName().equals("notStatus")) {
                        for (int j = 0; j < statusArr.length; j++) {
                            notUseStatus.add(statusArr[j]);
                        }
                    }
                }
            }
            return new AllTextTemplate("");
        }
        return super.getTemplate(tag, element);
    }

    LinkedList useStatus = new LinkedList();
    LinkedList notUseStatus = new LinkedList();

}

