
package edu.sc.seis.sod.process.waveformArm;

import edu.sc.seis.fissuresUtil.xml.*;

import edu.iris.Fissures.AuditInfo;
import edu.iris.Fissures.IfEvent.EventAccessOperations;
import edu.iris.Fissures.IfEvent.NoPreferredOrigin;
import edu.iris.Fissures.IfNetwork.Channel;
import edu.iris.Fissures.IfSeismogramDC.RequestFilter;
import edu.iris.Fissures.network.ChannelIdUtil;
import edu.iris.Fissures.seismogramDC.LocalSeismogramImpl;
import edu.iris.dmc.seedcodec.CodecException;
import edu.sc.seis.fissuresUtil.display.ParseRegions;
import edu.sc.seis.fissuresUtil.mseed.SeedFormatException;
import edu.sc.seis.sod.ConfigurationException;
import edu.sc.seis.sod.CookieJar;
import edu.sc.seis.sod.SodUtil;
import edu.sc.seis.sod.process.waveformArm.LocalSeismogramProcess;
import edu.sc.seis.sod.status.EventFormatter;
import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Iterator;
import java.util.LinkedList;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.ParserConfigurationException;
import org.apache.log4j.Logger;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.xml.sax.SAXException;

/**
 * SaveSeismogramToFile.java
 * A example config Element is:<br>
 * <pre>
 * &lt;saveSeismogramToFile>
 *    &lt;dataDirectory>research/sodtest/data&lt;/dataDirectory>
 * &lt;/saveSeismogramToFile>
 * </pre>
 *
 *
 * Created: Tue Mar 19 14:08:39 2002
 *
 * @author <a href="mailto:crotwell@pooh">Philip Crotwell</a>
 * @version
 */

public class SaveSeismogramToFile implements LocalSeismogramProcess {
    /**
     * Creates a new <code>SacFileProcessor</code> instance.
     *
     * @param config an <code>Element</code> that contains the configuration
     * for this Processor
     */
    public SaveSeismogramToFile (Element config) throws ConfigurationException {
        this.config = config;
        String fileTypeStr =
            SodUtil.getText(SodUtil.getElement(config, "fileType"));

        if (fileTypeStr == null || fileTypeStr.length() == 0 || fileTypeStr.equals(SeismogramFileTypes.MSEED.getValue())) {
            fileType = SeismogramFileTypes.MSEED;
        } else if (fileTypeStr.equals(SeismogramFileTypes.SAC.getValue())) {
            fileType = SeismogramFileTypes.SAC;
        } else if (fileTypeStr.equals(SeismogramFileTypes.PSN.getValue())) {
            fileType = SeismogramFileTypes.PSN;
        } else {
            throw new ConfigurationException("Unrecognized file type: "+fileTypeStr);
        }
        String datadirName =
            SodUtil.getText(SodUtil.getElement(config, "dataDirectory"));
        this.dataDirectory = new File(datadirName);
        if ( ! dataDirectory.exists()) {
            if ( ! dataDirectory.mkdirs()) {
                throw new ConfigurationException("Unable to create directory."+dataDirectory);
            } // end of if (!)

        } // end of if (dataDirectory.exits())

        Element subDSElement = SodUtil.getElement(config, "subEventDataSet");
        if (subDSElement != null) {
            String subDSText =
                SodUtil.getText(subDSElement);
            if ( subDSText != null && subDSText.length() != 0) {
                subDS = subDSText;
            } // end of if (dataDirectory.exits())
        }

        Element prefixElement = SodUtil.getElement(config, "prefix");
        if (prefixElement != null) {
            String dssPrefix =
                SodUtil.getText(prefixElement);
            if ( dssPrefix != null && dssPrefix.length() != 0) {
                prefix = dssPrefix;
            } // end of if (dataDirectory.exits())
        }

        nameGenerator = new EventFormatter(SodUtil.getElement(config,
                                                              "eventDirLabel"));
        if (masterDataSetElement == null) {
            createMasterDS();
        }
    }

