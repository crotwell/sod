/**
 * SeismogramImageProcess.java
 * 
 * @author Created by Philip Oliver-Paull
 */
package edu.sc.seis.sod.process.waveform;

import java.awt.Dimension;
import java.io.File;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;
import javax.swing.SwingUtilities;
import org.apache.log4j.Logger;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import edu.iris.Fissures.AuditInfo;
import edu.iris.Fissures.IfEvent.EventAccessOperations;
import edu.iris.Fissures.IfEvent.Origin;
import edu.iris.Fissures.IfNetwork.Channel;
import edu.iris.Fissures.IfSeismogramDC.RequestFilter;
import edu.iris.Fissures.model.MicroSecondDate;
import edu.iris.Fissures.model.TimeInterval;
import edu.iris.Fissures.model.UnitImpl;
import edu.iris.Fissures.seismogramDC.LocalSeismogramImpl;
import edu.sc.seis.TauP.Arrival;
import edu.sc.seis.TauP.TauModelException;
import edu.sc.seis.TauP.TauP_Time;
import edu.sc.seis.fissuresUtil.bag.TauPUtil;
import edu.sc.seis.fissuresUtil.cache.EventUtil;
import edu.sc.seis.fissuresUtil.display.BasicSeismogramDisplay;
import edu.sc.seis.fissuresUtil.display.DisplayUtils;
import edu.sc.seis.fissuresUtil.display.PhasePhilter;
import edu.sc.seis.fissuresUtil.display.registrar.PhaseAlignedTimeConfig;
import edu.sc.seis.fissuresUtil.exceptionHandler.GlobalExceptionHandler;
import edu.sc.seis.fissuresUtil.xml.DataSet;
import edu.sc.seis.fissuresUtil.xml.MemoryDataSet;
import edu.sc.seis.fissuresUtil.xml.MemoryDataSetSeismogram;
import edu.sc.seis.sod.CookieJar;
import edu.sc.seis.sod.FlagData;
import edu.sc.seis.sod.SodFlag;
import edu.sc.seis.sod.SodUtil;
import edu.sc.seis.sod.status.StringTreeLeaf;
import edu.sc.seis.sod.subsetter.requestGenerator.PhaseRequest;

public class SeismogramImageProcess implements WaveformProcess {

    public SeismogramImageProcess(SeismogramImageOutputLocator locator)
            throws TauModelException {
        this.locator = locator;
        initTaup();
        putDataInCookieJar = true;
    }

    public SeismogramImageProcess(Element el) throws Exception {
        NodeList nl = el.getChildNodes();
        for(int i = 0; i < nl.getLength(); i++) {
            Node n = nl.item(i);
            if(n.getNodeName().equals("phaseWindow")) {
                phaseWindow = new PhaseWindow((Element)n);
            } else if(n.getNodeName().equals("modelName")) {
                modelName = SodUtil.getNestedText((Element)n);
            } else if(n.getNodeName().equals("putDataInCookieJar")) {
                putDataInCookieJar = true;
            } else if(n.getNodeName().equals("dimension")) {
                dims = SodUtil.loadDimensions((Element)n);
            }
        }
        locator = new SeismogramImageOutputLocator(el);
        initTaup();
    }

    private void initTaup() throws TauModelException {
        tauP = new TauPUtil(modelName);
        if(tauptime == null) {
            tauptime = new TauP_Time("iasp91");
            tauptime.clearPhaseNames();
            tauptime.appendPhaseName("P");
        }
    }

    public WaveformResult process(EventAccessOperations event,
                                  Channel channel,
                                  RequestFilter[] original,
                                  RequestFilter[] available,
                                  LocalSeismogramImpl[] seismograms,
                                  CookieJar cookieJar) throws Exception {
        return process(event,
                       channel,
                       original,
                       seismograms,
                       locator.getFileType(),
                       cookieJar);
    }

    /** allows specifying a fileType, png or pdf. */
    public WaveformResult process(EventAccessOperations event,
                                  Channel channel,
                                  RequestFilter[] original,
                                  LocalSeismogramImpl[] seismograms,
                                  final String fileType,
                                  CookieJar cookieJar) throws Exception {
        return process(event,
                       channel,
                       original,
                       seismograms,
                       fileType,
                       DEFAULT_PHASES,
                       cookieJar);
    }

    /** allows specifying a fileType, png or pdf, and a list of phases. */
    public WaveformResult process(EventAccessOperations event,
                                  Channel channel,
                                  RequestFilter[] original,
                                  LocalSeismogramImpl[] seismograms,
                                  final String fileType,
                                  String[] phases,
                                  CookieJar cookieJar) throws Exception {
        return process(event,
                       channel,
                       original,
                       seismograms,
                       fileType,
                       phases,
                       relativeTime,
                       cookieJar);
    }

