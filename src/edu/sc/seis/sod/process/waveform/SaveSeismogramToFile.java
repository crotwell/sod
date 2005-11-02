package edu.sc.seis.sod.process.waveform;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamWriter;
import org.apache.log4j.Logger;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;
import edu.iris.Fissures.AuditInfo;
import edu.iris.Fissures.IfEvent.EventAccessOperations;
import edu.iris.Fissures.IfEvent.NoPreferredOrigin;
import edu.iris.Fissures.IfNetwork.Channel;
import edu.iris.Fissures.IfNetwork.ChannelId;
import edu.iris.Fissures.IfSeismogramDC.RequestFilter;
import edu.iris.Fissures.network.ChannelIdUtil;
import edu.iris.Fissures.seismogramDC.LocalSeismogramImpl;
import edu.sc.seis.TauP.Arrival;
import edu.sc.seis.TauP.TauModelException;
import edu.sc.seis.TauP.TauP_SetSac;
import edu.sc.seis.fissuresUtil.bag.LongShortTrigger;
import edu.sc.seis.fissuresUtil.bag.TauPUtil;
import edu.sc.seis.fissuresUtil.cache.EventUtil;
import edu.sc.seis.fissuresUtil.database.ConnMgr;
import edu.sc.seis.fissuresUtil.database.seismogram.JDBCSeismogramFiles;
import edu.sc.seis.fissuresUtil.display.ParseRegions;
import edu.sc.seis.fissuresUtil.display.configuration.DOMHelper;
import edu.sc.seis.fissuresUtil.sac.FissuresToSac;
import edu.sc.seis.fissuresUtil.xml.DataSet;
import edu.sc.seis.fissuresUtil.xml.DataSetSeismogram;
import edu.sc.seis.fissuresUtil.xml.DataSetToXML;
import edu.sc.seis.fissuresUtil.xml.DataSetToXMLStAX;
import edu.sc.seis.fissuresUtil.xml.IncomprehensibleDSMLException;
import edu.sc.seis.fissuresUtil.xml.MemoryDataSet;
import edu.sc.seis.fissuresUtil.xml.SeismogramFileTypes;
import edu.sc.seis.fissuresUtil.xml.StAXFileWriter;
import edu.sc.seis.fissuresUtil.xml.StdAuxillaryDataNames;
import edu.sc.seis.fissuresUtil.xml.URLDataSetSeismogram;
import edu.sc.seis.fissuresUtil.xml.UnsupportedFileTypeException;
import edu.sc.seis.fissuresUtil.xml.XMLUtil;
import edu.sc.seis.seisFile.sac.SacTimeSeries;
import edu.sc.seis.sod.ConfigurationException;
import edu.sc.seis.sod.CookieJar;
import edu.sc.seis.sod.status.FissuresFormatter;
import edu.sc.seis.sod.status.StringTreeLeaf;
import edu.sc.seis.sod.velocity.SimpleVelocitizer;

public class SaveSeismogramToFile implements WaveformProcess {

    public static final String SVN_PARAM = PhaseSignalToNoise.PHASE_STON_PREFIX + "ttp";

