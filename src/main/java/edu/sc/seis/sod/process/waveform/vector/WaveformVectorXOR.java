/**
 * ChannelGroupXOR.java
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
import edu.sc.seis.sod.CookieJar;
import edu.sc.seis.sod.MotionVectorArm;
import edu.sc.seis.sod.status.StringTree;
import edu.sc.seis.sod.status.StringTreeBranch;

public class WaveformVectorXOR extends WaveformVectorFork {

    public WaveformVectorXOR(Element config) throws ConfigurationException {
        super(config);
    }

    public WaveformVectorResult accept(CacheEvent event,
                                                     ChannelGroup channelGroup,
                                                     RequestFilter[][] original,
                                                     RequestFilter[][] available,
                                                     LocalSeismogramImpl[][] seismograms,
                                                     CookieJar cookieJar) throws Exception {
        WaveformVectorResult resultA, resultB;
        WaveformVectorProcess processorA, processorB;
        Iterator it = cgProcessList.iterator();
        processorA = (WaveformVectorProcess)it.next();
        processorB = (WaveformVectorProcess)it.next();
        resultA = MotionVectorArm.runProcessorThreadCheck(processorA,
                                                         event,
                                                         channelGroup,
                                                         original,
                                                         available,
                                                         copySeismograms(seismograms),
                                                         cookieJar);
        resultB = MotionVectorArm.runProcessorThreadCheck(processorB,
                                                         event,
                                                         channelGroup,
                                                         original,
                                                         available,
                                                         copySeismograms(seismograms),
                                                         cookieJar);
        boolean xorResult = resultA.isSuccess() != resultB.isSuccess();
        return new WaveformVectorResult(seismograms,
                                                     new StringTreeBranch(this,
                                                                          xorResult,
                                                                          new StringTree[] { resultA.getReason(), resultB.getReason() }));

    }

    public boolean isThreadSafe() {
        return true;
    }
}

