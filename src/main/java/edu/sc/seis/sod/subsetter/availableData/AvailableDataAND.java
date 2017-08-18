package edu.sc.seis.sod.subsetter.availableData;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Element;

import edu.sc.seis.seisFile.fdsnws.stationxml.Channel;
import edu.sc.seis.sod.ConfigurationException;
import edu.sc.seis.sod.hibernate.eventpair.CookieJar;
import edu.sc.seis.sod.model.event.CacheEvent;
import edu.sc.seis.sod.model.seismogram.RequestFilter;
import edu.sc.seis.sod.status.ShortCircuit;
import edu.sc.seis.sod.status.StringTree;
import edu.sc.seis.sod.status.StringTreeBranch;

public final class AvailableDataAND extends AvailableDataLogicalSubsetter
        implements AvailableDataSubsetter {

    public AvailableDataAND(Element config) throws ConfigurationException {
        super(config);
    }

    public StringTree accept(CacheEvent event,
                             Channel channel,
                             RequestFilter[] request,
                             RequestFilter[] available,
                             CookieJar cookieJar) throws Exception {
        StringTree[] result = new StringTree[filterList.size()];
        for(int i = 0; i < filterList.size(); i++) {
            AvailableDataSubsetter f = (AvailableDataSubsetter)filterList.get(i);
            result[i] = f.accept(event, channel, request, available, cookieJar);
            if(!result[i].isSuccess()) {
                for(int j = i + 1; j < result.length; j++) {
                    result[j] = new ShortCircuit(filterList.get(j));
                }
                return new StringTreeBranch(this, false, result);
            }
        }
        return new StringTreeBranch(this, true, result);
    }

    static Logger logger = LoggerFactory.getLogger(AvailableDataAND.class.getName());
}// AvailableDataAND
