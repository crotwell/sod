package edu.sc.seis.sod.process.waveform;

import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import edu.iris.Fissures.IfEvent.EventAccessOperations;
import edu.iris.Fissures.IfNetwork.Channel;
import edu.sc.seis.sod.ConfigurationException;
import edu.sc.seis.sod.SodUtil;
import edu.sc.seis.sod.Start;
import edu.sc.seis.sod.status.ChannelFormatter;
import edu.sc.seis.sod.status.EventFormatter;
import edu.sc.seis.sod.status.FileWritingTemplate;
import edu.sc.seis.sod.status.FissuresFormatter;
import edu.sc.seis.sod.status.StationFormatter;
import edu.sc.seis.sod.status.TemplateFileLoader;

/**
 * @author groves Created on Jan 10, 2005
 */
public class SeismogramImageOutputLocator {

    public SeismogramImageOutputLocator(String fileDir,
            EventFormatter eventDirFormatter,
            StationFormatter stationDirFormatter,
            ChannelFormatter imageNameFormatter, String prefix) {
        this.fileDir = fileDir;
        eventFormatter = eventDirFormatter;
        stationFormatter = stationDirFormatter;
        chanFormatter = imageNameFormatter;
    }

    public SeismogramImageOutputLocator(Element el) throws Exception {
        this(el, true);
    }

    public SeismogramImageOutputLocator(Element el, boolean useBase)
            throws Exception {
        NodeList nl = el.getChildNodes();
        for(int i = 0; i < nl.getLength(); i++) {
            Node n = nl.item(i);
            if(n.getNodeName().equals("fileDir")) {
                NodeList fileNodes = ((Element)n).getChildNodes();
                for(int j = 0; j < fileNodes.getLength(); j++) {
                    if(fileNodes.item(j).getNodeName().equals("statusBase")) {
                        if(useBase) {
                            fileDir += Start.getRunProps().getStatusBaseDir()
                                    + "/";
                        }
                    } else {
                        fileDir += fileNodes.item(j).getNodeValue();
                    }
                }
            } else if(n.getNodeName().equals("seismogramConfig")) {
                Element config = TemplateFileLoader.getTemplate((Element)n);
                parseOutputLocationCreators(SodUtil.getElement(config,
                                                               "outputLocation"));
            } else if(n.getNodeName().equals("outputLocationCreators")) {
                parseOutputLocationCreators((Element)n);
            } else if(n.getNodeName().equals("prefix")) {
                prefix = SodUtil.getNestedText((Element)n);
            } else if(n.getNodeName().equals("fileType")) {
                configuredFileType = SodUtil.getNestedText((Element)n);
            }
        }
        if(fileDir == null) {
            fileDir = FileWritingTemplate.getBaseDirectoryName();
        }
    }

    public String getFileType() {
        return configuredFileType;
    }

    public String getPrefix() {
        return prefix;
    }

    private void parseOutputLocationCreators(Element parent)
            throws ConfigurationException {
        Element eventDir = SodUtil.getElement(parent, "eventDir");
        eventFormatter = new EventFormatter(eventDir);
        Element stationDir = SodUtil.getElement(parent, "stationDir");
        stationFormatter = new StationFormatter(stationDir);
        Element picName = SodUtil.getElement(parent, "picName");
        chanFormatter = new ChannelFormatter(picName, true);
    }

    public String getLocation(EventAccessOperations event, Channel channel) {
        return getLocation(event, channel, configuredFileType);
    }

    public String getLocation(EventAccessOperations event,
                              Channel channel,
                              String fileType) {
        return FissuresFormatter.filize(fileDir + '/'
                + eventFormatter.getResult(event) + '/'
                + stationFormatter.getResult(channel.my_site.my_station) + '/'
                + prefix + chanFormatter.getResult(channel) + "." + fileType);
    }

    private String fileDir = "", prefix = "",
            configuredFileType = SeismogramImageProcess.PNG;

    private EventFormatter eventFormatter;

    private StationFormatter stationFormatter;

    private ChannelFormatter chanFormatter;
}