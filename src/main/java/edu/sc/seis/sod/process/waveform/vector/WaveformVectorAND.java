/**
 * ChannelGroupAND.java
 *
 * @author Created by Omnicore CodeGuide
 */

package edu.sc.seis.sod.process.waveform.vector;

import java.util.Iterator;
import java.util.LinkedList;

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
import edu.sc.seis.sod.status.StringTreeLeaf;

public class WaveformVectorAND extends WaveformVectorFork  {

    public WaveformVectorAND(Element config) throws ConfigurationException {
        super(config);
    }

    public WaveformVectorResult accept(CacheEvent event,
                                                     ChannelGroup channelGroup,
                                                     RequestFilter[][] original,
                                                     RequestFilter[][] available,
                                                     LocalSeismogramImpl[][] seismograms,
                                                     CookieJar cookieJar) throws Exception {
        LocalSeismogramImpl[][] out = copySeismograms(seismograms);

        // pass originals to the contained processors
        WaveformVectorProcess processor;
        LinkedList reasons = new LinkedList();
        Iterator it = cgProcessList.iterator();
        WaveformVectorResult result = new WaveformVectorResult(seismograms, new StringTreeLeaf(this, true));
        while (it.hasNext() && result.isSuccess()) {
            processor = (WaveformVectorProcess)it.next();
            result = MotionVectorArm.runProcessorThreadCheck(processor,
                                                             event,
                                                             channelGroup,
                                                             original,
                                                             available,
                                                             copySeismograms(seismograms),
                                                             cookieJar);
            reasons.addLast(result.getReason());
        } // end of while (it.hasNext())
        return new WaveformVectorResult(out,
                                                     new StringTreeBranch(this,
                                                                          result.isSuccess(),
                                                                              (StringTree[])reasons.toArray(new StringTree[0])));

    }

    public boolean isThreadSafe() {
        return true;
    }
}

