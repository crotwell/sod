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



public class ForkProcessor implements LocalSeismogramProcess {


    /**
     * Creates a new <code>RMean</code> instance.
     *
     * @param config an <code>Element</code> that contains the configuration
     * for this Processor
     */
    public ForkProcessor (Element config) {
        this.config = config;
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
        // TODO

        return out;
    }


    Element config;
}

