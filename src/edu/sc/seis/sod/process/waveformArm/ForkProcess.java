/**
 * ForkProcessor.java
 *
 * @author Philip Crotwell
 */

package edu.sc.seis.sod.process.waveformArm;
import edu.iris.Fissures.IfEvent.EventAccessOperations;
import edu.iris.Fissures.IfNetwork.Channel;
import edu.iris.Fissures.IfSeismogramDC.RequestFilter;
import edu.iris.Fissures.seismogramDC.LocalSeismogramImpl;
import edu.sc.seis.sod.ConfigurationException;
import edu.sc.seis.sod.CookieJar;
import edu.sc.seis.sod.SodUtil;
import edu.sc.seis.sod.status.StringTree;
import edu.sc.seis.sod.status.StringTreeBranch;
import edu.sc.seis.sod.status.StringTreeLeaf;
import java.util.Iterator;
import java.util.LinkedList;
import org.apache.log4j.Logger;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;


public class ForkProcess implements LocalSeismogramProcess {

    public ForkProcess (Element config) throws ConfigurationException {
        this.config = config;
        NodeList children = config.getChildNodes();
        Node node;
        for (int i=0; i<children.getLength(); i++) {
            node = children.item(i);
            if (node instanceof Element) {
                if (((Element)node).getTagName().equals("description")) {
                    // skip description element
                    continue;
                }
                Object sodElement = SodUtil.load((Element)node,"waveformArm");
                if(sodElement instanceof LocalSeismogramProcess) {
                    localSeisProcessList.add(sodElement);
                } else {
                    logger.warn("Unknown tag in LocalSeismogramArm config. " +sodElement);
                } // end of else
            } // end of if (node instanceof Element)
        } // end of for (int i=0; i<children.getSize(); i++)

    }

    /**
     * Forks the processing off the LocalSeismograms. The processes that
     * are contained in this tag are processed, but the return value off
     * the process method is the original seismograms. This allows, for
     * example to process both a original and a filtered version of the
     * seismograms independently.
     *
     * @param event an <code>EventAccessOperations</code> value
     * @param network a <code>NetworkAccess</code> value
     * @param channel a <code>Channel</code> value
     * @param original a <code>RequestFilter[]</code> value
     * @param available a <code>RequestFilter[]</code> value
     * @param seismograms a <code>LocalSeismogram[]</code> value
     * @param cookies a <code>CookieJar</code> value
     * @exception Exception if an error occurs
     */
    public LocalSeismogramResult process(EventAccessOperations event,
                                         Channel channel,
                                         RequestFilter[] original,
                                         RequestFilter[] available,
                                         LocalSeismogramImpl[] seismograms,
                                         CookieJar cookieJar
                                        ) throws Exception {
        LocalSeismogramImpl[] out = copySeismograms(seismograms);

        // pass originals to the contained processors
        LocalSeismogramProcess processor;
        LinkedList reasons = new LinkedList();
        Iterator it = localSeisProcessList.iterator();
        LocalSeismogramResult result = new LocalSeismogramResult(true, seismograms, new StringTreeLeaf(this, true));
        while (it.hasNext() && result.isSuccess()) {
            processor = (LocalSeismogramProcess)it.next();
            synchronized (processor) {
                result = processor.process(event, channel, original,
                                                available, result.getSeismograms(), cookieJar);
            }
            reasons.addLast(result.getReason());
        } // end of while (it.hasNext())
        return new LocalSeismogramResult(out,
                                         new StringTreeBranch(this,
                                                              result.isSuccess(),
                                                                  (StringTree[])reasons.toArray(new StringTree[0])));
    }

    public static LocalSeismogramImpl[] copySeismograms(LocalSeismogramImpl[] seismograms) {
        LocalSeismogramImpl[] out = new LocalSeismogramImpl[seismograms.length];
        for (int i = 0; i < out.length; i++) {
            out[i] = new LocalSeismogramImpl(seismograms[i], seismograms[i].data);
        }
        return out;
    }

    protected LinkedList localSeisProcessList = new LinkedList();

    Element config;

    private static final Logger logger = Logger.getLogger(ForkProcess.class);
}

