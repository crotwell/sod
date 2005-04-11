package edu.sc.seis.sod.process.waveform;

import java.awt.Dimension;
import java.io.File;
import java.io.IOException;
import java.io.OutputStream;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.apache.log4j.Category;
import org.w3c.dom.Element;
import edu.iris.Fissures.IfEvent.EventAccessOperations;
import edu.iris.Fissures.IfNetwork.Channel;
import edu.iris.Fissures.IfNetwork.ChannelId;
import edu.iris.Fissures.IfSeismogramDC.RequestFilter;
import edu.iris.Fissures.network.ChannelIdUtil;
import edu.iris.Fissures.seismogramDC.LocalSeismogramImpl;
import edu.sc.seis.fissuresUtil.database.event.JDBCEventAccess;
import edu.sc.seis.fissuresUtil.database.network.JDBCChannel;
import edu.sc.seis.fissuresUtil.display.ParseRegions;
import edu.sc.seis.fissuresUtil.display.RecordSectionDisplay;
import edu.sc.seis.fissuresUtil.display.configuration.DOMHelper;
import edu.sc.seis.fissuresUtil.display.configuration.SeismogramDisplayConfiguration;
import edu.sc.seis.fissuresUtil.display.registrar.CustomLayOutConfig;
import edu.sc.seis.fissuresUtil.xml.DataSet;
import edu.sc.seis.fissuresUtil.xml.DataSetSeismogram;
import edu.sc.seis.fissuresUtil.xml.DataSetToXML;
import edu.sc.seis.fissuresUtil.xml.MemoryDataSetSeismogram;
import edu.sc.seis.fissuresUtil.xml.URLDataSetSeismogram;
import edu.sc.seis.sod.ConfigurationException;
import edu.sc.seis.sod.CookieJar;
import edu.sc.seis.sod.MotionVectorArm;
import edu.sc.seis.sod.SodUtil;
import edu.sc.seis.sod.Start;
import edu.sc.seis.sod.WaveformArm;
import edu.sc.seis.sod.database.waveform.JDBCEventRecordSection;
import edu.sc.seis.sod.database.waveform.JDBCRecordSectionChannel;
import edu.sc.seis.sod.status.StringTreeLeaf;

public class RecordSectionDisplayGenerator implements WaveformProcess {

    public RecordSectionDisplayGenerator(Element config) throws SQLException,
            NoSuchFieldException {
        initConfig(config);
        String eventRecSecTableName = "eventRecordSection" + id;
        String recSecChanTableName = "recordSectionChannel" + id;
        recordSectionChannel = new JDBCRecordSectionChannel(recSecChanTableName,
                                                            eventRecSecTableName);
        eventRecordSection = new JDBCEventRecordSection(eventRecSecTableName,
                                                        recSecChanTableName);
        eventAccess = new JDBCEventAccess();
        channel = new JDBCChannel();
    }

    private void initConfig(Element config) throws NoSuchFieldException {
        id = SodUtil.getText(SodUtil.getElement(config, "id"));
        saveSeisId = DOMHelper.extractText(config, "saveSeisId", id);
        if(DOMHelper.hasElement(config, "fileNameBase")) {
            filename = SodUtil.getText(SodUtil.getElement(config,
                                                          "fileNameBase"))
                    + FILE_EXTENSION;
        }
        if(DOMHelper.hasElement(config, "percentSeisHeight")) {
            percentSeisHeight = new Double(SodUtil.getText(SodUtil.getElement(config,
                                                                              "percentSeisHeight"))).doubleValue();
        }
        int idealNumberOfSeismograms = 10;
        if(DOMHelper.hasElement(config, "idealNumberOfSeismograms")) {
            String idealNumText = SodUtil.getText(SodUtil.getElement(config,
                                                                     "idealNumberOfSeismograms"));
            idealNumberOfSeismograms = new Integer(idealNumText).intValue();
        }
        int maxNumberOfSeismograms = idealNumberOfSeismograms + 5;
        if(DOMHelper.hasElement(config, "maxNumberOfSeismograms")) {
            String maxSeisText = SodUtil.getText(SodUtil.getElement(config,
                                                                    "maximumSeismogramsPerRecordSection"));
            maxNumberOfSeismograms = new Integer(maxSeisText).intValue();
        }
        if(DOMHelper.hasElement(config, "distanceRange")) {
            distRange = new DistanceRange(SodUtil.getElement(config,
                                                             "distanceRange"));
        }
        spacer = new RecordSectionSpacer(distRange,
                                         idealNumberOfSeismograms,
                                         maxNumberOfSeismograms);
        if(DOMHelper.hasElement(config, "recordSectionSize")) {
            int width = new Integer(SodUtil.getText(SodUtil.getElement(SodUtil.getElement(config,
                                                                                          "recordSectionSize"),
                                                                       "width"))).intValue();
            int height = new Integer(SodUtil.getText(SodUtil.getElement(SodUtil.getElement(config,
                                                                                           "recordSectionSize"),
                                                                        "height"))).intValue();
            recSecDim = new Dimension(width, height);
        }
        if(DOMHelper.hasElement(config, "displayConfig")) {
            displayCreator = SeismogramDisplayConfiguration.create(DOMHelper.getElement(config,
                                                                                        "displayConfig"));
        }
    }

