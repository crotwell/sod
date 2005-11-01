/**
 * AlwaysSuccess.java
 * 
 * @author Philip Crotwell
 */
package edu.sc.seis.sod.process.waveform;

import org.w3c.dom.Element;
import edu.iris.Fissures.IfEvent.EventAccessOperations;
import edu.iris.Fissures.IfNetwork.Channel;
import edu.iris.Fissures.IfSeismogramDC.RequestFilter;
import edu.iris.Fissures.seismogramDC.LocalSeismogramImpl;
import edu.sc.seis.fissuresUtil.exceptionHandler.GlobalExceptionHandler;
import edu.sc.seis.sod.ConfigurationException;
import edu.sc.seis.sod.CookieJar;
import edu.sc.seis.sod.status.StringTreeBranch;

public class AlwaysSuccess extends ResultWrapper {

    public AlwaysSuccess(Element config) throws ConfigurationException {
        super(config);
    }

    public WaveformResult process(EventAccessOperations event,
                                  Channel channel,
                                  RequestFilter[] original,
                                  RequestFilter[] available,
                                  LocalSeismogramImpl[] seismograms,
                                  CookieJar cookieJar) throws Exception {
        try {
            WaveformResult result = subprocess.process(event,
                                                       channel,
                                                       original,
                                                       available,
                                                       seismograms,
                                                       cookieJar);
            return new WaveformResult(result.getSeismograms(),
                                      new StringTreeBranch(this,
                                                           true,
                                                           result.getReason()));
        } catch(Exception e) {
            GlobalExceptionHandler.handle("Caught an exception inside Always Success and moving on ...",
                                          e);
            return new WaveformResult(true, seismograms);
        }
    }

    public String toString() {
        return "AlwaysSuccess(" + subprocess.toString() + ")";
    }
}
