/**
 * PassChannelGroupRequest.java
 *
 * @author Created by Omnicore CodeGuide
 */

package edu.sc.seis.sod.subsetter.waveformArm;

import edu.iris.Fissures.IfEvent.EventAccessOperations;
import edu.iris.Fissures.IfSeismogramDC.RequestFilter;
import edu.sc.seis.sod.ChannelGroup;
import edu.sc.seis.sod.CookieJar;
import org.w3c.dom.Element;

public class PassChannelGroupRequest implements ChannelGroupRequestSubsetter {

    public PassChannelGroupRequest() {}

    public PassChannelGroupRequest(Element config) {}

    public boolean accept(EventAccessOperations event,
                          ChannelGroup channel,
                          RequestFilter[][] request,
                          CookieJar cookieJar)throws Exception {
        return true;
    }
}

