package edu.sc.seis.sod.subsetter.waveformArm;
import edu.sc.seis.sod.*;
import edu.iris.Fissures.IfEvent.EventAccessOperations;
import edu.iris.Fissures.IfNetwork.Channel;
import edu.iris.Fissures.IfNetwork.NetworkAccess;
import edu.iris.Fissures.IfSeismogramDC.RequestFilter;
import edu.sc.seis.sod.subsetter.Subsetter;

/**
 * RequestGenerator.java
 *
 *
 * Created: Thu Dec 13 17:25:25 2001
 *
 * @author <a href="mailto:">Philip Crotwell</a>
 * @version
 */

public interface  RequestGenerator extends Subsetter{

    /**
     * Describe <code>generateRequest</code> method here.
     *
     * @param event an <code>EventAccessOperations</code> value
     * @param network a <code>NetworkAccess</code> value
     * @param channel a <code>Channel</code> value
     * @param cookies a <code>CookieJar</code> value
     * @return a <code>RequestFilter[]</code> value
     * @exception Exception if an error occurs
     */
    public RequestFilter[] generateRequest(EventAccessOperations event,
                                           Channel channel) throws Exception;

}// RequestGenerator
