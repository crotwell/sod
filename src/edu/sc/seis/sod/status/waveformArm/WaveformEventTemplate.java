package edu.sc.seis.sod.status.waveformArm;

import edu.sc.seis.sod.status.*;

import edu.iris.Fissures.IfEvent.EventAccessOperations;
import edu.sc.seis.fissuresUtil.exceptionHandler.GlobalExceptionHandler;
import edu.sc.seis.fissuresUtil.map.colorizer.event.DefaultEventColorizer;
import edu.sc.seis.sod.ConfigurationException;
import edu.sc.seis.sod.EventChannelPair;
import edu.sc.seis.sod.Start;
import edu.sc.seis.sod.status.waveformArm.WaveformArmMonitor;
import java.io.IOException;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import org.w3c.dom.Element;

public class WaveformEventTemplate extends FileWritingTemplate implements WaveformArmMonitor{
    public WaveformEventTemplate(Element el, EventAccessOperations event) throws Exception {
        this(TemplateFileLoader.getTemplate(el),
             el.getAttribute("fileDir"),
             el.getAttribute("outputLocation"),
             event);
    }

    public WaveformEventTemplate(Element el, String baseDir, String outputLocation, EventAccessOperations event) throws IOException, ConfigurationException {
        super(baseDir, outputLocation);
        this.event = event;
        parse(el);
        if(Start.getWaveformArm() != null) {
            Start.getWaveformArm().addStatusMonitor(this);
        }
        write();
    }

    public void update(EventChannelPair ecp){
        if(ecp.getEvent().equals(event)){
            if(map != null){
                map.add(ecp.getEvent(), getOutputDirectory() + "/map.png");
            }
            write();
        }
    }

    protected Object getTemplate(String tag, Element el) throws ConfigurationException {
        if(tag.equals("stations")) {
            return new EventStationGroupTemplate(el, event);
        }else if(tag.equals("map")){
            try {
                synchronized(getClass()){
                    if(map == null){ map = new MapWaveformStatus(pool); }
                }
                map.add(event, getOutputDirectory() + "/map.png");
                return new GenericTemplate(){
                    public String getResult() { return "map.png";}
                };
            } catch (SQLException e) {
                GlobalExceptionHandler.handle("Trouble connecting to the event channel status db to create the waveform event channel status map",
                                              e);
                return new GenericTemplate(){
                    public String getResult() {
                        return "Trouble connecting to the event channel status db to create the map";
                    }
                };
            }
        }else if(tag.equals("event")){  return new EventTemplate(el); }
        return super.getTemplate(tag, el);
    }

    private class EventTemplate implements GenericTemplate{
        public EventTemplate(Element el) throws ConfigurationException {
            formatter = new EventFormatter(el);
        }

        public String getResult() { return formatter.getResult(event); }

        private EventFormatter formatter;
    }

    private EventAccessOperations event;

    private static MapPool pool = new MapPool(2, new DefaultEventColorizer());

    private static MapWaveformStatus map;
}
