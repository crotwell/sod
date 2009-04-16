/**
 * ChannelGroupFork.java
 * 
 * @author Created by Omnicore CodeGuide
 */
package edu.sc.seis.sod.process.waveform.vector;

import java.util.Iterator;
import java.util.LinkedList;

import org.apache.log4j.Logger;
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
import edu.sc.seis.sod.status.StringTree;
import edu.sc.seis.sod.status.StringTreeBranch;
import edu.sc.seis.sod.status.StringTreeLeaf;

public class WaveformVectorFork implements WaveformVectorProcessWrapper, Threadable {

    public WaveformVectorFork(Element config) throws ConfigurationException {
        this.config = config;
        NodeList children = config.getChildNodes();
        for(int i = 0; i < children.getLength(); i++) {
            Node node = children.item(i);
            if(node instanceof Element) {
                cgProcessList.add(MotionVectorArm.loadAndWrap((Element)node));
            }
        }
    }

    public WaveformVectorResult process(CacheEvent event,
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
        WaveformVectorResult result = new WaveformVectorResult(seismograms,
                                                               new StringTreeLeaf(this,
                                                                                  true));
        while(it.hasNext() && result.isSuccess()) {
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

    public static LocalSeismogramImpl[][] copySeismograms(LocalSeismogramImpl[][] seismograms) {
        LocalSeismogramImpl[][] out = new LocalSeismogramImpl[seismograms.length][];
        for(int i = 0; i < out.length; i++) {
            out[i] = new LocalSeismogramImpl[seismograms[i].length];
            for(int j = 0; j < seismograms[i].length; j++) {
                out[i][j] = new LocalSeismogramImpl(seismograms[i][j],
                                                    seismograms[i][j].data);
            }
        }
        return out;
    }

    protected LinkedList cgProcessList = new LinkedList();

    protected Element config;

    private static final Logger logger = Logger.getLogger(WaveformVectorFork.class);

    public WaveformVectorProcess[] getWrappedProcessors() {
        return (WaveformVectorProcess[])cgProcessList.toArray(new WaveformVectorProcess[0]);
    }

    public boolean isThreadSafe() {
        return true;
    }
}