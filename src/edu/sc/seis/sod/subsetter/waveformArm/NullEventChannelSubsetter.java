package edu.sc.seis.sod.subsetter.waveformArm;

import org.w3c.dom.Element;
import edu.iris.Fissures.IfEvent.EventAccessOperations;
import edu.iris.Fissures.IfNetwork.Channel;
import edu.iris.Fissures.IfNetwork.NetworkAccess;
import edu.sc.seis.sod.subsetter.waveformArm.EventChannelSubsetter;

/**
 * Describe class <code>NullEventChannelSubsetter</code> here.
 *
 * @author <a href="mailto:">Srinivasa Telukutla</a>
 * @version 1.0
 */
public class NullEventChannelSubsetter implements EventChannelSubsetter {

    public NullEventChannelSubsetter() {}

    public NullEventChannelSubsetter(Element config) {}

    public boolean accept(EventAccessOperations o, Channel channel) {
        return true;
    }
}// NullEventChannelSubsetter
