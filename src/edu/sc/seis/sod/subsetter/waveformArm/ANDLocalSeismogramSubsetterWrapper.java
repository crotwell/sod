/**
 * ANDLocalSeismogramSubsetterWrapper.java
 *
 * @author Created by Omnicore CodeGuide
 */

package edu.sc.seis.sod.subsetter.waveformArm;

import edu.iris.Fissures.IfEvent.EventAccessOperations;
import edu.iris.Fissures.IfSeismogramDC.RequestFilter;
import edu.iris.Fissures.seismogramDC.LocalSeismogramImpl;
import edu.sc.seis.sod.ChannelGroup;
import edu.sc.seis.sod.ConfigurationException;
import edu.sc.seis.sod.CookieJar;
import edu.sc.seis.sod.SodUtil;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

public class ANDLocalSeismogramSubsetterWrapper implements ChannelGroupLocalSeismogramSubsetter {

    public ANDLocalSeismogramSubsetterWrapper(LocalSeismogramSubsetter subsetter) {
        this.subsetter = subsetter;
    }

    public ANDLocalSeismogramSubsetterWrapper(Element config) throws ConfigurationException{
        NodeList childNodes = config.getChildNodes();
        Node node;
        for(int counter = 0; counter < childNodes.getLength(); counter++) {
            node = childNodes.item(counter);
            if(node instanceof Element) {
                subsetter =
                    (LocalSeismogramSubsetter) SodUtil.load((Element)node, "waveformArm");
                break;
            }
        }
    }


    public boolean accept(EventAccessOperations event,
                          ChannelGroup channelGroup,
                          RequestFilter[][] original,
                          RequestFilter[][] available,
                          LocalSeismogramImpl[][] seismograms,
                          CookieJar cookieJar) throws Exception {
        for (int i = 0; i < channelGroup.getChannels().length; i++) {
            if ( ! subsetter.accept(event, channelGroup.getChannels()[i], original[i], available[i], seismograms[i], cookieJar)) {
                return false;
            }
        }
        return true;
    }

    LocalSeismogramSubsetter subsetter;
}

