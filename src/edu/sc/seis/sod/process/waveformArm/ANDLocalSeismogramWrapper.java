/**
 * ANDLocalSeismogramSubsetterWrapper.java
 *
 * @author Created by Omnicore CodeGuide
 */

package edu.sc.seis.sod.process.waveformArm;
import edu.iris.Fissures.IfEvent.EventAccessOperations;
import edu.iris.Fissures.IfSeismogramDC.RequestFilter;
import edu.iris.Fissures.seismogramDC.LocalSeismogramImpl;
import edu.sc.seis.sod.ChannelGroup;
import edu.sc.seis.sod.ConfigurationException;
import edu.sc.seis.sod.CookieJar;
import edu.sc.seis.sod.SodUtil;
import edu.sc.seis.sod.status.StringTree;
import edu.sc.seis.sod.status.StringTreeBranch;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

public class ANDLocalSeismogramWrapper implements ChannelGroupLocalSeismogramProcess {

    public ANDLocalSeismogramWrapper(LocalSeismogramProcess subsetter) {
        this.process = subsetter;
    }

    public ANDLocalSeismogramWrapper(Element config) throws ConfigurationException{
        NodeList childNodes = config.getChildNodes();
        Node node;
        for(int counter = 0; counter < childNodes.getLength(); counter++) {
            node = childNodes.item(counter);
            if(node instanceof Element) {
                process =
                    (LocalSeismogramProcess) SodUtil.load((Element)node, "waveformArm");
                break;
            }
        }
    }

    public LocalSeismogramProcess getProcess() {
        return process;
    }


    public ChannelGroupLocalSeismogramResult process(EventAccessOperations event,
                                                     ChannelGroup channelGroup,
                                                     RequestFilter[][] original,
                                                     RequestFilter[][] available,
                                                     LocalSeismogramImpl[][] seismograms,
                                                     CookieJar cookieJar) throws Exception {
        LocalSeismogramImpl[][] out = new LocalSeismogramImpl[seismograms.length][];
        boolean b = true;
        StringTree[] reason = new StringTree[channelGroup.getChannels().length];
        for (int i = 0; i < channelGroup.getChannels().length; i++) {
            LocalSeismogramResult result = process.process(event,
                                                             channelGroup.getChannels()[i],
                                                             original[i],
                                                             available[i],
                                                             ForkProcess.copySeismograms(seismograms[i]),
                                                             cookieJar);
            out[i] = result.getSeismograms();
            reason[i] = result.getReason();
            b &= result.isSuccess();
        }
        if (! b) {
            return new ChannelGroupLocalSeismogramResult(false, seismograms, new StringTreeBranch(this, false, reason));
        }
        return new ChannelGroupLocalSeismogramResult(true, out, new StringTreeBranch(this, true, reason));
    }

    public String toString() {
        return "ANDLocalSeismogramWrapper("+process.toString()+")";
    }


    LocalSeismogramProcess process;
}