    /** allows specifying a fileType, png or pdf, and a list of phases. */
    public WaveformResult process(EventAccessOperations event,
                                  Channel channel,
                                  RequestFilter[] original,
                                  LocalSeismogramImpl[] seismograms,
                                  final String fileType,
                                  String[] phases,
                                  boolean relTime,
                                  final CookieJar cookieJar) throws Exception {
        logger.debug("process() called");
        // only needed if relTime
        PhaseAlignedTimeConfig phaseTime = null;
        if(relTime) {
            phaseTime = new PhaseAlignedTimeConfig();
            phaseTime.setTauP(tauptime);
        }
        final BasicSeismogramDisplay bsd = relTime ? new BasicSeismogramDisplay(phaseTime)
                : new BasicSeismogramDisplay();
        MemoryDataSetSeismogram memDSS = null;
        PhaseRequest phaseRequest = null;
        if(phaseWindow == null) {
            memDSS = new MemoryDataSetSeismogram(original[0], "");
            memDSS.setBeginTime(DisplayUtils.firstBeginDate(original)
                    .getFissuresTime());
            memDSS.setEndTime(DisplayUtils.lastEndDate(original)
                    .getFissuresTime());
        } else {
            phaseRequest = phaseWindow.getPhaseRequest();
            RequestFilter[] request = phaseRequest.generateRequest(event,
                                                                   channel,
                                                                   null);
            memDSS = new MemoryDataSetSeismogram(request[0], "");
            memDSS.setBeginTime(DisplayUtils.firstBeginDate(request)
                    .getFissuresTime());
            memDSS.setEndTime(DisplayUtils.lastEndDate(request)
                    .getFissuresTime());
        }
        for(int i = 0; i < seismograms.length; i++) {
            memDSS.add(seismograms[i]);
        }
        DataSet dataset = new MemoryDataSet("temp", "Temp Dataset for "
                + memDSS.getName(), "temp", new AuditInfo[0]);
        dataset.addDataSetSeismogram(memDSS, new AuditInfo[0]);
        dataset.addParameter(DataSet.EVENT, event, new AuditInfo[0]);
        bsd.add(new MemoryDataSetSeismogram[] {memDSS});
        Origin origin = EventUtil.extractOrigin(event);
        TimeInterval filterOffset = new TimeInterval(10, UnitImpl.SECOND);
        final MicroSecondDate originTime = new MicroSecondDate(origin.origin_time);
        final Arrival[] arrivals = PhasePhilter.filter(tauP.calcTravelTimes(channel.my_site.my_station,
                                                                            origin,
                                                                            phases),
                                                       filterOffset);
        final SodFlag[] flags = new SodFlag[arrivals.length];
        for(int i = 0; i < arrivals.length; i++) {
            MicroSecondDate flagTime = originTime.add(new TimeInterval(arrivals[i].getTime(),
                                                                       UnitImpl.SECOND));
            flags[i] = new SodFlag(flagTime, arrivals[i].getName(), bsd);
            bsd.add(flags[i]);
        }
        final String picFileName = locator.getLocation(event, channel, fileType);
        SwingUtilities.invokeAndWait(new Runnable() {

            public void run() {
                logger.debug("writing " + picFileName);
                try {
                    if(fileType.equals(PDF)) {
                        bsd.outputToPDF(new File(picFileName));
                    } else {
                        bsd.outputToPNG(new File(picFileName), dims);
                    }
                    if(putDataInCookieJar) {
                        if(!pairsInserted.add(new Integer(cookieJar.getEventChannelPair()
                                .getPairId()))) {
                            logger.debug("inserting same key into cookie jar!  You can fix this by making sure each channel in a vector process gets its correct cookie jar...");
                            return;
                        }
                        /*
                         * Currently only the regions around first P and first S
                         * Flags are made clickable
                         */
                        int pLeft = NUM_P_TO_MARK;
                        int sLeft = NUM_S_TO_MARK;
                        for(int i = 0; i < arrivals.length; i++) {
                            String phase = arrivals[i].getName();
                            FlagData flagData = flags[i].getFlagData();
                            if(phase.startsWith("P") && pLeft-- > 0) {
                                cookieJar.put(locator.getPrefix() + COOKIE_KEY
                                        + "P", flagData);
                            } else if(phase.startsWith("S") && sLeft-- > 0) {
                                cookieJar.put(locator.getPrefix() + COOKIE_KEY
                                        + "S", flagData);
                            }
                        }
                    }
                } catch(Throwable e) {
                    GlobalExceptionHandler.handle("unable to save seismogram image to "
                                                          + picFileName,
                                                  e);
                }
            }
        });
        return new WaveformResult(seismograms, new StringTreeLeaf(this, true));
    }

    private static Set pairsInserted = Collections.synchronizedSet(new HashSet());

    private boolean putDataInCookieJar = false;

    private SeismogramImageOutputLocator locator;

    private TauPUtil tauP;

    private PhaseWindow phaseWindow = null;

    private String modelName = "iasp91";

    private boolean relativeTime = false;

    private static TauP_Time tauptime = null;

    public static final String PDF = "pdf";

    public static final String PNG = "png";

    public static final String COOKIE_KEY = "SeismogramImageProcess_flagPixels_";

    private static final String[] DEFAULT_PHASES = {"ttp", "tts"};

    private static final int NUM_P_TO_MARK = 1;

    private static final int NUM_S_TO_MARK = 1;

    private static Dimension DEFAULT_DIMENSION = new Dimension(500, 200);

    private Dimension dims = DEFAULT_DIMENSION;

    private Logger logger = Logger.getLogger(SeismogramImageProcess.class);
}