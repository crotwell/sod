package edu.sc.seis.sod.subsetter.waveFormArm;

import edu.sc.seis.fissuresUtil.display.ParseRegions;
import edu.sc.seis.sod.*;

import edu.iris.Fissures.IfEvent.*;
import edu.iris.Fissures.event.*;
import edu.iris.Fissures.IfNetwork.*;
import edu.iris.Fissures.network.*;
import edu.iris.Fissures.IfSeismogramDC.*;
import org.w3c.dom.*;

import java.io.*;

/**
 * sample xml 
 * &lt;printlineWaveformProcessor/&gt;
 */

public class PrintlineWaveformProcessor implements LocalSeismogramProcess {
    /**
     * Creates a new <code>PrintlineWaveformProcessor</code> instance.
     *
     * @param config an <code>Element</code> value
     */
    public PrintlineWaveformProcessor (Element config){
	regions = new ParseRegions();
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
     */
    public void process(EventAccessOperations event, 
			NetworkAccess network, 
			Channel channel, 
			RequestFilter[] original, 
			RequestFilter[] available,
			LocalSeismogram[] seismograms, 
			CookieJar cookies) {
	
    }
   
    ParseRegions regions;

}// PrintlineWaveformProcessor
