package edu.sc.seis.sod.status.waveformArm;

import edu.iris.Fissures.IfEvent.EventAccessOperations;
import edu.iris.Fissures.IfEvent.NoPreferredOrigin;
import edu.sc.seis.fissuresUtil.exceptionHandler.GlobalExceptionHandler;
import edu.sc.seis.sod.ConfigurationException;
import edu.sc.seis.sod.EventChannelPair;
import edu.sc.seis.sod.SodUtil;
import edu.sc.seis.sod.Start;
import edu.sc.seis.sod.Status;
import edu.sc.seis.sod.status.EventFormatter;
import edu.sc.seis.sod.status.FileWritingTemplate;
import edu.sc.seis.sod.status.TemplateFileLoader;
import edu.sc.seis.sod.status.eventArm.EventArmMonitor;
import edu.sc.seis.sod.status.waveformArm.WaveformArmMonitor;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import javax.xml.parsers.ParserConfigurationException;
import org.apache.log4j.Logger;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

public class WaveformEventTemplateGenerator implements EventArmMonitor, WaveformArmMonitor {
    public WaveformEventTemplateGenerator(Element el) throws IOException, SAXException, ParserConfigurationException, ConfigurationException {
        if(Start.getEventArm() != null) Start.getEventArm().add(this);
        NodeList nl = el.getChildNodes();
        for (int i = 0; i < nl.getLength(); i++) {
            Node n = nl.item(i);
            if (n.getNodeName().equals("fileDir")){
                fileDir = n.getFirstChild().getNodeValue();
            }
            else if(n.getNodeName().equals("waveformConfig")){
                config = TemplateFileLoader.getTemplate((Element)n);

                Node tmp = SodUtil.getElement(config, "outputLocation");
                formatter = new EventFormatter((Element)tmp);
                config.removeChild(tmp);

                tmp = SodUtil.getElement(config, "filename");
                filename = tmp.getFirstChild().getNodeValue();
                config.removeChild(tmp);
            }
        }
        if (fileDir == null){
            fileDir = FileWritingTemplate.getBaseDirectoryName();
        }
        if(formatter == null  || config == null) {
            throw new IllegalArgumentException("The configuration element must contain a fileDir and a waveformConfig");
        }
    }

    public void change(EventAccessOperations event, Status status) {
        try {
        getTemplate(event);
        } catch (IOException e) {
            String msg = "Got an IOException changing event status: ";
            try {
                msg += event.get_preferred_origin().origin_time.date_time;
            } catch (NoPreferredOrigin ee) {
                msg += " NoPreferredOrigin ";
            }
            msg+=" status="+status.toString();
            GlobalExceptionHandler.handle(msg, e);
        } catch (ConfigurationException e) {
            String msg = "Got an ConfigurationException changing event status: ";
            try {
                msg += event.get_preferred_origin().origin_time.date_time;
            } catch (NoPreferredOrigin ee) {
                msg += " NoPreferredOrigin ";
            }
            msg+=" status="+status.toString();
            GlobalExceptionHandler.handle(msg, e);
        }
    }

    public WaveformEventTemplate getTemplate(EventAccessOperations ev) throws IOException, ConfigurationException {
        if(!eventTemplates.containsKey(ev)){
            eventTemplates.put(ev, new WaveformEventTemplate(config, fileDir, formatter.getResult(ev) + '/' + filename, ev));
        }
        return (WaveformEventTemplate)eventTemplates.get(ev);
    }

    public boolean contains(EventAccessOperations ev){
        return eventTemplates.containsKey(ev);
    }

    public void setArmStatus(String status) {}

    public void update(EventChannelPair ecp) {
        // do nothing, just be a WaveformArmMonitor to be loaded in the WaveformArm
    }



    private Element config;

    private EventFormatter formatter;

    private String fileDir, filename;

    private Map eventTemplates = new HashMap();

    private static Logger logger = Logger.getLogger(WaveformEventTemplateGenerator.class);
}
