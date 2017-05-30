package edu.sc.seis.sod.process.waveform;

import edu.sc.seis.seisFile.sac.SacTimeSeries;
import edu.sc.seis.sod.model.event.CacheEvent;
import edu.sc.seis.sod.model.station.ChannelImpl;

interface SacProcess {

    public void process(SacTimeSeries sac,
                        CacheEvent event,
                        ChannelImpl channel) throws Exception;
}