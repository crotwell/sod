/**
 * SeismogramImageProcess.java
 *
 * @author Created by Philip Oliver-Paull
 */

package edu.sc.seis.sod.process.waveformArm;

import edu.sc.seis.sod.status.*;

import edu.iris.Fissures.AuditInfo;
import edu.iris.Fissures.IfEvent.EventAccessOperations;
import edu.iris.Fissures.IfEvent.Origin;
import edu.iris.Fissures.IfNetwork.Channel;
import edu.iris.Fissures.IfSeismogramDC.RequestFilter;
import edu.iris.Fissures.model.MicroSecondDate;
import edu.iris.Fissures.model.TimeInterval;
import edu.iris.Fissures.model.UnitImpl;
import edu.iris.Fissures.network.ChannelIdUtil;
import edu.iris.Fissures.seismogramDC.LocalSeismogramImpl;
import edu.sc.seis.TauP.Arrival;
import edu.sc.seis.TauP.TauModelException;
import edu.sc.seis.TauP.TauP_Time;
import edu.sc.seis.fissuresUtil.bag.TauPUtil;
import edu.sc.seis.fissuresUtil.cache.CacheEvent;
import edu.sc.seis.fissuresUtil.display.BasicSeismogramDisplay;
import edu.sc.seis.fissuresUtil.display.DisplayUtils;
import edu.sc.seis.fissuresUtil.display.drawable.Flag;
import edu.sc.seis.fissuresUtil.display.registrar.PhaseAlignedTimeConfig;
import edu.sc.seis.fissuresUtil.exceptionHandler.GlobalExceptionHandler;
import edu.sc.seis.fissuresUtil.xml.DataSet;
import edu.sc.seis.fissuresUtil.xml.MemoryDataSet;
import edu.sc.seis.fissuresUtil.xml.MemoryDataSetSeismogram;
import edu.sc.seis.sod.CookieJar;
import edu.sc.seis.sod.SodUtil;
import edu.sc.seis.sod.process.waveformArm.LocalSeismogramProcess;
import java.awt.Dimension;
import java.io.File;
import javax.swing.SwingUtilities;
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
    private TauPUtil tauP;

    public SeismogramImageProcess(String fileDir, EventFormatter eventDirFormatter,
                                  StationFormatter stationDirFormatter,
                                  ChannelFormatter imageNameFormatter) throws Exception{
        this.fileDir = fileDir;
        eventFormatter = eventDirFormatter;
        stationFormatter = stationDirFormatter;
        chanFormatter = imageNameFormatter;
        initTaup();
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
            } else if(n.getNodeName().equals("modelName")) {
                modelName = SodUtil.getNestedText((Element)n);
            } else if(n.getNodeName().equals("prefix")) {
                prefix = SodUtil.getNestedText((Element)n);
            } else if(n.getNodeName().equals("fileType")) {
                fileType = SodUtil.getNestedText((Element)n);
            }
        }
        if (fileDir == null){
            fileDir = FileWritingTemplate.getBaseDirectoryName();
        }
        if (fileDir == null || eventFormatter == null || stationFormatter == null || chanFormatter == null){
            throw new IllegalArgumentException("The configuration element must contain a fileDir and a waveformSeismogramConfig");
        }
        initTaup();
    }

    private void initTaup() throws TauModelException{
        tauP = new TauPUtil(modelName);
        if (tauptime == null) {
            tauptime = new TauP_Time("iasp91");
            tauptime.clearPhaseNames();
            tauptime.appendPhaseName("P");
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
    public LocalSeismogramResult process(EventAccessOperations event,
                                         Channel channel,
                                         RequestFilter[] original,
                                         RequestFilter[] available,
                                         LocalSeismogramImpl[] seismograms, CookieJar cookieJar
                                        ) throws Exception {
        return process(event, channel, original, seismograms, fileType);
    }

    /** allows specifying a fileType, png or pdf. */
    public LocalSeismogramResult process(EventAccessOperations event,
                                         Channel channel,
                                         RequestFilter[] original,
                                         LocalSeismogramImpl[] seismograms,
                                         final String fileType
                                        ) throws Exception {
        return process(event, channel, original, seismograms, fileType, phases);
    }

    /** allows specifying a fileType, png or pdf, and a list of phases.*/
    public LocalSeismogramResult process(EventAccessOperations event,
                                         Channel channel,
                                         RequestFilter[] original,
                                         LocalSeismogramImpl[] seismograms,
                                         final String fileType,
                                         String[] phases
                                        ) throws Exception {
        return process(event, channel, original, seismograms, fileType, phases, relativeTime);
    }


    /** allows specifying a fileType, png or pdf, and a list of phases.*/
    public LocalSeismogramResult process(EventAccessOperations event,
                                         Channel channel,
                                         RequestFilter[] original,
                                         LocalSeismogramImpl[] seismograms,
                                         final String fileType,
                                         String[] phases,
                                         boolean relTime
                                        ) throws Exception {
        logger.debug("process() called");

        // only needed if relTime
        PhaseAlignedTimeConfig phaseTime = null;
        if (relTime) {
            phaseTime = new PhaseAlignedTimeConfig();
            phaseTime.setTauP(tauptime);
        }
        final BasicSeismogramDisplay bsd = relTime ? new BasicSeismogramDisplay(phaseTime) : new BasicSeismogramDisplay();

        MemoryDataSetSeismogram memDSS = new MemoryDataSetSeismogram(original[0], "");
        memDSS.setBeginTime(DisplayUtils.firstBeginDate(original).getFissuresTime());
        memDSS.setEndTime(DisplayUtils.lastEndDate(original).getFissuresTime());
        for (int i = 0; i < seismograms.length; i++) {
            memDSS.add(seismograms[i]);
        }
        DataSet dataset = new MemoryDataSet("temp", "Temp Dataset for "+memDSS.getName(), "temp", new AuditInfo[0]);
        dataset.addDataSetSeismogram(memDSS, new AuditInfo[0]);
        dataset.addParameter(dataset.EVENT, event, new AuditInfo[0]);
        bsd.add(new MemoryDataSetSeismogram[]{memDSS});

        Origin origin = CacheEvent.extractOrigin(event);
        MicroSecondDate originTime = new MicroSecondDate(origin.origin_time);
        Arrival[] arrivals =
            tauP.calcTravelTimes(channel.my_site.my_station, origin, phases);
        for (int i = 0; i < arrivals.length; i++) {
            MicroSecondDate flagTime = originTime.add(new TimeInterval(arrivals[i].getTime(), UnitImpl.SECOND));
            bsd.add(new Flag(flagTime, arrivals[i].getName()));
        }

        final String picFileName = FissuresFormatter.filize(fileDir + '/'
                                                                + eventFormatter.getResult(event) + '/'
                                                                + stationFormatter.getResult(channel.my_site.my_station) + '/'
                                                                + prefix
                                                                + chanFormatter.getResult(channel)
                                                                +"."+fileType);
        SwingUtilities.invokeAndWait(new Runnable(){
                    public void run(){
                        logger.debug("writing " + picFileName);
                        try {
                            if (fileType.equals(PDF)) {
                                logger.debug("NOAMP before pdf"+picFileName);
                                bsd.outputToPDF(new File(picFileName));
                                logger.debug("NOAMP after pdf"+picFileName);
                            } else {
                                bsd.outputToPNG(new File(picFileName), dimension);
                            }
                        } catch (Throwable e) {
                            GlobalExceptionHandler.handle("unable to save map to "+ picFileName, e);
                        }
                    }
                });


        return new LocalSeismogramResult(true, seismograms, new StringTreeLeaf(this, true));
    }

    private static TauP_Time tauptime = null;
    private static Dimension dimension = new Dimension(500, 200);
    private static String[] phases = {"P", "S"};
    private String modelName = "iasp91";
    private String prefix = "";
    private String fileType = PNG;
    private boolean relativeTime = false;
    public static final String PDF = "pdf";
    public static final String PNG = "png";
}


