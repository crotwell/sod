/**
 * EventChannelGroupSubsetter.java
 * 
 * @author Created by Omnicore CodeGuide
 */
package edu.sc.seis.sod.subsetter.eventChannel.vector;

import edu.iris.Fissures.IfEvent.EventAccessOperations;
import edu.sc.seis.sod.ChannelGroup;
import edu.sc.seis.sod.CookieJar;
import edu.sc.seis.sod.status.StringTree;
import edu.sc.seis.sod.subsetter.Subsetter;

public interface EventVectorSubsetter extends Subsetter {

    public StringTree accept(EventAccessOperations event,
                          ChannelGroup channel,
                          CookieJar cookieJar) throws Exception;
}