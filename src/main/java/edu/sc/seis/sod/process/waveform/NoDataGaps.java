package edu.sc.seis.sod.process.waveform;

import edu.sc.seis.seisFile.fdsnws.stationxml.Channel;
import edu.sc.seis.sod.hibernate.eventpair.MeasurementStorage;
import edu.sc.seis.sod.model.event.CacheEvent;
import edu.sc.seis.sod.model.seismogram.LocalSeismogramImpl;
import edu.sc.seis.sod.model.seismogram.RequestFilter;
import edu.sc.seis.sod.status.StringTreeLeaf;
import edu.sc.seis.sod.util.time.RangeTool;

/**
 * @author groves Created on Sep 8, 2004
 */
public class NoDataGaps implements WaveformProcess {

    public WaveformResult accept(CacheEvent event,
                                  Channel channel,
                                  RequestFilter[] original,
                                  RequestFilter[] available,
                                  LocalSeismogramImpl[] seismograms,
                                  MeasurementStorage cookieJar) {
        for(int i = 1; i < seismograms.length; i++) {
            LocalSeismogramImpl cur = seismograms[i];
            LocalSeismogramImpl prev = seismograms[i - 1];
            if(!RangeTool.areOverlapping(cur, prev)
                    && !RangeTool.areContiguous(cur, prev)) {
                String reason = "There is a gap in the returned data";
                return new WaveformResult(seismograms,
                                          new StringTreeLeaf(this,
                                                             false,
                                                             reason));
            }
        }
        return new WaveformResult(seismograms,
                                  new StringTreeLeaf(this,
                                                     true,
                                                     "There are no gaps in the returned data"));
    }
}