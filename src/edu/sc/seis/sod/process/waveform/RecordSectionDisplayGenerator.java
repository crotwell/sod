package edu.sc.seis.sod.process.waveform;

import java.awt.Dimension;
import java.io.File;
import java.io.IOException;
import java.sql.SQLException;
import java.util.HashMap;
import javax.xml.parsers.ParserConfigurationException;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;
import edu.iris.Fissures.IfEvent.EventAccessOperations;
import edu.iris.Fissures.IfEvent.EventAttr;
import edu.iris.Fissures.IfEvent.Origin;
import edu.iris.Fissures.IfNetwork.Channel;
import edu.iris.Fissures.IfNetwork.ChannelId;
import edu.iris.Fissures.IfSeismogramDC.RequestFilter;
import edu.iris.Fissures.model.QuantityImpl;
import edu.iris.Fissures.seismogramDC.LocalSeismogramImpl;
import edu.sc.seis.fissuresUtil.database.NotFound;
import edu.sc.seis.fissuresUtil.database.event.JDBCEventAccess;
import edu.sc.seis.fissuresUtil.database.network.JDBCChannel;
import edu.sc.seis.fissuresUtil.display.DisplayUtils;
import edu.sc.seis.fissuresUtil.display.RecordSectionDisplay;
import edu.sc.seis.fissuresUtil.exceptionHandler.GlobalExceptionHandler;
import edu.sc.seis.fissuresUtil.xml.DataSet;
import edu.sc.seis.fissuresUtil.xml.DataSetSeismogram;
import edu.sc.seis.fissuresUtil.xml.DataSetToXML;
import edu.sc.seis.fissuresUtil.xml.IncomprehensibleDSMLException;
import edu.sc.seis.fissuresUtil.xml.UnsupportedFileTypeException;
import edu.sc.seis.sod.ConfigurationException;
import edu.sc.seis.sod.CookieJar;
import edu.sc.seis.sod.MotionVectorArm;
import edu.sc.seis.sod.SodUtil;
import edu.sc.seis.sod.Start;
import edu.sc.seis.sod.WaveformArm;
import edu.sc.seis.sod.database.waveform.JDBCEventRecordSection;
import edu.sc.seis.sod.database.waveform.JDBCRecordSectionChannel;
import edu.sc.seis.sod.status.EventFormatter;
import edu.sc.seis.sod.status.StringTreeLeaf;
import edu.sc.seis.sod.subsetter.origin.MagnitudeRange;

public class RecordSectionDisplayGenerator implements WaveformProcess {

    public RecordSectionDisplayGenerator(Element config) throws SQLException {
        id = SodUtil.getText(SodUtil.getElement(config, "id"));
        displayOption = SodUtil.getText(SodUtil.getElement(config,
                                                           "displayOption"));
        fileNameBase = SodUtil.getText(SodUtil.getElement(config,
                                                          "fileNameBase"));
        numSeisPerImage = new Integer(SodUtil.getText(SodUtil.getElement(config,
                                                                         "numSeisPerRecordSection"))).intValue();
        if(config.getElementsByTagName("individualSeisHeight") != null) {
            indSeisHeight = new Integer(SodUtil.getText(SodUtil.getElement(config,
                                                                           "individualSeisHeight"))).intValue();
        } else {
            indSeisHeight = DEFAULT_SEIS_HEIGHT;
        }
        NodeList groups = config.getElementsByTagName("recordSectionGrouping");
        if(config.getElementsByTagName("recordSectionSize") != null) {
            int width = new Integer(SodUtil.getText(SodUtil.getElement(SodUtil.getElement(config,
                                                                                          "recordSectionSize"),
                                                                       "width"))).intValue();
            int height = new Integer(SodUtil.getText(SodUtil.getElement(SodUtil.getElement(config,
                                                                                           "recordSectionSize"),
                                                                        "height"))).intValue();
            recSecDim = new Dimension(width, height);
        } else {
            recSecDim = new Dimension(DEFAULT_WIDTH, DEFAULT_HEIGHT);
        }
        if(groups != null) {
            groupings = new RecordSectionGrouping[groups.getLength()];
            for(int i = 0; i < groups.getLength(); i++) {
                groupings[i] = new RecordSectionGrouping((Element)groups.item(i));
                if(groupings[i].getIndSeisHeight() == 0) {
                    groupings[i].setIndSeisHeight(indSeisHeight);
                }
                if(groupings[i].getRecSecDimension() == null) {
                    groupings[i].setRecSecDimension(recSecDim);
                }
            }
        } else {
            groupings = new RecordSectionGrouping[] {new RecordSectionGrouping(new MagnitudeRange(),
                                                                               new DistanceRange(0,
                                                                                                 180),
                                                                               indSeisHeight,
                                                                               new Dimension(500,
                                                                                             500))};
        }
        String eventRecSecTableName = "eventRecordSection" + id;
        String recSecChanTableName = "recordSectionChannel" + id;
        recordSectionChannel = new JDBCRecordSectionChannel(recSecChanTableName,
                                                            eventRecSecTableName);
        eventRecordSection = new JDBCEventRecordSection(eventRecSecTableName,
                                                        recSecChanTableName);
        eventAccess = new JDBCEventAccess();
        channel = new JDBCChannel();
    }

