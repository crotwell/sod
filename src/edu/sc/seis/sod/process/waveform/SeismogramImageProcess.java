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
import edu.iris.Fissures.IfSeismogramDC.RequestFilter;
import edu.iris.Fissures.model.MicroSecondDate;
import edu.iris.Fissures.model.TimeInterval;
import edu.iris.Fissures.model.UnitImpl;
import edu.iris.Fissures.seismogramDC.LocalSeismogramImpl;
import edu.sc.seis.TauP.Arrival;
import edu.sc.seis.TauP.TauModelException;
import edu.sc.seis.TauP.TauP_Time;
import edu.sc.seis.fissuresUtil.bag.TauPUtil;
import edu.sc.seis.fissuresUtil.cache.EventUtil;
import edu.sc.seis.fissuresUtil.display.BasicSeismogramDisplay;
import edu.sc.seis.fissuresUtil.display.DisplayUtils;
import edu.sc.seis.fissuresUtil.display.PhasePhilter;
import edu.sc.seis.fissuresUtil.display.registrar.PhaseAlignedTimeConfig;
import edu.sc.seis.fissuresUtil.exceptionHandler.GlobalExceptionHandler;
import edu.sc.seis.fissuresUtil.xml.DataSet;
import edu.sc.seis.fissuresUtil.xml.MemoryDataSet;
import edu.sc.seis.fissuresUtil.xml.MemoryDataSetSeismogram;
import edu.sc.seis.sod.CookieJar;
import edu.sc.seis.sod.FlagData;
import edu.sc.seis.sod.SodFlag;
import edu.sc.seis.sod.SodUtil;
import edu.sc.seis.sod.status.ChannelFormatter;
import edu.sc.seis.sod.status.EventFormatter;
import edu.sc.seis.sod.status.FileWritingTemplate;
import edu.sc.seis.sod.status.FissuresFormatter;
import edu.sc.seis.sod.status.StationFormatter;
import edu.sc.seis.sod.status.StringTreeLeaf;
import edu.sc.seis.sod.status.TemplateFileLoader;
import edu.sc.seis.sod.subsetter.requestGenerator.PhaseRequest;

public class SeismogramImageProcess implements WaveformProcess {
	
    private Logger logger = Logger.getLogger(SeismogramImageProcess.class);
	
    private String fileDir;
	
    private EventFormatter eventFormatter;
	
    private StationFormatter stationFormatter;
	
    private ChannelFormatter chanFormatter;
	
    private TauPUtil tauP;
	
    public SeismogramImageProcess(String fileDir,
								  EventFormatter eventDirFormatter,
								  StationFormatter stationDirFormatter,
								  ChannelFormatter imageNameFormatter) throws Exception {
		this.fileDir = fileDir;
		eventFormatter = eventDirFormatter;
		stationFormatter = stationDirFormatter;
		chanFormatter = imageNameFormatter;
		initTaup();
    }
	
    public SeismogramImageProcess(Element el) throws Exception {
		NodeList nl = el.getChildNodes();
		for(int i = 0; i < nl.getLength(); i++) {
			Node n = nl.item(i);
			if(n.getNodeName().equals("fileDir")) {
				fileDir = n.getFirstChild().getNodeValue();
			} else if(n.getNodeName().equals("phaseWindow")) {
				phaseWindow = new PhaseWindow((Element)n);
			} else if(n.getNodeName().equals("seismogramConfig")) {
				Element seismogramImageConfig = TemplateFileLoader.getTemplate((Element)n);
				Node tmpEl = SodUtil.getElement(seismogramImageConfig,
												"outputLocation");
				Node tmpEl2 = SodUtil.getElement((Element)tmpEl, "eventDir");
				eventFormatter = new EventFormatter((Element)tmpEl2);
				tmpEl2 = SodUtil.getElement((Element)tmpEl, "stationDir");
				stationFormatter = new StationFormatter((Element)tmpEl2);
				seismogramImageConfig.removeChild(tmpEl);
				tmpEl = SodUtil.getElement(seismogramImageConfig, "picName");
				chanFormatter = new ChannelFormatter((Element)tmpEl);
				seismogramImageConfig.removeChild(tmpEl);
			} else if(n.getNodeName().equals("modelName")) {
				modelName = SodUtil.getNestedText((Element)n);
			} else if(n.getNodeName().equals("prefix")) {
				prefix = SodUtil.getNestedText((Element)n);
			} else if(n.getNodeName().equals("fileType")) {
				fileType = SodUtil.getNestedText((Element)n);
			}
		}
		if(fileDir == null) {
			fileDir = FileWritingTemplate.getBaseDirectoryName();
		}
		if(fileDir == null || eventFormatter == null
		   || stationFormatter == null || chanFormatter == null) { throw new IllegalArgumentException("The configuration element must contain a fileDir and a waveformSeismogramConfig"); }
		initTaup();
    }
	
    private void initTaup() throws TauModelException {
		tauP = new TauPUtil(modelName);
		if(tauptime == null) {
			tauptime = new TauP_Time("iasp91");
			tauptime.clearPhaseNames();
			tauptime.appendPhaseName("P");
		}
    }
	
