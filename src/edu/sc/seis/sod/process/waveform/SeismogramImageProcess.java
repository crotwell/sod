/**
 * SeismogramImageProcess.java
 * 
 * @author Created by Philip Oliver-Paull
 */
package edu.sc.seis.sod.process.waveform;

import java.awt.Dimension;
import java.io.File;
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
import edu.sc.seis.fissuresUtil.display.configuration.SeismogramDisplayConfiguration;
import edu.sc.seis.fissuresUtil.display.drawable.Flag;
import edu.sc.seis.fissuresUtil.display.registrar.BasicTimeConfig;
import edu.sc.seis.fissuresUtil.display.registrar.TimeConfig;
import edu.sc.seis.fissuresUtil.exceptionHandler.GlobalExceptionHandler;
import edu.sc.seis.fissuresUtil.xml.DataSet;
import edu.sc.seis.fissuresUtil.xml.DataSetSeismogram;
import edu.sc.seis.fissuresUtil.xml.MemoryDataSet;
import edu.sc.seis.fissuresUtil.xml.MemoryDataSetSeismogram;
import edu.sc.seis.sod.CookieJar;
import edu.sc.seis.sod.SodUtil;
import edu.sc.seis.sod.status.StringTreeLeaf;
import edu.sc.seis.sod.subsetter.requestGenerator.PhaseRequest;

public class SeismogramImageProcess implements WaveformProcess {

    public SeismogramImageProcess(SeismogramImageOutputLocator locator) {
        this.locator = locator;
    }

    public SeismogramImageProcess(Element el) throws Exception {
        NodeList nl = el.getChildNodes();
        for(int i = 0; i < nl.getLength(); i++) {
            Node n = nl.item(i);
            if(n.getNodeName().equals("phaseWindow")) {
                phaseWindow = new PhaseWindow((Element)n);
            } else if(n.getNodeName().equals("modelName")) {
                modelName = SodUtil.getNestedText((Element)n);
                tauP = TauPUtil.getTauPUtil(modelName);
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
                sdc = SeismogramDisplayConfiguration.create((Element)n);
            }
        }
        locator = new SeismogramImageOutputLocator(el);
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
        SeismogramDisplay bsd = sdc.createDisplay();
        bsd.setTimeConfig(new BasicTimeConfig());
        MemoryDataSetSeismogram memDSS = createSeis(seismograms, original);
        DataSet dataset = new MemoryDataSet("temp", "Temp Dataset for "
                + memDSS.getName(), "temp", new AuditInfo[0]);
        dataset.addDataSetSeismogram(memDSS, new AuditInfo[0]);
        dataset.addParameter(DataSet.EVENT, event, new AuditInfo[0]);
        dataset.addParameter(DataSet.CHANNEL
                                     + ChannelIdUtil.toString(channel.get_id()),
                             channel,
                             new AuditInfo[0]);
        Origin o = EventUtil.extractOrigin(event);
        addFlags(getArrivals(channel, o, phases), o, bsd, memDSS);
        String picFileName = locator.getLocation(event, channel, fileType);
        if(seismograms.length > 0) {
            setTimeWindow(bsd.getTimeConfig(), phaseWindow, memDSS);
        }
        SwingUtilities.invokeAndWait(new ImageWriter(bsd, fileType, picFileName));
        return new WaveformResult(seismograms, new StringTreeLeaf(this, true));
    }

    public void setTimeWindow(TimeConfig tc,
                              PhaseWindow pw,
                              DataSetSeismogram dss) throws Exception {
        if(pw != null) {
            PhaseRequest pr = pw.getPhaseRequest();
            RequestFilter rf = pr.generateRequest(dss.getEvent(),
                                                  dss.getChannel());
            double[] shiftNScale = DisplayUtils.getShiftAndScale(new MicroSecondTimeRange(rf),
                                                                 tc.getTime(dss));
            System.out.println("SHIFTING BY " + shiftNScale[0] + " SCALING BY "
                    + shiftNScale[1]);
            tc.shaleTime(shiftNScale[0], shiftNScale[1]);
        }
    }

    protected class ImageWriter implements Runnable {

        private final SeismogramDisplay bsd;

        private final String fileType;

        private final String picFileName;

        private ImageWriter(SeismogramDisplay bsd, String fileType,
                String picFileName) {
            this.bsd = bsd;
            this.fileType = fileType;
            this.picFileName = picFileName;
        }

        public void run() {
            logger.debug("writing " + picFileName);
            try {
                if(fileType.equals(PDF)) {
                    ((BasicSeismogramDisplay)bsd).outputToPDF(new File(picFileName));
                } else {
                    bsd.outputToPNG(new File(picFileName), dims);
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