package edu.sc.seis.sod.process.waveform.vector;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import edu.iris.Fissures.IfSeismogramDC.RequestFilter;
import edu.iris.Fissures.seismogramDC.LocalSeismogramImpl;
import edu.sc.seis.fissuresUtil.cache.CacheEvent;
import edu.sc.seis.fissuresUtil.hibernate.ChannelGroup;
import edu.sc.seis.sod.ConfigurationException;
import edu.sc.seis.sod.CookieJar;
import edu.sc.seis.sod.MotionVectorArm;
import edu.sc.seis.sod.Threadable;
import edu.sc.seis.sod.process.waveform.ForkProcess;
import edu.sc.seis.sod.status.StringTree;
import edu.sc.seis.sod.status.StringTreeBranch;
import edu.sc.seis.sod.status.StringTreeLeaf;

/**
 * @author groves Created on Mar 23, 2005
 */
public class VectorForkProcess implements WaveformVectorProcess, Threadable {

    public VectorForkProcess(Element config) throws ConfigurationException {
        NodeList children = config.getChildNodes();
        for(int i = 0; i < children.getLength(); i++) {
            Node node = children.item(i);
            if(node instanceof Element) {
                WaveformVectorProcess sodElement = MotionVectorArm.loadAndWrap((Element)node);
                processes.add(sodElement);
            } // end of if (node instanceof Element)
        } // end of for (int i=0; i<children.getSize(); i++)
    }

    public WaveformVectorResult accept(CacheEvent event,
                                        ChannelGroup channelGroup,
                                        RequestFilter[][] original,
                                        RequestFilter[][] available,
                                        LocalSeismogramImpl[][] seismograms,
                                        CookieJar cookieJar) throws Exception {
        LocalSeismogramImpl[][] out = copySeismograms(seismograms);
        LinkedList reasons = new LinkedList();
        Iterator it = processes.iterator();
        WaveformVectorResult result = new WaveformVectorResult(seismograms,
                                                               new StringTreeLeaf(this,
                                                                                  true));
        logger.info("start vectorFork");
        while(it.hasNext() && result.isSuccess()) {
            WaveformVectorProcess processor = (WaveformVectorProcess)it.next();
            logger.info("vectorFork processing "
                    + processor.getClass().getName());
            result = MotionVectorArm.runProcessorThreadCheck(processor,
                                                             event,
                                                             channelGroup,
                                                             original,
                                                             available,
                                                             result.getSeismograms(),
                                                             cookieJar);
            reasons.addLast(result.getReason());
        } // end of while (it.hasNext())
        return new WaveformVectorResult(out,
                                        new StringTreeBranch(this,
                                                             result.isSuccess(),
                                                             (StringTree[])reasons.toArray(new StringTree[0])));
    }

    private LocalSeismogramImpl[][] copySeismograms(LocalSeismogramImpl[][] seismograms) {
        LocalSeismogramImpl[][] out = new LocalSeismogramImpl[seismograms.length][];
        for(int i = 0; i < out.length; i++) {
            out[i] = ForkProcess.copySeismograms(seismograms[i]);
        }
        return out;
    }

    public WaveformVectorProcess[] getWrappedProcessors() {
        return (WaveformVectorProcess[])processes.toArray(new WaveformVectorProcess[0]);
    }

    public boolean isThreadSafe() {
        return true;
    }
    
    private static final org.apache.log4j.Logger logger = org.apache.log4j.Logger.getLogger(VectorForkProcess.class);

    private List processes = new ArrayList();
}