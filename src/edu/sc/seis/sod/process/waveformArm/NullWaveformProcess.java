package edu.sc.seis.sod.process.waveformArm;

import edu.iris.Fissures.IfEvent.EventAccessOperations;
import edu.iris.Fissures.IfNetwork.Channel;
import edu.iris.Fissures.IfSeismogramDC.RequestFilter;
import edu.iris.Fissures.seismogramDC.LocalSeismogramImpl;
import edu.sc.seis.sod.CookieJar;
import edu.sc.seis.sod.status.StringTreeLeaf;
import org.w3c.dom.Element;

/**
 * sample xml
 * &lt;printlineWaveformProcessor/&gt;
 */

public class NullWaveformProcess implements WaveformProcess {
    /**
     * Creates a new <code>NullWaveformProcess</code> instance.
     *
     */
    public NullWaveformProcess (){
    }

    public NullWaveformProcess (Element config){
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
    public LocalSeismogramResult process(EventAccessOperations event,
                                         Channel channel,
                                         RequestFilter[] original,
                                         RequestFilter[] available,
                                         LocalSeismogramImpl[] seismograms, CookieJar cookieJar) {
        return new LocalSeismogramResult(true, seismograms, new StringTreeLeaf(this, true));
    }


}// NullWaveformProcess
