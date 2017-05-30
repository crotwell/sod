package edu.sc.seis.sod.subsetter.requestGenerator;

import org.w3c.dom.Element;

import edu.sc.seis.sod.hibernate.eventpair.CookieJar;
import edu.sc.seis.sod.model.event.CacheEvent;
import edu.sc.seis.sod.model.seismogram.RequestFilter;
import edu.sc.seis.sod.model.station.ChannelImpl;


/**
 * Describe class <code>NullRequestGenerator</code> here.
 *
 * @author Srinivasa Telukutla
 * @version 1.0
 */
public class NullRequestGenerator implements RequestGenerator {
    public NullRequestGenerator() {}

    public NullRequestGenerator(Element config) {}

    public RequestFilter[] generateRequest(CacheEvent event,
                                           ChannelImpl channel, CookieJar cookieJar){
        return new RequestFilter[0];
    }
}// NullRequestGenerator
