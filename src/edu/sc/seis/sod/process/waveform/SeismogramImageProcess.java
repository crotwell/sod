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
import edu.iris.Fissures.IfNetwork.Station;
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
import edu.sc.seis.fissuresUtil.display.SeismogramDisplay;
import edu.sc.seis.fissuresUtil.display.configuration.SeismogramDisplayConfiguration;
import edu.sc.seis.fissuresUtil.display.registrar.BasicTimeConfig;
import edu.sc.seis.fissuresUtil.display.registrar.PhaseAlignedTimeConfig;
import edu.sc.seis.fissuresUtil.display.registrar.TimeConfig;
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
            } else if(n.getNodeName().equals("showOnlyFirstArrivals")) {
                showOnlyFirst = true;
            } else if(n.getNodeName().equals("phaseNameMappings")) {
                renamer = new PhasePhilter.PhaseRenamer(n);
            } else if(n.getNodeName().equals("phaseFlags")) {
                Element subEl = (Element)n;
                NodeList flagEls = subEl.getElementsByTagName("phase");
                phaseFlagNames = new String[flagEls.getLength()];
                for(int j = 0; j < flagEls.getLength(); j++) {
                    phaseFlagNames[j] = SodUtil.nodeValueOfXPath((Element)flagEls.item(j),
                                                                 "text()");
                }
            } else if(n.getNodeName().equals("displayConfig")) {
                sdc = new SeismogramDisplayConfiguration((Element)n);
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
                       phaseFlagNames,
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

    public static MemoryDataSetSeismogram createSeis(LocalSeismogramImpl[] seismograms,
                                                     RequestFilter[] original,
                                                     PhaseWindow phaseWindow,
                                                     EventAccessOperations ev,
                                                     Channel chan)
            throws Exception {
        MemoryDataSetSeismogram memDSS = null;
        if(phaseWindow == null) {
            memDSS = new MemoryDataSetSeismogram(original[0], "");
            memDSS.setBeginTime(DisplayUtils.firstBeginDate(original)
                    .getFissuresTime());
            memDSS.setEndTime(DisplayUtils.lastEndDate(original)
                    .getFissuresTime());
        } else {
            PhaseRequest req = phaseWindow.getPhaseRequest();
            RequestFilter[] request = req.generateRequest(ev, chan, null);
            memDSS = new MemoryDataSetSeismogram(request[0], "");
            memDSS.setBeginTime(DisplayUtils.firstBeginDate(request)
                    .getFissuresTime());
            memDSS.setEndTime(DisplayUtils.lastEndDate(request)
                    .getFissuresTime());
        }
        for(int i = 0; i < seismograms.length; i++) {
            memDSS.add(seismograms[i]);
        }
        return memDSS;
    }

    protected TimeConfig getTimeConfig(boolean relTime) {
        if(relTime) {
            PhaseAlignedTimeConfig phaseTime = new PhaseAlignedTimeConfig();
            phaseTime.setTauP(tauptime);
            return phaseTime;
        } else {
            return new BasicTimeConfig();
        }
    }

    protected Arrival[] getArrivals(Channel chan, Origin o, String[] phases)
            throws TauModelException {
        Station sta = chan.my_site.my_station;
        TimeInterval filterOffset = new TimeInterval(10, UnitImpl.SECOND);
        Arrival[] arrivals = PhasePhilter.filter(tauP.calcTravelTimes(sta,
                                                                      o,
                                                                      phases),
                                                 filterOffset);
        if(showOnlyFirst) {
            arrivals = PhasePhilter.mindPsAndSs(arrivals);
        }
        return arrivals;
    }

    protected SodFlag[] createSodFlags(Arrival[] arrivals,
                                       Origin o,
                                       SeismogramDisplay bsd) {
        final SodFlag[] flags = new SodFlag[arrivals.length];
        MicroSecondDate originTime = new MicroSecondDate(o.origin_time);
        for(int i = 0; i < arrivals.length; i++) {
            MicroSecondDate flagTime = originTime.add(new TimeInterval(arrivals[i].getTime(),
                                                                       UnitImpl.SECOND));
            flags[i] = new SodFlag(flagTime, renamer.rename(arrivals[i]), bsd);
        }
        return flags;
    }

    /** allows specifying a fileType, png or pdf, and a list of phases. */
    public WaveformResult process(EventAccessOperations event,
                                  Channel channel,
                                  RequestFilter[] original,
                                  LocalSeismogramImpl[] seismograms,
                                  String fileType,
                                  String[] phases,
                                  boolean relTime,
                                  CookieJar cookieJar) throws Exception {
        logger.debug("process() called");
        SeismogramDisplay bsd = sdc.createDisplay();
        bsd.setTimeConfig(getTimeConfig(relTime));
        MemoryDataSetSeismogram memDSS = createSeis(seismograms,
                                                    original,
                                                    phaseWindow,
                                                    event,
                                                    channel);
        DataSet dataset = new MemoryDataSet("temp", "Temp Dataset for "
                + memDSS.getName(), "temp", new AuditInfo[0]);
        dataset.addDataSetSeismogram(memDSS, new AuditInfo[0]);
        Origin o = EventUtil.extractOrigin(event);
        dataset.addParameter(DataSet.EVENT, event, new AuditInfo[0]);
        Arrival[] arrivals = getArrivals(channel, o, phases);
        SodFlag[] flags = createSodFlags(arrivals, o, bsd);
        String picFileName = locator.getLocation(event, channel, fileType);
        SwingUtilities.invokeAndWait(new ImageWriter(cookieJar,
                                                     bsd,
                                                     fileType,
                                                     picFileName,
                                                     flags));
        return new WaveformResult(seismograms, new StringTreeLeaf(this, true));
    }

    protected class ImageWriter implements Runnable {

        private final CookieJar cookieJar;

        private final SeismogramDisplay bsd;

        private final String fileType;

        private final String picFileName;

        private final SodFlag[] flags;

        private ImageWriter(CookieJar cookieJar, SeismogramDisplay bsd,
                String fileType, String picFileName, SodFlag[] flags) {
            this.cookieJar = cookieJar;
            this.bsd = bsd;
            this.fileType = fileType;
            this.picFileName = picFileName;
            this.flags = flags;
        }

        public void run() {
            logger.debug("writing " + picFileName);
            try {
                if(fileType.equals(PDF)) {
                    ((BasicSeismogramDisplay)bsd).outputToPDF(new File(picFileName));
                } else {
                    bsd.outputToPNG(new File(picFileName), dims);
                }
                if(putDataInCookieJar) {
                    if(!pairsInserted.add(new Integer(cookieJar.getEventChannelPair()
                            .getPairId()))) {
                        logger.debug("inserting same key into cookie jar!  You can fix this by making sure each channel in a vector process gets its correct cookie jar...");
                        return;
                    }
                    //Currently only the regions around first P and first S
                    //Flags are made clickable
                    int pLeft = NUM_P_TO_MARK;
                    int sLeft = NUM_S_TO_MARK;
                    for(int i = 0; i < flags.length; i++) {
                        String phase = flags[i].getName();
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
    }

    private static Set pairsInserted = Collections.synchronizedSet(new HashSet());

    private boolean putDataInCookieJar = false;

    private SeismogramDisplayConfiguration sdc = new SeismogramDisplayConfiguration();

    protected SeismogramImageOutputLocator locator;

    private TauPUtil tauP;

    private boolean showOnlyFirst;

    protected PhaseWindow phaseWindow = null;

    private PhasePhilter.PhaseRenamer renamer = new PhasePhilter.PhaseRenamer();

    private String modelName = "iasp91";

    private boolean relativeTime = false;

    protected String[] phaseFlagNames = DEFAULT_PHASES;

    protected Dimension dims = DEFAULT_DIMENSION;

    private static TauP_Time tauptime = null;

    public static final String PDF = "pdf";

    public static final String PNG = "png";

    public static final String COOKIE_KEY = "SeismogramImageProcess_flagPixels_";

    private static final String[] DEFAULT_PHASES = {"ttp", "tts"};

    private static final int NUM_P_TO_MARK = 1;

    private static final int NUM_S_TO_MARK = 1;

    private static Dimension DEFAULT_DIMENSION = new Dimension(500, 200);

    private Logger logger = Logger.getLogger(SeismogramImageProcess.class);
}