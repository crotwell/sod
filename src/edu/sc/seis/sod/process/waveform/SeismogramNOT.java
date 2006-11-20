/**
 * SeismogramNOT.java
 * 
 * @author Philip Crotwell
 */
package edu.sc.seis.sod.process.waveform;

import edu.iris.Fissures.IfEvent.EventAccessOperations;
import edu.iris.Fissures.IfNetwork.Channel;
import edu.iris.Fissures.IfSeismogramDC.RequestFilter;
import edu.iris.Fissures.seismogramDC.LocalSeismogramImpl;
import edu.sc.seis.sod.ConfigurationException;
import edu.sc.seis.sod.CookieJar;
import edu.sc.seis.sod.status.StringTreeBranch;
import java.util.Iterator;
import org.w3c.dom.Element;

public class SeismogramNOT extends ForkProcess {

    public SeismogramNOT(Element config) throws ConfigurationException {
        super(config);
    }

    public WaveformResult process(EventAccessOperations event,
                                  Channel channel,
                                  RequestFilter[] original,
                                  RequestFilter[] available,
                                  LocalSeismogramImpl[] seismograms,
                                  CookieJar cookieJar) throws Exception {
        WaveformResult result;
        Iterator it = localSeisProcessList.iterator();
        WaveformProcess processor = (WaveformProcess)it.next();
        synchronized(processor) {
            result = processor.process(event,
                                       channel,
                                       original,
                                       available,
                                       seismograms,
                                       cookieJar);
        }
        return new WaveformResult(result.getSeismograms(),
                                  new StringTreeBranch(this,
                                                       !result.isSuccess(),
                                                       result.getReason()));
    }
}
