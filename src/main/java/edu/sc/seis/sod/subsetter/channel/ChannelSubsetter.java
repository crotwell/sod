package edu.sc.seis.sod.subsetter.channel;

import edu.iris.Fissures.network.ChannelImpl;
import edu.sc.seis.sod.source.network.NetworkSource;
import edu.sc.seis.sod.status.StringTree;
import edu.sc.seis.sod.subsetter.Subsetter;

/**
 * ChannelSubsetter.java
 * 
 * 
 * Created: Thu Dec 13 17:15:47 2001
 * 
 * @author Philip Crotwell
 * @version
 */
public interface ChannelSubsetter extends Subsetter {

    public StringTree accept(ChannelImpl channel, NetworkSource network)
            throws Exception;
}// ChannelSubsetter
