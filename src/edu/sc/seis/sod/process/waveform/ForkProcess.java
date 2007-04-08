/**
 * ForkProcessor.java
 * 
 * @author Philip Crotwell
 */
package edu.sc.seis.sod.process.waveform;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import edu.iris.Fissures.IfEvent.EventAccessOperations;
import edu.iris.Fissures.IfNetwork.Channel;
import edu.iris.Fissures.IfSeismogramDC.RequestFilter;
import edu.iris.Fissures.seismogramDC.LocalSeismogramImpl;
import edu.sc.seis.sod.ConfigurationException;
import edu.sc.seis.sod.CookieJar;
import edu.sc.seis.sod.SodUtil;
import edu.sc.seis.sod.Threadable;
import edu.sc.seis.sod.status.StringTree;
import edu.sc.seis.sod.status.StringTreeBranch;
import edu.sc.seis.sod.status.StringTreeLeaf;

public class ForkProcess implements WaveformProcess, Threadable {

    public ForkProcess(Element config) throws ConfigurationException {
        NodeList children = config.getChildNodes();
        Node node;
        for(int i = 0; i < children.getLength(); i++) {
            node = children.item(i);
            if(node instanceof Element) {
                Object sodElement = SodUtil.load((Element)node, "waveform");
                localSeisProcessList.add(sodElement);
            } // end of if (node instanceof Element)
        } // end of for (int i=0; i<children.getSize(); i++)
    }

    public boolean isThreadSafe() {
        return true;
    }

    /**
     * Forks the processing off the LocalSeismograms. The processes that are
     * contained in this tag are processed, but the return value off the process
     * method is the original seismograms. This allows, for example to process
     * both a original and a filtered version of the seismograms independently.
     */
    public WaveformResult process(EventAccessOperations event,
                                  Channel channel,
                                  RequestFilter[] original,
                                  RequestFilter[] available,
                                  LocalSeismogramImpl[] seismograms,
                                  CookieJar cookieJar) throws Exception {
        return new WaveformResult(copySeismograms(seismograms),
                                  doAND(event,
                                        channel,
                                        original,
                                        available,
                                        seismograms,
                                        cookieJar).getReason());
    }

    protected WaveformResult doAND(EventAccessOperations event,
                                   Channel channel,
                                   RequestFilter[] original,
                                   RequestFilter[] available,
                                   LocalSeismogramImpl[] seismograms,
                                   CookieJar cookieJar) throws Exception {
        // pass originals to the contained processors
        List reasons = new ArrayList(localSeisProcessList.size());
        Iterator it = localSeisProcessList.iterator();
        WaveformResult result = new WaveformResult(seismograms,
                                                   new StringTreeLeaf(this,
                                                                      true));
        while(it.hasNext() && result.isSuccess()) {
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