/**
 * SeismogramOR.java
 * 
 * @author Philip Crotwell
 */
package edu.sc.seis.sod.process.waveform;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import org.w3c.dom.Element;
import edu.iris.Fissures.IfEvent.EventAccessOperations;
import edu.iris.Fissures.IfNetwork.Channel;
import edu.iris.Fissures.IfSeismogramDC.RequestFilter;
import edu.iris.Fissures.seismogramDC.LocalSeismogramImpl;
import edu.sc.seis.sod.ConfigurationException;
import edu.sc.seis.sod.CookieJar;
import edu.sc.seis.sod.Threadable;
import edu.sc.seis.sod.status.StringTree;
import edu.sc.seis.sod.status.StringTreeBranch;

public class SeismogramOR extends ForkProcess {

    public SeismogramOR(Element config) throws ConfigurationException {
        super(config);
    }

    public WaveformResult process(EventAccessOperations event,
                                  Channel channel,
                                  RequestFilter[] original,
                                  RequestFilter[] available,
                                  LocalSeismogramImpl[] seismograms,
                                  CookieJar cookieJar) throws Exception {
        List reasons = new ArrayList();
        Iterator it = localSeisProcessList.iterator();
        WaveformResult result = new WaveformResult(false, seismograms, this);
        while(it.hasNext() && !result.isSuccess()) {
            WaveformProcess processor = (WaveformProcess)it.next();
            if(processor instanceof Threadable
                    && ((Threadable)processor).isThreadSafe()) {
                result = processor.process(event,
                                           channel,
                                           original,
                                           available,
                                           result.getSeismograms(),
                                           cookieJar);
            } else {
                synchronized(processor) {
                    result = processor.process(event,
                                               channel,
                                               original,
                                               available,
                                               result.getSeismograms(),
                                               cookieJar);
                }
            }
            reasons.add(result.getReason());
        } // end of while (it.hasNext())
        return new WaveformResult(result.getSeismograms(),
                                  new StringTreeBranch(this,
                                                       result.isSuccess(),
                                                       (StringTree[])reasons.toArray(new StringTree[0])));
    }
}
