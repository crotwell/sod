package edu.sc.seis.sod.process.waveFormArm;
import edu.iris.Fissures.IfEvent.EventAccessOperations;
import edu.iris.Fissures.IfNetwork.Channel;
import edu.iris.Fissures.IfNetwork.NetworkAccess;
import edu.iris.Fissures.IfSeismogramDC.RequestFilter;
import edu.iris.Fissures.seismogramDC.LocalSeismogramImpl;
import edu.sc.seis.sod.CookieJar;
import edu.sc.seis.sod.process.waveFormArm.WaveFormArmProcess;
/**
 * LocalSeismogramProcess.java
 *
 *
 * Created: Thu Dec 13 18:03:03 2001
 *
 * @author <a href="mailto:">Philip Crotwell</a>
 * @version
 */

public interface LocalSeismogramProcess extends WaveFormArmProcess {

    /**
     * Processes localSeismograms, possibly modifying them.
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
                     CookieJar cookies) throws Exception;

}// LocalSeismogramProcessor
