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

public class NullWaveformProcess implements LocalSeismogramProcess {
    /**
     * Creates a new <code>NullWaveformProcess</code> instance.
     *
     */
    public NullWaveformProcess (){
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
    public LocalSeismogram[] process(EventAccessOperations event, 
			NetworkAccess network, 
			Channel channel, 
			RequestFilter[] original, 
			RequestFilter[] available,
			LocalSeismogram[] seismograms, 
			CookieJar cookies) {
	return seismograms;
    }
   

}// NullWaveformProcess
