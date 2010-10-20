package edu.sc.seis.sod.process.waveform;

import edu.iris.Fissures.network.ChannelImpl;
import edu.sc.seis.fissuresUtil.cache.CacheEvent;
import edu.sc.seis.seisFile.sac.SacTimeSeries;

interface SacProcess {

    public void process(SacTimeSeries sac,
                        CacheEvent event,
                        ChannelImpl channel) throws Exception;
}