    public SaveSeismogramToFile(Element config) throws ConfigurationException {
        String fileTypeStr = DOMHelper.extractText(config,
                                                   "fileType",
                                                   SeismogramFileTypes.MSEED.getValue());
        if(fileTypeStr.equals(SeismogramFileTypes.MSEED.getValue())) {
            fileType = SeismogramFileTypes.MSEED;
        } else if(fileTypeStr.equals(SeismogramFileTypes.SAC.getValue())) {
            fileType = SeismogramFileTypes.SAC;
            if(DOMHelper.hasElement(config, "sacHeader")) {
                Element sacHeader = DOMHelper.extractElement(config,
                                                             "sacHeader");
                NodeList nl = DOMHelper.getElements(sacHeader, "phaseTime");
                for(int i = 0; i < nl.getLength(); i++) {
                    Element phaseEl = (Element)nl.item(i);
                    String model = DOMHelper.extractText(phaseEl, "model");
                    String phaseName = DOMHelper.extractText(phaseEl,
                                                             "phaseName");
                    int tHeader = Integer.parseInt(DOMHelper.extractText(phaseEl,
                                                                         "tHeader"));
                    sacHeaderList.add(new PhaseHeaderProcess(model,
                                                             phaseName,
                                                             tHeader));
                }
            }
        }
        String datadirName = DOMHelper.extractText(config,
                                                   "dataDirectory",
                                                   DEFAULT_DATA_DIRECTORY);
        dataDirectory = new File(datadirName);
        if(!dataDirectory.exists()) {
            if(!dataDirectory.mkdirs()) {
                throw new ConfigurationException("Unable to create directory."
                        + dataDirectory);
            } // end of if (!)
        } // end of if (dataDirectory.exits())
        subDS = DOMHelper.extractText(config, "subEventDataSet", "");
        prefix = DOMHelper.extractText(config, "prefix", "");
        id = DOMHelper.extractText(config, "id", "");
        masterId = DOMHelper.extractText(config, "masterId", "master_genid"
                + Math.random());
        masterFileName = DOMHelper.extractText(config, "masterFileName", "");
        preserveRequest = DOMHelper.hasElement(config, "preserveRequest");
        if(DOMHelper.hasElement(config, "storeSeismogramsInDB")) {
            try {
                jdbcSeisFile = new JDBCSeismogramFiles(ConnMgr.createConnection());
            } catch(SQLException e) {
                throw new ConfigurationException("Trouble creating database connection",
                                                 e);
            }
            storeSeismogramsInDB = true;
        }
        eventDirTemplate = DOMHelper.extractText(config,
                                                 "eventDirLabel",
                                                 DEFAULT_TEMPLATE);
        eventNameTemplate = DOMHelper.extractText(config,
                                                  "eventName",
                                                  DEFAULT_TEMPLATE);
        createMasterDS(DOMHelper.extractText(config, "masterDSName", "Master"));
        cookiesToParams.add(SVN_PARAM);
    }

    public WaveformResult process(EventAccessOperations event,
                                  Channel channel,
                                  RequestFilter[] original,
                                  RequestFilter[] available,
                                  LocalSeismogramImpl[] seismograms,
                                  CookieJar cookieJar) throws Exception {
        logger.info("Got " + seismograms.length + " seismograms for "
                + ChannelIdUtil.toString(channel.get_id()) + " for event in "
                + regions.getRegionName(event.get_attributes().region) + " at "
                + event.get_preferred_origin().origin_time.date_time);
        if(seismograms.length == 0) {
            return new WaveformResult(seismograms, new StringTreeLeaf(this,
                                                                      true));
        }
        URLDataSetSeismogram urlDSS;
        if(preserveRequest) {
            urlDSS = saveInDataSet(event,
                                   channel,
                                   seismograms,
                                   fileType,
                                   original[0],
                                   cookieJar);
        } else {
            urlDSS = saveInDataSet(event,
                                   channel,
                                   seismograms,
                                   fileType,
                                   null,
                                   cookieJar);
        }
        URL[] urls = urlDSS.getURLs();
        for(int i = 0; i < urls.length; i++) {
            cookieJar.put(getCookieName(prefix, channel.get_id(), i),
                          urls[i].getFile());
        }
        boolean found = false;
        Iterator it = masterDSNames.iterator();
        while(it.hasNext()) {
            if(lastDataSet.getName().equals(it.next())) {
                found = true;
            }
        }
        if(!found) {
            masterDSNames.add(lastDataSet.getName());
            updateMasterDataSet(dataSetFile, lastDataSet.getName());
        }
        return new WaveformResult(seismograms, new StringTreeLeaf(this, true));
    }

    public static String getCookieName(String prefix, ChannelId channel, int i) {
        return COOKIE_PREFIX + prefix + ChannelIdUtil.toString(channel) + "_"
                + i;
    }

    public String getId() {
        return id;
    }

