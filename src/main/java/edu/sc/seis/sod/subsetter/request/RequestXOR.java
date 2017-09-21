package edu.sc.seis.sod.subsetter.request;

import org.w3c.dom.Element;

import edu.sc.seis.seisFile.fdsnws.stationxml.Channel;
import edu.sc.seis.sod.ConfigurationException;
import edu.sc.seis.sod.hibernate.eventpair.MeasurementStorage;
import edu.sc.seis.sod.model.event.CacheEvent;
import edu.sc.seis.sod.model.seismogram.RequestFilter;
import edu.sc.seis.sod.status.StringTree;
import edu.sc.seis.sod.status.StringTreeBranch;

public final class RequestXOR extends RequestLogical implements RequestSubsetter {

    /**
     * Creates a new <code>RequestSubsetterXOR</code> instance.
     * 
     * @param config
     *            an <code>Element</code> value
     * @exception ConfigurationException
     *                if an error occurs
     */
    public RequestXOR(Element config) throws ConfigurationException {
        super(config);
    }

    public StringTree accept(CacheEvent event,
                             Channel channel,
                          RequestFilter[] original,
                          MeasurementStorage cookieJar) throws Exception {
        RequestSubsetter filterA = (RequestSubsetter)filterList.get(0);
        RequestSubsetter filterB = (RequestSubsetter)filterList.get(1);
        StringTree[] result = new StringTree[2];
        result[0] = filterA.accept(event, channel, original, cookieJar);
        result[1] = filterB.accept(event, channel, original, cookieJar);
        return new StringTreeBranch(this, (result[0].isSuccess() != result[0].isSuccess()), result);
    }
}// RequestSubsetterXOR
