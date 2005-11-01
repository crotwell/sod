package edu.sc.seis.sod.process.waveform;

import org.w3c.dom.Element;
import edu.iris.Fissures.IfEvent.EventAccessOperations;
import edu.iris.Fissures.IfNetwork.Channel;
import edu.iris.Fissures.IfSeismogramDC.RequestFilter;
import edu.iris.Fissures.seismogramDC.LocalSeismogramImpl;
import edu.sc.seis.sod.ConfigurationException;
import edu.sc.seis.sod.CookieJar;
import edu.sc.seis.sod.status.StringTreeLeaf;

public class ExampleExternalProcess implements WaveformProcess {

    public ExampleExternalProcess(Element config) throws ConfigurationException {}

    public WaveformResult process(EventAccessOperations event,
                                  Channel channel,
                                  RequestFilter[] original,
                                  RequestFilter[] available,
                                  LocalSeismogramImpl[] seismograms,
                                  CookieJar cookieJar) throws Exception {
        if(seismograms.length == 1) {
            return new WaveformResult(true, seismograms, this);
        } else {
            return new WaveformResult(seismograms,
                                      new StringTreeLeaf("seismograms does not have length one, "
                                                                 + seismograms.length,
                                                         false));
        }
    }
}