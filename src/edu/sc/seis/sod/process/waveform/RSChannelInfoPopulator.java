package edu.sc.seis.sod.process.waveform;

/**
 * @author danala Created on Mar 30, 2005
 */
import java.awt.Color;
import java.awt.Dimension;
import java.io.File;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.HashMap;
import org.apache.log4j.Category;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;
import edu.iris.Fissures.IfEvent.EventAccessOperations;
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
import edu.sc.seis.fissuresUtil.xml.DataSet;
import edu.sc.seis.fissuresUtil.xml.DataSetSeismogram;
import edu.sc.seis.fissuresUtil.xml.DataSetToXML;
import edu.sc.seis.fissuresUtil.xml.MemoryDataSetSeismogram;
import edu.sc.seis.fissuresUtil.xml.URLDataSetSeismogram;
import edu.sc.seis.sod.ConfigurationException;
import edu.sc.seis.sod.MotionVectorArm;
import edu.sc.seis.sod.Start;
import edu.sc.seis.sod.CookieJar;
import edu.sc.seis.sod.SodUtil;
import edu.sc.seis.sod.WaveformArm;
import edu.sc.seis.sod.database.waveform.JDBCRecordSectionChannel;
import edu.sc.seis.sod.status.StringTreeLeaf;

/**
 * @author danala Created on Mar 30, 2005
 */
public class RSChannelInfoPopulator implements WaveformProcess {

    public RSChannelInfoPopulator(Element config) throws Exception {
        id = SodUtil.getText(SodUtil.getElement(config, "id"));
        initConfig(config);
        recordSectionChannel = new JDBCRecordSectionChannel();
        eventAccess = new JDBCEventAccess();
        channelTable = new JDBCChannel();
    }

    private void initConfig(Element config) throws NoSuchFieldException {
        id = SodUtil.getText(SodUtil.getElement(config, "id"));
        if(DOMHelper.hasElement(config, "fileNameBase")) {
            fileNameBase = SodUtil.getText(SodUtil.getElement(config,
                                                              "fileNameBase"));
        }
        if(DOMHelper.hasElement(config, "numSeisPerRecordSection")) {
            numSeisPerRecSec = new Integer(SodUtil.getText(SodUtil.getElement(config,
                                                                              "numSeisPerRecordSection"))).intValue();
        }
        if(DOMHelper.hasElement(config, "percentSeisHeight")) {
            percentSeisHeight = new Double(SodUtil.getText(SodUtil.getElement(config,
                                                                              "percentSeisHeight"))).doubleValue();
        }
        if(DOMHelper.hasElement(config, "minimumSpacing")) {
            minSpacing = new Double(SodUtil.getText(SodUtil.getElement(config,
                                                                       "minimumSpacing"))).doubleValue();
        }
        if(DOMHelper.hasElement(config, "recordSectionSize")) {
            int width = new Integer(SodUtil.getText(SodUtil.getElement(SodUtil.getElement(config,
                                                                                          "recordSectionSize"),
                                                                       "width"))).intValue();
            int height = new Integer(SodUtil.getText(SodUtil.getElement(SodUtil.getElement(config,
                                                                                           "recordSectionSize"),
                                                                        "height"))).intValue();
            recSecDim = new Dimension(width, height);
        }
        if(DOMHelper.hasElement(config, "distanceRange")) {
            distRange = new DistanceRange(SodUtil.getElement(config,
                                                             "distanceRange"));
        }
        colors = extractColors(config);
        if(DOMHelper.hasElement(config, "displayConfig")) {
            displayCreator = SeismogramDisplayConfiguration.create(DOMHelper.getElement(config,
                                                                                        "displayConfig"));
        }
    }

    public String getId() {
        return id;
    }

    public String getFileNameBase(){
        return fileNameBase;
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
        throw new ConfigurationException("RSChannelInfoPopulator needs a SaveSeismogramToFile process");
    }

    public MemoryDataSetSeismogram[] wrap(DataSetSeismogram[] dss, DataSet ds)
            throws Exception {
        MemoryDataSetSeismogram[] memDss = new MemoryDataSetSeismogram[dss.length];
        for(int i = 0; i < memDss.length; i++) {
            memDss[i] = new MemoryDataSetSeismogram(((URLDataSetSeismogram)dss[i]).getSeismograms(),
                                                    ds,
                                                    dss[i].getName());
        }
        return memDss;
    }

    public double getIdealSpacing() {
        return getRange() / (numSeisPerRecSec - 1);
    }

    public double getRange() {
        return distRange.getMaxDistance() - distRange.getMinDistance();
    }

