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

public class RecordSectionDisplayGenerator implements WaveformProcess{
	
	public RecordSectionDisplayGenerator(Element config) {
		
	}
	public WaveformResult process(EventAccessOperations event, Channel channel,
								  RequestFilter[] original, RequestFilter[] available,
								  LocalSeismogramImpl[] seismograms,
								  CookieJar cookieJar) throws Exception {
		/*	try {
			DataSet ds = DataSetToXML.load(SaveSeismogramToFile.getDSMLFile(event).toURI().toURL());
			dss = new DataSetSeismogram[ds.getDataSetSeismogramNames().length];
			for(int i=0;i<ds.getDataSetSeismogramNames().length;i++){
				dss[i] = ds.getDataSetSeismogram(ds.getDataSetSeismogramNames()[i]);
			}
			RecordSectionDisplay display = new RecordSectionDisplay();
			display.add(dss);
			try {
				File outPNG = new File(SaveSeismogramToFile.getEventDirectory(event),"recordSection.png");
				display.outputToPNG(outPNG,new Dimension(500,500));
			} catch (IOException e) {
				throw new IOException("Problem writing recordSection output to PNG " +e);
			}
		}catch(IOException e) {
			throw new IOException("Problem opening dsml file" + e);
			
		 } */
		return new WaveformResult(seismograms, new StringTreeLeaf(this, true));
	}
	public DataSetSeismogram[] dss;
}
