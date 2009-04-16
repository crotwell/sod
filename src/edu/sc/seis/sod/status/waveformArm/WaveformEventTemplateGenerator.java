package edu.sc.seis.sod.status.waveformArm;

import java.io.IOException;

import javax.xml.parsers.ParserConfigurationException;

import org.apache.log4j.Logger;
import org.w3c.dom.Element;
import org.xml.sax.SAXException;

import edu.sc.seis.fissuresUtil.cache.CacheEvent;
import edu.sc.seis.sod.ConfigurationException;
import edu.sc.seis.sod.EventChannelPair;
import edu.sc.seis.sod.EventNetworkPair;
import edu.sc.seis.sod.EventStationPair;
import edu.sc.seis.sod.EventVectorPair;
import edu.sc.seis.sod.SodUtil;
import edu.sc.seis.sod.Start;
import edu.sc.seis.sod.Status;
import edu.sc.seis.sod.status.EventFormatter;
import edu.sc.seis.sod.status.FileWritingTemplate;
import edu.sc.seis.sod.status.TemplateFileLoader;
import edu.sc.seis.sod.status.eventArm.EventMonitor;

public class WaveformEventTemplateGenerator implements EventMonitor,
        WaveformMonitor {

    public WaveformEventTemplateGenerator(Element el) throws IOException,
            SAXException, ParserConfigurationException, ConfigurationException {
        config = TemplateFileLoader.getTemplate(el);
        Element tmp = SodUtil.getElement(config, "outputLocation");
        formatter = new EventFormatter(tmp);
        config.removeChild(tmp);
        tmp = SodUtil.getElement(config, "filename");
        filename = tmp.getFirstChild().getNodeValue();
        config.removeChild(tmp);
        fileDir = FileWritingTemplate.getBaseDirectoryName();
        if(formatter == null || config == null) { throw new IllegalArgumentException("The configuration element must contain a fileDir and a waveformConfig"); }
        template = new WaveformEventTemplate(config,
                                             fileDir,
                                             formatter,
                                             filename);
        if(Start.getEventArm() != null) {
            Start.getEventArm().add(this);
        }
    }

    public void change(CacheEvent event, Status status) {
        if(!added) {
            if(Start.getWaveformRecipe() != null) {
                Start.getWaveformRecipe().addStatusMonitor(template);
                added = true;
            }
        }
        template.update(event, status);
    }

    public void setArmStatus(String status) {}

    public void update(EventNetworkPair ecp) {
        template.update(ecp);
    }

    public void update(EventStationPair ecp) {
        template.update(ecp);
    }
    
    public void update(EventChannelPair ecp) {
        template.update(ecp);
    }
    
    public void update(EventVectorPair ecp) {
        template.update(ecp);
    }

    private boolean added = false;

    private WaveformEventTemplate template;

    private Element config;

    private EventFormatter formatter;

    private String fileDir, filename;

    private static Logger logger = Logger.getLogger(WaveformEventTemplateGenerator.class);
}