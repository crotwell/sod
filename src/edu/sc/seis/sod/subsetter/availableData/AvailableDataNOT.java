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
import edu.sc.seis.sod.status.StringTreeLeaf;

public final class AvailableDataNOT extends AvailableDataLogicalSubsetter
        implements AvailableDataSubsetter {

    public AvailableDataNOT(Element config) throws ConfigurationException {
        super(config);
    }

    public StringTree accept(EventAccessOperations event,
                          Channel channel,
                          RequestFilter[] original,
                          RequestFilter[] available,
                          CookieJar cookieJar) throws Exception {
        Iterator it = filterList.iterator();
        StringTree result;
        if(it.hasNext()) {
            AvailableDataSubsetter filter = (AvailableDataSubsetter)it.next();
            result = filter.accept(event, channel, original, available, cookieJar);
            return new StringTreeBranch(this, ! result.isSuccess(), result);
        }
        return new StringTreeLeaf(this, false, "Empty NOT");
    }
}// AvailableDataNOT
