package edu.sc.seis.sod.subsetter.networkArm;
import edu.iris.Fissures.IfNetwork.Channel;
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

    public boolean accept(Channel channel) throws Exception;

}// ChannelSubsetter
