/**
 * LocalSeismogramProcessWrapper.java
 *
 * @author Created by Omnicore CodeGuide
 */

package edu.sc.seis.sod.process.waveformArm;

import edu.iris.Fissures.IfEvent.EventAccessOperations;
import edu.iris.Fissures.IfSeismogramDC.RequestFilter;
import edu.iris.Fissures.seismogramDC.LocalSeismogramImpl;
import edu.sc.seis.sod.ChannelGroup;
import edu.sc.seis.sod.CookieJar;

public class LocalSeismogramProcessWrapper implements ChannelGroupLocalSeismogramProcess {

    public LocalSeismogramProcessWrapper(LocalSeismogramProcess sp) {
        this.seisProcess = sp;
    }

    public LocalSeismogramImpl[][] process(EventAccessOperations event,
                                           ChannelGroup channelGroup,
                                           RequestFilter[][] original,
                                           RequestFilter[][] available,
                                           LocalSeismogramImpl[][] seismograms,
                                           CookieJar cookieJar) throws Exception {
        LocalSeismogramImpl[][] out = new LocalSeismogramImpl[seismograms.length][];
        for (int i = 0; i < out.length; i++) {
            out[i] = seisProcess.process(event, channelGroup.getChannels()[i], original[i], available[i], seismograms[i], cookieJar);
        }
        return out;
    }

    LocalSeismogramProcess seisProcess;
}

