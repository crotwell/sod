package edu.sc.seis.sod.subsetter.waveFormArm;
import edu.sc.seis.sod.subsetter.*;

import edu.iris.Fissures.IfEvent.EventAccessOperations;
import edu.sc.seis.sod.EventChannelPair;
import edu.sc.seis.sod.RunStatus;
import edu.sc.seis.sod.Start;
import edu.sc.seis.sod.WaveFormStatus;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import org.w3c.dom.Element;

public class WaveformEventTemplate extends FileWritingTemplate implements WaveFormStatus{
    public WaveformEventTemplate(Element el, EventAccessOperations event) throws IOException{
        this(TemplateFileLoader.getTemplate(el),
             el.getAttribute("outputLocation"),
             event);
    }
    
    public WaveformEventTemplate(Element el, String outputLocation, EventAccessOperations event){
        super(outputLocation);
        this.event = event;
        parse(el);
        if(Start.getWaveformArm() != null) Start.getWaveformArm().addStatusMonitor(this);
        write();
    }
    
    public void update(EventChannelPair ecp) {
        if(ecp.getEvent().equals(event)){
            Iterator it = waveformStatusListeners.iterator();
            while(it.hasNext()) ((WaveFormStatus)it.next()).update(ecp);
            it = channelListeners.iterator();
            while(it.hasNext()) ((ChannelGroupTemplate)it.next()).change(ecp.getChannel(),
                                                                         RunStatus.translate(ecp.getStatus()));
            write();
        }
    }
    
    protected Object getTemplate(String tag, Element el) {
        if(tag.equals("channels")) {
            ChannelGroupTemplate cgt = new ChannelGroupTemplate(el);
            channelListeners.add(cgt);
            return cgt;
        }
        if(tag.equals("map")){
            MapWaveFormStatus map = new MapWaveFormStatus(getOutputDirectory() + "/map.png", pool);
            map.add(event);
            waveformStatusListeners.add(map);
            map.write();
            return new GenericTemplate(){
                public String getResult() { return "map.png";}
            };
        }
        if(tag.equals("event")){  return new EventTemplate(el); }
        if(tag.equals("now")) return new NowTemplate();
        return null;
    }
    
    private class EventTemplate implements GenericTemplate{
        public EventTemplate(Element el){ formatter = new EventFormatter(el); }
        
        public String getResult() { return formatter.getResult(event); }
        
        private EventFormatter formatter;
    }
    
    private EventAccessOperations event;
    
    private static MapPool pool = new MapPool(2);
    
    private List waveformStatusListeners = new ArrayList();
    
    private List channelListeners = new ArrayList();
}
