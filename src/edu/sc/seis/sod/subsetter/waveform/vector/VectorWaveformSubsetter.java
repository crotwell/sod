/**
 * ChannelGroupLocalSeismogramSubsetter.java
 *
 * @author Created by Omnicore CodeGuide
 */

package edu.sc.seis.sod.subsetter.waveform.vector;

import edu.iris.Fissures.IfEvent.EventAccessOperations;
import edu.iris.Fissures.IfSeismogramDC.RequestFilter;
import edu.iris.Fissures.seismogramDC.LocalSeismogramImpl;
import edu.sc.seis.sod.ChannelGroup;
import edu.sc.seis.sod.CookieJar;
import edu.sc.seis.sod.subsetter.Subsetter;

public interface VectorWaveformSubsetter extends Subsetter {

    public boolean accept(EventAccessOperations event,
                          ChannelGroup channelGroup,
                          RequestFilter[][] original,
                          RequestFilter[][] available,
                          LocalSeismogramImpl[][] seismograms,
                          CookieJar cookieJar) throws Exception;

}

