/**
 * ChannelGroupLegacyExecute.java
 *
 * @author Philip Crotwell
 */

package edu.sc.seis.sod.process.waveform.vector;

import edu.iris.Fissures.IfEvent.EventAccessOperations;
import edu.iris.Fissures.IfNetwork.Channel;
import edu.iris.Fissures.IfSeismogramDC.RequestFilter;
import edu.iris.Fissures.seismogramDC.LocalSeismogramImpl;
import edu.sc.seis.sod.ChannelGroup;
import edu.sc.seis.sod.CookieJar;
import edu.sc.seis.sod.process.waveform.LegacyExecute;
import edu.sc.seis.sod.process.waveform.SaveSeismogramToFile;
import edu.sc.seis.sod.status.StringTreeLeaf;
import org.w3c.dom.Element;

public class LegacyVectorExecute extends LegacyExecute implements WaveformVectorProcess {

    public LegacyVectorExecute(Element config) {
        super(config);
    }

    public WaveformVectorResult process(EventAccessOperations event,
                                                     ChannelGroup channelGroup,
                                                     RequestFilter[][] original,
                                                     RequestFilter[][] available,
                                                     LocalSeismogramImpl[][] seismograms,
                                                     CookieJar cookieJar) throws Exception {

        LocalSeismogramImpl[][] out = new LocalSeismogramImpl[seismograms.length][];
        for (int i = 0; i < out.length; i++) {
            out[i] = new LocalSeismogramImpl[seismograms[i].length];
            System.arraycopy(seismograms[i], 0, out[i], 0, out[i].length);
        }
        String args = command;
        for (int j = 0; j < channelGroup.getChannels().length; j++) {
            Channel channel = channelGroup.getChannels()[j];
            for (int i=0; i<seismograms[j].length; i++) {
                args += " "+(String)cookieJar.get(SaveSeismogramToFile.getCookieName(prefix, channel.get_id(), i));
            } // end of for (int i=0; i<seismograms.length; i++)
        }
        int exitValue = process(args);
        return new WaveformVectorResult(out, new StringTreeLeaf(this, exitValue==0, "exit value="+exitValue));
    }
}