    public SaveSeismogramToFile getSaveSeismogramToFile()
            throws ConfigurationException {
        WaveformArm waveform = Start.getWaveformArm();
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
                                  CookieJar cookieJar)
            throws ParserConfigurationException, IOException,
            IncomprehensibleDSMLException, UnsupportedFileTypeException,
            ConfigurationException, NotFound, SQLException {
        try {
            saveSeisToFile = getSaveSeismogramToFile();
            DataSet ds = DataSetToXML.load(saveSeisToFile.getDSMLFile(event)
                    .toURI()
                    .toURL());
            String[] dataSeisNames = ds.getDataSetSeismogramNames();
            int numDSSeismograms = dataSeisNames.length;
            DataSetSeismogram[] dss = new DataSetSeismogram[numDSSeismograms];
            for(int i = 0; i < numDSSeismograms; i++) {
                dss[i] = ds.getDataSetSeismogram(dataSeisNames[i]);
            }
            EventAttr eventAttr = event.get_attributes();
            Origin eventOrigin = EventFormatter.getOrigin(event);
            RecordSectionGrouping selectedGroup = null;
            for(int j = 0; j < groupings.length; j++) {
                if(groupings[j].getMagnitudeRange().accept(event,
                                                           eventAttr,
                                                           eventOrigin)) {
                    selectedGroup = groupings[j];
                    break;
                }
            }
         /*   if(displayOption.equals("BEST")) {
                if(selectedGroup != null) {
                    outputBestRecordSections(event, selectedGroup, dss);
                }
            } else {*/
                outputRecordSections(event, dss);
           // }
        } catch(IOException e) {
            throw new IOException("Problem opening dsml file in RecordSectionDisplayGenerator"
                    + e);
        }
        return new WaveformResult(seismograms, new StringTreeLeaf(this, true));
    }

    public void sort(DataSetSeismogram[] dataSeis) {
        for(int i = 0; i < dataSeis.length; i++) {
            QuantityImpl distance = DisplayUtils.calculateDistance(dataSeis[i]);
            for(int j = 0; j < dataSeis.length; j++) {
                if(distance.greaterThan(DisplayUtils.calculateDistance(dataSeis[j]))) {
                    DataSetSeismogram tempSeis = dataSeis[i];
                    dataSeis[i] = dataSeis[j];
                    dataSeis[j] = tempSeis;
                }
            }
        }
    }

    public void outputBestRecordSections(EventAccessOperations event,
                                         RecordSectionGrouping group,
                                         DataSetSeismogram[] dataSeis) {}