    protected void createMasterDS(String name) throws ConfigurationException {
        AuditInfo[] audit = new AuditInfo[1];
        audit[0] = new AuditInfo(System.getProperty("user.name"),
                                 "seismogram loaded via sod.");
        DataSet masterDataSet = new MemoryDataSet(masterId,
                                                  name,
                                                  System.getProperty("user.name"),
                                                  audit);
        try {
            // seismogram file type doesn't matter here as no data will be added
            // directly to master dataset
            String fileName = (masterFileName.equals("") || masterFileName == null) ? DataSetToXMLStAX.createFileName(masterDataSet)
                    : masterFileName;
            masterDSFile = new File(dataDirectory, fileName);
            if(!masterDSFile.exists()) {
                dsToXML.createFile(masterDataSet, dataDirectory, masterDSFile);
            }
        } catch(IOException e) {
            throw new ConfigurationException("Problem trying to create top level dataset",
                                             e);
        } catch(XMLStreamException e) {
            throw new ConfigurationException("Problem trying to create top-level dataset",
                                             e);
        }
    }

    /**
     * creates a temporary dataset to be used for merging
     */
    public static DataSet createTempDataSet(String name) {
        AuditInfo audit = new AuditInfo(System.getProperty("user.name"),
                                        "seismogram loaded via sod");
        DataSet tempDS = new MemoryDataSet("temp_dataset",
                                           name,
                                           System.getProperty("user.name"),
                                           new AuditInfo[] {audit});
        return tempDS;
    }

    protected void updateMasterDataSet(File childDataset, String childName)
            throws FileNotFoundException, XMLStreamException, IOException {
        // the merging code has been rendered useless since the advent of the
        // newlines
        // after end elements, So I'm just going to do it this way since I know
        // this works.
        StAXFileWriter masterDSWriter = XMLUtil.openXMLFileForAppending(masterDSFile);
        dsToXML.writeRef(masterDSWriter.getStreamWriter(),
                         getRelativeURLString(masterDSFile, childDataset),
                         childName);
        masterDSWriter.close();
        // }
    }

    protected URLDataSetSeismogram saveInDataSet(EventAccessOperations event,
                                                 Channel channel,
                                                 LocalSeismogramImpl[] seismograms,
                                                 SeismogramFileTypes type)
            throws Exception {
        return saveInDataSet(event, channel, seismograms, type, null, null);
    }

