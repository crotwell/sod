/**
 * RequestGeneratorWrapper.java
 *
 * @author Created by Omnicore CodeGuide
 */

package edu.sc.seis.sod.subsetter.waveformArm;

import edu.iris.Fissures.IfEvent.EventAccessOperations;
import edu.iris.Fissures.IfSeismogramDC.RequestFilter;
import edu.sc.seis.sod.ChannelGroup;
import edu.sc.seis.sod.ConfigurationException;
import edu.sc.seis.sod.CookieJar;
import edu.sc.seis.sod.SodUtil;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

public class RequestGeneratorWrapper implements ChannelGroupRequestGenerator  {

    public RequestGeneratorWrapper(RequestGenerator rg) {
        this.requestGenerator = rg;
    }


    public RequestFilter[][] generateRequest(EventAccessOperations event,
                                             ChannelGroup channelGroup,
                                             CookieJar cookieJar) throws Exception {
        RequestFilter[][] out = new RequestFilter[channelGroup.getChannels().length][];
        for (int i = 0; i < channelGroup.getChannels().length; i++) {
            out[i] = requestGenerator.generateRequest(event, channelGroup.getChannels()[i], cookieJar);
        }
        return out;

    }

    RequestGenerator requestGenerator;

}

