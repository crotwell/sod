package edu.sc.seis.sod.subsetter.requestGenerator;

import org.w3c.dom.Element;

import edu.iris.Fissures.IfSeismogramDC.RequestFilter;
import edu.iris.Fissures.network.ChannelImpl;
import edu.sc.seis.fissuresUtil.cache.CacheEvent;
import edu.sc.seis.sod.CookieJar;


/**
 * Describe class <code>NullRequestGenerator</code> here.
 *
 * @author <a href="mailto:">Srinivasa Telukutla</a>
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
