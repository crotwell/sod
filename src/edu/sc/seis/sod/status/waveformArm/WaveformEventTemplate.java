package edu.sc.seis.sod.status.waveformArm;

import edu.sc.seis.sod.status.*;

import edu.iris.Fissures.IfEvent.EventAccessOperations;
import edu.sc.seis.fissuresUtil.exceptionHandler.GlobalExceptionHandler;
import edu.sc.seis.sod.ConfigurationException;
import edu.sc.seis.sod.EventChannelPair;
import edu.sc.seis.sod.Stage;
import edu.sc.seis.sod.Standing;
import edu.sc.seis.sod.Status;
import edu.sc.seis.sod.status.eventArm.EventTemplate;
import edu.sc.seis.sod.status.waveformArm.WaveformArmMonitor;
import java.io.IOException;
import java.sql.SQLException;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import org.w3c.dom.Element;

public class WaveformEventTemplate extends Template implements WaveformArmMonitor{
    public WaveformEventTemplate(Element el, String baseDir,
                                 EventFormatter dirNameCreator, String pageName) throws IOException, ConfigurationException {
        this.baseDir = baseDir;
        this.dirNameCreator = dirNameCreator;
        this.pageName = pageName;
        parse(el);
    }

    public String getOutputDirectory(EventAccessOperations ev){
        return baseDir + "/" + dirNameCreator.getResult(ev) + "/";
    }

    protected Object textTemplate(final String text) {
        return new GenericTemplate() {
            public String getResult() { return text; }
        };
    }


    public void update(EventAccessOperations ev, Status status){
        String outputDir = getOutputDirectory(ev);
        String loc = outputDir + pageName;
        if(status.equals(Status.get(Stage.EVENT_ORIGIN_SUBSETTER,
                                    Standing.IN_PROG))){
            try{
                FileWritingTemplate.testOutputLoc(loc);
            } catch (IOException e) {
                GlobalExceptionHandler.handle(e);
            }
        }
        update(ev, outputDir, pageName);
    }

    public void update(EventChannelPair ecp){
        String outputDir = getOutputDirectory(ecp.getEvent());
        update(ecp.getEvent(), outputDir, pageName);
    }


    public void update(EventAccessOperations ev, String outputDir, String fileLoc){
        if(map != null){ map.add(ev, outputDir + "map.png"); }
        String loc = outputDir + fileLoc;
        synchronized(toBeRendered){
            if (!toBeRendered.containsKey(loc)){
                toBeRendered.put(loc, ev);
                OutputScheduler.getDefault().schedule(writer);
            }
        }
    }

    public class Writer implements Runnable{
        public void run() {
            EventAccessOperations[] evs= new EventAccessOperations[0];
            String[] fileLocs = new String[0];
            synchronized(toBeRendered){
                int numEvsWaiting = toBeRendered.size();
                if(toBeRendered.size() > 0){
                    evs = new EventAccessOperations[toBeRendered.size()];
                    fileLocs = new String[toBeRendered.size()];
                    Iterator it = toBeRendered.keySet().iterator();
                    while(it.hasNext()){
                        String loc= (String)it.next();
                        fileLocs[--numEvsWaiting] = loc;
                        evs[numEvsWaiting] = (EventAccessOperations)toBeRendered.get(loc);
                    }
                    toBeRendered.clear();
                }
            }
            for (int i = 0; i < evs.length; i++) {
                FileWritingTemplate.write(fileLocs[i], getResult(evs[i]));
            }
        }

        private String getResult(EventAccessOperations ev) {
            StringBuffer buf = new StringBuffer();
            Iterator it = templates.iterator();
            while(it.hasNext()){
                Object cur = it.next();
                if(cur instanceof EventTemplate){
                    buf.append(((EventTemplate)cur).getResult(ev));
                }else if(cur instanceof GenericTemplate){
                    buf.append(((GenericTemplate)cur).getResult());
                }
            }
            return buf.toString();
        }
    }

    protected Object getTemplate(String tag, Element el) throws ConfigurationException {
        if(tag.equals("eventStations")) {
            return new EventStationGroupTemplate(el);
        }else if(tag.equals("map")){
            try {
                if(map == null){ map = new MapWaveformStatus(); }
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
        }else if(tag.equals("event")){  return new EventFormatter(el); }
        else if(tag.equals("menu")){
            try {
                return new MenuTemplate(TemplateFileLoader.getTemplate(el), baseDir + "/1/2/test.html", baseDir);
            } catch (Exception e) {
                GlobalExceptionHandler.handle("Problem getting template for Menu", e);
            }
        }
        return super.getTemplate(tag, el);
    }

    private Map toBeRendered = Collections.synchronizedMap(new HashMap());
    private EventFormatter dirNameCreator;
    private String pageName;
    private String baseDir;
    private MapWaveformStatus map;
    private Writer writer = new Writer();
}
