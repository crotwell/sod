package edu.sc.seis.sod.process.waveform;

import java.awt.Dimension;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import javax.swing.text.AbstractWriter;

import org.w3c.dom.Element;

import edu.iris.Fissures.AuditInfo;
import edu.iris.Fissures.IfNetwork.ChannelId;
import edu.iris.Fissures.IfSeismogramDC.RequestFilter;
import edu.iris.Fissures.model.MicroSecondDate;
import edu.iris.Fissures.network.ChannelIdUtil;
import edu.iris.Fissures.network.ChannelImpl;
import edu.iris.Fissures.seismogramDC.LocalSeismogramImpl;
import edu.sc.seis.fissuresUtil.cache.CacheEvent;
import edu.sc.seis.fissuresUtil.database.NotFound;
import edu.sc.seis.fissuresUtil.dataset.DataSetEventOrganizer;
import edu.sc.seis.fissuresUtil.display.RecordSectionDisplay;
import edu.sc.seis.fissuresUtil.display.configuration.DOMHelper;
import edu.sc.seis.fissuresUtil.display.configuration.SeismogramDisplayConfiguration;
import edu.sc.seis.fissuresUtil.display.registrar.CustomLayOutConfig;
import edu.sc.seis.fissuresUtil.hibernate.EventSeismogramFileReference;
import edu.sc.seis.fissuresUtil.hibernate.NetworkDB;
import edu.sc.seis.fissuresUtil.hibernate.SeismogramFileRefDB;
import edu.sc.seis.fissuresUtil.time.ReduceTool;
import edu.sc.seis.fissuresUtil.xml.DataSet;
import edu.sc.seis.fissuresUtil.xml.DataSetSeismogram;
import edu.sc.seis.fissuresUtil.xml.DataSetToXML;
import edu.sc.seis.fissuresUtil.xml.MemoryDataSet;
import edu.sc.seis.fissuresUtil.xml.MemoryDataSetSeismogram;
import edu.sc.seis.fissuresUtil.xml.StdDataSetParamNames;
import edu.sc.seis.fissuresUtil.xml.URLDataSetSeismogram;
import edu.sc.seis.sod.ConfigurationException;
import edu.sc.seis.sod.CookieJar;
import edu.sc.seis.sod.LocalSeismogramArm;
import edu.sc.seis.sod.MotionVectorArm;
import edu.sc.seis.sod.SodUtil;
import edu.sc.seis.sod.Start;
import edu.sc.seis.sod.hibernate.RecordSectionItem;
import edu.sc.seis.sod.hibernate.SodDB;
import edu.sc.seis.sod.status.StringTreeLeaf;
import edu.sc.seis.sod.subsetter.eventChannel.EventChannelSubsetter;
import edu.sc.seis.sod.subsetter.eventChannel.PassEventChannel;
import edu.sc.seis.sod.subsetter.requestGenerator.vector.RequestGeneratorWrapper;
import edu.sc.seis.sod.subsetter.requestGenerator.vector.VectorRequestGenerator;

public class RSChannelInfoPopulator implements WaveformProcess {

    public RSChannelInfoPopulator(Element config) throws Exception {
        initConfig(config);
        saveSeisToFile = getSeismogramWriter(saveSeisId);
    }

    public static final String GENS_POPS_XPATH = "//recordSectionDisplayGenerator | //RSChannelInfoPopulator | //externalWaveformProcess[classname/text() = \"edu.sc.seis.rev.map.RecordSectionAndMapGenerator\"]";