    // Used to save a seismogram locally.
    protected URLDataSetSeismogram saveInDataSet(EventAccessOperations event,
                                                 Channel channel,
                                                 LocalSeismogramImpl[] seismograms,
                                                 SeismogramFileTypes type,
                                                 RequestFilter request,
                                                 CookieJar cookies)
            throws Exception {
        if(subDS.length() != 0) {
            prepareDataset(event, subDS);
        } else {
            prepareDataset(event);
        }
        File eventDirectory = getEventDirectory(event);
        File seisFileDirectory = new File(eventDirectory, "data");
        seisFileDirectory.mkdirs();
        AuditInfo[] audit = new AuditInfo[1];
        audit[0] = new AuditInfo(System.getProperty("user.name"),
                                 "seismogram loaded via sod.");
        URL[] seisURL = new URL[seismograms.length];
        SeismogramFileTypes[] seisFileTypeArray = new SeismogramFileTypes[seisURL.length];
        String[] seisURLStr = new String[seismograms.length];
        String suffix = ".mseed";
        if(type == SeismogramFileTypes.SAC) {
            suffix = ".sac";
        }
        File baseFile = URLDataSetSeismogram.getBaseFile(seisFileDirectory,
                                                         channel,
                                                         suffix);
        boolean existingFilesForSeis = baseFile.exists();
        for(int n = 1; baseFile.exists(); n++) {
            logger.debug("Removing existing seismogram " + baseFile);
            baseFile.delete();
            if(storeSeismogramsInDB) {
                int numRemoved = jdbcSeisFile.removeSeismogramFromDatabase(channel,
                                                                           baseFile.toString());
                if(numRemoved == 0) {
                    logger.warn("No seismograms removed from database, but they're being stored there and a file exists for the seismogram");
                    throw new RuntimeException();
                }
            }
            baseFile = URLDataSetSeismogram.makeFile(seisFileDirectory,
                                                     n,
                                                     channel,
                                                     suffix);
        }
        for(int i = 0; i < seismograms.length; i++) {
            // seismograms from the DMC in particular, have the times in the
            // channel_id wrong. This is due to the server not interacting with
            // the oracle database, only the mseed files
            // so we set the channel of the seismogram to match the original
            // channel from the request
            logger.debug("saveInDataset " + i + " "
                    + ChannelIdUtil.toString(seismograms[i].channel_id));
            seismograms[i].channel_id = channel.get_id();
            File seisFile = URLDataSetSeismogram.getUnusedFileName(seisFileDirectory,
                                                                   channel,
                                                                   ".sac");
            if(type != SeismogramFileTypes.SAC) {
                seisFile = URLDataSetSeismogram.saveAs(seismograms[i],
                                                       seisFileDirectory,
                                                       channel,
                                                       event,
                                                       type);
            } else {
                SacTimeSeries sac = FissuresToSac.getSAC(seismograms[i],
                                                         channel,
                                                         event != null ? event.get_preferred_origin()
                                                                 : null);
                Iterator it = sacHeaderList.iterator();
                while(it.hasNext()) {
                    ((SacHeaderProcess)it.next()).process(sac, event, channel);
                }
                sac.write(seisFile);
            }
            if(storeSeismogramsInDB) {
                jdbcSeisFile.saveSeismogramToDatabase(channel,
                                                      seismograms[i],
                                                      seisFile.toString(),
                                                      type);
            }
            seisURLStr[i] = getRelativeURLString(dataSetFile, seisFile);
            seisURL[i] = seisFile.toURI().toURL();
            seisFileTypeArray[i] = type; // all are the same
            bytesWritten += seisFile.length();
        }
        URLDataSetSeismogram urlDSS = new URLDataSetSeismogram(seisURL,
                                                               seisFileTypeArray,
                                                               null,
                                                               "",
                                                               request);
        urlDSS.setName(prefix + DataSetSeismogram.generateName(channel));
        for(int i = 0; i < seisURL.length; i++) {
            urlDSS.addToCache(seisURL[i], type, seismograms[i]);
        }
        urlDSS.addAuxillaryData(StdAuxillaryDataNames.NETWORK_BEGIN,
                                channel.get_id().network_id.begin_time.date_time);
        urlDSS.addAuxillaryData(StdAuxillaryDataNames.CHANNEL_BEGIN,
                                channel.get_id().begin_time.date_time);
        if(cookies != null) {
            Iterator it = cookiesToParams.iterator();
            while(it.hasNext()) {
                String key = (String)it.next();
                Object cookie = cookies.get(key);
                if(cookie != null) {
                    urlDSS.addAuxillaryData(key, ""
                            + ((LongShortTrigger)cookie).getValue());
                }
                // Hardwired for LongShort now, should be extended to add
                // anything to the aux data from the cookiejar
            }
        }
        lastDataSet.remove(urlDSS);
        lastDataSet.addDataSetSeismogram(urlDSS, new AuditInfo[] {});
        if(!existingFilesForSeis) {// First insertion of dss into dataset, so
            // add the channel to the data set and append it and the urldss
            // to the dataset file
            logger.debug("Appending to seismogram to dsml");
            StAXFileWriter staxWriter = XMLUtil.openXMLFileForAppending(dataSetFile);
            dsToXML.writeURLDataSetSeismogram(staxWriter.getStreamWriter(),
                                              urlDSS,
                                              dataSetFile.getParentFile()
                                                      .toURI()
                                                      .toURL());
            lastDataSet.addParameter(DataSet.CHANNEL
                    + ChannelIdUtil.toString(channel.get_id()), channel, audit);
            dsToXML.writeParameter(staxWriter.getStreamWriter(),
                                   DataSet.CHANNEL
                                           + ChannelIdUtil.toString(channel.get_id()),
                                   channel);
            staxWriter.close();
        } else {// rewrite the whole dataset to file to get the new one out
            // there and remove the old urldss seismogram from the file
            logger.debug("Rewriting dsml file to remove old seismogram and add its replacement");
            writeDataSet(lastDataSet);
        }
        lastDataSetFileModTime = dataSetFile.lastModified();
        return urlDSS;
    }

    protected File getParentDirectory() {
        return dataDirectory;
    }

