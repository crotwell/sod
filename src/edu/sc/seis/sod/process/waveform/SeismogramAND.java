/**
 * SeismogramAND.java
 * 
 * @author Philip Crotwell
 */
package edu.sc.seis.sod.process.waveform;

import org.w3c.dom.Element;
import edu.iris.Fissures.IfEvent.EventAccessOperations;
import edu.iris.Fissures.IfNetwork.Channel;
import edu.iris.Fissures.IfSeismogramDC.RequestFilter;
import edu.iris.Fissures.seismogramDC.LocalSeismogramImpl;
import edu.sc.seis.sod.ConfigurationException;
import edu.sc.seis.sod.CookieJar;

public class SeismogramAND extends ForkProcess {

    public SeismogramAND(Element config) throws ConfigurationException {
        super(config);
    }

    public WaveformResult process(EventAccessOperations event,
                                  Channel channel,
                                  RequestFilter[] original,
                                  RequestFilter[] available,
                                  LocalSeismogramImpl[] seismograms,
                                  CookieJar cookieJar) throws Exception {
        return doAND(event,
                     channel,
                     original,
                     available,
                     seismograms,
                     cookieJar);
    }
}