    private void initConfig(Element config) throws NoSuchFieldException,
            ConfigurationException {
        orientationId = SodUtil.getText(SodUtil.getElement(config, "orientationId"));
        recordSectionId = SodUtil.getText(SodUtil.getElement(config, "recordSectionId"));
        saveSeisId = DOMHelper.extractText(config, "writerName", orientationId);
        if(DOMHelper.hasElement(config, "embeddedEventChannelProcessor")) {
            channelAcceptor = (EventChannelSubsetter)SodUtil.load(SodUtil.getFirstEmbeddedElement(SodUtil.getElement(config, "embeddedEventChannelProcessor")),
            "eventChannel");
        } else {
            channelAcceptor = new PassEventChannel();
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
        if(DOMHelper.hasElement(config, "recordSectionSize")) {
            int width = new Integer(SodUtil.getText(SodUtil.getElement(SodUtil.getElement(config,
                                                                                          "recordSectionSize"),
                                                                       "width"))).intValue();
            int height = new Integer(SodUtil.getText(SodUtil.getElement(SodUtil.getElement(config,
                                                                                           "recordSectionSize"),
                                                                        "height"))).intValue();
            recSecDim = new Dimension(width, height);
        }
        if(distRange != null) {
            spacer = new RecordSectionSpacer(distRange,
                                             idealNumberOfSeismograms,
                                             maxNumberOfSeismograms);
        }
        if(DOMHelper.hasElement(config, "displayConfig")) {
            displayCreator = SeismogramDisplayConfiguration.create(DOMHelper.getElement(config,
                                                                                        "displayConfig"));
        }
    }

    public Dimension getRecSecDimension() {
        return recSecDim;
    }

    public AbstractSeismogramWriter getSeismogramWriter() throws Exception {
        return saveSeisToFile;
    }

    public static AbstractSeismogramWriter getSeismogramWriter(String saveId)
            throws Exception {
        String xpath = "//mseedWriter[name/text() = \"" + saveId + "\"] | "+
                       "//sacWriter[name/text() = \"" + saveId + "\"]";
        return extractSaveSeis(xpath,
                               "No Writer element with writerName "
                                       + saveId + " found");
    }

    private static AbstractSeismogramWriter extractSaveSeis(String xpath,
                                                        String errorMsgIfNotFound)
            throws ConfigurationException {
        Element saveSeisConf = DOMHelper.extractElement(Start.getConfig(),
                                                        xpath);
        if(saveSeisConf == null) {
            throw new ConfigurationException(errorMsgIfNotFound);
        }
        return (AbstractSeismogramWriter) SodUtil.load(saveSeisConf, "waveform");
    }

    public MemoryDataSetSeismogram[] wrap(DataSetSeismogram[] dss)
            throws Exception {
        MemoryDataSetSeismogram[] memDss = new MemoryDataSetSeismogram[dss.length];
        for(int i = 0; i < memDss.length; i++) {
            memDss[i] = new MemoryDataSetSeismogram(((URLDataSetSeismogram)dss[i]).getSeismograms(),
                                                    dss[i].getDataSet(),
                                                    dss[i].getName(),
                                                    dss[i].getRequestFilter());
        }
        return memDss;
    }

    public WaveformResult process(CacheEvent event,
                                  ChannelImpl chan,
                                  RequestFilter[] original,
                                  RequestFilter[] available,
                                  LocalSeismogramImpl[] seismograms,
                                  CookieJar cookieJar) throws Exception {
        boolean out = updateTable(event,
                                  chan,
                                  original,
                                  available,
                                  seismograms,
                                  cookieJar);
        return new WaveformResult(seismograms, new StringTreeLeaf(this, out));
    }

    public boolean updateTable(CacheEvent event,
                               ChannelImpl channel,
                               RequestFilter[] original,
                               RequestFilter[] available,
                               LocalSeismogramImpl[] seismograms,
                               CookieJar cookieJar) throws Exception {
        if( ! channelAcceptor.accept(event,
                                   channel,
                                   cookieJar).isSuccess()) {
            return false;
        }
        URLDataSetSeismogram[] dss;
        synchronized(this) {
            URLDataSetSeismogram chanDSS = SeismogramFileRefDB.getSingleton().getDataSetSeismogram(channel.get_id(), 
                                                                                                   event, 
                                                                                                   ReduceTool.cover(original));
            dss = addToCache(event, channel, chanDSS).toArray(new URLDataSetSeismogram[0]);
        }
            SodDB soddb = SodDB.getSingleton();
            if(soddb.getRecordSectionItem(orientationId,
                                          recordSectionId, event, channel) != null) {
                return false;
            }
            soddb.put(new RecordSectionItem(orientationId,
                                                           recordSectionId,
                                                           event,
                                                           channel,
                                                           false));
            DataSetSeismogram[] bestSeismos = dss;
            if(spacer != null) {
                bestSeismos = spacer.spaceOut(dss);
            }
            ChannelId[] bestChans = getChannelIds(bestSeismos);
            soddb.updateBestForRecordSection(orientationId,
                                                            recordSectionId,
                                                            event,
                                                            bestChans);
            return true;
    }

    public ChannelId[] getChannelIds(DataSetSeismogram[] dss)
            throws SQLException, NotFound {
        ChannelId[] channelIds = new ChannelId[dss.length];
        for(int j = 0; j < dss.length; j++) {
            channelIds[j] = dss[j].getRequestFilter().channel_id;
        }
        return channelIds;
    }

    public  URLDataSetSeismogram[] extractSeismograms(CacheEvent event)
            throws Exception {
        List<URLDataSetSeismogram> copy = extractSeismogramsFromDB(event);
        List<URLDataSetSeismogram> out = new ArrayList<URLDataSetSeismogram>();
        for (URLDataSetSeismogram urlDSS : copy) {
            // this is bad, but not sure how to populate the cookie jar
            if(channelAcceptor.accept(event, (ChannelImpl)urlDSS.getChannel(), new CookieJar()).isSuccess()) {
                out.add(urlDSS);
            }
        }
        return out.toArray(new URLDataSetSeismogram[0]);
    }

    private synchronized static List<URLDataSetSeismogram> addToCache(CacheEvent event, ChannelImpl chan, URLDataSetSeismogram dss) throws Exception {
        extractSeismogramsFromDB(event); // make sure cache is for current event
        lastDSS.get(0).getDataSet().addDataSetSeismogram(dss, new AuditInfo[0]);
        dss.getDataSet().addParameter(StdDataSetParamNames.CHANNEL, chan, new AuditInfo[0]);
        lastDSS.add(dss);
        return extractSeismogramsFromDB(event);
    }
    
    private synchronized static List<URLDataSetSeismogram> extractSeismogramsFromDB(CacheEvent event)
            throws Exception {  
        if (lastDSS.size() == 0 || lastEvent.getDbid() != event.getDbid()) {
            // new event
            logger.debug("Not a repeat event, getting dss from db. lastEvent="+lastEvent+"  newEvent="+event);
        
            List<EventSeismogramFileReference> seisFileRefs = SeismogramFileRefDB.getSingleton().getSeismogramsForEvent(event);
            DataSet ds = new MemoryDataSet("fake id", "temp name", "RSChannelInfoPopulator", new AuditInfo[0]);
            ds.addParameter(StdDataSetParamNames.EVENT, event, new AuditInfo[0]);
            List<URLDataSetSeismogram> dssList = new ArrayList<URLDataSetSeismogram>();
            for (EventSeismogramFileReference esRef : seisFileRefs) {
                try {
                    ChannelImpl chan = NetworkDB.getSingleton().getChannel(esRef.getNetworkCode(),
                                                                           esRef.getStationCode(),
                                                                           esRef.getSiteCode(),
                                                                           esRef.getChannelCode(),
                                                                           new MicroSecondDate(esRef.getBeginTime()));
                    RequestFilter[] rf = null;
                    if (Start.getWaveformRecipe() instanceof LocalSeismogramArm) {
                        rf = ((LocalSeismogramArm)Start.getWaveformRecipe()).getRequestGenerator().generateRequest(event, chan, null);
                    } else {
                        VectorRequestGenerator vrg = ((MotionVectorArm)Start.getWaveformRecipe()).getRequestGenerator();
                        if (vrg instanceof RequestGeneratorWrapper) {
                            rf = ((RequestGeneratorWrapper)vrg).getRequestGenerator().generateRequest(event, chan, null);
                        }
                    }
                    URLDataSetSeismogram dss = esRef.getDataSetSeismogram(ds, ReduceTool.cover(rf));
                    dssList.add(dss);
                    ds.addParameter(StdDataSetParamNames.CHANNEL, chan, new AuditInfo[0]);
            } catch (NotFound e) {
                logger.error("no channel in dataset for id="
                             + esRef.getNetworkCode()+"."+esRef.getStationCode()+"."+esRef.getSiteCode()+"."+esRef.getChannelCode()
                             + " even though seismogram is in dataset. Skipping this seismogram.");
                     continue;
            }
        }
        lastEvent = event;
        lastDSS = dssList;
        
        }

        List<URLDataSetSeismogram> copy = new ArrayList<URLDataSetSeismogram>(lastDSS.size());
        copy.addAll(lastDSS);
        return copy;
    }

    public RecordSectionDisplay getConfiguredRSDisplay() {
        RecordSectionDisplay rsDisplay = (RecordSectionDisplay)displayCreator.createDisplay();
        if(distRange != null) {
            CustomLayOutConfig custConfig = new CustomLayOutConfig(distRange.getMinDistance(),
                                                                   distRange.getMaxDistance(),
                                                                   percentSeisHeight);
            custConfig.setSwapAxes(rsDisplay.getSwapAxes());
            rsDisplay.setLayout(custConfig);
        }
        return rsDisplay;
    }

    public static ChannelId getMatchingChanIdIgnoreDates(ChannelId chan,
                                                         ChannelId[] channels) {
        for(int i = 0; i < channels.length; i++) {
            if(ChannelIdUtil.areEqualExceptForBeginTime(chan, channels[i])) {
                if(!ChannelIdUtil.areEqual(chan, channels[i])) {
                    logger.debug("seismogram channel "
                            + ChannelIdUtil.toString(chan)
                            + " has a different start time than dataset channel "
                            + ChannelIdUtil.toString(channels[i]));
                }
                return channels[i];
            }
        }
        return null;
    }

    public static ChannelId getMatchingChanIdByStationCode(ChannelId chan,
                                                           ChannelId[] channels) {
        for(int i = 0; i < channels.length; i++) {
            if(channels[i].station_code.equals(chan.station_code)
                    && channels[i].channel_code.equals(chan.channel_code)) {
                if(!ChannelIdUtil.areEqual(chan, channels[i])) {
                    logger.debug("seismogram channel "
                            + ChannelIdUtil.toString(chan)
                            + " is not totally equal to dataset channel "
                            + ChannelIdUtil.toString(channels[i]));
                }
                return channels[i];
            }
        }
        return null;
    }

    public String getOrientationId() {
        return orientationId;
    }

    public String getRecordSectionId() {
        return recordSectionId;
    }

    public String getSaveSeisId() {
        return saveSeisId;
    }

    private AbstractSeismogramWriter saveSeisToFile;

    private String orientationId, saveSeisId, recordSectionId;

    private DistanceRange distRange;// = new DistanceRange(0, 180);

    private double percentSeisHeight = 10;

    private Dimension recSecDim = new Dimension(500, 500);

    protected RecordSectionSpacer spacer;

    private SeismogramDisplayConfiguration displayCreator;

    private EventChannelSubsetter channelAcceptor;
    
    private static CacheEvent lastEvent = null;
    
    private static List<URLDataSetSeismogram> lastDSS = new ArrayList<URLDataSetSeismogram>();

    private static final org.apache.log4j.Logger logger = org.apache.log4j.Logger.getLogger(RSChannelInfoPopulator.class);
}