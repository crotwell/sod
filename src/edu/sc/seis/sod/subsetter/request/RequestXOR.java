package edu.sc.seis.sod.subsetter.request;

import org.w3c.dom.Element;
import edu.iris.Fissures.IfEvent.EventAccessOperations;
import edu.iris.Fissures.IfNetwork.Channel;
import edu.iris.Fissures.IfSeismogramDC.RequestFilter;
import edu.sc.seis.sod.ConfigurationException;
import edu.sc.seis.sod.CookieJar;

public final class RequestXOR extends RequestLogical implements Request {

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
    public boolean accept(EventAccessOperations event,
                          Channel channel,
                          RequestFilter[] original,
                          CookieJar cookieJar) throws Exception {
        Request filterA = (Request)filterList.get(0);
        Request filterB = (Request)filterList.get(1);
        return (filterA.accept(event, channel, original, cookieJar) != filterB.accept(event,
                                                                                      channel,
                                                                                      original,
                                                                                      cookieJar));
    }
}// RequestSubsetterXOR
