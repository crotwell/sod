/**
 * ORLocalSeismogramSubsetterWrapper.java
 *
 * @author Created by Omnicore CodeGuide
 */

package edu.sc.seis.sod.process.waveform.vector;
import edu.iris.Fissures.IfEvent.EventAccessOperations;
import edu.iris.Fissures.IfSeismogramDC.RequestFilter;
import edu.iris.Fissures.seismogramDC.LocalSeismogramImpl;
import edu.sc.seis.sod.ChannelGroup;
import edu.sc.seis.sod.ConfigurationException;
import edu.sc.seis.sod.CookieJar;
import edu.sc.seis.sod.SodUtil;
import edu.sc.seis.sod.process.waveform.ForkProcess;
import edu.sc.seis.sod.process.waveform.WaveformProcess;
import edu.sc.seis.sod.process.waveform.WaveformResult;
import edu.sc.seis.sod.status.StringTree;
import edu.sc.seis.sod.status.StringTreeBranch;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

public class ORWaveformProcessWrapper implements WaveformVectorProcess {

    public ORWaveformProcessWrapper(WaveformProcess subsetter) {
        this.subsetter = subsetter;
    }

    public ORWaveformProcessWrapper(Element config) throws ConfigurationException{
        NodeList childNodes = config.getChildNodes();
        Node node;
        for(int counter = 0; counter < childNodes.getLength(); counter++) {
            node = childNodes.item(counter);
            if(node instanceof Element) {
                Object obj = SodUtil.load((Element)node, "waveform");
                if (obj instanceof WaveformProcess) {
                    subsetter =
                        (WaveformProcess) obj;
                } else {
                    throw new ConfigurationException("Object loaded is not an instance of LocalSeismogramProcess: "+obj.getClass().getName());
                }
                break;
            }
        }
    }


    public WaveformVectorResult process(EventAccessOperations event,
                                                     ChannelGroup channelGroup,
                                                     RequestFilter[][] original,
                                                     RequestFilter[][] available,
                                                     LocalSeismogramImpl[][] seismograms,
                                                     CookieJar cookieJar) throws Exception {
        LocalSeismogramImpl[][] out = new LocalSeismogramImpl[seismograms.length][];
        for (int i = 0; i < seismograms.length; i++) {
            out[i] = seismograms[i];
        }
        boolean b = false;
        StringTree[] reason = new StringTree[channelGroup.getChannels().length];
        for (int i = 0; b == false && i < channelGroup.getChannels().length; i++) {
            WaveformResult result = subsetter.process(event,
                                                             channelGroup.getChannels()[i],
                                                             original[i],
                                                             available[i],
                                                             ForkProcess.copySeismograms(seismograms[i]),
                                                             cookieJar);
            out[i] = result.getSeismograms();
            b |= result.isSuccess();
            reason[i] = result.getReason();
        }
        if (b) {
            return new WaveformVectorResult(true, out, new StringTreeBranch(this, true, reason));
        }
        return new WaveformVectorResult(false, seismograms, new StringTreeBranch(this, false, reason));
    }

    public String toString() {
        return "ORLocalSeismogramWrapper("+subsetter.toString()+")";
    }

    WaveformProcess subsetter;
}

