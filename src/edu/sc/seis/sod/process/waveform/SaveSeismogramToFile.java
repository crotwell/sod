/**
 * SaveSeismogramToFileAlt.java
 *
 * @author Created by Omnicore CodeGuide
 */

package edu.sc.seis.sod.process.waveform;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import javax.xml.namespace.QName;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamWriter;
import org.apache.log4j.Logger;
import org.w3c.dom.Element;
import edu.iris.Fissures.AuditInfo;
import edu.iris.Fissures.IfEvent.EventAccessOperations;
import edu.iris.Fissures.IfEvent.NoPreferredOrigin;
import edu.iris.Fissures.IfNetwork.Channel;
import edu.iris.Fissures.IfNetwork.ChannelId;
import edu.iris.Fissures.IfSeismogramDC.RequestFilter;
import edu.iris.Fissures.network.ChannelIdUtil;
import edu.iris.Fissures.seismogramDC.LocalSeismogramImpl;
import edu.iris.dmc.seedcodec.CodecException;
import edu.sc.seis.fissuresUtil.cache.EventUtil;
import edu.sc.seis.fissuresUtil.display.ParseRegions;
import edu.sc.seis.fissuresUtil.mseed.SeedFormatException;
import edu.sc.seis.fissuresUtil.xml.DataSet;
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
import edu.sc.seis.sod.ConfigurationException;
import edu.sc.seis.sod.CookieJar;
import edu.sc.seis.sod.SodUtil;
import edu.sc.seis.sod.status.EventFormatter;
import edu.sc.seis.sod.status.FissuresFormatter;
import edu.sc.seis.sod.status.StringTreeLeaf;



public class SaveSeismogramToFile implements WaveformProcess{
	
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
		dataDirectory = new File(datadirName);
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
		
		createMasterDS();
		
