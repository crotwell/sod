package edu.sc.seis.sod.example;

import edu.iris.Fissures.IfEvent.EventAccessOperations;
import edu.sc.seis.sod.ChannelGroup;
import edu.sc.seis.sod.CookieJar;
import edu.sc.seis.sod.status.Pass;
import edu.sc.seis.sod.status.StringTree;
import edu.sc.seis.sod.subsetter.eventChannel.vector.EventVectorSubsetter;

public class EventVectorSubsetterExample implements EventVectorSubsetter {

    public StringTree accept(EventAccessOperations event,
                             ChannelGroup channel,
                             CookieJar cookieJar) throws Exception {
        return new Pass(this);
    }
}
