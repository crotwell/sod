/**
 * LocalSeismogramTemplateGenerator.java
 *
 * @author Created by Philip Oliver-Paull
 */

package edu.sc.seis.sod.process.waveFormArm;
import edu.iris.Fissures.IfEvent.EventAccessOperations;
import edu.iris.Fissures.IfNetwork.Channel;
import edu.iris.Fissures.IfNetwork.NetworkAccess;
import edu.iris.Fissures.IfSeismogramDC.LocalSeismogram;
import edu.iris.Fissures.IfSeismogramDC.RequestFilter;
import edu.sc.seis.sod.CookieJar;
import edu.sc.seis.sod.SodUtil;
import edu.sc.seis.sod.Start;
import edu.sc.seis.sod.process.waveFormArm.LocalSeismogramProcess;
import edu.sc.seis.sod.status.ChannelFormatter;
import edu.sc.seis.sod.status.EventFormatter;
import edu.sc.seis.sod.status.StationFormatter;
import edu.sc.seis.sod.status.TemplateFileLoader;
import edu.sc.seis.sod.status.waveFormArm.LocalSeismogramTemplate;
import java.util.HashMap;
import java.util.Map;
import org.apache.log4j.Logger;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

public class LocalSeismogramTemplateGenerator implements LocalSeismogramProcess{
    
    private SeismogramImageProcess seismoImageProcess;
    private Map templates = new HashMap();
    private String fileDir, fileName;
    private Element waveformSeismogramConfig;
    private EventFormatter eventFormatter;
    private StationFormatter stationFormatter;
    private Logger logger = Logger.getLogger(LocalSeismogramTemplateGenerator.class);
    
    public LocalSeismogramTemplateGenerator(Element el) throws Exception{
        NodeList nl = el.getChildNodes();
        for (int i = 0; i < nl.getLength(); i++) {
            Node n = nl.item(i);
            if (n.getNodeName().equals("fileDir")){
                fileDir = n.getFirstChild().getNodeValue();
            }
		}
		if (fileDir == null){
			fileDir = Start.getProperties().getProperty("sod.start.StatusBaseDirectory", "status");
		}
		for (int i = 0; i < nl.getLength(); i++) {
            Node n = nl.item(i);
            if (n.getNodeName().equals("seismogramConfig")){
                waveformSeismogramConfig = TemplateFileLoader.getTemplate((Element)n);
                
                Node tmpEl = SodUtil.getElement(waveformSeismogramConfig, "outputLocation");
                Node tmpEl2 = SodUtil.getElement((Element)tmpEl, "eventDir");
                eventFormatter = new EventFormatter((Element)tmpEl2);
                tmpEl2 = SodUtil.getElement((Element)tmpEl, "stationDir");
                stationFormatter = new StationFormatter((Element)tmpEl2);
                waveformSeismogramConfig.removeChild(tmpEl);
                
                tmpEl = SodUtil.getElement(waveformSeismogramConfig, "filename");
                if (tmpEl != null){
                    fileName = tmpEl.getFirstChild().getNodeValue();
                    waveformSeismogramConfig.removeChild(tmpEl);
                }
                
                tmpEl = SodUtil.getElement(waveformSeismogramConfig, "picName");
                if (tmpEl != null){
                    ChannelFormatter chanFormatter = new ChannelFormatter((Element)tmpEl);
                    seismoImageProcess = new SeismogramImageProcess(fileDir, eventFormatter,
                                                                    stationFormatter, chanFormatter);
                    waveformSeismogramConfig.removeChild(tmpEl);
                }
            }
        }
        if (fileDir == null || waveformSeismogramConfig == null || eventFormatter == null || stationFormatter == null){
            throw new IllegalArgumentException("The configuration element must contain a fileDir and a waveformSeismogramConfig");
        }
    }
    
    /**
     * Processes localSeismograms, possibly modifying them.
     *
     * @param event an <code>EventAccessOperations</code> value
     * @param network a <code>NetworkAccess</code> value
     * @param channel a <code>Channel</code> value
     * @param original a <code>RequestFilter[]</code> value
     * @param available a <code>RequestFilter[]</code> value
     * @param seismograms a <code>LocalSeismogram[]</code> value
     * @param cookies a <code>CookieJar</code> value
     * @exception Exception if an error occurs
     */
    public LocalSeismogram[] process(EventAccessOperations event, NetworkAccess network, Channel channel,
                                     RequestFilter[] original, RequestFilter[] available, LocalSeismogram[] seismograms,
                                     CookieJar cookies) throws Exception {
        logger.debug("process() called");
        
        if (seismoImageProcess != null){
            seismoImageProcess.process(event, network, channel, original, available, seismograms, cookies);
        }
        else {
            logger.debug("There was no picname in config.  I am not generating pictures.");
        }
        
        if (fileName != null){
            getTemplate(event, channel);
        }
        else {
            logger.debug("There was no fileName in config. I am not generating html pages.");
        }
        
        return seismograms;
    }
    
    public LocalSeismogramTemplate getTemplate(EventAccessOperations event, Channel chan) throws Exception{
        String eventStationString = eventFormatter.getResult(event)
            + stationFormatter.getResult(chan.my_site.my_station);
        LocalSeismogramTemplate template = (LocalSeismogramTemplate)templates.get(eventStationString);
        if (template == null){
            String outputLocation = eventFormatter.getResult(event) + '/'
                + stationFormatter.getResult(chan.my_site.my_station) + '/' + fileName;
            template = new LocalSeismogramTemplate(waveformSeismogramConfig, fileDir, outputLocation,
                                                     event, chan.my_site.my_station);
            templates.put(eventStationString, template);
        }
        template.update(chan);
        return template;
    }
    
}

