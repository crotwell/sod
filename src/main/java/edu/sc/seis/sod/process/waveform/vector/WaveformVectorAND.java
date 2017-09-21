/**
 * ChannelGroupAND.java
 *
 * @author Created by Omnicore CodeGuide
 */

package edu.sc.seis.sod.process.waveform.vector;

import org.w3c.dom.Element;

import edu.sc.seis.sod.ConfigurationException;
import edu.sc.seis.sod.hibernate.eventpair.MeasurementStorage;
import edu.sc.seis.sod.model.event.CacheEvent;
import edu.sc.seis.sod.model.seismogram.LocalSeismogramImpl;
import edu.sc.seis.sod.model.seismogram.RequestFilter;
import edu.sc.seis.sod.model.station.ChannelGroup;

public class WaveformVectorAND extends WaveformVectorFork  {

    public WaveformVectorAND(Element config) throws ConfigurationException {
        super(config);
    }

    public WaveformVectorResult accept(CacheEvent event,
                                                     ChannelGroup channelGroup,
                                                     RequestFilter[][] original,
                                                     RequestFilter[][] available,
                                                     LocalSeismogramImpl[][] seismograms,
                                                     MeasurementStorage cookieJar) throws Exception {
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

