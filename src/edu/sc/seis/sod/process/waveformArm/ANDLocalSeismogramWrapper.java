/**
 * ANDLocalSeismogramSubsetterWrapper.java
 *
 * @author Created by Omnicore CodeGuide
 */

package edu.sc.seis.sod.process.waveformArm;
import edu.sc.seis.sod.subsetter.waveformArm.*;

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

public class ANDLocalSeismogramWrapper implements ChannelGroupLocalSeismogramProcess {

    public ANDLocalSeismogramWrapper(LocalSeismogramProcess subsetter) {
        this.subsetter = subsetter;
    }

    public ANDLocalSeismogramWrapper(Element config) throws ConfigurationException{
        NodeList childNodes = config.getChildNodes();
        Node node;
        for(int counter = 0; counter < childNodes.getLength(); counter++) {
            node = childNodes.item(counter);
            if(node instanceof Element) {
                subsetter =
                    (LocalSeismogramProcess) SodUtil.load((Element)node, "waveformArm");
                break;
            }
        }
    }


    public ChannelGroupLocalSeismogramResult process(EventAccessOperations event,
                                                     ChannelGroup channelGroup,
                                                     RequestFilter[][] original,
                                                     RequestFilter[][] available,
                                                     LocalSeismogramImpl[][] seismograms,
                                                     CookieJar cookieJar) throws Exception {
        LocalSeismogramImpl[][] out = new LocalSeismogramImpl[seismograms.length][];
        boolean b = true;
        for (int i = 0; i < channelGroup.getChannels().length; i++) {
            LocalSeismogramResult result = subsetter.process(event,
                                                             channelGroup.getChannels()[i],
                                                             original[i],
                                                             available[i],
                                                             ForkProcess.copySeismograms(seismograms[i]),
                                                             cookieJar);
            out[i] = result.getSeismograms();
            b &= result.isSuccess();
        }
        if (! b) {
            return ChannelGroupLocalSeismogramResult.FAIL;
        }
        return new ChannelGroupLocalSeismogramResult(true, out);
    }

    LocalSeismogramProcess subsetter;
}

