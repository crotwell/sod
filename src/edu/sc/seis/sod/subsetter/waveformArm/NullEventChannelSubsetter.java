package edu.sc.seis.sod.subsetter.waveformArm;

import org.w3c.dom.Element;

import edu.iris.Fissures.IfEvent.EventAccessOperations;
import edu.iris.Fissures.IfNetwork.Channel;
import edu.iris.Fissures.IfNetwork.NetworkAccess;
import edu.sc.seis.sod.CookieJar;
import edu.sc.seis.sod.subsetter.waveformArm.EventChannelSubsetter;


/**
 * Describe class <code>NullEventChannelSubsetter</code> here.
 *
 * @author <a href="mailto:">Srinivasa Telukutla</a>
 * @version 1.0
 */
public class NullEventChannelSubsetter implements EventChannelSubsetter {

    public NullEventChannelSubsetter() {
    }
    public NullEventChannelSubsetter(Element config) {
    }
    /**
     * Describe <code>accept</code> method here.
     *
     * @param o an <code>EventAccessOperations</code> value
     * @param networkAccess a <code>NetworkAccess</code> value
     * @param station a <code>Channel</code> value
     * @param cookies a <code>CookieJar</code> value
     * @return a <code>boolean</code> value
     */
    public boolean accept(EventAccessOperations o, NetworkAccess networkAccess, Channel channel,  CookieJar cookies) {
    return true;
    }

}// NullEventChannelSubsetter
