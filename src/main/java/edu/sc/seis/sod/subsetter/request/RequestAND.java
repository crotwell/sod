package edu.sc.seis.sod.subsetter.request;

import java.util.Iterator;

import org.w3c.dom.Element;

import edu.iris.Fissures.IfSeismogramDC.RequestFilter;
import edu.iris.Fissures.network.ChannelImpl;
import edu.sc.seis.fissuresUtil.cache.CacheEvent;
import edu.sc.seis.sod.ConfigurationException;
import edu.sc.seis.sod.CookieJar;
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
                          ChannelImpl channel,
                          RequestFilter[] original,
                          CookieJar cookieJar) throws Exception {
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

private static final org.apache.log4j.Logger logger = org.apache.log4j.Logger.getLogger(RequestAND.class);}// RequestSubsetterAND
