/**
 * ChannelGroupAvailableDataSubsetter.java
 * 
 * @author Created by Omnicore CodeGuide
 */
package edu.sc.seis.sod.subsetter.availableData.vector;

import edu.iris.Fissures.IfEvent.EventAccessOperations;
import edu.iris.Fissures.IfSeismogramDC.RequestFilter;
import edu.sc.seis.sod.ChannelGroup;
import edu.sc.seis.sod.CookieJar;
import edu.sc.seis.sod.subsetter.Subsetter;

public interface VectorAvailableDataSubsetter extends Subsetter {

    public boolean accept(EventAccessOperations event,
                          ChannelGroup channelGroup,
                          RequestFilter[][] original,
                          RequestFilter[][] available,
                          CookieJar cookieJar) throws Exception;
}