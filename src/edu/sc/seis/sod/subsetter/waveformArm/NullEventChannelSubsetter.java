package edu.sc.seis.sod.subsetter.waveformArm;

import edu.iris.Fissures.IfEvent.EventAccessOperations;
import edu.iris.Fissures.IfNetwork.Channel;
import edu.sc.seis.sod.CookieJar;
import edu.sc.seis.sod.subsetter.waveformArm.EventChannelSubsetter;
import org.w3c.dom.Element;

/**
 * Describe class <code>NullEventChannelSubsetter</code> here.
 *
 * @author <a href="mailto:">Srinivasa Telukutla</a>
 * @version 1.0
 */
public class NullEventChannelSubsetter implements EventChannelSubsetter {

    public NullEventChannelSubsetter() {}

    public NullEventChannelSubsetter(Element config) {}

    public boolean accept(EventAccessOperations o, Channel channel, CookieJar cookieJar) {
        return true;
    }
}// NullEventChannelSubsetter