    public SaveSeismogramToFile getSaveSeismogramToFile()
            throws ConfigurationException {
        return getSaveSeismogramToFile(Start.getWaveformArm(), saveSeisId);
    }

    public static SaveSeismogramToFile getSaveSeismogramToFile(WaveformArm waveform,
                                                               String id)
            throws ConfigurationException {
        WaveformProcess[] waveformProcesses = null;
        if(waveform.getLocalSeismogramArm() != null) {
            waveformProcesses = waveform.getLocalSeismogramArm().getProcesses();
        } else {
            MotionVectorArm moVec = waveform.getMotionVectorArm();
            waveformProcesses = moVec.getWaveformProcesses();
        }
        for(int i = 0; i < waveformProcesses.length; i++) {
            if(waveformProcesses[i] instanceof SaveSeismogramToFile) {
                SaveSeismogramToFile saveSeis = (SaveSeismogramToFile)waveformProcesses[i];
                if(id.equals(saveSeis.getId())) { return saveSeis; }
            }
        }
        throw new ConfigurationException("RecordSectionDisplayGenerator needs a SaveSeismogramToFile process");
    }

    public WaveformResult process(EventAccessOperations event,
                                  Channel chan,
                                  RequestFilter[] original,
                                  RequestFilter[] available,
                                  LocalSeismogramImpl[] seismograms,
                                  CookieJar cookieJar) throws Exception {
        acceptableChannels.add(ChannelIdUtil.toString(chan.get_id()));
        try {
            saveSeisToFile = getSaveSeismogramToFile();
            DataSet ds = DataSetToXML.load(saveSeisToFile.getDSMLFile(event)
                    .toURI()
                    .toURL());
            String[] dataSeisNames = ds.getDataSetSeismogramNames();
            DataSetSeismogram[] dss = new DataSetSeismogram[dataSeisNames.length];
            List acceptableSeis = new ArrayList();
            for(int i = 0; i < dataSeisNames.length; i++) {
                dss[i] = ds.getDataSetSeismogram(dataSeisNames[i]);
                if(acceptableChannels.contains(ChannelIdUtil.toString(dss[i].getChannelId()))) {
                    acceptableSeis.add(dss[i]);
                }
            }
            String regionName = PR.getRegionName(event.get_attributes().region);
            String dateTime = event.get_preferred_origin().origin_time.date_time;
            String msg = "Got " + dss.length
                    + " DataSetSeismograms from DSML file for event in "
                    + regionName + " at " + dateTime;
            logger.debug(msg);
            dss = (DataSetSeismogram[])acceptableSeis.toArray(new DataSetSeismogram[0]);
            outputBestRecordSection(event, dss);
        } catch(IOException e) {
            logger.debug("Problem opening dsml file in RecordSectionDisplayGenerator",
                         e);
            throw new IOException("Problem opening dsml file in RecordSectionDisplayGenerator"
                    + e);
        }
        return new WaveformResult(seismograms, new StringTreeLeaf(this, true));
    }

