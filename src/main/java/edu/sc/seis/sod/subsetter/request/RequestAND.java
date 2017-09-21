package edu.sc.seis.sod.subsetter.request;

import java.util.Iterator;

import org.w3c.dom.Element;

import edu.sc.seis.seisFile.fdsnws.stationxml.Channel;
import edu.sc.seis.sod.ConfigurationException;
import edu.sc.seis.sod.hibernate.eventpair.MeasurementStorage;
import edu.sc.seis.sod.model.event.CacheEvent;
import edu.sc.seis.sod.model.seismogram.RequestFilter;
import edu.sc.seis.sod.status.StringTree;
import edu.sc.seis.sod.status.StringTreeBranch;

public final class RequestAND extends RequestLogical implements RequestSubsetter {

    /**
     * Creates a new <code>RequestSubsetterAND</code> instance.
     * 
     * @param config
     *            an <code>Element</code> value
     * @exception ConfigurationException
     *                if an error occurs
     */
    public RequestAND(Element config) throws ConfigurationException {
        super(config);
    }

    public StringTree accept(CacheEvent event,
                          Channel channel,
                          RequestFilter[] original,
                          MeasurementStorage cookieJar) throws Exception {
        Iterator it = filterList.iterator();
        StringTree[] result = new StringTree[filterList.size()];
        int i =0;
        while(it.hasNext()) {
            RequestSubsetter filter = (RequestSubsetter)it.next();
            result[i] = filter.accept(event, channel, original, cookieJar);
            if ( ! result[i].isSuccess()) { 
              return new StringTreeBranch(this, false, result);
            }
            i++;
        }
        return new StringTreeBranch(this, true, result);
    }

private static final org.slf4j.Logger logger = org.slf4j.LoggerFactory.getLogger(RequestAND.class);}// RequestSubsetterAND
