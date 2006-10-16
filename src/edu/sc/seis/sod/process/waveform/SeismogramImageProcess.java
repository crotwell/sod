/**
 * SeismogramImageProcess.java
 * 
 * @author Created by Philip Oliver-Paull
 */
package edu.sc.seis.sod.process.waveform;

import java.awt.Dimension;
import java.io.File;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import javax.swing.SwingUtilities;
import org.apache.log4j.Logger;
import org.w3c.dom.Element;
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
import edu.iris.Fissures.network.ChannelIdUtil;
import edu.iris.Fissures.seismogramDC.LocalSeismogramImpl;
import edu.sc.seis.TauP.Arrival;
import edu.sc.seis.TauP.TauModelException;
import edu.sc.seis.fissuresUtil.bag.TauPUtil;
import edu.sc.seis.fissuresUtil.cache.EventUtil;
import edu.sc.seis.fissuresUtil.display.BasicSeismogramDisplay;
import edu.sc.seis.fissuresUtil.display.DisplayUtils;
import edu.sc.seis.fissuresUtil.display.MicroSecondTimeRange;
import edu.sc.seis.fissuresUtil.display.PhasePhilter;
import edu.sc.seis.fissuresUtil.display.SeismogramDisplay;
import edu.sc.seis.fissuresUtil.display.borders.TitleBorder;
import edu.sc.seis.fissuresUtil.display.configuration.BorderConfiguration;
import edu.sc.seis.fissuresUtil.display.configuration.DOMHelper;
import edu.sc.seis.fissuresUtil.display.configuration.SeismogramDisplayConfiguration;
import edu.sc.seis.fissuresUtil.display.drawable.Flag;
import edu.sc.seis.fissuresUtil.display.registrar.BasicTimeConfig;
import edu.sc.seis.fissuresUtil.display.registrar.TimeConfig;
import edu.sc.seis.fissuresUtil.exceptionHandler.GlobalExceptionHandler;
import edu.sc.seis.fissuresUtil.xml.DataSet;
import edu.sc.seis.fissuresUtil.xml.DataSetSeismogram;
import edu.sc.seis.fissuresUtil.xml.MemoryDataSet;
import edu.sc.seis.fissuresUtil.xml.MemoryDataSetSeismogram;
import edu.sc.seis.sod.ConfigurationException;
import edu.sc.seis.sod.CookieJar;
import edu.sc.seis.sod.SodUtil;
import edu.sc.seis.sod.status.StringTreeLeaf;
import edu.sc.seis.sod.subsetter.requestGenerator.PhaseRequest;

public class SeismogramImageProcess implements WaveformProcess {

    public SeismogramImageProcess(SeismogramImageOutputLocator locator) {
        this.locator = locator;
    }

    public SeismogramImageProcess(Element el) throws Exception {
        if(DOMHelper.hasElement(el, "phaseWindow")) {
            phaseWindow = new PhaseWindow(SodUtil.getElement(el, "phaseWindow"));
        }
        if(DOMHelper.hasElement(el, "modelName")) {
            modelName = SodUtil.getNestedText(SodUtil.getElement(el,
                                                                 "modelName"));
            tauP = TauPUtil.getTauPUtil(modelName);
        }
        if(DOMHelper.hasElement(el, "dimension")) {
            dims = SodUtil.loadDimensions(SodUtil.getElement(el, "dimension"));
        }
        if(DOMHelper.hasElement(el, "showOnlyFirstArrivals")) {
            showOnlyFirst = true;
        }
        if(DOMHelper.hasElement(el, "phaseNameMappings")) {
            renamer = new PhasePhilter.PhaseRenamer(SodUtil.getElement(el,
                                                                       "phaseNameMappings"));
        }
        if(DOMHelper.hasElement(el, "phaseFlags")) {
            Element subEl = SodUtil.getElement(el, "phaseFlags");
            NodeList flagEls = subEl.getElementsByTagName("phase");
            phaseFlagNames = new String[flagEls.getLength()];
            for(int j = 0; j < flagEls.getLength(); j++) {
                phaseFlagNames[j] = SodUtil.nodeValueOfXPath((Element)flagEls.item(j),
                                                             "text()");
            }
        }
        if(DOMHelper.hasElement(el, "displayConfig")) {
            sdc = SeismogramDisplayConfiguration.create(SodUtil.getElement(el,
                                                                           "displayConfig"));
        }
        if(DOMHelper.hasElement(el, "titleBorder")) {
            titleBorder = new BorderConfiguration();
            titleBorder.configure(DOMHelper.getElement(el, "titleBorder"));
            configureTitlers(el, titleBorder);
        }
        locator = new SeismogramImageOutputLocator(el);
    }

