package edu.sc.seis.sod.subsetter.waveFormArm;

import edu.sc.seis.fissuresUtil.display.ParseRegions;
import edu.sc.seis.fissuresUtil.sac.*;
import edu.sc.seis.sod.*;
import edu.iris.Fissures.IfEvent.*;
import edu.iris.Fissures.event.*;
import edu.iris.Fissures.IfNetwork.*;
import edu.iris.Fissures.network.*;
import edu.iris.Fissures.IfSeismogramDC.*;
import edu.iris.Fissures.seismogramDC.*;
import org.w3c.dom.*;

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
	    File eventDirectory = new File(dataDirectory, eventDirName);
	    if ( ! eventDirectory.exists()) {
		if ( ! eventDirectory.mkdirs()) {
		    throw new ConfigurationException("Unable to create directory."+eventDirectory);
		} // end of if (!)
	    } // end of if (dataDirectory.exits())

	    SacTimeSeries sac;
	    for (int i=0; i<seismograms.length; i++) {
		File seisFile = new File(eventDirectory, 
					 ChannelIdUtil.toStringNoDates(seismograms[i].channel_id));
		int n =0;
		while (seisFile.exists()) {
		    n++;
		    seisFile = new File(eventDirectory,
					ChannelIdUtil.toStringNoDates(seismograms[i].channel_id)+"."+n);
		} // end of while (seisFile.exists())
		
		sac = FissuresToSac.getSAC((LocalSeismogramImpl)seismograms[i],
					   channel,
					   event.get_preferred_origin());
		sac.write(seisFile);
	    } // end of for (int i=0; i<seismograms.length; i++)
	    
	} catch(Exception e) {
	    
	    System.out.println("Exception caught while writing to file in PrintLineWaveformProcess");
	}
	
    }
   
    ParseRegions regions;

    Element config;

    File dataDirectory;

}// SacFileProcessor
