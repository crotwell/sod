package edu.sc.seis.sod.subsetter.channel;
import edu.iris.Fissures.IfNetwork.Channel;
import edu.iris.Fissures.IfNetwork.NetworkAccess;
import edu.sc.seis.sod.subsetter.Subsetter;
/**
 * ChannelSubsetter.java
 *
 *
 * Created: Thu Dec 13 17:15:47 2001
 *
 * @author <a href="mailto:">Philip Crotwell</a>
 * @version
 */

public interface ChannelSubsetter extends Subsetter {

    public boolean accept(Channel channel, NetworkAccess network) throws Exception;

}// ChannelSubsetter