    public MemoryDataSetSeismogram[] wrap(DataSetSeismogram[] dss)
            throws Exception {
        MemoryDataSetSeismogram[] memDss = new MemoryDataSetSeismogram[dss.length];
        for(int i = 0; i < memDss.length; i++) {
            memDss[i] = new MemoryDataSetSeismogram(((URLDataSetSeismogram)dss[i]).getSeismograms(),
                                                    dss[i].getDataSet(),
                                                    dss[i].getName());
        }
        return memDss;
    }

    public void outputBestRecordSection(EventAccessOperations event,
                                        DataSetSeismogram[] dataSeis)
            throws Exception {
        writeImage(wrap(spacer.spaceOut(dataSeis)), event, filename);
    }

    public void outputBestRecordSection(EventAccessOperations event,
                                        DataSetSeismogram[] dataSeis,
                                        OutputStream out) throws Exception {
        DataSetSeismogram[] bestSeismos = spacer.spaceOut(dataSeis);
        RecordSectionDisplay rsDisplay = getConfiguredRSDisplay();
        rsDisplay.add(wrap(bestSeismos));
        rsDisplay.outputToPNG(out, recSecDim);
    }

    private void writeImage(DataSetSeismogram[] dataSeis,
                            EventAccessOperations event,
                            String fileName) throws Exception {
        int recSecId = -1;
        int eventId = eventAccess.getDBId(event);
        String base = Start.getRunProps().getStatusBaseDir() + "/earthquakes";
        String dir = saveSeisToFile.getLabel(event);
        new File(base + "/" + dir).mkdirs();
        String fullName = dir + "/" + fileName;
        RecordSectionDisplay rsDisplay = getConfiguredRSDisplay();
        rsDisplay.add(dataSeis);
        logger.debug("Added " + dataSeis.length
                + " seismograms to RecordSectionDisplay");
        try {
            File outPNG = new File(base + "/" + fullName);
            rsDisplay.outputToPNG(outPNG, recSecDim);
        } catch(IOException e) {
            logger.debug("Problem writing recordsection output to PNG", e);
            throw new IOException("Problem writing recordSection output to PNG "
                    + e);
        }
        if(!eventRecordSection.imageExists(eventId, fullName)) {
            recSecId = eventRecordSection.insert(eventId, fullName);
        } else {
            recSecId = eventRecordSection.getRecSecId(eventId, fullName);
            recordSectionChannel.deleteRecSec(recSecId);
        }
        Map seisToPixelMap = rsDisplay.getPixelMap();
        for(int j = 0; j < dataSeis.length; j++) {
            ChannelId channel_id = dataSeis[j].getRequestFilter().channel_id;
            int channelId = channel.getDBId(channel_id);
            double[] pixelInfo = (double[])seisToPixelMap.get(channel_id);
            recordSectionChannel.insert(recSecId, channelId, pixelInfo);
        }
    }

    private RecordSectionDisplay getConfiguredRSDisplay() {
        RecordSectionDisplay rsDisplay = (RecordSectionDisplay)displayCreator.createDisplay();
        CustomLayOutConfig custConfig = new CustomLayOutConfig(distRange.getMinDistance(),
                                                               distRange.getMaxDistance(),
                                                               percentSeisHeight);
        custConfig.setSwapAxes(rsDisplay.getSwapAxes());
        rsDisplay.setLayout(custConfig);
        return rsDisplay;
    }

    private SaveSeismogramToFile saveSeisToFile;

    private String id, saveSeisId;

    private DistanceRange distRange = new DistanceRange(0, 180);

    private JDBCEventRecordSection eventRecordSection;

    private JDBCRecordSectionChannel recordSectionChannel;

    private JDBCEventAccess eventAccess;

    private JDBCChannel channel;

    private String filename = "recordsection" + FILE_EXTENSION;

    private double percentSeisHeight = 10;

    private RecordSectionSpacer spacer;

    private SeismogramDisplayConfiguration displayCreator;

    private Dimension recSecDim = new Dimension(500, 500);

    private Set acceptableChannels = new HashSet();

    private static final String FILE_EXTENSION = ".png";

    private static final ParseRegions PR = ParseRegions.getInstance();

    private static Category logger = Category.getInstance(RecordSectionDisplayGenerator.class.getName());
}