    protected  void createMasterDS() throws ConfigurationException {
        AuditInfo[] audit = new AuditInfo[1];
        audit[0] = new AuditInfo(System.getProperty("user.name"),
                                 "seismogram loaded via sod.");
        DataSet masterDataSet = new MemoryDataSet("master_genid"+Math.random(),
                                                  "Master",
                                                  System.getProperty("user.name"),
                                                  audit);

        try {
            // seismogram file type doesn't matter here as no data will be added
            // directly to master dataset
            masterDataSetElement = dsToXML.createDocument(masterDataSet, dataDirectory, SeismogramFileTypes.SAC);
            masterDSFile = new File(dataDirectory, dsToXML.createFileName(masterDataSet));
            dsToXML.writeToFile(masterDataSetElement, masterDSFile);
        } catch (IOException e) {
            throw new ConfigurationException("Problem trying to create top level dataset", e);
        } catch (ParserConfigurationException e) {
            throw new ConfigurationException("Problem trying to create top level dataset", e);
        }
    }

    /**
     * Describe <code>process</code> method here.
     *
     * @param event an <code>EventAccessOperations</code> value
     * @param network a <code>NetworkAccess</code> value
     * @param channel a <code>Channel</code> value
     * @param original a <code>RequestFilter[]</code> value
     * @param available a <code>RequestFilter[]</code> value
     * @param seismograms a <code>LocalSeismogram[]</code> value
     * @param cookies a <code>CookieJar</code> value
     * @exception Exception if an error occurs
     */
    public LocalSeismogramImpl[] process(EventAccessOperations event,
                                         Channel channel,
                                         RequestFilter[] original,
                                         RequestFilter[] available,
                                         LocalSeismogramImpl[] seismograms, CookieJar cookieJar) throws Exception {

        logger.info("Got "+seismograms.length+" seismograms for "+
                        ChannelIdUtil.toString(channel.get_id())+
                        " for event in "+
                        regions.getRegionName(event.get_attributes().region)+
                        " at "+event.get_preferred_origin().origin_time.date_time);

        if (seismograms.length == 0) { return seismograms; }
        synchronized(masterDataSetElement) {
            saveInDataSet(event, channel, seismograms, fileType);

            boolean found = false;
            Iterator it = masterDSNames.iterator();
            while (it.hasNext()) {
                if (lastDataSet.getName().equals(it.next())) {
                    found = true;
                }
            }
            if ( ! found) {
                masterDSNames.add(lastDataSet.getName());
                updateMasterDataSet(lastDataSetFile, lastDataSet.getName());
            }
        }
        return seismograms;
    }

    protected void updateMasterDataSet(File childDataset, String childName)
        throws IOException, ParserConfigurationException, ConfigurationException {
        Document doc = masterDataSetElement.getOwnerDocument();
        Element child = dsToXML.insertRef(masterDataSetElement,
                                          getRelativeURLString(masterDSFile, childDataset),
                                          childName);
        dsToXML.writeToFile(masterDataSetElement, masterDSFile);
    }

    protected URLDataSetSeismogram saveInDataSet(EventAccessOperations event,
                                                 Channel channel,
                                                 LocalSeismogramImpl[] seismograms,
                                                 SeismogramFileTypes seisFileType)
        throws ConfigurationException,
        CodecException,
        IOException,
        NoPreferredOrigin,
        ParserConfigurationException,
        UnsupportedFileTypeException,
        SeedFormatException, SAXException {

        if (subDS.length() != 0) {
            return saveInDataSet(event, channel, seismograms, seisFileType, getDataSet(event, subDS));
        } else {
            return saveInDataSet(event, channel, seismograms, seisFileType, getDataSet(event));
        }
    }

