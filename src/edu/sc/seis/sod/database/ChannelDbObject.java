package edu.sc.seis.sod.database;

import edu.iris.Fissures.IfNetwork.Channel;

/**
 * ChannelDbObject.java
 *
 *
 * Created: Tue Oct 22 14:38:01 2002
 *
 * @author <a href="mailto:">Srinivasa Telukutla</a>
 * @version
 */

public class ChannelDbObject extends DbObject{
    public ChannelDbObject (int dbid, Channel channel){
	super(dbid);
	this.channel = channel;
    }
    
    public Channel getChannel() {
	return this.channel;
    }
    
    private Channel channel; 
}// ChannelDbObject
