package edu.sc.seis.sod.process.waveform;

import org.w3c.dom.Element;
import edu.iris.Fissures.IfEvent.EventAccessOperations;
import edu.iris.Fissures.IfNetwork.Channel;
import edu.iris.Fissures.IfSeismogramDC.RequestFilter;
import edu.iris.Fissures.seismogramDC.LocalSeismogramImpl;
import edu.sc.seis.fissuresUtil.bag.Calculus;
import edu.sc.seis.sod.ConfigurationException;
import edu.sc.seis.sod.CookieJar;


/**
 * @author crotwell
 * Created on Mar 18, 2005
 */
public class Integrate implements WaveformProcess {
    
    public Integrate (Element config) throws ConfigurationException {
        this.config = config;
    }
    
    /**
     *
     */
    public WaveformResult process(EventAccessOperations event,
                                  Channel channel,
                                  RequestFilter[] original,
                                  RequestFilter[] available,
                                  LocalSeismogramImpl[] seismograms,
                                  CookieJar cookieJar) throws Exception {
        for(int i = 0; i < seismograms.length; i++) {
            seismograms[i] = Calculus.integrate(seismograms[i]);
        }
        return new WaveformResult(true, seismograms, this);
    }
    
    Element config;
}
