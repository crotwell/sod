/**
 * SeismogramAND.java
 * 
 * @author Philip Crotwell
 */
package edu.sc.seis.sod.process.waveform;

import org.w3c.dom.Element;

import edu.sc.seis.seisFile.fdsnws.stationxml.Channel;
import edu.sc.seis.sod.ConfigurationException;
import edu.sc.seis.sod.hibernate.eventpair.CookieJar;
import edu.sc.seis.sod.model.event.CacheEvent;
import edu.sc.seis.sod.model.seismogram.LocalSeismogramImpl;
import edu.sc.seis.sod.model.seismogram.RequestFilter;

public class SeismogramAND extends ForkProcess {

    public SeismogramAND(Element config) throws ConfigurationException {
        super(config);
    }

    public WaveformResult accept(CacheEvent event,
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
