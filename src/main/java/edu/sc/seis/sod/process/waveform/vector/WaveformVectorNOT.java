/**
 * ChannelGroupNOT.java
 * 
 * @author Created by Omnicore CodeGuide
 */
package edu.sc.seis.sod.process.waveform.vector;

import java.util.Iterator;

import org.w3c.dom.Element;

import edu.iris.Fissures.IfSeismogramDC.RequestFilter;
import edu.iris.Fissures.seismogramDC.LocalSeismogramImpl;
import edu.sc.seis.fissuresUtil.cache.CacheEvent;
import edu.sc.seis.fissuresUtil.hibernate.ChannelGroup;
import edu.sc.seis.sod.ConfigurationException;
import edu.sc.seis.sod.MotionVectorArm;
import edu.sc.seis.sod.hibernate.eventpair.CookieJar;
import edu.sc.seis.sod.status.StringTree;
import edu.sc.seis.sod.status.StringTreeBranch;

public class WaveformVectorNOT extends WaveformVectorFork {

    public WaveformVectorNOT(Element config) throws ConfigurationException {
        super(config);
    }

    public WaveformVectorResult accept(CacheEvent event,
                                        ChannelGroup channelGroup,
                                        RequestFilter[][] original,
                                        RequestFilter[][] available,
                                        LocalSeismogramImpl[][] seismograms,
                                        CookieJar cookieJar) throws Exception {
        // pass originals to the contained processors
        WaveformVectorProcess processor;
        Iterator it = processes.iterator();
        processor = (WaveformVectorProcess)it.next();
        WaveformVectorResult result = MotionVectorArm.runProcessorThreadCheck(processor,
                                                         event,
                                                         channelGroup,
                                                         original,
                                                         available,
                                                         seismograms,
                                                         cookieJar);
        return new WaveformVectorResult(result.getSeismograms(),
                                        new StringTreeBranch(this,
                                                             !result.isSuccess(),
                                                             new StringTree[] {result.getReason()}));
    }

    public boolean isThreadSafe() {
        return true;
    }
}