		//        OutputScheduler.getDefault().scheduleForExit(new Runnable(){
		//                    public void run() {
		//                        if (lastDataSetStaxWriter != null){
		//                            try {
		//                                lastDataSetStaxWriter.close();
		//                            } catch (Exception e) {
		//                                GlobalExceptionHandler.handle(e);
		//                            }
		//                        }
		//                    }
		//                });
    }
	
    public WaveformResult process(EventAccessOperations event,
								  Channel channel,
								  RequestFilter[] original,
								  RequestFilter[] available,
								  LocalSeismogramImpl[] seismograms,
								  CookieJar cookieJar)
		throws Exception {
		
		logger.info("Got "+seismograms.length+" seismograms for "+
						ChannelIdUtil.toString(channel.get_id())+
						" for event in "+
						regions.getRegionName(event.get_attributes().region)+
						" at "+event.get_preferred_origin().origin_time.date_time);
		
		if (seismograms.length == 0) {
			return new WaveformResult(seismograms, new StringTreeLeaf(this, true));
		}
		
		URLDataSetSeismogram urlDSS = saveInDataSet(event, channel, seismograms, fileType);
		URL[] urls = urlDSS.getURLs();
		for (int i = 0; i < urls.length; i++) {
			cookieJar.put(getCookieName(prefix, channel.get_id(), i),
						  urls[i].getFile());
		}
		boolean found = false;
		Iterator it = masterDSNames.iterator();
		while (it.hasNext()) {
			if (lastDataSet.getName().equals(it.next())) {
				found = true;
			}
		}
		if ( ! found) {
			masterDSNames.add(lastDataSet.getName());
			updateMasterDataSet(dataSetFile, lastDataSet.getName());
		}
		
		return new WaveformResult(seismograms, new StringTreeLeaf(this, true));
    }
	
    public static String getCookieName(String prefix, ChannelId channel, int i) {
		return  COOKIE_PREFIX+prefix+ChannelIdUtil.toString(channel)+"_"+i;
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
			masterDSFile = new File(dataDirectory, DataSetToXMLStAX.createFileName(masterDataSet));
			if (!masterDSFile.exists()){
				dsToXML.createFile(masterDataSet, dataDirectory, masterDSFile, SeismogramFileTypes.SAC);
			}
		} catch (IOException e) {
			throw new ConfigurationException("Problem trying to create top level dataset", e);
		}catch (XMLStreamException e) {
			throw new ConfigurationException("Problem trying to create top-level dataset", e);
		}
    }
	
    /**
	 * creates a temporary dataset to be used for merging
	 */
    public static DataSet createTempDataSet(String name){
		AuditInfo audit = new AuditInfo(System.getProperty("user.name"),
										"seismogram loaded via sod");
		DataSet tempDS = new MemoryDataSet("temp_dataset",
										   name,
										   System.getProperty("user.name"),
										   new AuditInfo[]{audit});
		return tempDS;
    }
	
    protected void updateMasterDataSet(File childDataset, String childName)
		throws FileNotFoundException, XMLStreamException, IOException{
		
		//the merging code has been rendered useless since the advent of the newlines
		//after end elements, So I'm just going to do it this way since I know this works.
		StAXFileWriter masterDSWriter = XMLUtil.openXMLFileForAppending(masterDSFile);
		dsToXML.writeRef(masterDSWriter.getStreamWriter(),
						 getRelativeURLString(masterDSFile, childDataset),
						 childName);
		masterDSWriter.close();
		
		//      //create temporary dataset with new information to be merged into master dataset
		//      DataSet tempMasterDS = createTempDataSet("Temp Master");
		//      File tempMasterDSFile = new File(dataDirectory, DataSetToXMLStAX.createFileName(tempMasterDS));
		//      StAXFileWriter staxWriter = new StAXFileWriter(tempMasterDSFile);
		//      XMLStreamWriter tempWriter = staxWriter.getStreamWriter();
		//      dsToXML.writeDataSetStartElement(tempWriter);
		//      dsToXML.insertDSInfo(tempWriter, tempMasterDS, dataDirectory, SeismogramFileTypes.SAC);
		//      dsToXML.writeRef(tempWriter,
		//                       getRelativeURLString(masterDSFile, childDataset),
		//                       childName);
		//      XMLUtil.writeEndElementWithNewLine(tempWriter);
		//      staxWriter.close();
		//
		//      XMLUtil.mergeDocs(masterDSFile, tempMasterDSFile, datasetRef, dataSetEl);
		//
		//      if (tempMasterDSFile.exists()){
		//          tempMasterDSFile.delete();
		//      }
    }
	
    //purely a debugging venture
    private void printFile(File file) throws IOException{
		System.out.println("******************");
		System.out.println(file.getName() + ':');
		BufferedReader r1 = new BufferedReader(new FileReader(file));
		StringBuffer buf = new StringBuffer();
		String line;
		while((line = r1.readLine()) != null){
			buf.append(line);
		}
		r1.close();
		System.out.println(buf.toString());
		System.out.println("******************");
    }
	
    protected URLDataSetSeismogram saveInDataSet(EventAccessOperations event,
												 Channel channel,
												 LocalSeismogramImpl[] seismograms,
												 SeismogramFileTypes fileType)
		throws CodecException,
		IOException,
		NoPreferredOrigin,
		UnsupportedFileTypeException,
		SeedFormatException,
		XMLStreamException,
		IncomprehensibleDSMLException,
		ParserConfigurationException {
		
		if (subDS.length() != 0) {
			prepareDataset(event, subDS);
		}
		else {
			prepareDataset(event);
		}
		
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
			
			seisURLStr[i] = getRelativeURLString(dataSetFile, seisFile);
			seisURL[i] = seisFile.toURI().toURL();
			seisFileTypeArray[i] = fileType;  // all are the same
			
			bytesWritten += seisFile.length();
		}
		URLDataSetSeismogram urlDSS = new URLDataSetSeismogram(seisURL,
															   seisFileTypeArray,
															   lastDataSet);
		if (prefix != null && prefix.length() != 0) {
			urlDSS.setName(prefix+urlDSS.getName());
		}
		for (int i = 0; i < seisURL.length; i++) {
			urlDSS.addToCache(seisURL[i], fileType, seismograms[i]);
		}
		
		urlDSS.addAuxillaryData(StdAuxillaryDataNames.NETWORK_BEGIN,
								channel.get_id().network_id.begin_time.date_time);
		urlDSS.addAuxillaryData(StdAuxillaryDataNames.CHANNEL_BEGIN,
								channel.get_id().begin_time.date_time);
		lastDataSet.addDataSetSeismogram(urlDSS, audit);
		StAXFileWriter staxWriter = XMLUtil.openXMLFileForAppending(dataSetFile);
		dsToXML.writeURLDataSetSeismogram(staxWriter.getStreamWriter(),
										  urlDSS,
										  dataSetFile.getParentFile().toURI().toURL());
		
		lastDataSet.addParameter(DataSet.CHANNEL+ChannelIdUtil.toString(channel.get_id()),
								 channel,
								 audit);
		dsToXML.writeParameter(staxWriter.getStreamWriter(),
							   DataSet.CHANNEL+ChannelIdUtil.toString(channel.get_id()),
							   channel);
		lastDataSetFileModTime = dataSetFile.lastModified();
		staxWriter.close();
		return urlDSS;
		
    }
	
    protected File getParentDirectory() {
		return dataDirectory;
    }
	
    protected File getEventDirectory(EventAccessOperations event)
		throws IOException {
		
		String eventDirName = getLabel(event);
		File eventDirectory = new File(dataDirectory, FissuresFormatter.filize(eventDirName));
		if ( ! eventDirectory.exists()) {
			if ( ! eventDirectory.mkdirs()) {
				throw new IOException("Unable to create directory."+eventDirectory);
			} // end of if (!)
		} // end of if (dataDirectory.exits())
		
		return eventDirectory;
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
	
    protected DataSet prepareDataset(EventAccessOperations event)
		throws IOException,
		UnsupportedFileTypeException,
		IncomprehensibleDSMLException,
		ParserConfigurationException,
		XMLStreamException {
		
		File eventDirectory = getEventDirectory(event);
		
		// assume that processing is in event order and never reopens
		// bad but just temporary
		if (lastDataSet != null && lastDataSet.getEvent().equals(event)) {
			return lastDataSet;
		}
		
		//always create it so we can get the file name
		logger.debug("creating new dataset "+getLabel(event));
		DataSet dataset = new MemoryDataSet(EventUtil.extractOrigin(event).origin_time.date_time,
											getLabel(event),
											System.getProperty("user.name"),
											new AuditInfo[0]);
		dataSetFile = new File(eventDirectory, DataSetToXML.createFileName(dataset));
		if (dataSetFile.exists()) {
			dataset = DataSetToXML.load(dataSetFile.toURI().toURL());
		}
		else {
			StAXFileWriter staxWriter =
				new StAXFileWriter(dataSetFile);
			XMLStreamWriter writer = staxWriter.getStreamWriter();
			dsToXML.writeDataSetStartElement(writer);
			dsToXML.insertDSInfo(writer, dataset, eventDirectory, fileType);
			dataset.addParameter(DataSet.EVENT, event, new AuditInfo[0]);
			dsToXML.writeParameter(writer, DataSet.EVENT, event);
			XMLUtil.writeEndElementWithNewLine(writer);
			staxWriter.close();
		}
		
		//if (lastDataSetStaxWriter != null){
		//lastDataSetStaxWriter.close();
		//}
		//lastDataSetStaxWriter = XMLUtil.openXMLFileForAppending(dataSetFile);
		lastDataSet = dataset;
		lastEvent = event;
		
		return dataset;
    }
	public File getDSMLFile(EventAccessOperations event) throws IOException {
		File eventDirectory = getEventDirectory(event);
		DataSet dataset = new MemoryDataSet(EventUtil.extractOrigin(event).origin_time.date_time,
											getLabel(event),
											System.getProperty("user.name"),
											new AuditInfo[0]);
		File dsmlFile = new File(eventDirectory, DataSetToXML.createFileName(dataset));
		if(dsmlFile.exists())
			return dataSetFile;
		else
			throw new FileNotFoundException("Dsml File not found for " + EventUtil.getEventInfo(event));
	}
    public DataSet prepareDataset(EventAccessOperations event, String subDSName)
		throws IOException,
		ParserConfigurationException,
		IncomprehensibleDSMLException,
		UnsupportedFileTypeException, XMLStreamException{
		
		DataSet eventDS = prepareDataset(event);
		String[] dsNames = eventDS.getDataSetNames();
		for (int i = 0; i < dsNames.length; i++) {
			if (dsNames[i].equals(subDSName)) {
				return eventDS.getDataSet(dsNames[i]);
			}
		}
		MemoryDataSet dataset = new MemoryDataSet(EventUtil.extractOrigin(event).origin_time.date_time+"/"+subDSName,
												  subDSName,
												  System.getProperty("user.name"),
												  new AuditInfo[0]);
		eventDS.addDataSet(dataset, new AuditInfo[0]);
		return dataset;
    }
	
    public static long getBytesWritten(){ return bytesWritten; }
	
    private static long bytesWritten = 0;
	
    static long lastDataSetFileModTime;
	
    private static QName dataSetEl = new QName("http://www.seis.sc.edu/xschema/dataset/2.0", "dataset");
    private static QName datasetRef = new QName("http://www.seis.sc.edu/xschema/dataset/2.0", "datasetRef");
    private static QName xlinkHref = new QName("http://www.w3.org/1999/xlink", "href");
	
    private Element config;
	
    static File dataDirectory;
	
    static File masterDSFile;
	
    static List masterDSNames = new ArrayList();
	
    static ParseRegions regions = ParseRegions.getInstance();
	
    static DataSet lastDataSet = null;
	
    static File dataSetFile;
	
    //static StAXFileWriter lastDataSetStaxWriter;
	
    static EventAccessOperations lastEvent = null;
	
    static DataSetToXMLStAX dsToXML = new DataSetToXMLStAX();
	
    SeismogramFileTypes fileType;
	
    String subDS = "";
	
    String prefix = "";
	
    public static final String COOKIE_PREFIX = "SeisFile_";
	
    EventFormatter nameGenerator = null;
	
    private static final Logger logger =
		Logger.getLogger(SaveSeismogramToFile.class);
}

