/**
 * ForkProcessor.java
 *
 * @author Philip Crotwell
 */

package edu.sc.seis.sod.process.waveFormArm;
import edu.iris.Fissures.IfEvent.EventAccessOperations;
import edu.iris.Fissures.IfNetwork.Channel;
import edu.iris.Fissures.IfNetwork.NetworkAccess;
import edu.iris.Fissures.IfSeismogramDC.LocalSeismogram;
import edu.iris.Fissures.IfSeismogramDC.RequestFilter;
import edu.sc.seis.sod.CookieJar;
import org.w3c.dom.Element;
import edu.iris.Fissures.seismogramDC.LocalSeismogramImpl;
import java.util.LinkedList;
import java.util.Iterator;
import org.w3c.dom.NodeList;
import org.w3c.dom.Node;
import edu.sc.seis.sod.SodUtil;
import org.apache.log4j.Logger;
import edu.sc.seis.sod.ConfigurationException;


public class ForkProcess implements LocalSeismogramProcess {


    /**
     * Creates a new <code>RMean</code> instance.
     *
     * @param config an <code>Element</code> that contains the configuration
     * for this Processor
     */
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
                Object sodElement = SodUtil.load((Element)node,"waveFormArm");
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
     * example tto process both a original and a rmeaned version of the
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
    public LocalSeismogramImpl[] process(EventAccessOperations event,
                                         NetworkAccess network,
                                         Channel channel,
                                         RequestFilter[] original,
                                         RequestFilter[] available,
                                         LocalSeismogramImpl[] seismograms,
                                         CookieJar cookies) throws Exception {
        LocalSeismogramImpl[] out = new LocalSeismogramImpl[seismograms.length];
        for (int i = 0; i < out.length; i++) {
            out[i] = new LocalSeismogramImpl(seismograms[i], seismograms[i].data);
        }

        // pass originals to the contained processors
        LocalSeismogramProcess processor;
        Iterator it = localSeisProcessList.iterator();
        while (it.hasNext()) {
            processor = (LocalSeismogramProcess)it.next();
            synchronized (processor) {
                seismograms = processor.process(event,
                                                network,
                                                channel,
                                                original,
                                                available,
                                                seismograms,
                                                cookies);
            }
        } // end of while (it.hasNext())
        return out;
    }

    private LinkedList localSeisProcessList = new LinkedList();

    Element config;

    private static final Logger logger = Logger.getLogger(ForkProcess.class);
}

