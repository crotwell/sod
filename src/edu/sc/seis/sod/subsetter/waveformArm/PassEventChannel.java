package edu.sc.seis.sod.subsetter.waveformArm;

import edu.iris.Fissures.IfEvent.EventAccessOperations;
import edu.iris.Fissures.IfNetwork.Channel;
import edu.sc.seis.sod.ChannelGroup;
import edu.sc.seis.sod.CookieJar;
import edu.sc.seis.sod.subsetter.waveformArm.EventChannelSubsetter;
import org.w3c.dom.Element;

/**
 * Describe class <code>PassEventChannel</code> here.
 *
 * @author <a href="mailto:">Srinivasa Telukutla</a>
 * @version 1.0
 */
public class PassEventChannel implements EventChannelSubsetter, EventChannelGroupSubsetter {

    public PassEventChannel() {}

    public PassEventChannel(Element config) {}

    public boolean accept(EventAccessOperations o, Channel channel, CookieJar cookieJar) {
        return true;
    }

    public boolean accept(EventAccessOperations event, ChannelGroup channel, CookieJar cookieJar) throws Exception {
        return true;
    }

}// NullEventChannelSubsetter