    public DataSetSeismogram[] getBestSeismos(DataSetSeismogram[] dataSeis) {
        if(dataSeis.length > 0) {
            sortByDistance(dataSeis);
            HashMap distMap = new HashMap();
            for(int i = 0; i < dataSeis.length; i++) {
                QuantityImpl distance = DisplayUtils.calculateDistance(dataSeis[i]);
                distMap.put(dataSeis[i], distance);
            }
            ArrayList dssList = new ArrayList();
            if(dataSeis.length == 1) {
                dssList.add(dataSeis[0]);
            } else {
                dssList.add(dataSeis[0]);
                double curDist = ((QuantityImpl)distMap.get(dataSeis[0])).get_value();
                double spacing = getIdealSpacing();
                while(curDist <= getRange()) {
                    DataSetSeismogram curSeis = getNext(dataSeis,
                                                        curDist,
                                                        distMap);
                    if(curSeis != null) {
                        if(!dssList.contains(curSeis)) {
                            dssList.add(curSeis);
                        }
                        curDist = ((QuantityImpl)distMap.get(curSeis)).get_value();
                    } else {
                        curDist += spacing;
                    }
                }
                //Add the farthest seismogram but remove any seismogram already
                // added that was closer than minSpacing.
                DataSetSeismogram lastAddedSeis = (DataSetSeismogram)dssList.get(dssList.size() - 1);
                DataSetSeismogram lastSeis = dataSeis[dataSeis.length - 1];
                boolean addLastSeis = (((QuantityImpl)distMap.get(lastSeis)).get_value()
                        - ((QuantityImpl)distMap.get(lastAddedSeis)).get_value() >= minSpacing) ? true
                        : false;
                if(!addLastSeis) {
                    dssList.remove(dssList.size() - 1);
                }
                dssList.add(lastSeis);
            }
            DataSetSeismogram[] bestSeismos = new DataSetSeismogram[dssList.size()];
            bestSeismos = (DataSetSeismogram[])dssList.toArray(bestSeismos);
            return bestSeismos;
        }
        return null;
    }

    private DataSetSeismogram getNext(DataSetSeismogram[] seismos,
                                      double curDist,
                                      HashMap distMap) {
        double spacing = getIdealSpacing();
        for(int i = 0; i < seismos.length; i++) {
            double distance = ((QuantityImpl)distMap.get(seismos[i])).get_value();
            if(distance >= (curDist + minSpacing)
                    && (distance <= curDist + spacing)) { return seismos[i]; }
        }
        return null;
    }

    public WaveformResult process(EventAccessOperations event,
                                  Channel channel,
                                  RequestFilter[] original,
                                  RequestFilter[] available,
                                  LocalSeismogramImpl[] seismograms,
                                  CookieJar cookieJar) throws Exception {
        saveSeisToFile = getSaveSeismogramToFile();
        int eq_dbid = eventAccess.getDBId(event);
        DataSetSeismogram[] dss = loadSeismograms(event);
        RecordSectionDisplay rsDisplay = getConfiguredRSDisplay();
        rsDisplay.add(dss);
        File temp = File.createTempFile("tempRecSec", "png");
        rsDisplay.outputToPNG(temp);
        temp.delete();
        HashMap pixelMap = rsDisplay.getPixelMap();
        int[] channelIds = getChannelDBIds(dss);
        for(int j = 0; j < channelIds.length; j++) {
            if(!recordSectionChannel.channelExists(id, eq_dbid, channelIds[j])) {
                recordSectionChannel.insert(id,
                                            eq_dbid,
                                            channelIds[j],
                                            (double[])pixelMap.get(dss[j].getRequestFilter().channel_id),
                                            0);
            }
        }
        DataSetSeismogram[] bestSeismos = getBestSeismos(dss);
        recordSectionChannel.updateChannels(id,
                                            eq_dbid,
                                            getChannelDBIds(bestSeismos));
        return new WaveformResult(seismograms, new StringTreeLeaf(this, true));
    }

    public int[] getChannelDBIds(DataSetSeismogram[] dss) throws SQLException,
            NotFound {
        int[] channelIds = new int[dss.length];
        for(int j = 0; j < dss.length; j++) {
            ChannelId channel_id = dss[j].getRequestFilter().channel_id;
            channelIds[j] = channelTable.getDBId(channel_id);
        }
        return channelIds;
    }

    public DataSetSeismogram[] loadSeismograms(EventAccessOperations eao)
            throws Exception {
        DataSet ds = DataSetToXML.load(saveSeisToFile.getDSMLFile(eao)
                .toURI()
                .toURL());
        String[] dataSeisNames = ds.getDataSetSeismogramNames();
        DataSetSeismogram[] dss = new DataSetSeismogram[dataSeisNames.length];
        for(int i = 0; i < dataSeisNames.length; i++) {
            dss[i] = ds.getDataSetSeismogram(dataSeisNames[i]);
        }
        return dss;
    }

    public RecordSectionDisplay getConfiguredRSDisplay() {
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
                                                               distRange.getMaxDistance(),
                                                               percentSeisHeight);
        custConfig.setSwapAxes(rsDisplay.getSwapAxes());
        rsDisplay.setLayout(custConfig);
        return rsDisplay;
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

    private DistanceRange distRange = new DistanceRange(0, 180);

    private Color[] colors;

    private int numSeisPerRecSec = 10;

    private JDBCRecordSectionChannel recordSectionChannel;

    private JDBCEventAccess eventAccess;

    private JDBCChannel channelTable;

    private double percentSeisHeight = 10;

    static Category logger = Category.getInstance(RSChannelInfoPopulator.class.getName());

    private double minSpacing = 10;

    private SeismogramDisplayConfiguration displayCreator = null;

    private Dimension recSecDim = new Dimension(500, 500);

    private String fileNameBase = "recordsection";

    public final static String fileExtension = ".png";
}