    protected File getEventDirectory(EventAccessOperations event)
            throws IOException {
        String eventDirName = getLabel(event);
        File eventDirectory = new File(dataDirectory,
                                       FissuresFormatter.filize(eventDirName));
        if(!eventDirectory.exists()) {
            if(!eventDirectory.mkdirs()) {
                throw new IOException("Unable to create directory."
                        + eventDirectory);
            } // end of if (!)
        } // end of if (dataDirectory.exits())
        return eventDirectory;
    }

    public String getLabel(EventAccessOperations event) {
        return velocitizer.evaluate(eventDirTemplate, event);
    }

    public String getName(EventAccessOperations event) {
        return velocitizer.evaluate(eventNameTemplate, event);
    }

    String getRelativeURLString(File base, File ref) {
        File baseUp = base.getParentFile();
        File refUp = ref;
        String out = ref.getName();
        // try to see is one of ref's ancestors is base's parent
        while((refUp = refUp.getParentFile()) != null) {
            if(baseUp.equals(refUp)) {
                // found it
                return out;
            }
            out = refUp.getName() + "/" + out;
        }
        // baseUp is not a direct ancestor of ref, fall back to absolute?
        return ref.getPath();
    }

    protected DataSet prepareDataset(EventAccessOperations event)
            throws IOException, UnsupportedFileTypeException,
            IncomprehensibleDSMLException, ParserConfigurationException,
            XMLStreamException {
        // assume that processing is in event order and never reopens
        // bad but just temporary
        if(lastDataSet != null && lastDataSet.getEvent().equals(event)) {
            return lastDataSet;
        }
        MemoryDataSet dataset = createDataSet(event);
        dataSetFile = makeDSMLFilename(event);
        if(dataSetFile.exists()) {
            dataset = (MemoryDataSet)DataSetToXML.load(dataSetFile.toURI()
                    .toURL());
        } else {
            logger.debug("creating new dataset " + getName(event));
            dataset = new MemoryDataSet(EventUtil.extractOrigin(event).origin_time.date_time,
                                        getName(event),
                                        System.getProperty("user.name"),
                                        new AuditInfo[0]);
            dataset.addParameter(DataSet.EVENT, event, new AuditInfo[0]);
            writeDataSet(dataset);
        }
        lastDataSet = dataset;
        lastEvent = event;
        return dataset;
    }

    private void writeDataSet(DataSet dataset) throws IOException,
            XMLStreamException {
        StAXFileWriter staxWriter = new StAXFileWriter(dataSetFile);
        XMLStreamWriter writer = staxWriter.getStreamWriter();
        dsToXML.writeDataSetStartElement(writer);
        addEvent(dataset, writer);
        addChannels(dataset, staxWriter);
        writeURLDSS(dataset, staxWriter);
        XMLUtil.writeEndElementWithNewLine(writer);
        staxWriter.close();
    }

    private void writeURLDSS(DataSet dataset, StAXFileWriter staxWriter)
            throws XMLStreamException, MalformedURLException {
        String[] names = dataset.getDataSetSeismogramNames();
        for(int i = 0; i < names.length; i++) {
            URLDataSetSeismogram urlDSS = (URLDataSetSeismogram)dataset.getDataSetSeismogram(names[i]);
            dsToXML.writeURLDataSetSeismogram(staxWriter.getStreamWriter(),
                                              urlDSS,
                                              dataSetFile.getParentFile()
                                                      .toURI()
                                                      .toURL());
        }
    }

    private void addChannels(DataSet dataset, StAXFileWriter staxWriter)
            throws XMLStreamException {
        ChannelId[] chanIds = dataset.getChannelIds();
        for(int i = 0; i < chanIds.length; i++) {
            Channel channel = dataset.getChannel(chanIds[i]);
            dsToXML.writeParameter(staxWriter.getStreamWriter(),
                                   DataSet.CHANNEL
                                           + ChannelIdUtil.toString(channel.get_id()),
                                   channel);
        }
    }

    private void addEvent(DataSet dataset, XMLStreamWriter writer)
            throws XMLStreamException, IOException {
        EventAccessOperations event = dataset.getEvent();
        dsToXML.insertDSInfo(writer, dataset, getEventDirectory(event));
        dsToXML.writeParameter(writer, DataSet.EVENT, event);
    }

