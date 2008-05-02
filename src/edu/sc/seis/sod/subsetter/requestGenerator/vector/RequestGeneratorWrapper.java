/**
 * RequestGeneratorWrapper.java
 * 
 * @author Created by Omnicore CodeGuide
 */
package edu.sc.seis.sod.subsetter.requestGenerator.vector;

import org.w3c.dom.Element;
import org.w3c.dom.NodeList;
import edu.iris.Fissures.IfSeismogramDC.RequestFilter;
import edu.sc.seis.fissuresUtil.cache.CacheEvent;
import edu.sc.seis.fissuresUtil.hibernate.ChannelGroup;
import edu.sc.seis.sod.ConfigurationException;
import edu.sc.seis.sod.CookieJar;
import edu.sc.seis.sod.SodUtil;
import edu.sc.seis.sod.subsetter.requestGenerator.RequestGenerator;

public class RequestGeneratorWrapper implements VectorRequestGenerator {

    public RequestGeneratorWrapper(Element config)
            throws ConfigurationException {
        NodeList nl = config.getChildNodes();
        for(int i = 0; i < nl.getLength(); i++) {
            if(nl.item(i) instanceof Element) {
                Element e = (Element)nl.item(i);
                Object o = SodUtil.load(e, "requestGenerator");
                this.requestGenerator = (RequestGenerator)o;
                break;
            }
        }
    }

    public RequestGeneratorWrapper(RequestGenerator rg) {
        this.requestGenerator = rg;
    }

    public RequestFilter[][] generateRequest(CacheEvent event,
                                             ChannelGroup channelGroup,
                                             CookieJar cookieJar)
            throws Exception {
        RequestFilter[][] out = new RequestFilter[channelGroup.getChannels().length][];
        for(int i = 0; i < channelGroup.getChannels().length; i++) {
            out[i] = requestGenerator.generateRequest(event,
                                                      channelGroup.getChannels()[i],
                                                      cookieJar);
        }
        return out;
    }

    RequestGenerator requestGenerator;
}