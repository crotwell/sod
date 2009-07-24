/**
 * AvailableDataGroupOR.java
 * 
 * @author Created by Omnicore CodeGuide
 */
package edu.sc.seis.sod.subsetter.availableData.vector;

import org.w3c.dom.Element;

import edu.iris.Fissures.IfSeismogramDC.RequestFilter;
import edu.sc.seis.fissuresUtil.cache.CacheEvent;
import edu.sc.seis.fissuresUtil.hibernate.ChannelGroup;
import edu.sc.seis.sod.ConfigurationException;
import edu.sc.seis.sod.CookieJar;
import edu.sc.seis.sod.status.ShortCircuit;
import edu.sc.seis.sod.status.StringTree;
import edu.sc.seis.sod.status.StringTreeBranch;

public class VectorAvailableDataOR extends VectorAvailableDataLogicalSubsetter
        implements VectorAvailableDataSubsetter {

    public VectorAvailableDataOR(Element config) throws ConfigurationException {
        super(config);
    }

    public StringTree accept(CacheEvent event,
                             ChannelGroup channel,
                             RequestFilter[][] original,
                             RequestFilter[][] available,
                             CookieJar cookieJar) throws Exception {
        StringTree[] result = new StringTree[filterList.size()];
        for(int i = 0; i < filterList.size(); i++) {
            VectorAvailableDataSubsetter filter = (VectorAvailableDataSubsetter)filterList.get(i);
            result[i] = filter.accept(event,
                                      channel,
                                      original,
                                      available,
                                      cookieJar);
            if(result[i].isSuccess()) {
                for(int j = i + 1; j < result.length; j++) {
                    result[j] = new ShortCircuit(filterList.get(j));
                }
                return new StringTreeBranch(this, true, result);
            }
        }
        return new StringTreeBranch(this, false, result);
    }
}