/**
 * ChannelGroupRequestGenerator.java
 *
 * @author Created by Omnicore CodeGuide
 */

package edu.sc.seis.sod.subsetter.waveformArm;

import edu.iris.Fissures.IfEvent.EventAccessOperations;
import edu.iris.Fissures.IfSeismogramDC.RequestFilter;
import edu.sc.seis.sod.ChannelGroup;
import edu.sc.seis.sod.CookieJar;
import edu.sc.seis.sod.subsetter.Subsetter;

public interface ChannelGroupRequestGenerator extends Subsetter {

    /** Generates the request for each of the channels. The return value
     * is a 2 dimensional array where the first index corresponds to the channel
     * in the channel group. */
    public RequestFilter[][] generateRequest(EventAccessOperations event,
                                             ChannelGroup channelGroup,
                                             CookieJar cookieJar) throws Exception;
}

