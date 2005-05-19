package edu.sc.seis.sod.process.waveform;

import java.awt.Dimension;
import java.io.File;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;
import javax.xml.transform.TransformerException;
import org.apache.xpath.XPathAPI;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;
import edu.iris.Fissures.IfEvent.EventAccessOperations;
import edu.iris.Fissures.IfNetwork.Channel;
import edu.iris.Fissures.IfNetwork.ChannelId;
import edu.iris.Fissures.IfSeismogramDC.RequestFilter;
import edu.iris.Fissures.network.ChannelIdUtil;
import edu.iris.Fissures.seismogramDC.LocalSeismogramImpl;
import edu.sc.seis.fissuresUtil.database.ConnMgr;
import edu.sc.seis.fissuresUtil.database.NotFound;
import edu.sc.seis.fissuresUtil.database.event.JDBCEventAccess;
import edu.sc.seis.fissuresUtil.database.network.JDBCChannel;
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
import edu.sc.seis.sod.SodUtil;
import edu.sc.seis.sod.Start;
import edu.sc.seis.sod.database.waveform.JDBCRecordSectionChannel;
import edu.sc.seis.sod.status.StringTreeLeaf;

/**
 * @author danala Created on Mar 30, 2005
 */
public class RSChannelInfoPopulator implements WaveformProcess {

    public RSChannelInfoPopulator(Element config) throws Exception {
        this(config, ConnMgr.createConnection());
    }

    public RSChannelInfoPopulator(Element config, Connection conn)
            throws Exception {
        initConfig(config);
        saveSeisToFile = getSaveSeismogramToFile();
        recordSectionChannel = new JDBCRecordSectionChannel(conn);
        eventAccess = new JDBCEventAccess(recordSectionChannel.getConnection());
        channel = new JDBCChannel();
        internalId = find(config);
    }

    private static int find(Element config) throws TransformerException {
        NodeList nl = XPathAPI.selectNodeList(config, GENS_POPS_XPATH);
        for(int i = 0; i < nl.getLength(); i++) {
            if(nl.item(i).equals(config)) {
                return i;
            }
        }
        throw new RuntimeException("This element doesn't match any nodes returned by "
                + GENS_POPS_XPATH);
    }

    public static final String GENS_POPS_XPATH = "//recordSectionDisplayGenerator | //RSChannelInfoPopulator | //externalWaveformProcess[classname/text() = \"edu.sc.seis.rev.map.RecordSectionAndMapGenerator\"]";

    private void initConfig(Element config) throws NoSuchFieldException {
        id = SodUtil.getText(SodUtil.getElement(config, "id"));
        saveSeisId = DOMHelper.extractText(config, "saveSeisId", id);
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
        if(DOMHelper.hasElement(config, "recordSectionSize")) {
            int width = new Integer(SodUtil.getText(SodUtil.getElement(SodUtil.getElement(config,
                                                                                          "recordSectionSize"),
                                                                       "width"))).intValue();
            int height = new Integer(SodUtil.getText(SodUtil.getElement(SodUtil.getElement(config,
                                                                                           "recordSectionSize"),
                                                                        "height"))).intValue();
            recSecDim = new Dimension(width, height);
        }
        spacer = new RecordSectionSpacer(distRange,
                                         idealNumberOfSeismograms,
                                         maxNumberOfSeismograms);
        if(DOMHelper.hasElement(config, "displayConfig")) {
            displayCreator = SeismogramDisplayConfiguration.create(DOMHelper.getElement(config,
                                                                                        "displayConfig"));
        }
    }

    public Dimension getRecSecDimension() {
        return recSecDim;
    }

    public JDBCRecordSectionChannel getRSChannel() {
        return recordSectionChannel;
    }

    public SaveSeismogramToFile getSaveSeismogramToFile() throws Exception {
        return getSaveSeismogramToFile(saveSeisId);
    }

    public SaveSeismogramToFile getFirstSaveSeismogramToFile() throws Exception {
        return extractSaveSeis("(//saveSeismogramToFile)[1]",
                               "No SaveSeismogramToFile found");
    }

    public static SaveSeismogramToFile getSaveSeismogramToFile(String saveId)
            throws Exception {
        String xpath = "//saveSeismogramToFile[id/text() = \"" + saveId + "\"]";
        return extractSaveSeis(xpath,
                               "No SaveSeismogramToFile element with id "
                                       + saveId + " found");
    }

    private static SaveSeismogramToFile extractSaveSeis(String xpath,
                                                        String errorMsgIfNotFound)
            throws ConfigurationException {
        Element saveSeisConf = DOMHelper.extractElement(Start.getConfig(),
                                                        xpath);
        if(saveSeisConf != null) {
            return new SaveSeismogramToFile(saveSeisConf);
        } else {
            throw new ConfigurationException(errorMsgIfNotFound);
        }
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

    public WaveformResult process(EventAccessOperations event,
                                  Channel chan,
                                  RequestFilter[] original,
                                  RequestFilter[] available,
                                  LocalSeismogramImpl[] seismograms,
                                  CookieJar cookieJar) throws Exception {
        acceptableChannels.add(ChannelIdUtil.toString(chan.get_id()));
        int eq_dbid = eventAccess.getDBId(event);
        DataSetSeismogram[] dss = extractSeismograms(event);
        ArrayList acceptableSeis = new ArrayList();
        for(int i = 0; i < dss.length; i++) {
            if(acceptableChannels.contains(ChannelIdUtil.toString(dss[i].getChannelId()))) {
                acceptableSeis.add(dss[i]);
            }
        }
        dss = (DataSetSeismogram[])acceptableSeis.toArray(new DataSetSeismogram[0]);
//        RecordSectionDisplay rsDisplay = getConfiguredRSDisplay();
//        rsDisplay.add(wrap(dss));
//        File temp = File.createTempFile("tempRecSec", "png");
//        rsDisplay.outputToPNG(temp);
//        temp.delete();
//        HashMap pixelMap = rsDisplay.getPixelMap();
        int[] channelIds = getChannelDBIds(dss);
        for(int j = 0; j < channelIds.length; j++) {
            if(!recordSectionChannel.channelExists(id, eq_dbid, channelIds[j])) {
                recordSectionChannel.insert(id,
                                            eq_dbid,
                                            channelIds[j],
                                            new double[]{0,0,0,0},//(double[])pixelMap.get(dss[j].getRequestFilter().channel_id),
                                            0,
                                            internalId);
            }
        }
        DataSetSeismogram[] bestSeismos = spacer.spaceOut(dss);
        recordSectionChannel.updateChannels(id,
                                            eq_dbid,
                                            getChannelDBIds(bestSeismos),
                                            internalId);
        return new WaveformResult(seismograms, new StringTreeLeaf(this, true));
    }

    public int[] getChannelDBIds(DataSetSeismogram[] dss) throws SQLException,
            NotFound {
        int[] channelIds = new int[dss.length];
        for(int j = 0; j < dss.length; j++) {
            ChannelId channel_id = dss[j].getRequestFilter().channel_id;
            channelIds[j] = channel.getDBId(channel_id);
        }
        return channelIds;
    }

    public DataSetSeismogram[] extractSeismograms(EventAccessOperations eao)
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

    private JDBCRecordSectionChannel recordSectionChannel;

    private JDBCEventAccess eventAccess;

    private JDBCChannel channel;

    private double percentSeisHeight = 10;

    private Dimension recSecDim = new Dimension(500, 500);

    protected RecordSectionSpacer spacer;

    private SeismogramDisplayConfiguration displayCreator;

    private Set acceptableChannels = new HashSet();

    private int internalId;
}