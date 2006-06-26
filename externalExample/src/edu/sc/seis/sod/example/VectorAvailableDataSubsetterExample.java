package edu.sc.seis.sod.example;

import edu.iris.Fissures.IfEvent.EventAccessOperations;
import edu.iris.Fissures.IfSeismogramDC.RequestFilter;
import edu.sc.seis.sod.ChannelGroup;
import edu.sc.seis.sod.CookieJar;
import edu.sc.seis.sod.status.Pass;
import edu.sc.seis.sod.status.StringTree;
import edu.sc.seis.sod.subsetter.availableData.vector.VectorAvailableDataSubsetter;


public class VectorAvailableDataSubsetterExample implements
        VectorAvailableDataSubsetter {

    public StringTree accept(EventAccessOperations event,
                             ChannelGroup channelGroup,
                             RequestFilter[][] original,
                             RequestFilter[][] available,
                             CookieJar cookieJar) throws Exception {
        return new Pass(this);
    }
}
