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
import edu.sc.seis.sod.SodUtil;
import edu.sc.seis.sod.Threadable;
import edu.sc.seis.sod.process.waveform.ForkProcess;
import edu.sc.seis.sod.process.waveform.WaveformProcess;
import edu.sc.seis.sod.status.StringTree;
import edu.sc.seis.sod.status.StringTreeBranch;
import edu.sc.seis.sod.status.StringTreeLeaf;
import edu.sc.seis.sod.subsetter.Subsetter;
import edu.sc.seis.sod.subsetter.availableData.AvailableDataSubsetter;
import edu.sc.seis.sod.subsetter.availableData.vector.ANDAvailableDataWrapper;
import edu.sc.seis.sod.subsetter.availableData.vector.VectorAvailableDataLogicalSubsetter;
import edu.sc.seis.sod.subsetter.availableData.vector.VectorAvailableDataSubsetter;
import edu.sc.seis.sod.subsetter.request.vector.VectorRequestLogical;
import edu.sc.seis.sod.subsetter.request.vector.VectorRequestSubsetter;

/**
 * @author groves Created on Mar 23, 2005
 */
public class WaveformVectorFork implements WaveformVectorProcess, Threadable {

    public WaveformVectorFork(Element config) throws ConfigurationException {
        NodeList children = config.getChildNodes();
        for(int i = 0; i < children.getLength(); i++) {
            Node node = children.item(i);
            if(node instanceof Element) {
                WaveformVectorProcess sodElement = load((Element)node);
                processes.add(sodElement);
            } // end of if (node instanceof Element)
        } // end of for (int i=0; i<children.getSize(); i++)
    }

    public static final List<String> packages;
    
    static {
        packages = new LinkedList<String>();
        packages.add("waveform.vector");
        packages.addAll(VectorAvailableDataLogicalSubsetter.packages);
        packages.addAll(ForkProcess.packages);
    }
    
    public List<String> getPackages() {
        return packages;
    }

    protected Subsetter getSubsetter(final Subsetter s) throws ConfigurationException {
        return createSubsetter(s);
    }
    
    public static WaveformVectorProcess load(Element el) throws ConfigurationException {
        Object o = SodUtil.load(el, packages);
        if (o instanceof Subsetter) {
        return createSubsetter((Subsetter)o);
        } else {
            throw new ConfigurationException("Not an Element: "+o.getClass().getName());
        }
    }
    public static WaveformVectorProcess createSubsetter(final Subsetter s) throws ConfigurationException {
        if (s instanceof WaveformVectorProcess) {
            return (WaveformVectorProcess)s;
        } else if (s instanceof WaveformProcess) {
            return new ANDWaveformProcessWrapper((WaveformProcess)s);
        } else {
            return new WaveformVectorProcess() {
                VectorAvailableDataSubsetter ecs = VectorAvailableDataLogicalSubsetter.createSubsetter(s);
                public WaveformVectorResult accept(CacheEvent event,
                                                   ChannelGroup channelGroup,
                                                   RequestFilter[][] request,
                                                   RequestFilter[][] available,
                                                   LocalSeismogramImpl[][] seismograms,
                                                   CookieJar cookieJar) throws Exception {
                    return new WaveformVectorResult(seismograms,
                                                    ecs.accept(event, channelGroup, request, available, cookieJar));
                }
            };
        }
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
    
    private static final org.apache.log4j.Logger logger = org.apache.log4j.Logger.getLogger(WaveformVectorFork.class);

    private List processes = new ArrayList();
}