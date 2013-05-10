package edu.sc.seis.sod.status;


import java.lang.reflect.Field;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.Map;

import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

import edu.iris.Fissures.IfNetwork.Channel;
import edu.sc.seis.sod.ConfigurationException;
import edu.sc.seis.sod.SodUtil;
import edu.sc.seis.sod.Standing;
import edu.sc.seis.sod.Status;

public class ChannelGroupTemplate extends Template implements GenericTemplate{
    Map channelMap = new HashMap();

    public ChannelGroupTemplate(Element el) throws ConfigurationException {
        parse(el);
    }

    public String getResult() {
        StringBuffer buf = new StringBuffer();
        synchronized(channelMap){
            Iterator it = channelMap.keySet().iterator();
            while(it.hasNext()){
                Channel curChan = (Channel)it.next();
                Status status = (Status)channelMap.get(curChan);
                if ((useStanding.size()==0 && ! notUseStanding.contains(status.getStanding()))
                    || useStanding.contains(status.getStanding())) {
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
            if(status == null)throw new IllegalArgumentException("Status should not be null");
            channelMap.put(chan, status);
        }
    }

    public String getStatus(Channel chan){
        return channelMap.get(chan).toString();
    }

    public Object getTemplate(String tag, Element element) throws ConfigurationException {
        if(tag.equals("channel")) {
            return new ChannelFormatter(element, this);
        } else if (tag.equals("statusFilter")) {
            NodeList nl = element.getChildNodes();
            for (int i = 0; i < nl.getLength(); i++) {
                if (nl.item(i) instanceof Element) {
                    Element child = (Element)nl.item(i);
                    String name = SodUtil.getNestedText(child);
                    try {
                        if (child.getTagName().equals("status")) {
                            useStanding.add(Standing.getForName(name));
                        } else if (child.getTagName().equals("notStatus")) {
                            notUseStanding.add(Standing.getForName(name));
                        }
                    } catch (NoSuchFieldException e) {
                        // this means the config file is wrong, the name is not
                        // a valid Standing...
                        String msg = "status tag "+name+" is not a valid Standing, please use one of: ";

                        Field[] fields = Standing.class.getFields();
                        for (int fieldIndex = 0; fieldIndex < fields.length; fieldIndex++) {
                            if (fields[fieldIndex].getType().equals(Standing.class)) {
                                msg+=((fieldIndex==0)?" ":", ")+fields[fieldIndex].getName();
                            }
                        }
                        throw new ConfigurationException(msg);
                    }
                }
            }
            return new AllTextTemplate("");
        }
        return getCommonTemplate(tag, element);
    }

    LinkedList useStanding = new LinkedList();
    LinkedList notUseStanding = new LinkedList();

}

