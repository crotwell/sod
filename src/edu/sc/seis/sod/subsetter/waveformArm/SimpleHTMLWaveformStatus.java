/**
 * SimpleHTMLWaveformStatus.java
 *
 * @author Created by Charles Groves
 */

package edu.sc.seis.sod.subsetter.waveFormArm;






import edu.iris.Fissures.IfEvent.EventAccessOperations;
import edu.sc.seis.fissuresUtil.cache.CacheEvent;
import edu.sc.seis.sod.ConfigurationException;
import edu.sc.seis.sod.EventChannelPair;
import edu.sc.seis.sod.SodUtil;
import edu.sc.seis.sod.WaveFormStatus;
import edu.sc.seis.sod.subsetter.LinkSection;
import edu.sc.seis.sod.subsetter.NameGenerator;
import edu.sc.seis.sod.subsetter.SimpleHTMLPage;
import java.io.File;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.HashMap;
import java.util.Map;
import org.w3c.dom.Element;

public class SimpleHTMLWaveformStatus extends SimpleHTMLPage implements WaveFormStatus{
    public SimpleHTMLWaveformStatus(Element config) throws ConfigurationException{
        super("Waveform Arm Status", new File(SodUtil.makeOutputDirectory(config),
                                              "./waveformstatus.html"), true);
        eventSection = new LinkSection(this, "Events in waveform arm");
        sections.add(eventSection);
        write();
        if(config != null){
            nameGen = new NameGenerator(SodUtil.getElement(config, "eventDirLabel"));
        }else{
            nameGen = new NameGenerator();
        }
    }
    
    public void update(EventChannelPair ecp){
        EventChannelPage page = null;
        if(eventsToPages.containsKey(ecp.getEvent())){
            page = (EventChannelPage)eventsToPages.get(ecp.getEvent());
        }else{
            String title = formatEvent(ecp.getEvent());
            File eventFile = new File(getDirectory(), fileizeEvent(ecp.getEvent()));
            page = new EventChannelPage(title, eventFile, ecp.getEvent());
            eventsToPages.put(ecp.getEvent(), page);
            eventSection.add(page);
            write();
        }
        page.add(ecp.getChannel());
    }
    
    public String fileizeEvent(EventAccessOperations event){
        return nameGen.getFilizedName(event) + "/event.html";
    }
    
    public String formatEvent(EventAccessOperations event){
        return CacheEvent.getEventInfo(event,eventOutputFormat);
    }
    
    private Map eventsToPages =  new HashMap();
    
    private LinkSection eventSection;
    
    private NameGenerator nameGen;
    
    private String eventOutputFormat = CacheEvent.LOC + " | " + CacheEvent.TIME + " | Mag: " + CacheEvent.MAG + " | Depth: " + CacheEvent.DEPTH + " " + CacheEvent.DEPTH_UNIT;
}
