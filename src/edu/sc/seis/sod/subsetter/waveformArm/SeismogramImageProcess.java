/**
 * SeismogramImageProcess.java
 *
 * @author Created by Philip Oliver-Paull
 */

package edu.sc.seis.sod.subsetter.waveFormArm;

import edu.iris.Fissures.IfEvent.EventAccessOperations;
import edu.iris.Fissures.IfNetwork.Channel;
import edu.iris.Fissures.IfNetwork.NetworkAccess;
import edu.iris.Fissures.IfSeismogramDC.LocalSeismogram;
import edu.iris.Fissures.IfSeismogramDC.RequestFilter;
import edu.iris.Fissures.seismogramDC.LocalSeismogramImpl;
import edu.sc.seis.fissuresUtil.display.BasicSeismogramDisplay;
import edu.sc.seis.fissuresUtil.display.DisplayUtils;
import edu.sc.seis.fissuresUtil.xml.DataSet;
import edu.sc.seis.fissuresUtil.xml.MemoryDataSetSeismogram;
import edu.sc.seis.sod.CommonAccess;
import edu.sc.seis.sod.CookieJar;
import edu.sc.seis.sod.LocalSeismogramProcess;
import edu.sc.seis.sod.SodUtil;
import edu.sc.seis.sod.subsetter.ChannelFormatter;
import edu.sc.seis.sod.subsetter.EventFormatter;
import edu.sc.seis.sod.subsetter.StationFormatter;
import edu.sc.seis.sod.subsetter.TemplateFileLoader;
import java.io.IOException;
import java.util.Timer;
import java.util.TimerTask;
import org.apache.log4j.Logger;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

public class SeismogramImageProcess implements LocalSeismogramProcess {

    private Logger logger = Logger.getLogger(SeismogramImageProcess.class);
    private String fileDir;
    private EventFormatter eventFormatter;
    private StationFormatter stationFormatter;
    private ChannelFormatter chanFormatter;
    private Timer t = new Timer();

    public SeismogramImageProcess(String fileDir, EventFormatter eventDirFormatter,
                                  StationFormatter stationDirFormatter,
                                  ChannelFormatter imageNameFormatter){
        this.fileDir = fileDir;
        eventFormatter = eventDirFormatter;
        stationFormatter = stationDirFormatter;
        chanFormatter = imageNameFormatter;
    }

    public SeismogramImageProcess(Element el) throws Exception{
        NodeList nl = el.getChildNodes();
        for (int i = 0; i < nl.getLength(); i++) {
            Node n = nl.item(i);
            if (n.getNodeName().equals("fileDir")){
                fileDir = n.getFirstChild().getNodeValue();
            }
            else if (n.getNodeName().equals("seismogramConfig")){
                Element seismogramImageConfig = TemplateFileLoader.getTemplate((Element)n);

                Node tmpEl = SodUtil.getElement(seismogramImageConfig, "outputLocation");
                Node tmpEl2 = SodUtil.getElement((Element)tmpEl, "eventDir");
                eventFormatter = new EventFormatter((Element)tmpEl2);
                tmpEl2 = SodUtil.getElement((Element)tmpEl, "stationDir");
                stationFormatter = new StationFormatter((Element)tmpEl2);
                seismogramImageConfig.removeChild(tmpEl);

                tmpEl = SodUtil.getElement(seismogramImageConfig, "picName");
                chanFormatter = new ChannelFormatter((Element)tmpEl);
                seismogramImageConfig.removeChild(tmpEl);
            }
        }
        if (fileDir == null || eventFormatter == null || stationFormatter == null || chanFormatter == null){
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

        BasicSeismogramDisplay bsd = new BasicSeismogramDisplay(null);
        bsd.PRINTING = true;

        MemoryDataSetSeismogram memDSS = new MemoryDataSetSeismogram(original[0], "");
        memDSS.setBeginTime(DisplayUtils.firstBeginDate(original).getFissuresTime());
        memDSS.setEndTime(DisplayUtils.lastEndDate(original).getFissuresTime());
        for (int i = 0; i < seismograms.length; i++) {
            memDSS.add((LocalSeismogramImpl)seismograms[i]);
        }
        bsd.add(new MemoryDataSetSeismogram[]{memDSS});

        String picFileName = fileDir + '/'
            + eventFormatter.getResult(event) + '/'
            + stationFormatter.getResult(channel.my_site.my_station) + '/'
            + chanFormatter.getResult(channel);
        logger.debug("writing " + picFileName);
        bsd.outputToPNG(picFileName);

        return seismograms;
    }
}


