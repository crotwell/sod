/**
 * SeismogramNOT.java
 * 
 * @author Philip Crotwell
 */
package edu.sc.seis.sod.process.waveform;

import java.util.Iterator;

import org.w3c.dom.Element;

import edu.iris.Fissures.IfSeismogramDC.RequestFilter;
import edu.iris.Fissures.network.ChannelImpl;
import edu.iris.Fissures.seismogramDC.LocalSeismogramImpl;
import edu.sc.seis.fissuresUtil.cache.CacheEvent;
import edu.sc.seis.sod.ConfigurationException;
import edu.sc.seis.sod.CookieJar;
import edu.sc.seis.sod.LocalSeismogramArm;
import edu.sc.seis.sod.status.StringTreeBranch;

public class SeismogramNOT extends ForkProcess {

    public SeismogramNOT(Element config) throws ConfigurationException {
        super(config);
    }

    public WaveformResult process(CacheEvent event,
                                  ChannelImpl channel,
                                  RequestFilter[] original,
                                  RequestFilter[] available,
                                  LocalSeismogramImpl[] seismograms,
                                  CookieJar cookieJar) throws Exception {
        WaveformResult result;
        Iterator it = localSeisProcessList.iterator();
        WaveformProcess process = (WaveformProcess)it.next();
        result = LocalSeismogramArm.runProcessorThreadCheck(process, 
                                                            event,
                                                            channel,
                                                            original,
                                                            available,
                                                            seismograms,
                                                            cookieJar);
        return new WaveformResult(result.getSeismograms(),
                                  new StringTreeBranch(this,
                                                       !result.isSuccess(),
                                                       result.getReason()));
    }
}
