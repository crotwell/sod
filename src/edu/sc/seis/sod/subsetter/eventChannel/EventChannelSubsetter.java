package edu.sc.seis.sod.subsetter.eventChannel;

import edu.iris.Fissures.IfEvent.EventAccessOperations;
import edu.iris.Fissures.IfNetwork.Channel;
import edu.sc.seis.sod.CookieJar;
import edu.sc.seis.sod.subsetter.Subsetter;

/**
 * EventChannelSubsetter.java Created: Thu Dec 13 17:19:47 2001
 * 
 * @author <a href="mailto:">Philip Crotwell </a>
 * @version
 */
public interface EventChannelSubsetter extends Subsetter {

    public boolean accept(EventAccessOperations event,
                          Channel channel,
                          CookieJar cookieJar) throws Exception;
}// EventChannelSubsetter
