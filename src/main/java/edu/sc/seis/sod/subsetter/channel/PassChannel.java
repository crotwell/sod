package edu.sc.seis.sod.subsetter.channel;

import edu.iris.Fissures.network.ChannelImpl;
import edu.sc.seis.sod.source.network.NetworkSource;
import edu.sc.seis.sod.status.Pass;
import edu.sc.seis.sod.status.StringTree;

/**
 * PassChannel.java
 *
 *
 * Created: Thu Dec 13 17:09:18 2001
 *
 * @author Philip Crotwell
 * @version
 */

public class PassChannel implements ChannelSubsetter{

    public StringTree accept(ChannelImpl channel, NetworkSource network) { return new Pass(this); }

}// PassChannel