    public void outputRecordSections(EventAccessOperations event,
                                     DataSetSeismogram[] dataSeis)
            throws IOException, SQLException, NotFound {
        int dataSeisArrLen = dataSeis.length;
        boolean bestDisplay = false;
        if(displayOption.equals("BEST")) {
            bestDisplay = true;
        }
        if(dataSeisArrLen > 0) {
            int fileNameCounter = 0;
            if(dataSeisArrLen <= numSeisPerImage) {
                writeImage(dataSeis, event, fileNameBase + fileNameCounter
                        + fileExtension);
                return;
            } else {
                sort(dataSeis);
                DataSetSeismogram[] tempDSS;
                int spacing = dataSeisArrLen / numSeisPerImage;
                int imageCount = bestDisplay ? 1 : spacing;
                for(int i = 0; i < imageCount; i++) {
                    tempDSS = new DataSetSeismogram[numSeisPerImage];
                    int index = i;
                    for(int j = 0; j < numSeisPerImage; j++) {
                        tempDSS[j] = dataSeis[index];
                        index += spacing;
                    }
                    DataSetSeismogram[] filteredSeismograms = filterByMagnitude(tempDSS,
                                                                                event);
                    writeImage(filteredSeismograms, event, fileNameBase
                            + fileNameCounter + fileExtension);
                    fileNameCounter++;
                }
                if(!bestDisplay) {
                    if((dataSeisArrLen % numSeisPerImage) != 0) {
                        int maxIndex = numSeisPerImage * spacing - 1;
                        tempDSS = new DataSetSeismogram[dataSeisArrLen
                                - maxIndex - 1];
                        for(int k = 0; k < dataSeisArrLen - maxIndex - 1; k++) {
                            tempDSS[k] = dataSeis[maxIndex + k + 1];
                        }
                        writeImage(tempDSS, event, fileNameBase
                                + fileNameCounter + fileExtension);
                    }
                }
            }
        }
    }

    private DataSetSeismogram[] filterByMagnitude(DataSetSeismogram[] seismograms,
                                                  EventAccessOperations event) {
        DataSetSeismogram[] tempDSS = new DataSetSeismogram[seismograms.length];
        float magnitude = EventFormatter.getOrigin(event).magnitudes[0].value;
        float maxDistDegrees = 0;
        if(magnitude >= 5) {
            maxDistDegrees = 180;
        } else if(magnitude >= 4 && magnitude < 5) {
            maxDistDegrees = 60;
        } else {
            maxDistDegrees = 30;
        }
        int seisCount = 0;
        for(int i = 0; i < seismograms.length; i++) {
            double distance = DisplayUtils.calculateDistance(seismograms[i])
                    .get_value();
            if(distance <= maxDistDegrees) {
                tempDSS[seisCount++] = seismograms[i];
            }
        }
        return tempDSS;
    }

    private void writeImage(DataSetSeismogram[] dataSeis,
                            EventAccessOperations event,
                            String fileName) throws IOException, NotFound,
            SQLException {
        int recSecId = -1;
        int eventId = eventAccess.getDBId(event);
        String base = Start.getRunProps().getStatusBaseDir() + "/earthquakes";
        String dir = saveSeisToFile.getLabel(event);
        new File(base + "/" + dir).mkdirs();
        String fullName = dir + "/" + fileName;
        if(!eventRecordSection.imageExists(eventId, fullName)) {
            recSecId = eventRecordSection.insert(eventId, fullName);
        } else {
            recSecId = eventRecordSection.getRecSecId(eventId, fullName);
        }
        RecordSectionDisplay rsDisplay = new RecordSectionDisplay();
        rsDisplay.add(dataSeis);
        try {
            File outPNG = new File(base + "/" + fullName);
            rsDisplay.outputToPNG(outPNG, new Dimension(500, 500));
            HashMap seisToPixelMap = rsDisplay.getPixelMap();
            for(int j = 0; j < dataSeis.length; j++) {
                ChannelId channel_id = dataSeis[j].getRequestFilter().channel_id;
                int channelId = channel.getDBId(channel_id);
                double[] pixelInfo = (double[])seisToPixelMap.get(channel_id);
                if(recordSectionChannel.channelExists(eventId, channelId)) {
                    recordSectionChannel.updateRecordSection(recSecId,
                                                             eventId,
                                                             channelId,
                                                             pixelInfo);
                } else {
                    recordSectionChannel.insert(recSecId, channelId, pixelInfo);
                }
            }
        } catch(IOException e) {
            throw new IOException("Problem writing recordSection output to PNG "
                    + e);
        }
    }

