/**
 * ChannelGroup.java
 *
 * @author Jagadeesh Danala
 * @version
 */

package edu.sc.seis.sod;
import edu.iris.Fissures.IfNetwork.Channel;

public class ChannelGroup {
	public ChannelGroup(Channel[] channels) {
		this.channels = channels;
    }
	
    public Channel[] getChannels() {
		return channels;
    }
	public boolean contains(Channel c) {
		for(int i=0;i<channels.length;i++) {
			if(channels[i].equals(c)) return true;
		}
		return false;
	}
	private Channel[] channels;
}