    protected void configureTitlers(Element el, BorderConfiguration titleBorder)
            throws ConfigurationException {
        if(DOMHelper.hasElement(el, "titler")) {
            NodeList titlerConfigs = DOMHelper.getElements(el, "titler");
            for(int i = 0; i < titlerConfigs.getLength(); i++) {
                SeismogramTitler titler = new SeismogramTitler(titleBorder);
                titler.configure((Element)titlerConfigs.item(i));
                titlers.add(titler);
            }
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
                       true,
                       cookieJar);
    }

    public static MemoryDataSetSeismogram createSeis(LocalSeismogramImpl[] seismograms,
                                                     RequestFilter[] original)
            throws Exception {
        MemoryDataSetSeismogram memDSS = new MemoryDataSetSeismogram(original[0],
                                                                     "");
        memDSS.setBeginTime(DisplayUtils.firstBeginDate(original)
                .getFissuresTime());
        memDSS.setEndTime(DisplayUtils.lastEndDate(original).getFissuresTime());
        for(int i = 0; i < seismograms.length; i++) {
            memDSS.add(seismograms[i]);
        }
        return memDSS;
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

    protected void addFlags(Arrival[] arrivals,
                            Origin o,
                            SeismogramDisplay bsd,
                            DataSetSeismogram seis) {
        MicroSecondDate originTime = new MicroSecondDate(o.origin_time);
        for(int i = 0; i < arrivals.length; i++) {
            MicroSecondDate flagTime = originTime.add(new TimeInterval(arrivals[i].getTime(),
                                                                       UnitImpl.SECOND));
            bsd.add(new Flag(flagTime,
                             renamer.rename(arrivals[i]),
                             bsd.getDrawableSeismogram(seis)));
        }
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
        MemoryDataSetSeismogram memDSS = createDataSetSeismogram(event,
                                                                 channel,
                                                                 original,
                                                                 seismograms);
        SeismogramDisplay bsd = createPopulatedDisplay(event,
                                                       channel,
                                                       new DataSetSeismogram[] {memDSS},
                                                       phases);
        String picFileName = locator.getLocation(event, channel, fileType);
        writeImage(bsd, locator.getFileType(), picFileName);
        return new WaveformResult(seismograms, new StringTreeLeaf(this, true));
    }

    public SeismogramDisplay createPopulatedDisplay(EventAccessOperations event,
                                                    Channel channel,
                                                    DataSetSeismogram[] seis,
                                                    String[] phases)
            throws Exception {
        MicroSecondTimeRange timeWindow = null;
        if(seis.length > 0) {
            timeWindow = getTimeWindow(phaseWindow, seis[0]);
            updateTitlers(event, channel, timeWindow);
        }
        SeismogramDisplay bsd = sdc.createDisplay();
        TimeConfig tc = new BasicTimeConfig();
        bsd.setTimeConfig(tc);
        populateDisplay(bsd, event, channel, seis, phases);
        if(seis.length > 0) {
            setTimeWindow(tc, timeWindow, tc.getTime(seis[0]));
        }
        return bsd;
    }

    private void populateDisplay(SeismogramDisplay sd,
                                 EventAccessOperations event,
                                 Channel channel,
                                 DataSetSeismogram[] seis,
                                 String[] phases) throws TauModelException {
        Origin o = EventUtil.extractOrigin(event);
        addFlags(getArrivals(channel, o, phases), o, sd, seis[0]);
        if(seis.length > 0) {
            sd.add(seis);
        }
    }

    private MemoryDataSetSeismogram createDataSetSeismogram(EventAccessOperations event,
                                                            Channel channel,
                                                            RequestFilter[] original,
                                                            LocalSeismogramImpl[] seismograms)
            throws Exception {
        MemoryDataSetSeismogram memDSS = createSeis(seismograms, original);
        DataSet dataset = new MemoryDataSet("temp", "Temp Dataset for "
                + memDSS.getName(), "temp", new AuditInfo[0]);
        dataset.addDataSetSeismogram(memDSS, new AuditInfo[0]);
        dataset.addParameter(DataSet.EVENT, event, new AuditInfo[0]);
        dataset.addParameter(DataSet.CHANNEL
                                     + ChannelIdUtil.toString(channel.get_id()),
                             channel,
                             new AuditInfo[0]);
        return memDSS;
    }

    public static void setTimeWindow(TimeConfig tc, DataSetSeismogram dss)
            throws Exception {
        setTimeWindow(tc, null, dss);
    }

    public static void setTimeWindow(TimeConfig tc,
                                     PhaseWindow pw,
                                     DataSetSeismogram dss) throws Exception {
        setTimeWindow(tc, getTimeWindow(pw, dss), tc.getTime(dss));
    }

    public static void setTimeWindow(TimeConfig tc,
                                     MicroSecondTimeRange newTime,
                                     MicroSecondTimeRange currentTime) {
        double[] shiftNScale = DisplayUtils.getShiftAndScale(newTime,
                                                             currentTime);
        tc.shaleTime(shiftNScale[0], shiftNScale[1]);
    }

    public static MicroSecondTimeRange getTimeWindow(PhaseWindow pw,
                                                     DataSetSeismogram dss)
            throws Exception {
        RequestFilter rf;
        if(pw != null) {
            PhaseRequest pr = pw.getPhaseRequest();
            rf = pr.generateRequest(dss.getEvent(), dss.getChannel());
        } else {
            rf = dss.getRequestFilter();
        }
        return new MicroSecondTimeRange(rf);
    }

    public void updateTitlers(EventAccessOperations event,
                              Channel channel,
                              MicroSecondTimeRange timeRange) {
        Iterator it = titlers.iterator();
        while(it.hasNext()) {
            ((SeismogramTitler)it.next()).title(event, channel, timeRange);
        }
    }

    public BorderConfiguration getTitleBorder() {
        return titleBorder;
    }

    protected void writeImage(SeismogramDisplay disp,
                              String fileType,
                              String picFileName) throws Exception {
        SwingUtilities.invokeAndWait(new ImageWriter(disp,
                                                     fileType,
                                                     picFileName));
    }

    protected class ImageWriter implements Runnable {

        private final SeismogramDisplay bsd;

        private final String fileType;

        private final String picFileName;

        private ImageWriter(SeismogramDisplay bsd,
                            String fileType,
                            String picFileName) {
            this.bsd = bsd;
            this.fileType = fileType;
            this.picFileName = picFileName;
            if(!(fileType.equals(PDF) || fileType.equals(PNG))) {
                throw new IllegalArgumentException("Unknown fileType:"
                        + fileType);
            }
        }

        public void run() {
            logger.debug("writing " + picFileName);
            try {
                if(fileType.equals(PDF)) {
                    if(titleBorder != null) {
                        bsd.outputToPDF(new File(picFileName),
                                        (TitleBorder)titleBorder.createBorder(bsd));
                    } else {
                        bsd.outputToPDF(new File(picFileName));
                    }
                } else if(fileType.equals(PNG)) {
                    bsd.outputToPNG(new File(picFileName), dims);
                } else {
                    // should never happen
                    throw new RuntimeException("Unknown fileType:" + fileType);
                }
            } catch(Throwable e) {
                GlobalExceptionHandler.handle("unable to save seismogram image to "
                                                      + picFileName,
                                              e);
            }
        }
    }

    private SeismogramDisplayConfiguration sdc = new SeismogramDisplayConfiguration();

    protected SeismogramImageOutputLocator locator;

    protected BorderConfiguration titleBorder;

    protected List titlers = new ArrayList();

    private TauPUtil tauP = TauPUtil.getTauPUtil();

    private boolean showOnlyFirst;

    protected PhaseWindow phaseWindow;

    private PhasePhilter.PhaseRenamer renamer = new PhasePhilter.PhaseRenamer();

    private String modelName = "iasp91";

    protected String[] phaseFlagNames = DEFAULT_PHASES;

    protected Dimension dims = DEFAULT_DIMENSION;

    public static final String PDF = "pdf";

    public static final String PNG = "png";

    private static final String[] DEFAULT_PHASES = {};

    private static Dimension DEFAULT_DIMENSION = new Dimension(500, 200);

    private Logger logger = Logger.getLogger(SeismogramImageProcess.class);
}