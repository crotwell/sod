package edu.sc.seis.sod.subsetter.availableData;

import java.util.Iterator;
import org.w3c.dom.Element;
import edu.iris.Fissures.IfEvent.EventAccessOperations;
import edu.iris.Fissures.IfNetwork.Channel;
import edu.iris.Fissures.IfSeismogramDC.RequestFilter;
import edu.sc.seis.sod.ConfigurationException;
import edu.sc.seis.sod.CookieJar;

public final class AvailableDataNOT extends AvailableDataLogicalSubsetter
        implements AvailableDataSubsetter {

    public AvailableDataNOT(Element config) throws ConfigurationException {
        super(config);
    }

    public boolean accept(EventAccessOperations event,
                          Channel channel,
                          RequestFilter[] original,
                          RequestFilter[] available,
                          CookieJar cookieJar) throws Exception {
        Iterator it = filterList.iterator();
        while(it.hasNext()) {
            AvailableDataSubsetter filter = (AvailableDataSubsetter)it.next();
            if(filter.accept(event, channel, original, available, cookieJar)) { return false; }
        }
        return true;
    }
}// AvailableDataNOT
