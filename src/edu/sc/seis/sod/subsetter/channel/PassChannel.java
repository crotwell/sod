package edu.sc.seis.sod.subsetter.channel;

import edu.iris.Fissures.IfNetwork.Channel;

/**
 * PassChannel.java
 *
 *
 * Created: Thu Dec 13 17:09:18 2001
 *
 * @author <a href="mailto:">Philip Crotwell</a>
 * @version
 */

public class  PassChannel implements ChannelSubsetter{

    public boolean accept(Channel channel) { return true; }

}// PassChannel