    protected URLDataSetSeismogram saveInDataSet(EventAccessOperations event,
                                                 Channel channel,
                                                 LocalSeismogramImpl[] seismograms,
                                                 SeismogramFileTypes seisFileType,
                                                 DataSet dataset)
        throws ConfigurationException,
        CodecException,
        IOException,
        NoPreferredOrigin,
        ParserConfigurationException,
        UnsupportedFileTypeException,
        SeedFormatException, SAXException {


        synchronized(masterDataSetElement) {
            File eventDirectory = getEventDirectory(event);
            File dataDirectory = new File(eventDirectory, "data");
            dataDirectory.mkdirs();

            AuditInfo[] audit = new AuditInfo[1];
            audit[0] = new AuditInfo(System.getProperty("user.name"),
                                     "seismogram loaded via sod.");
            URL[] seisURL = new URL[seismograms.length];
            SeismogramFileTypes[] seisFileTypeArray = new SeismogramFileTypes[seisURL.length];
            String[] seisURLStr = new String[seismograms.length];
            for (int i=0; i<seismograms.length; i++) {
                // seismograms from the DMC in particular, have the times in the
                // channel_id wrong. This is due to the server not interacting with
                // the oracle database, only the mseed files
                // so we set the channel of the seismogram to match the original
                // channel from the request
                logger.debug("saveInDataset "+i+" "+ChannelIdUtil.toString(seismograms[i].channel_id));
                seismograms[i].channel_id = channel.get_id();

                File seisFile = URLDataSetSeismogram.saveAs(seismograms[i],
                                                       dataDirectory,
                                                       channel,
                                                       event,
                                                       fileType);
                seisURLStr[i] = getRelativeURLString(lastDataSetFile, seisFile);
                seisURL[i] = seisFile.toURI().toURL();
                seisFileTypeArray[i] = seisFileType;  // all are the same
                bytesWritten += seisFile.length();
            }
            URLDataSetSeismogram urlDSS = new URLDataSetSeismogram(seisURL,
                                                                   seisFileTypeArray,
                                                                   lastDataSet);
            if (prefix != null && prefix.length() != 0) {
                urlDSS.setName(prefix+urlDSS.getName());
            }
            for (int i = 0; i < seisURL.length; i++) {
                urlDSS.addToCache(seisURL[i], seisFileType, seismograms[i]);
            }

            urlDSS.addAuxillaryData(StdAuxillaryDataNames.NETWORK_BEGIN,
                                    channel.get_id().network_id.begin_time.date_time);
            urlDSS.addAuxillaryData(StdAuxillaryDataNames.CHANNEL_BEGIN,
                                    channel.get_id().begin_time.date_time);

            lastDataSet.addDataSetSeismogram(urlDSS, audit);
            dsToXML.insert(lastDataSetElement,urlDSS, lastDataSetFile.getParentFile().toURI().toURL());
            lastDataSet.addParameter(DataSet.CHANNEL+ChannelIdUtil.toString(channel.get_id()),
                                     channel,
                                     audit);
            dsToXML.insert(lastDataSetElement,
                           DataSet.CHANNEL+ChannelIdUtil.toString(channel.get_id()),
                           channel);

            dsToXML.writeToFile(lastDataSetElement, lastDataSetFile);
            lastDataSetFileModTime = lastDataSetFile.lastModified();
            return urlDSS;
        }
    }

    protected File saveDataSet(DataSet ds)
        throws IOException, ParserConfigurationException, ConfigurationException {

        File outFile;
        if (ds.getEvent() != null) {
            outFile = dsToXML.save(ds, getEventDirectory(ds.getEvent()), fileType);
        } else {
            outFile = dsToXML.save(ds, dataDirectory, fileType);
        }
        logger.debug("DSML saved to "+outFile.getName());
        Runtime runtime = Runtime.getRuntime();
        String s = "Memory usage: "+
            edu.sc.seis.fissuresUtil.exceptionHandler.ExceptionReporterUtils.getMemoryUsage();
        System.out.println(s);
        return outFile;
    }

    protected File getParentDirectory() {
        return dataDirectory;
    }

    protected File getEventDirectory(EventAccessOperations event)
        throws ConfigurationException {
        String eventDirName = getLabel(event);
        File eventDirectory = new File(dataDirectory, EventFormatter.filize(eventDirName));
        if ( ! eventDirectory.exists()) {
            if ( ! eventDirectory.mkdirs()) {
                throw new ConfigurationException("Unable to create directory."+eventDirectory);
            } // end of if (!)
        } // end of if (dataDirectory.exits())

        return eventDirectory;
    }

