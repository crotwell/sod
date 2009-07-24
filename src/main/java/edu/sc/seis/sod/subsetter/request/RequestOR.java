package edu.sc.seis.sod.subsetter.request;

import java.util.Iterator;

import org.w3c.dom.Element;

import edu.iris.Fissures.IfNetwork.Channel;
import edu.iris.Fissures.IfSeismogramDC.RequestFilter;
import edu.sc.seis.fissuresUtil.cache.CacheEvent;
import edu.sc.seis.sod.ConfigurationException;
import edu.sc.seis.sod.CookieJar;

public final class RequestOR extends RequestLogical implements Request {

    /**
     * Creates a new <code>RequestSubsetterOR</code> instance.
     * 
     * @param config
     *            an <code>Element</code> value
     * @exception ConfigurationException
     *                if an error occurs
     */
    public RequestOR(Element config) throws ConfigurationException {
        super(config);
    }

    /**
     * Describe <code>accept</code> method here.
     * 
     * @param event
     *            an <code>EventAccessOperations</code> value
     * @param network
     *            a <code>NetworkAccess</code> value
     * @param channel
     *            a <code>Channel</code> value
     * @param original
     *            a <code>RequestFilter[]</code> value
     * @param cookies
     *            a <code>CookieJar</code> value
     * @return a <code>boolean</code> value
     * @exception Exception
     *                if an error occurs
     */
    public boolean accept(CacheEvent event,
                          Channel channel,
                          RequestFilter[] original,
                          CookieJar cookieJar) throws Exception {
        Iterator it = filterList.iterator();
        while(it.hasNext()) {
            Request filter = (Request)it.next();
            if(filter.accept(event, channel, original, cookieJar)) { return true; }
        }
        return false;
    }
}// RequestSubsetterOR
