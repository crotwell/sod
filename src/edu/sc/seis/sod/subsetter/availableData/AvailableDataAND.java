package edu.sc.seis.sod.subsetter.availableData;

import org.apache.log4j.Category;
import org.w3c.dom.Element;
import edu.iris.Fissures.IfEvent.EventAccessOperations;
import edu.iris.Fissures.IfNetwork.Channel;
import edu.iris.Fissures.IfSeismogramDC.RequestFilter;
import edu.sc.seis.sod.ConfigurationException;
import edu.sc.seis.sod.CookieJar;
import edu.sc.seis.sod.status.ShortCircuit;
import edu.sc.seis.sod.status.StringTree;
import edu.sc.seis.sod.status.StringTreeBranch;

public final class AvailableDataAND extends AvailableDataLogicalSubsetter
        implements AvailableDataSubsetter {

    public AvailableDataAND(Element config) throws ConfigurationException {
        super(config);
    }

    public StringTree accept(EventAccessOperations event,
                             Channel channel,
                             RequestFilter[] original,
                             RequestFilter[] available,
                             CookieJar cookieJar) throws Exception {
        StringTree[] result = new StringTree[filterList.size()];
        for(int i = 0; i < filterList.size(); i++) {
            AvailableDataSubsetter f = (AvailableDataSubsetter)filterList.get(i);
            result[i] = f.accept(event, channel, original, available, cookieJar);
            if(!result[i].isSuccess()) {
                for(int j = i + 1; j < result.length; j++) {
                    result[j] = new ShortCircuit(filterList.get(j));
                }
                return new StringTreeBranch(this, false, result);
            }
        }
        return new StringTreeBranch(this, true, result);
    }

    static Category logger = Category.getInstance(AvailableDataAND.class.getName());
}// AvailableDataAND