    /**
	 * Processes localSeismograms, possibly modifying them.
	 *
	 * @param event
	 *            an <code>EventAccessOperations</code> value
	 * @param network
	 *            a <code>NetworkAccess</code> value
	 * @param channel
	 *            a <code>Channel</code> value
	 * @param original
	 *            a <code>RequestFilter[]</code> value
	 * @param available
	 *            a <code>RequestFilter[]</code> value
	 * @param seismograms
	 *            a <code>LocalSeismogram[]</code> value
	 * @param cookies
	 *            a <code>CookieJar</code> value
	 * @exception Exception
	 *                if an error occurs
	 */
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
					   fileType,
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
					   phases,
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
					   relativeTime,
					   cookieJar);
    }
	
    /** allows specifying a fileType, png or pdf, and a list of phases. */
    public WaveformResult process(EventAccessOperations event,
								  Channel channel,
								  RequestFilter[] original,
								  LocalSeismogramImpl[] seismograms,
								  final String fileType,
								  String[] phases,
								  boolean relTime,
								  final CookieJar cookieJar) throws Exception {
		logger.debug("process() called");
		// only needed if relTime
		PhaseAlignedTimeConfig phaseTime = null;
		if(relTime) {
			phaseTime = new PhaseAlignedTimeConfig();
			phaseTime.setTauP(tauptime);
		}
		final BasicSeismogramDisplay bsd = relTime ? new BasicSeismogramDisplay(phaseTime)
			: new BasicSeismogramDisplay();
		MemoryDataSetSeismogram memDSS = null;
		PhaseRequest phaseRequest = null;
		String tempPrefix = "";
		if(phaseWindow == null) {
			memDSS = new MemoryDataSetSeismogram(original[0], "");
			memDSS.setBeginTime(DisplayUtils.firstBeginDate(original)
									.getFissuresTime());
			memDSS.setEndTime(DisplayUtils.lastEndDate(original)
								  .getFissuresTime());
			tempPrefix = "original_";
		} else {
			phaseRequest = phaseWindow.getPhaseRequest();
			RequestFilter[] request = phaseRequest.generateRequest(event,
																   channel,
																   null);
			memDSS = new MemoryDataSetSeismogram(request[0], "");
			memDSS.setBeginTime(DisplayUtils.firstBeginDate(request)
									.getFissuresTime());
			memDSS.setEndTime(DisplayUtils.lastEndDate(request)
								  .getFissuresTime());
		}
		for(int i = 0; i < seismograms.length; i++) {
			memDSS.add(seismograms[i]);
		}
		DataSet dataset = new MemoryDataSet("temp", "Temp Dataset for "
												+ memDSS.getName(), "temp", new AuditInfo[0]);
		dataset.addDataSetSeismogram(memDSS, new AuditInfo[0]);
		dataset.addParameter(DataSet.EVENT, event, new AuditInfo[0]);
		bsd.add(new MemoryDataSetSeismogram[] {memDSS});
		Origin origin = EventUtil.extractOrigin(event);
		TimeInterval filterOffset = new TimeInterval(10 , UnitImpl.SECOND);
		final MicroSecondDate originTime = new MicroSecondDate(origin.origin_time);
		final Arrival[] arrivals = PhasePhilter.filter(tauP.calcTravelTimes(channel.my_site.my_station,
																			origin,
																			phases), filterOffset);
		final SodFlag[] flags = new SodFlag[arrivals.length];
		final String phasePrefix = tempPrefix;
		for(int i = 0; i < arrivals.length; i++) {
			MicroSecondDate flagTime = originTime.add(new TimeInterval(arrivals[i].getTime(),
																	   UnitImpl.SECOND));
			flags[i] = new SodFlag(flagTime, arrivals[i].getName(), bsd);
			bsd.add(flags[i]);
		}
		final String picFileName = FissuresFormatter.filize(fileDir + '/'
																+ eventFormatter.getResult(event) + '/'
																+ stationFormatter.getResult(channel.my_site.my_station) + '/'
																+ prefix + chanFormatter.getResult(channel) + "." + fileType);
		SwingUtilities.invokeAndWait(new Runnable() {
					
					public void run() {
						logger.debug("writing " + picFileName);
						try {
							if(fileType.equals(PDF)) {
								bsd.outputToPDF(new File(picFileName));
							} else {
								bsd.outputToPNG(new File(picFileName), dimension);
							}
							/*Currently only the regions around
							 first P and first S Flags are made clickable
							 */
							int pFlagCount = NUM_PFLAGS_MARKED;
							int sFlagCount = NUM_SFLAGS_MARKED;
							for(int i = 0; i < arrivals.length; i++) {
								String phase = arrivals[i].getName();
								boolean putData = false;
								if((phase.startsWith("P") && (pFlagCount != 0)) ||
									   (phase.startsWith("S") && (sFlagCount !=0))) {
									putData = true;
									if(phase.startsWith("P"))
										pFlagCount--;
									else if(phase.startsWith("S"))
										sFlagCount--;
								}
								if(putData) {
									FlagData flagData = flags[i].getFlagData();
									cookieJar.put(phasePrefix
													  + "SeismogramImageProcess_flagPixels_"
													  + arrivals[i].getName(), flagData);
								}
							}
						} catch(Throwable e) {
							GlobalExceptionHandler.handle("unable to save seismogram image to "
															  + picFileName,
														  e);
						}
					}
				});
		return new WaveformResult(true, seismograms, new StringTreeLeaf(this,
																		true));
    }
	
    private CookieJar cookierJar = null;
	
    private static TauP_Time tauptime = null;
	
    private static Dimension dimension = new Dimension(500, 200);
	
    //private static String[] phases = {"P", "S"};
    private static String[] phases = {"ttp", "tts"};
	
    private PhaseWindow phaseWindow = null;
	
    private String modelName = "iasp91";
	
    private String prefix = "";
	
    private String fileType = PNG;
	
    private boolean relativeTime = false;
	
    public static final String PDF = "pdf";
	
	private static final int NUM_PFLAGS_MARKED = 1;
	
	private static final int NUM_SFLAGS_MARKED = 1;
	
    public static final String PNG = "png";
}
