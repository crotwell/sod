package edu.sc.seis.sod.process.waveform;

import edu.sc.seis.seisFile.fdsnws.stationxml.Channel;
import edu.sc.seis.seisFile.sac.SacTimeSeries;
import edu.sc.seis.sod.model.event.CacheEvent;

interface SacProcess {

    public void process(SacTimeSeries sac,
                        CacheEvent event,
                        Channel channel) throws Exception;
}