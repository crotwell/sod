/**
 * PassChannelGroupAvailableData.java
 *
 * @author Created by Omnicore CodeGuide
 */

package edu.sc.seis.sod.subsetter.waveformArm;

import edu.iris.Fissures.IfEvent.EventAccessOperations;
import edu.iris.Fissures.IfSeismogramDC.RequestFilter;
import edu.sc.seis.sod.ChannelGroup;
import edu.sc.seis.sod.CookieJar;
import edu.sc.seis.sod.subsetter.Subsetter;
import org.w3c.dom.Element;

public class PassChannelGroupAvailableData implements ChannelGroupAvailableDataSubsetter {

     public PassChannelGroupAvailableData() {}

    public PassChannelGroupAvailableData(Element config) {}

    public boolean accept(EventAccessOperations event,
                          ChannelGroup channelGroup,
                          RequestFilter[][] original,
                          RequestFilter[][] available,
                          CookieJar cookieJar) throws Exception{
        return true;
    }
}

