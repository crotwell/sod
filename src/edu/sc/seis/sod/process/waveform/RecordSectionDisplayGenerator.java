package edu.sc.seis.sod.process.waveform;

import java.awt.Color;
import java.awt.Dimension;
import java.io.File;
import java.io.IOException;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
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
import edu.sc.seis.fissuresUtil.display.BasicSeismogramDisplay;
import edu.sc.seis.fissuresUtil.display.DisplayUtils;
import edu.sc.seis.fissuresUtil.display.RecordSectionDisplay;
import edu.sc.seis.fissuresUtil.display.SeismogramDisplay;
import edu.sc.seis.fissuresUtil.display.borders.DistanceBorder;
import edu.sc.seis.fissuresUtil.display.borders.TimeBorder;
import edu.sc.seis.fissuresUtil.display.configuration.DOMHelper;
import edu.sc.seis.fissuresUtil.display.configuration.SeismogramDisplayConfiguration;
import edu.sc.seis.fissuresUtil.display.registrar.CustomLayOutConfig;
import edu.sc.seis.fissuresUtil.display.registrar.IndividualizedAmpConfig;
import edu.sc.seis.fissuresUtil.display.registrar.RMeanAmpConfig;
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

    private void initConfig(Element config) {
        id = SodUtil.getText(SodUtil.getElement(config, "id"));
        displayOption = SodUtil.getText(SodUtil.getElement(config,
                                                           "displayOption"));
        fileNameBase = SodUtil.getText(SodUtil.getElement(config,
                                                          "fileNameBase"));
        if(SodUtil.getElement(config, "numSeisPerImage") != null) {
            numSeisPerImage = new Integer(SodUtil.getText(SodUtil.getElement(config,
                                                                             "numSeisPerRecordSection"))).intValue();
        } else {
            numSeisPerImage = DEFAULT_SEIS_PER_IMAGE;
        }
        SodUtil.getElement(config, "individualSeisHeight");
        if(SodUtil.getElement(config, "individualSeisHeight") != null) {
            indSeisHeight = new Integer(SodUtil.getText(SodUtil.getElement(config,
                                                                           "individualSeisHeight"))).intValue();
        } else {
            indSeisHeight = DEFAULT_SEIS_HEIGHT;
        }
        if(SodUtil.getElement(config, "minimumSpacing") != null) {
            minSpacing = new Double(SodUtil.getText(SodUtil.getElement(config,
                                                                       "minimumSpacing"))).doubleValue();
        } else {
            minSpacing = MIN_SPACING;
        }
        NodeList groups = config.getElementsByTagName("recordSectionGrouping");
        if(SodUtil.getElement(config, "recordSectionSize") != null) {
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
        if(groups.getLength() != 0) {
            groupings = new RecordSectionGrouping[groups.getLength()];
            for(int i = 0; i < groups.getLength(); i++) {
                groupings[i] = new RecordSectionGrouping((Element)groups.item(i));
                if(groupings[i].getIndSeisHeight() == 0) {
                    groupings[i].setIndSeisHeight(indSeisHeight);
                }
                if(groupings[i].getRecSecDimension() == null) {
                    groupings[i].setRecSecDimension(recSecDim);
                }
                if(groupings[i].getMinSpacing() == 0) {
                    groupings[i].setMinSpacing(minSpacing);
                }
            }
        } else {
            groupings = new RecordSectionGrouping[] {new RecordSectionGrouping(new MagnitudeRange(),
                                                                               new DistanceRange(0,
                                                                                                 180),
                                                                               indSeisHeight,
                                                                               minSpacing,
                                                                               new Dimension(500,
                                                                                             500))};
        }
        colors = extractColors(config);
        if(DOMHelper.hasElement(config, "displayConfig")) {
            displayCreator = SeismogramDisplayConfiguration.create(DOMHelper.getElement(config,
                                                                                        "displayConfig"));
        }
    }

    public static Color[] extractColors(Element config) {
        Element el = SodUtil.getElement(config, "colors");
        if(el == null) { return SeismogramDisplay.COLORS; }
        NodeList colors = el.getElementsByTagName("color");
        Color[] colorSeq = new Color[colors.getLength()];
        for(int i = 0; i < colors.getLength(); i++) {
            String colorStr = "0X" + SodUtil.getText((Element)colors.item(i));
            colorSeq[i] = Color.decode(colorStr);
        }
        return colorSeq;
    }

    public SaveSeismogramToFile getSaveSeismogramToFile()
            throws ConfigurationException {
        return getSaveSeismogramToFile(Start.getWaveformArm(), id);
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
                                  CookieJar cookieJar)
            throws ParserConfigurationException, IOException,
            IncomprehensibleDSMLException, UnsupportedFileTypeException,
            ConfigurationException, NotFound, SQLException,
            InterruptedException {
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
            sortByDistance(dss);
            if(displayOption.equals("BEST")) {
                outputBestRecordSections(event, selectedGroup, dss);
            } else {
                outputAllRecordSections(event, dss);
            }
        } catch(IOException e) {
            throw new IOException("Problem opening dsml file in RecordSectionDisplayGenerator"
                    + e);
        }
        return new WaveformResult(seismograms, new StringTreeLeaf(this, true));
    }

    public void outputBestRecordSections(EventAccessOperations event,
                                         RecordSectionGrouping group,
                                         DataSetSeismogram[] dataSeis)
            throws IOException, NotFound, SQLException {
        DistanceRange distRange = group.getDistanceRange();
        double minSpacing = group.getMinSpacing();
        double range = (distRange.getMaxDistance() - distRange.getMinDistance());
        double spacing = (range) / (numSeisPerImage - 1);
        ArrayList dssList = new ArrayList();
        if(dataSeis.length > 0) {
            DataSetSeismogram lastFrontSeis = dataSeis[0];
            DataSetSeismogram lastSeis = dataSeis[dataSeis.length - 1];
            DataSetSeismogram lastBackSeis = lastSeis;
            double curFrontDistance = DisplayUtils.calculateDistance(lastFrontSeis)
                    .get_value();
            double lastSeisDist = DisplayUtils.calculateDistance(lastBackSeis)
                    .get_value();
            DataSetSeismogram curSeis = null;
            double firstSeisDistance = curFrontDistance;
            double curBackDistance = lastSeisDist;
            dssList.add(lastFrontSeis);
            while(curFrontDistance <= range / 2) {
                curSeis = getBestSeis(dataSeis,
                                      lastFrontSeis,
                                      curFrontDistance,
                                      spacing,
                                      minSpacing);
                if(curSeis != null) {
                    dssList.add(curSeis);
                    curFrontDistance = DisplayUtils.calculateDistance(curSeis)
                            .get_value();
                    lastFrontSeis = curSeis;
                }
                curFrontDistance += spacing;
            }
            while(curBackDistance > range / 2) {
                curSeis = getBestSeis(dataSeis,
                                      lastBackSeis,
                                      curBackDistance,
                                      spacing,
                                      minSpacing);
                if(curSeis != null) {
                    dssList.add(curSeis);
                    curBackDistance = DisplayUtils.calculateDistance(curSeis)
                            .get_value();
                    lastBackSeis = curSeis;
                }
                curBackDistance -= spacing;
            }
            double lastFrontSeisDist = DisplayUtils.calculateDistance(lastFrontSeis)
                    .get_value();
            double lastBackSeisDist = DisplayUtils.calculateDistance(lastBackSeis)
                    .get_value();
            if(Math.abs(lastFrontSeisDist - lastBackSeisDist) < minSpacing) {
                dssList.remove(dssList.size() - 1);
            }
            if(Math.abs(firstSeisDistance - lastSeisDist) >= minSpacing) {
                dssList.add(lastSeis);
            }
            DataSetSeismogram[] tempDSS = new DataSetSeismogram[dssList.size()];
            tempDSS = (DataSetSeismogram[])dssList.toArray(tempDSS);
            writeImage(tempDSS,
                       event,
                       fileNameBase + fileExtension,
                       distRange,
                       group.getRecSecDimension());
        }
    }

    public DataSetSeismogram getBestSeis(DataSetSeismogram[] dataSeis,
                                         DataSetSeismogram lastSeis,
                                         double curDistance,
                                         double idealSpacing,
                                         double minSpacing) {
        double maxDist = idealSpacing / 2;
        double upperDistBound = curDistance + maxDist;
        double lastSeisDist = DisplayUtils.calculateDistance(lastSeis)
                .get_value();
        DataSetSeismogram bestSeis = null;
        for(int i = 0; i < dataSeis.length; i++) {
            double seisDistance = DisplayUtils.calculateDistance((dataSeis[i]))
                    .get_value();
            double distanceBetween = Math.abs(curDistance - seisDistance);
            //include the seismogram only if it is exactly in the center or the
            //upper half of the distance interval window,
            //to make sure that there is no overlap in the interval.
            if((distanceBetween < maxDist || (distanceBetween == maxDist && seisDistance < curDistance))
                    && (Math.abs(seisDistance - lastSeisDist) >= minSpacing)) {
                bestSeis = dataSeis[i];
                maxDist = distanceBetween;
            }
            if(seisDistance > upperDistBound) {
                break;
            }
        }
        return bestSeis;
    }

    public void outputAllRecordSections(EventAccessOperations event,
                                        DataSetSeismogram[] dataSeis)
            throws IOException, SQLException, NotFound {
        int dataSeisArrLen = dataSeis.length;
        DistanceRange distRange = new DistanceRange(0, 180);
        if(dataSeisArrLen > 0) {
            int fileNameCounter = 0;
            if(dataSeisArrLen <= numSeisPerImage) {
                writeImage(dataSeis, event, fileNameBase + fileNameCounter
                        + fileExtension, distRange, recSecDim);
                return;
            } else {
                DataSetSeismogram[] tempDSS;
                int spacing = dataSeisArrLen / numSeisPerImage;
                for(int i = 0; i < spacing; i++) {
                    tempDSS = new DataSetSeismogram[numSeisPerImage];
                    int index = i;
                    for(int j = 0; j < numSeisPerImage; j++) {
                        tempDSS[j] = dataSeis[index];
                        index += spacing;
                    }
                    writeImage(tempDSS, event, fileNameBase + fileNameCounter
                            + fileExtension, distRange, recSecDim);
                    fileNameCounter++;
                }
                if((dataSeisArrLen % numSeisPerImage) != 0) {
                    int maxIndex = numSeisPerImage * spacing - 1;
                    tempDSS = new DataSetSeismogram[dataSeisArrLen - maxIndex
                            - 1];
                    for(int k = 0; k < dataSeisArrLen - maxIndex - 1; k++) {
                        tempDSS[k] = dataSeis[maxIndex + k + 1];
                    }
                    writeImage(tempDSS, event, fileNameBase + fileNameCounter
                            + fileExtension, distRange, recSecDim);
                }
            }
        }
    }

    private void writeImage(DataSetSeismogram[] dataSeis,
                            EventAccessOperations event,
                            String fileName,
                            DistanceRange distRange,
                            Dimension recordSecDim) throws IOException,
            NotFound, SQLException {
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
        RecordSectionDisplay rsDisplay;
        if(displayCreator == null) {
            rsDisplay = new RecordSectionDisplay(true);
            DistanceBorder distBorder = new DistanceBorder(rsDisplay,
                                                           DistanceBorder.BOTTOM,
                                                           DistanceBorder.ASCENDING);
            distBorder.setPreferredSize(new Dimension(BasicSeismogramDisplay.PREFERRED_WIDTH,
                                                      50));
            TimeBorder timeBorder = new TimeBorder(rsDisplay,
                                                   TimeBorder.ASCENDING);
            timeBorder.setPreferredSize(new Dimension(80, 50));
            rsDisplay.setDistBorder(distBorder,
                                    RecordSectionDisplay.BOTTOM_CENTER);
            rsDisplay.setTimeBorder(timeBorder,
                                    RecordSectionDisplay.CENTER_LEFT);
            rsDisplay.setAmpConfig(new IndividualizedAmpConfig(new RMeanAmpConfig()));
            rsDisplay.setColors(colors);
        } else {
            rsDisplay = (RecordSectionDisplay)displayCreator.createDisplay();
        }
        CustomLayOutConfig custConfig = new CustomLayOutConfig(distRange.getMinDistance(),
                                                               distRange.getMaxDistance());
        boolean swapAxes = false;
        if(rsDisplay.getSwapAxes()){
            swapAxes = true;
        }
        custConfig.setSwapAxes(swapAxes);
        rsDisplay.setLayout(custConfig);
        rsDisplay.add(dataSeis);
        try {
            File outPNG = new File(base + "/" + fullName);
            rsDisplay.outputToPNG(outPNG, recordSecDim);
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

    public static void sortByDistance(DataSetSeismogram[] seismograms) {
        Arrays.sort(seismograms, new Comparator() {

            public int compare(Object o1, Object o2) {
                DataSetSeismogram obj1 = (DataSetSeismogram)o1;
                DataSetSeismogram obj2 = (DataSetSeismogram)o2;
                QuantityImpl dist1 = DisplayUtils.calculateDistance(obj1);
                QuantityImpl dist2 = DisplayUtils.calculateDistance(obj2);
                if(dist1.lessThan(dist2)) {
                    return -1;
                } else if(dist1.greaterThan(dist2)) {
                    return 1;
                } else {
                    return 0;
                }
            }
        });
    }

    private class RecordSectionGrouping {

        public RecordSectionGrouping(Element el) {
            try {
                magRange = new MagnitudeRange((Element)(el.getElementsByTagName("magnitudeRange")).item(0));
                distRange = new DistanceRange((Element)el.getElementsByTagName("distanceRange")
                        .item(0));
                if(SodUtil.getElement(el, "individualSeisHeight") != null) {
                    indSeisHeight = new Integer(SodUtil.getText(SodUtil.getElement(el,
                                                                                   "individualSeisHeight"))).intValue();
                }
                if(SodUtil.getElement(el, "minimumSpacing") != null) {
                    indSeisHeight = new Integer(SodUtil.getText(SodUtil.getElement(el,
                                                                                   "minimumSpacing"))).intValue();
                }
                if(SodUtil.getElement(el, "recordSectionSize") != null) {
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
                DistanceRange distRange, int indSeisHeight, double minSpacing,
                Dimension recSecDim) {
            this.magRange = magRange;
            this.distRange = distRange;
            this.indSeisHeight = indSeisHeight;
            this.minSpacing = minSpacing;
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

        public double getMinSpacing() {
            return minSpacing;
        }

        public DistanceRange getDistanceRange() {
            return distRange;
        }

        public void setRecSecDimension(Dimension recSecDim) {
            this.recSecDim = recSecDim;
        }

        public void setIndSeisHeight(int indSeisHeight) {
            this.indSeisHeight = indSeisHeight;
        }

        public void setMinSpacing(double minSpacing) {
            this.minSpacing = minSpacing;
        }

        private int indSeisHeight = 0;

        private double minSpacing = 0;

        private Dimension recSecDim;

        private MagnitudeRange magRange;

        private DistanceRange distRange;
    }

    private class DistanceRange {

        public DistanceRange(Element el) {
            minDistance = new Double(SodUtil.getText(SodUtil.getElement(el,
                                                                        "min"))).doubleValue();
            maxDistance = new Double(SodUtil.getText(SodUtil.getElement(el,
                                                                        "max"))).doubleValue();
        }

        public DistanceRange(double minDistance, double maxDistance) {
            this.minDistance = minDistance;
            this.maxDistance = maxDistance;
        }

        public double getMinDistance() {
            return minDistance;
        }

        public double getMaxDistance() {
            return maxDistance;
        }

        private double minDistance;

        private double maxDistance;
    }

    private SaveSeismogramToFile saveSeisToFile;

    private String id;

    private Color[] colors;

    private int numSeisPerImage;

    private String displayOption = "";

    private JDBCEventRecordSection eventRecordSection;

    private JDBCRecordSectionChannel recordSectionChannel;

    private JDBCEventAccess eventAccess;

    private JDBCChannel channel;

    private String fileNameBase;

    private int indSeisHeight;

    private double minSpacing;

    private RecordSectionGrouping[] groupings;

    private SeismogramDisplayConfiguration displayCreator = null;

    private Dimension recSecDim;

    public static final int MIN_SPACING = 10;

    public static final int DEFAULT_WIDTH = 500;

    public static final int DEFAULT_HEIGHT = 500;

    public static final int DEFAULT_SEIS_HEIGHT = 40;

    public static final int DEFAULT_SEIS_PER_IMAGE = 10;

    private final static String fileExtension = ".png";
}