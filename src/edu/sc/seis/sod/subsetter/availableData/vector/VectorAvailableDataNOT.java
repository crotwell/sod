/**
 * AvailableDataGroupNOT.java
 * 
 * @author Created by Omnicore CodeGuide
 */
package edu.sc.seis.sod.subsetter.availableData.vector;

import java.util.Iterator;
import org.w3c.dom.Element;
import edu.iris.Fissures.IfEvent.EventAccessOperations;
import edu.iris.Fissures.IfSeismogramDC.RequestFilter;
import edu.sc.seis.sod.ChannelGroup;
import edu.sc.seis.sod.ConfigurationException;
import edu.sc.seis.sod.CookieJar;

public class VectorAvailableDataNOT extends VectorAvailableDataLogicalSubsetter
        implements VectorAvailableDataSubsetter {

    public VectorAvailableDataNOT(Element config) throws ConfigurationException {
        super(config);
    }

    public boolean accept(EventAccessOperations event,
                          ChannelGroup channel,
                          RequestFilter[][] original,
                          RequestFilter[][] available,
                          CookieJar cookieJar) throws Exception {
        Iterator it = filterList.iterator();
        while(it.hasNext()) {
            VectorAvailableDataSubsetter filter = (VectorAvailableDataSubsetter)it.next();
            if(filter.accept(event, channel, original, available, cookieJar)) { return false; }
        }
        return true;
    }
}