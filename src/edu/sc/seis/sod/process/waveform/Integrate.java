package edu.sc.seis.sod.process.waveform;

import edu.iris.Fissures.IfEvent.EventAccessOperations;
import edu.iris.Fissures.IfNetwork.Channel;
import edu.iris.Fissures.IfSeismogramDC.RequestFilter;
import edu.iris.Fissures.seismogramDC.LocalSeismogramImpl;
import edu.sc.seis.sod.CookieJar;


/**
 * @author crotwell
 * Created on Mar 18, 2005
 */
public class Integrate implements WaveformProcess {

    /**
     *
     */
    public Integrate() {
        super();
        // TODO Auto-generated constructor stub
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
        // TODO Auto-generated method stub
        return null;
    }
}
