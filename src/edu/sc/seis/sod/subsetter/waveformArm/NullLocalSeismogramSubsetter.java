package edu.sc.seis.sod.subsetter.waveformArm;

import edu.iris.Fissures.IfEvent.EventAccessOperations;
import edu.iris.Fissures.IfNetwork.Channel;
import edu.iris.Fissures.IfSeismogramDC.RequestFilter;
import edu.iris.Fissures.seismogramDC.LocalSeismogramImpl;
import edu.sc.seis.sod.ChannelGroup;
import edu.sc.seis.sod.CookieJar;
import org.w3c.dom.Element;

/**
 * NullLocalSeismogramSubsetter.java
 *
 *
 * Created: Fri Apr 12 13:41:05 2002
 *
 * @author <a href="mailto:">Srinivasa Telukutla</a>
 * @version
 */

public class NullLocalSeismogramSubsetter implements LocalSeismogramSubsetter, ChannelGroupLocalSeismogramSubsetter {

    public NullLocalSeismogramSubsetter (){}

    public NullLocalSeismogramSubsetter(Element config) {}

    public boolean accept(EventAccessOperations event, Channel channel,
                          RequestFilter[] original, RequestFilter[] available,
                          LocalSeismogramImpl[] seismograms, CookieJar cookieJar) throws Exception {
        return true;
    }


    public boolean accept(EventAccessOperations event,
                          ChannelGroup channelGroup,
                          RequestFilter[][] original,
                          RequestFilter[][] available,
                          LocalSeismogramImpl[][] seismograms,
                          CookieJar cookieJar) throws Exception {
        return true;
    }

}// LocalSeismogram
