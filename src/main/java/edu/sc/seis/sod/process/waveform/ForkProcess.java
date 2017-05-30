/**
 * ForkProcessor.java
 * 
 * @author Philip Crotwell
 */
package edu.sc.seis.sod.process.waveform;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import edu.sc.seis.sod.ConfigurationException;
import edu.sc.seis.sod.LocalSeismogramArm;
import edu.sc.seis.sod.SodUtil;
import edu.sc.seis.sod.Threadable;
import edu.sc.seis.sod.hibernate.eventpair.CookieJar;
import edu.sc.seis.sod.model.event.CacheEvent;
import edu.sc.seis.sod.model.seismogram.LocalSeismogramImpl;
import edu.sc.seis.sod.model.seismogram.RequestFilter;
import edu.sc.seis.sod.model.station.ChannelImpl;
import edu.sc.seis.sod.status.StringTree;
import edu.sc.seis.sod.status.StringTreeBranch;
import edu.sc.seis.sod.status.StringTreeLeaf;
import edu.sc.seis.sod.subsetter.Subsetter;
import edu.sc.seis.sod.subsetter.availableData.AvailableDataLogicalSubsetter;
import edu.sc.seis.sod.subsetter.availableData.AvailableDataSubsetter;

public class ForkProcess implements WaveformProcess, Threadable {

    public ForkProcess(Element config) throws ConfigurationException {
        NodeList children = config.getChildNodes();
        Node node;
        String [] packageArray = packages.toArray(new String[0]);
        for(int i = 0; i < children.getLength(); i++) {
            node = children.item(i);
            if(node instanceof Element) {
                Object sodElement = SodUtil.load((Element)node, packageArray);
                if (sodElement instanceof Subsetter) {
                    localSeisProcessList.add(createSubsetter((Subsetter)sodElement));
                }
            } // end of if (node instanceof Element)
        } // end of for (int i=0; i<children.getSize(); i++)
    }

    public boolean isThreadSafe() {
        return true;
    }

    public static final List<String> packages;
    
    static {
        packages = new LinkedList<String>();
        packages.add("waveform");
        packages.addAll(AvailableDataLogicalSubsetter.packages);
    }

    public static WaveformProcess createSubsetter(final Subsetter s) throws ConfigurationException {
        if (s instanceof WaveformProcess) {
            return (WaveformProcess)s;
        } else {
            final AvailableDataSubsetter subsetter = (AvailableDataSubsetter)AvailableDataLogicalSubsetter.createSubsetter(s);
            return new WaveformProcess() {

                public WaveformResult accept(CacheEvent event,
                                             ChannelImpl channel,
                                             RequestFilter[] request,
                                             RequestFilter[] available,
                                             LocalSeismogramImpl[] seismograms,
                                             CookieJar cookieJar) throws Exception {
                    return new WaveformResult(seismograms,
                                              subsetter.accept(event, channel, request, available, cookieJar));
                }
            };
        }
    }

    /**
     * Forks the processing off the LocalSeismograms. The processes that are
     * contained in this tag are processed, but the return value off the process
     * method is the original seismograms. This allows, for example to process
     * both a original and a filtered version of the seismograms independently.
     */
    public WaveformResult accept(CacheEvent event,
                                  ChannelImpl channel,
                                  RequestFilter[] request,
                                  RequestFilter[] available,
                                  LocalSeismogramImpl[] seismograms,
                                  CookieJar cookieJar) throws Exception {
        return new WaveformResult(copySeismograms(seismograms),
                                  doAND(event,
                                        channel,
                                        request,
                                        available,
                                        seismograms,
                                        cookieJar).getReason());
    }

    protected WaveformResult doAND(CacheEvent event,
                                   ChannelImpl channel,
                                   RequestFilter[] request,
                                   RequestFilter[] available,
                                   LocalSeismogramImpl[] seismograms,
                                   CookieJar cookieJar) throws Exception {
        // pass originals to the contained processors
        List<StringTree> reasons = new ArrayList<StringTree>(localSeisProcessList.size());
        Iterator it = localSeisProcessList.iterator();
        WaveformResult result = new WaveformResult(seismograms,
                                                   new StringTreeLeaf(this,
                                                                      true));
        while(it.hasNext() && result.isSuccess()) {
            WaveformProcess processor = (WaveformProcess)it.next();
            result = LocalSeismogramArm.runProcessorThreadCheck(processor, 
                                                                event,
                                                                channel,
                                                                request,
                                                                available,
                                                                result.getSeismograms(),
                                                                cookieJar);
            reasons.add(result.getReason());
        } // end of while (it.hasNext())
        return new WaveformResult(result.getSeismograms(),
                                  new StringTreeBranch(this,
                                                       result.isSuccess(),
                                                       (StringTree[])reasons.toArray(new StringTree[0])));
    }

    public static LocalSeismogramImpl[] copySeismograms(LocalSeismogramImpl[] seismograms) {
        LocalSeismogramImpl[] out = new LocalSeismogramImpl[seismograms.length];
        for(int i = 0; i < out.length; i++) {
            out[i] = new LocalSeismogramImpl(seismograms[i],
                                             seismograms[i].data);
        }
        return out;
    }

    public String toString() {
        String s = SodUtil.getSimpleName(getClass()) + "(";
        Iterator it = localSeisProcessList.iterator();
        while(it.hasNext()) {
            s += it.next().toString() + ",";
        }
        s = s.substring(0, s.length() - 1);
        s += ")";
        return s;
    }

    protected List localSeisProcessList = new ArrayList();

    public WaveformProcess[] getWrappedProcessors() {
        return (WaveformProcess[])localSeisProcessList.toArray(new WaveformProcess[0]);
    }
}