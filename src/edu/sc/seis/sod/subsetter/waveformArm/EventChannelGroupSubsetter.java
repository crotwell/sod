/**
 * EventChannelGroupSubsetter.java
 *
 * @author Created by Omnicore CodeGuide
 */

package edu.sc.seis.sod.subsetter.waveformArm;

import edu.iris.Fissures.IfEvent.EventAccessOperations;
import edu.sc.seis.sod.ChannelGroup;
import edu.sc.seis.sod.CookieJar;
import edu.sc.seis.sod.subsetter.Subsetter;

public interface EventChannelGroupSubsetter extends Subsetter {

    public boolean accept(EventAccessOperations event,
                          ChannelGroup channel, CookieJar cookieJar) throws Exception;

}

