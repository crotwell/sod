package edu.sc.seis.sod.subsetter.availableData;

import java.util.Iterator;
import org.w3c.dom.Element;
import edu.iris.Fissures.IfEvent.EventAccessOperations;
import edu.iris.Fissures.IfNetwork.Channel;
import edu.iris.Fissures.IfSeismogramDC.RequestFilter;
import edu.sc.seis.sod.ConfigurationException;
import edu.sc.seis.sod.CookieJar;
import edu.sc.seis.sod.status.StringTree;
import edu.sc.seis.sod.status.StringTreeBranch;

public final class AvailableDataOR extends AvailableDataLogicalSubsetter
        implements AvailableDataSubsetter {

    public AvailableDataOR(Element config) throws ConfigurationException {
        super(config);
    }

    public StringTree accept(EventAccessOperations event,
                          Channel channel,
                          RequestFilter[] original,
                          RequestFilter[] available,
                          CookieJar cookieJar) throws Exception {
        Iterator it = filterList.iterator();
        StringTree[] result = new StringTree[filterList.size()];
        int i=0;
        while(it.hasNext()) {
            AvailableDataSubsetter filter = (AvailableDataSubsetter)it.next();
            result[i] = filter.accept(event, channel, original, available, cookieJar);
            if(result[i].isSuccess()) { break; }
            i++;
        }
        return new StringTreeBranch(this, result[i].isSuccess(), result);
    }
}// AvailableDataOR