    private class RecordSectionGrouping {

        public RecordSectionGrouping(Element el) {
            try {
                magRange = new MagnitudeRange((Element)(el.getElementsByTagName("magnitudeRange")).item(0));
                distRange = new DistanceRange((Element)el.getElementsByTagName("distanceRange")
                        .item(0));
                if(el.getElementsByTagName("individualSeisHeight") != null) {
                    indSeisHeight = new Integer(SodUtil.getText(SodUtil.getElement(el,
                                                                                   "individualSeisHeight"))).intValue();
                }
                if(el.getElementsByTagName("recordSectionSize") != null) {
                    int width = new Integer(SodUtil.getText(SodUtil.getElement(SodUtil.getElement(el,
                                                                                                  "recordSectionSize"),
                                                                               "width"))).intValue();
                    int height = new Integer(SodUtil.getText(SodUtil.getElement(SodUtil.getElement(el,
                                                                                                   "recordSectionSize"),
                                                                                "height"))).intValue();
                    recSecDim = new Dimension(width, height);
                }
            } catch(ConfigurationException ce) {
                GlobalExceptionHandler.handle("Error in the recordsectiondisplaygenerator configuration",
                                              ce);
            }
        }

        public RecordSectionGrouping(MagnitudeRange magRange,
                DistanceRange distRange, int indSeisHeight, Dimension recSecDim) {
            this.magRange = magRange;
            this.distRange = distRange;
            this.indSeisHeight = indSeisHeight;
            this.recSecDim = recSecDim;
        }

        public MagnitudeRange getMagnitudeRange() {
            return magRange;
        }

        public Dimension getRecSecDimension() {
            return recSecDim;
        }

        public int getIndSeisHeight() {
            return indSeisHeight;
        }

        public int getMinDistance() {
            return distRange.minDistance;
        }

        public int getMaxDistance() {
            return distRange.maxDistance;
        }

        public void setRecSecDimension(Dimension recSecDim) {
            this.recSecDim = recSecDim;
        }

        public void setIndSeisHeight(int indSeisHeight) {
            this.indSeisHeight = indSeisHeight;
        }

        private int indSeisHeight = 0;

        private Dimension recSecDim;

        private MagnitudeRange magRange;

        private DistanceRange distRange;
    }

    private class DistanceRange {

        public DistanceRange(Element el) {
            minDistance = new Integer(SodUtil.getText(SodUtil.getElement(el,
                                                                         "min"))).intValue();
            maxDistance = new Integer(SodUtil.getText(SodUtil.getElement(el,
                                                                         "max"))).intValue();
        }

        public DistanceRange(int minDistance, int maxDistance) {
            this.minDistance = minDistance;
            this.maxDistance = maxDistance;
        }

        public int minDistance;

        public int maxDistance;
    }

    private SaveSeismogramToFile saveSeisToFile;

    private String id;

    private int numSeisPerImage = 6;

    private String displayOption = "";

    private JDBCEventRecordSection eventRecordSection;

    private JDBCRecordSectionChannel recordSectionChannel;

    private JDBCEventAccess eventAccess;

    private JDBCChannel channel;

    private String fileNameBase;

    private int indSeisHeight;

    private double minMag = 0;

    private double maxMag = 10;

    private RecordSectionGrouping[] groupings;

    private Dimension recSecDim;

    public static final int DEFAULT_WIDTH = 500;

    public static final int DEFAULT_HEIGHT = 500;

    public static final int DEFAULT_SEIS_HEIGHT = 40;

    private final static String fileExtension = ".png";
}