    public File getDSMLFile(EventAccessOperations event) throws IOException {
        File dsmlFile = makeDSMLFilename(event);
        if(dsmlFile.exists()) {
            return dsmlFile;
        }
        throw new FileNotFoundException("Dsml File "
                + dsmlFile.getAbsolutePath() + " not found for "
                + EventUtil.getEventInfo(event));
    }

    public File makeDSMLFilename(EventAccessOperations event)
            throws IOException {
        return new File(getEventDirectory(event),
                        DataSetToXML.createFileName(createDataSet(event)));
    }

    private MemoryDataSet createDataSet(EventAccessOperations event) {
        return new MemoryDataSet(EventUtil.extractOrigin(event).origin_time.date_time,
                                 getName(event),
                                 System.getProperty("user.name"),
                                 new AuditInfo[0]);
    }

    public DataSet prepareDataset(EventAccessOperations event, String subDSName)
            throws IOException, ParserConfigurationException,
            IncomprehensibleDSMLException, UnsupportedFileTypeException,
            XMLStreamException {
        DataSet eventDS = prepareDataset(event);
        String[] dsNames = eventDS.getDataSetNames();
        for(int i = 0; i < dsNames.length; i++) {
            if(dsNames[i].equals(subDSName)) {
                return eventDS.getDataSet(dsNames[i]);
            }
        }
        MemoryDataSet dataset = new MemoryDataSet(EventUtil.extractOrigin(event).origin_time.date_time
                                                          + "/" + subDSName,
                                                  subDSName,
                                                  System.getProperty("user.name"),
                                                  new AuditInfo[0]);
        eventDS.addDataSet(dataset, new AuditInfo[0]);
        return dataset;
    }

    public static long getBytesWritten() {
        return bytesWritten;
    }

    private static long bytesWritten = 0;

    static long lastDataSetFileModTime;

    static File dataDirectory;

    static File masterDSFile;

    static List masterDSNames = new ArrayList();

    static ParseRegions regions = ParseRegions.getInstance();

    static DataSet lastDataSet = null;

    static File dataSetFile;

    // static StAXFileWriter lastDataSetStaxWriter;
    static EventAccessOperations lastEvent = null;

    static DataSetToXMLStAX dsToXML = new DataSetToXMLStAX();

    SeismogramFileTypes fileType;

    private List cookiesToParams = new ArrayList();

    private String subDS, prefix, id, masterId, masterFileName, svnType;

    private ArrayList sacHeaderList = new ArrayList();

    private boolean preserveRequest = false, storeSeismogramsInDB = false;

    public static final String COOKIE_PREFIX = "SeisFile_";

    public static final String DEFAULT_TEMPLATE = "Event_$event.getTime('yyyy_DDD_HH_mm_ss')";

    public static final String DEFAULT_DATA_DIRECTORY = "seismograms";

    private SimpleVelocitizer velocitizer = new SimpleVelocitizer();

    private String eventDirTemplate;

    private String eventNameTemplate;

    private static final Logger logger = Logger.getLogger(SaveSeismogramToFile.class);

    private JDBCSeismogramFiles jdbcSeisFile;
}

abstract class SacHeaderProcess {

    abstract void process(SacTimeSeries sac,
                          EventAccessOperations event,
                          Channel channel);
}

class PhaseHeaderProcess extends SacHeaderProcess {

    PhaseHeaderProcess(String model, String phaseName, int tHeader) {
        this.model = model;
        this.phaseName = phaseName;
        this.tHeader = tHeader;
    }

    void process(SacTimeSeries sac, EventAccessOperations event, Channel channel) {
        try {
            Arrival[] arrivals = TauPUtil.getTauPUtil(model)
                    .calcTravelTimes(channel.my_site.my_location,
                                     event.get_preferred_origin(),
                                     new String[] {phaseName});
            TauP_SetSac.setSacTHeader(sac, tHeader, arrivals[0]);
        } catch(TauModelException e) {
            logger.warn("Problem setting travel times for " + phaseName
                    + " in " + model, e);
        } catch(NoPreferredOrigin e) {
            logger.warn("Sigh...", e);
        }
    }

    String model;

    String phaseName;

    int tHeader;

    private static final org.apache.log4j.Logger logger = org.apache.log4j.Logger.getLogger(PhaseHeaderProcess.class);
}