    protected DataSet getDataSet(EventAccessOperations event)
        throws NoPreferredOrigin, ConfigurationException, ParserConfigurationException, IOException, SAXException, ParserConfigurationException, IOException, SAXException, UnsupportedFileTypeException {
        DataSet dataset;

        File eventDirectory = getEventDirectory(event);

        // assume that processing is in event order and never reopens
        // bad but just temporary
        if (lastDataSet != null && lastDataSet.getEvent().equals(event)) {
            // check for file modification outside of this object
            if (lastDataSetFileModTime == lastDataSetFile.lastModified()) {
                dataset = lastDataSet;
                return dataset;
            }
        }

        logger.debug("creating new dataset "+getLabel(event));
        //temp
        dataset = new MemoryDataSet(event.get_preferred_origin().origin_time.date_time,
                                    getLabel(event),
                                    System.getProperty("user.name"),
                                    new AuditInfo[0]);
        dataset.addParameter(dataset.EVENT, event, new AuditInfo[0]);
        lastDataSet = dataset;
        lastEvent = event;
        lastDataSetFile = new File(eventDirectory, DataSetToXML.createFileName(dataset));
        if (lastDataSetFile.exists()) {
            lastDataSet = DataSetToXML.load(lastDataSetFile.toURI().toURL());
            dataset = lastDataSet;
        }
        lastDataSetElement = dsToXML.createDocument(lastDataSet, eventDirectory, fileType);
        lastDataSetFileModTime = lastDataSetFile.lastModified();
        //dataset = getDataSet(eventDirectory, eventDirName, event);

        return dataset;
    }

    public DataSet getDataSet(EventAccessOperations event, String subDSName)
        throws NoPreferredOrigin, ConfigurationException, ParserConfigurationException, IOException, SAXException, ParserConfigurationException, IOException, SAXException, UnsupportedFileTypeException {
        DataSet eventDS = getDataSet(event);
        String[] dsNames = eventDS.getDataSetNames();
        for (int i = 0; i < dsNames.length; i++) {
            if (dsNames[i].equals(subDSName)) {
                return eventDS.getDataSet(dsNames[i]);
            }
        }
        MemoryDataSet dataset = new MemoryDataSet(event.get_preferred_origin().origin_time.date_time+"/"+subDSName,
                                                  subDSName,
                                                  System.getProperty("user.name"),
                                                  new AuditInfo[0]);
        eventDS.addDataSet(dataset, new AuditInfo[0]);
        return dataset;
    }

    protected DataSet getXMLDataSet(EventAccessOperations event)
        throws MalformedURLException, ParserConfigurationException, ConfigurationException {

        File eventDirectory = getEventDirectory(event);

        // load dataset if it already exists
        String eventDirName = getLabel(event);
        File dsFile = new File(eventDirectory, eventDirName+".dsml");
        XMLDataSet dataset;
        if (dsFile.exists()) {
            dataset = XMLDataSet.load(dsFile.toURL());
        } else {
            DocumentBuilder docBuilder = XMLDataSet.getDocumentBuilder();
            dataset = new XMLDataSet(docBuilder,
                                     eventDirectory.toURL(),
                                     "genid"+Math.round(Math.random()*Integer.MAX_VALUE),
                                     eventDirName,
                                     System.getProperty("user.name"));

            // add event since dataset is new
            if (event != null) {
                AuditInfo[] audit = new AuditInfo[1];
                audit[0] = new AuditInfo(System.getProperty("user.name"),
                                         "event loaded via sod.");
                dataset.addParameter( StdDataSetParamNames.EVENT, event, audit);
            }
        } // end of else
        return dataset;
    }

    protected String getLabel(EventAccessOperations event) {
        return nameGenerator.getFilizedName(event);
    }

    String getRelativeURLString(File base, File ref) {
        File baseUp = base.getParentFile();
        File refUp = ref;
        String out = ref.getName();
        // try to see is one of ref's ancestors is base's parent
        while ((refUp = refUp.getParentFile()) != null) {
            if (baseUp.equals(refUp)) {
                // found it
                return out;
            }
            out = refUp.getName()+"/"+out;
        }

        // baseUp is not a direct ancestor of ref, fall back to absolute?
        return ref.getPath();
    }

    public static int getBytesWritten(){ return bytesWritten; }

    private static int bytesWritten = 0;

    static DataSetToXML dsToXML = new DataSetToXML();

    static EventAccessOperations lastEvent = null;

    static DataSet lastDataSet = null;

    static Element lastDataSetElement = null;

    static File lastDataSetFile;

    static long lastDataSetFileModTime;

    static Element masterDataSetElement;

    static File masterDSFile;

    static LinkedList masterDSNames = new LinkedList();

    EventFormatter nameGenerator = null;

    static ParseRegions regions = ParseRegions.getInstance();

    Element config;

    File dataDirectory;

    SeismogramFileTypes fileType;

    String prefix = "";

    String subDS = "";

    private static final Logger logger =
        Logger.getLogger(SaveSeismogramToFile.class);

}// SacFileProcessor

