/**
 * ChannelGroupAND.java
 *
 * @author Created by Omnicore CodeGuide
 */

package edu.sc.seis.sod.process.waveform.vector;

import org.w3c.dom.Element;

import edu.iris.Fissures.IfSeismogramDC.RequestFilter;
import edu.iris.Fissures.seismogramDC.LocalSeismogramImpl;
import edu.sc.seis.fissuresUtil.cache.CacheEvent;
import edu.sc.seis.fissuresUtil.hibernate.ChannelGroup;
import edu.sc.seis.sod.ConfigurationException;
import edu.sc.seis.sod.CookieJar;

public class WaveformVectorAND extends WaveformVectorFork  {

    public WaveformVectorAND(Element config) throws ConfigurationException {
        super(config);
    }

    public WaveformVectorResult accept(CacheEvent event,
                                                     ChannelGroup channelGroup,
                                                     RequestFilter[][] original,
                                                     RequestFilter[][] available,
                                                     LocalSeismogramImpl[][] seismograms,
                                                     CookieJar cookieJar) throws Exception {
        return doAND(event,
                     channelGroup,
                     original,
                     available,
                     seismograms,
                     cookieJar);

    }

    public boolean isThreadSafe() {
        return true;
    }
}

