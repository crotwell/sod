package edu.sc.seis.sod.subsetter.availableData;

import org.w3c.dom.Element;
import edu.iris.Fissures.IfEvent.EventAccessOperations;
import edu.iris.Fissures.IfNetwork.Channel;
import edu.iris.Fissures.IfSeismogramDC.RequestFilter;
import edu.sc.seis.sod.ConfigurationException;
import edu.sc.seis.sod.CookieJar;
import edu.sc.seis.sod.status.StringTree;
import edu.sc.seis.sod.status.StringTreeBranch;

public final class AvailableDataXOR extends AvailableDataLogicalSubsetter
        implements AvailableDataSubsetter {

    public AvailableDataXOR(Element config) throws ConfigurationException {
        super(config);
    }

    public StringTree accept(EventAccessOperations event,
                          Channel channel,
                          RequestFilter[] original,
                          RequestFilter[] available,
                          CookieJar cookieJar) throws Exception {
        AvailableDataSubsetter filterA = (AvailableDataSubsetter)filterList.get(0);
        StringTree resultA = filterA.accept(event, channel, original, available, cookieJar);
        AvailableDataSubsetter filterB = (AvailableDataSubsetter)filterList.get(1);
        StringTree resultB = filterB.accept(event, channel, original, available, cookieJar);
        return new StringTreeBranch(this, resultA.isSuccess() != resultB.isSuccess(), new StringTree[] {resultA, resultB});
    }
}// AvailableDataXOR
