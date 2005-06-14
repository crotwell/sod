package edu.sc.seis.sod.process.waveform;

import edu.iris.Fissures.IfEvent.EventAccessOperations;
import edu.iris.Fissures.IfNetwork.Channel;
import edu.iris.Fissures.IfSeismogramDC.RequestFilter;
import edu.iris.Fissures.seismogramDC.LocalSeismogramImpl;
import edu.sc.seis.fissuresUtil.time.RangeTool;
import edu.sc.seis.sod.CookieJar;
import edu.sc.seis.sod.status.StringTreeLeaf;

/**
 * @author groves Created on Sep 8, 2004
 */
public class NoDataGaps implements WaveformProcess {

    public WaveformResult process(EventAccessOperations event,
                                  Channel channel,
                                  RequestFilter[] original,
                                  RequestFilter[] available,
                                  LocalSeismogramImpl[] seismograms,
                                  CookieJar cookieJar) {
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