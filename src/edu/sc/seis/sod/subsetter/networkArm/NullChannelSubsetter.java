package edu.sc.seis.sod.subsetter.networkArm;

import edu.iris.Fissures.IfNetwork.Channel;

/**
 * NullChannelSubsetter.java
 *
 *
 * Created: Thu Dec 13 17:09:18 2001
 *
 * @author <a href="mailto:">Philip Crotwell</a>
 * @version
 */

public class  NullChannelSubsetter implements ChannelSubsetter{

    public boolean accept(Channel channel) { return true; }

}// NullChannelSubsetter
