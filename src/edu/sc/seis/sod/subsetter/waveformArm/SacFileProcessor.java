
package edu.sc.seis.sod.subsetter.waveFormArm;

import edu.sc.seis.fissuresUtil.display.ParseRegions;
import edu.sc.seis.fissuresUtil.sac.*;
import edu.sc.seis.fissuresUtil.xml.*;
import edu.sc.seis.sod.*;
import edu.iris.Fissures.*;
import edu.iris.Fissures.IfEvent.*;
import edu.iris.Fissures.event.*;
import edu.iris.Fissures.IfNetwork.*;
import edu.iris.Fissures.network.*;
import edu.iris.Fissures.IfSeismogramDC.*;
import edu.iris.Fissures.seismogramDC.*;
import javax.xml.parsers.*;

import org.w3c.dom.*;
import org.apache.log4j.*;

import java.io.*;

/**
 * SacFileProcessor.java
     * A example config Element is:<br>
     * <pre>
     * &lt;SacFileProcessor>
     *    &lt;dataDirectory>research/sodtest/data&lt;/dataDirectory>
     * &lt;/SacFileProcessor>
     * </pre>
 *
 *
 * Created: Tue Mar 19 14:08:39 2002
 *
 * @author <a href="mailto:crotwell@pooh">Philip Crotwell</a>
 * @version
 */

public class SacFileProcessor implements LocalSeismogramProcess {
    /**
     * Creates a new <code>SacFileProcessor</code> instance.
     *
     * @param config an <code>Element</code> that contains the configuration
     * for this Processor
     */
    public SacFileProcessor (Element config) {
	this.config = config;
	regions = new ParseRegions();
	String datadirName = 
	    SodUtil.getText(SodUtil.getElement(config, "dataDirectory"));
	this.dataDirectory = new File(datadirName);
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
    public void process(EventAccessOperations event, 
			NetworkAccess network, 
			Channel channel, 
			RequestFilter[] original, 
			RequestFilter[] available,
			LocalSeismogram[] seismograms, 
			CookieJar cookies) throws Exception {
	try {
	    System.out.println("Got "+seismograms.length+" seismograms for "+
			       ChannelIdUtil.toStringNoDates(channel.get_id())+
			       " for event in "+
			       regions.getRegionName(event.get_attributes().region)+
			       " at "+event.get_preferred_origin().origin_time.date_time);
	    if ( ! dataDirectory.exists()) {
		if ( ! dataDirectory.mkdirs()) {
		    throw new ConfigurationException("Unable to create directory."+dataDirectory);
		} // end of if (!)
		
	    } // end of if (dataDirectory.exits())
	    String eventDirName = 
		regions.getRegionName(event.get_attributes().region)+
		" "+event.get_preferred_origin().origin_time.date_time;
	    eventDirName = eventDirName.replace(' ', '_');
	    eventDirName = eventDirName.replace(',', '_');
	    File eventDirectory = new File(dataDirectory, eventDirName);
	    if ( ! eventDirectory.exists()) {
		if ( ! eventDirectory.mkdirs()) {
		    throw new ConfigurationException("Unable to create directory."+eventDirectory);
		} // end of if (!)
	    } // end of if (dataDirectory.exits())

	    // load dataset if it already exists
	    File dsFile = new File(eventDirectory, eventDirName+".dsml");
	    XMLDataSet dataset;
	    if (dsFile.exists()) {
		dataset = XMLDataSet.load(dsFile.toURL());
	    } else {
		DocumentBuilderFactory factory
		    = DocumentBuilderFactory.newInstance();
		DocumentBuilder docBuilder = factory.newDocumentBuilder();
		dataset = new XMLDataSet(docBuilder, 
					 eventDirectory.toURL(), 
					 "genid"+Math.round(Math.random()*Integer.MAX_VALUE),
					 eventDirName,
					 System.getProperty("user.name"));

		// add event since dataset is new
		Document doc = dataset.getElement().getOwnerDocument();

//		XMLParameter.insert(dataset.getElement(),"event_info", event);
		AuditInfo[] audit = new AuditInfo[1];
		audit[0] = new AuditInfo(System.getProperty("user.name"),
					 "event loaded via sod.");
		dataset.addParameter( "event", event, audit);
	    } // end of else

	    SacTimeSeries sac;
	    String seisFilename = "";
	    for (int i=0; i<seismograms.length; i++) {
		seisFilename = ChannelIdUtil.toStringNoDates(seismograms[i].channel_id);
		File seisFile = new File(eventDirectory, seisFilename); 
		int n =0;
		while (seisFile.exists()) {
		    n++;
		    
		    seisFilename = 
			ChannelIdUtil.toStringNoDates(seismograms[i].channel_id)+"."+n;
		    seisFile = new File(eventDirectory, seisFilename);
		} // end of while (seisFile.exists())
		LocalSeismogramImpl lseis = 
		    (LocalSeismogramImpl)seismograms[i];
		sac = FissuresToSac.getSAC(lseis,
					   channel,
					   event.get_preferred_origin());
		sac.write(seisFile);
		AuditInfo[] audit = new AuditInfo[1];
		audit[0] = new AuditInfo(System.getProperty("user.name"),
					 "seismogram loaded via sod.");
		dataset.addSeismogramRef(lseis, seisFile.toURL(), 
					 seisFilename, 
					 new Property[0], 
					 lseis.parm_ids,
					 audit);
	    }
	    try {
		File outFile = new File(eventDirectory, eventDirName+".dsml");
		OutputStream fos = new BufferedOutputStream(
				      new FileOutputStream(outFile));
		dataset.write(fos);
		fos.close();
	    } catch(Exception ex) {
		System.out.println("EXCEPTION CAUGHT WHILE trying to save dataset"
				   +ex.toString());
		ex.printStackTrace();
	    }
	} catch(Exception e) {
	    
	    System.out.println("Exception caught while writing to file in PrintLineWaveformProcess");
		e.printStackTrace();
	}
	
    }
   
    ParseRegions regions;

    Element config;

    File dataDirectory;

    static Category logger = 
	Category.getInstance(LocalSeismogramArm.class.getName());
    
}// SacFileProcessor
