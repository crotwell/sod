package edu.sc.seis.sod.process.waveform;

import edu.sc.seis.fissuresUtil.xml.DataSet;
import java.net.URL;
import edu.sc.seis.fissuresUtil.xml.DataSetToXML;
import javax.xml.parsers.ParserConfigurationException;
import java.io.IOException;
import java.io.*;
import edu.sc.seis.fissuresUtil.xml.DataSetSeismogram;
import edu.sc.seis.fissuresUtil.display.RecordSectionDisplay;
import org.apache.log4j.BasicConfigurator;
import edu.sc.seis.sod.CookieJar;
import edu.iris.Fissures.IfSeismogramDC.RequestFilter;
import edu.iris.Fissures.seismogramDC.LocalSeismogramImpl;
import edu.iris.Fissures.IfNetwork.Channel;
import edu.iris.Fissures.IfEvent.EventAccessOperations;
import org.w3c.dom.Element;
import edu.sc.seis.fissuresUtil.xml.DataSet;
import edu.sc.seis.sod.status.StringTreeLeaf;
import java.awt.Dimension;
import edu.sc.seis.sod.ConfigurationException;
import edu.sc.seis.fissuresUtil.xml.IncomprehensibleDSMLException;
import edu.sc.seis.fissuresUtil.xml.UnsupportedFileTypeException;
import edu.sc.seis.sod.Start;
import edu.sc.seis.sod.process.waveform.vector.WaveformVectorProcess;
import edu.sc.seis.sod.status.waveformArm.WaveformMonitor;

public class RecordSectionDisplayGenerator implements WaveformProcess{
	
	public RecordSectionDisplayGenerator(Element config) throws ConfigurationException {
	
	}
	public SaveSeismogramToFile getSaveSeismogramToFile() throws ConfigurationException {
		WaveformProcess[] waveformProcesses = Start.getWaveformArm().getLocalSeismogramArm().getProcesses();
		for(int i=0;i<waveformProcesses.length;i++) {
			if(waveformProcesses[i] instanceof SaveSeismogramToFile) {
				return (SaveSeismogramToFile) waveformProcesses[i];
			}
		}
		throw new ConfigurationException("RecordSectionDisplayGenerator needs a SaveSeismogramToFile process");
	}
	public WaveformResult process(EventAccessOperations event, Channel channel,
								  RequestFilter[] original, RequestFilter[] available,
								  LocalSeismogramImpl[] seismograms,
								  CookieJar cookieJar) throws ParserConfigurationException,
								  IOException, IncomprehensibleDSMLException,
								  UnsupportedFileTypeException, ConfigurationException   {
		try {
				saveSeisToFile = getSaveSeismogramToFile();
			DataSet ds = DataSetToXML.load(saveSeisToFile.getDSMLFile(event).toURI().toURL());
			String[] dataSeisNames = ds.getDataSetSeismogramNames();
			int numDSSeismograms = dataSeisNames.length;
			dss = new DataSetSeismogram[numDSSeismograms];
			for(int i=0;i<numDSSeismograms;i++){
				dss[i] = ds.getDataSetSeismogram(dataSeisNames[i]);
			}
			RecordSectionDisplay recordSectiondisplay = new RecordSectionDisplay();
			recordSectiondisplay.add(dss);
			try {
				File outPNG = new File(saveSeisToFile.getEventDirectory(event),"recordSection.png");
				recordSectiondisplay.outputToPNG(outPNG,new Dimension(500,500));
			} catch (IOException e) {
				throw new IOException("Problem writing recordSection output to PNG " +e);
			}
		}catch(IOException e) {
			throw new IOException("Problem opening dsml file in RecordSectionDisplayGenerator" + e);
		}
		return new WaveformResult(seismograms, new StringTreeLeaf(this, true));
	}
	public DataSetSeismogram[] dss;
	private SaveSeismogramToFile saveSeisToFile;
}
