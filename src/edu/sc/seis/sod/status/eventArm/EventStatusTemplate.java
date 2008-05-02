package edu.sc.seis.sod.status.eventArm;

import edu.sc.seis.fissuresUtil.cache.CacheEvent;
import edu.sc.seis.sod.ConfigurationException;
import edu.sc.seis.sod.EventChannelPair;
import edu.sc.seis.sod.EventNetworkPair;
import edu.sc.seis.sod.EventStationPair;
import edu.sc.seis.sod.SodUtil;
import edu.sc.seis.sod.Start;
import edu.sc.seis.sod.Status;
import edu.sc.seis.sod.status.FileWritingTemplate;
import edu.sc.seis.sod.status.GenericTemplate;
import edu.sc.seis.sod.status.TemplateFileLoader;
import edu.sc.seis.sod.status.eventArm.EventMonitor;
import edu.sc.seis.sod.status.waveformArm.WaveformMonitor;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import javax.xml.parsers.ParserConfigurationException;
import org.apache.log4j.Logger;
import org.w3c.dom.DOMException;
import org.w3c.dom.Element;
import org.xml.sax.SAXException;

public class EventStatusTemplate extends FileWritingTemplate implements
        EventMonitor, WaveformMonitor {

    public EventStatusTemplate(Element el) throws IOException, SAXException,
            ParserConfigurationException, ConfigurationException {
        super(FileWritingTemplate.getBaseDirectoryName(),
              extractConstructorFilenameArg(el));
        Element config = TemplateFileLoader.getTemplate(el);
        config.removeChild(SodUtil.getElement(config, "filename"));
        parse(config);
    }

    private static String extractConstructorFilenameArg(Element el)
            throws IOException, SAXException, ParserConfigurationException,
            DOMException {
        Element templateConfig = TemplateFileLoader.getTemplate(el);
        Element fileNameElement = SodUtil.getElement(templateConfig, "filename");
        String filename = fileNameElement.getFirstChild().getNodeValue();
        return filename;
    }

    public void setArmStatus(String status) {
        this.armStatus = status;
        write();
    }

    public Object getTemplate(String tag, Element el)
            throws ConfigurationException {
        if(tag.equals("events")) {
            return new EventGroupTemplate(el);
        } else if(tag.equals("armStatus")) {
            return new ArmStatusFormatter();
        } else if(tag.equals("mapEventStatus")) { return new MapImgSrc(el); }
        return super.getTemplate(tag, el);
    }

    private class MapImgSrc implements GenericTemplate {

        public MapImgSrc(Element el) {
            String mapFileLoc = MapEventStatus.getLocation(el);
            synchronized(elementsToMaps) {
                if(elementsToMaps.containsKey(mapFileLoc)) {
                    map = (MapEventStatus)elementsToMaps.get(mapFileLoc);
                } else {
                    map = new MapEventStatus(el);
                    elementsToMaps.put(mapFileLoc, map);
                }
            }
        }

        public String getResult() {
            String myloc = getOutputDirectory().toString() + '/'
                    + getFilename();
            return SodUtil.getRelativePath(myloc, map.getLocation(), "/");
        }

        private MapEventStatus map;
    }

    private static Map elementsToMaps = new HashMap();

    public void update(EventNetworkPair ecp) {
        write();
    }

    public void update(EventStationPair ecp) {
        write();
    }

    public void update(EventChannelPair ecp) {
        write();
    }

    public void change(CacheEvent event, Status status) {
        if(!addedToWaveformArm && Start.getWaveformArm() != null) {
            Start.getWaveformArm().addStatusMonitor(this);
            addedToWaveformArm = true;
        }
        write();
    }

    private class ArmStatusFormatter implements GenericTemplate {

        public String getResult() {
            return armStatus;
        }
    }

    private boolean addedToWaveformArm = false;

    private Logger logger = Logger.getLogger(EventStatusTemplate.class);

    private String armStatus = "";
}