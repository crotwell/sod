/**
 * AvailableDataGroupXOR.java
 * 
 * @author Created by Omnicore CodeGuide
 */
package edu.sc.seis.sod.subsetter.availableData.vector;

import org.w3c.dom.Element;
import edu.iris.Fissures.IfEvent.EventAccessOperations;
import edu.iris.Fissures.IfSeismogramDC.RequestFilter;
import edu.sc.seis.sod.ChannelGroup;
import edu.sc.seis.sod.ConfigurationException;
import edu.sc.seis.sod.CookieJar;

public class VectorAvailableDataXOR extends VectorAvailableDataLogicalSubsetter
        implements VectorAvailableDataSubsetter {

    public VectorAvailableDataXOR(Element config) throws ConfigurationException {
        super(config);
    }

    public boolean accept(EventAccessOperations event,
                          ChannelGroup channel,
                          RequestFilter[][] original,
                          RequestFilter[][] available,
                          CookieJar cookieJar) throws Exception {
        VectorAvailableDataSubsetter filterA = (VectorAvailableDataSubsetter)filterList.get(0);
        VectorAvailableDataSubsetter filterB = (VectorAvailableDataSubsetter)filterList.get(1);
        return (filterA.accept(event, channel, original, available, cookieJar) != filterB.accept(event,
                                                                                                 channel,
                                                                                                 original,
                                                                                                 available,
                                                                                                 cookieJar));
    }
}