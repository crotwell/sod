package edu.sc.seis.sod.subsetter.availableData;

import org.w3c.dom.Element;
import edu.iris.Fissures.IfEvent.EventAccessOperations;
import edu.iris.Fissures.IfNetwork.Channel;
import edu.iris.Fissures.IfSeismogramDC.RequestFilter;
import edu.sc.seis.sod.ChannelGroup;
import edu.sc.seis.sod.CookieJar;
import edu.sc.seis.sod.subsetter.availableData.vector.VectorAvailableDataSubsetter;

/**
 * Describe class <code>NullAvailableDataSubsetter</code> here.
 * 
 * @author <a href="mailto:">Srinivasa Telukutla </a>
 * @version 1.0
 */
public class PassAvailableData implements AvailableDataSubsetter,
        VectorAvailableDataSubsetter {

    public PassAvailableData() {}

    public PassAvailableData(Element config) {}

    public boolean accept(EventAccessOperations event,
                          Channel channel,
                          RequestFilter[] original,
                          RequestFilter[] available,
                          CookieJar cookieJar) {
        return true;
    }

    public boolean accept(EventAccessOperations event,
                          ChannelGroup channel,
                          RequestFilter[] original,
                          RequestFilter[] available,
                          CookieJar cookieJar) throws Exception {
        return true;
    }

    public boolean accept(EventAccessOperations event,
                          ChannelGroup channelGroup,
                          RequestFilter[][] original,
                          RequestFilter[][] available,
                          CookieJar cookieJar) throws Exception {
        return true;
    }
}// PassAvailableData
