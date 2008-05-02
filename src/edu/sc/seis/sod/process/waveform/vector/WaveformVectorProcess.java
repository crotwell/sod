/**
 * ChannelGroupLocalSeismogramProcess.java
 * 
 * @author Created by Omnicore CodeGuide
 */
package edu.sc.seis.sod.process.waveform.vector;

import edu.iris.Fissures.IfSeismogramDC.RequestFilter;
import edu.iris.Fissures.seismogramDC.LocalSeismogramImpl;
import edu.sc.seis.fissuresUtil.cache.CacheEvent;
import edu.sc.seis.fissuresUtil.hibernate.ChannelGroup;
import edu.sc.seis.sod.CookieJar;

public interface WaveformVectorProcess {

    public WaveformVectorResult process(CacheEvent event,
                                                     ChannelGroup channelGroup,
                                                     RequestFilter[][] original,
                                                     RequestFilter[][] available,
                                                     LocalSeismogramImpl[][] seismograms,
                                                     CookieJar cookieJar)
            throws Exception;
}
