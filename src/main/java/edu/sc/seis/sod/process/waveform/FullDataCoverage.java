package edu.sc.seis.sod.process.waveform;

import org.w3c.dom.Element;

import edu.iris.Fissures.IfSeismogramDC.RequestFilter;
import edu.iris.Fissures.network.ChannelImpl;
import edu.iris.Fissures.seismogramDC.LocalSeismogramImpl;
import edu.iris.Fissures.seismogramDC.RequestFilterUtil;
import edu.sc.seis.fissuresUtil.cache.CacheEvent;
import edu.sc.seis.fissuresUtil.time.CoverageTool;
import edu.sc.seis.sod.CookieJar;
import edu.sc.seis.sod.status.StringTreeLeaf;

/**
 * @author groves Created on Sep 8, 2004
 */
public class FullDataCoverage implements WaveformProcess {

    public FullDataCoverage() {
        super();
    }

    public FullDataCoverage(Element config) {}

    public WaveformResult accept(CacheEvent event,
                                  ChannelImpl channel,
                                  RequestFilter[] original,
                                  RequestFilter[] available,
                                  LocalSeismogramImpl[] seismograms,
                                  CookieJar cookieJar) {
        if (seismograms.length == 0) {
            return new WaveformResult(seismograms,
                                      new StringTreeLeaf(this, false, "No seismograms"));
        }
        RequestFilter[] notCovered = CoverageTool.notCovered(original, seismograms);
        notCovered = RequestFilterUtil.removeSmallRequests(notCovered, seismograms[0].getSampling().getPeriod()); // remove time windows smaller than one sample
        if (notCovered.length == 0) {
            String reason = "Data returned completly covers the request";
            return new WaveformResult(seismograms,
                                      new StringTreeLeaf(this, true, reason));
        }

        String reason = "Data does not cover "+notCovered.length+" sections of the request. ";
        for (int i = 0; i < notCovered.length; i++) {
            reason += notCovered[i].start_time.date_time + " to " + notCovered[i].end_time.date_time+",  ";
        }
        return new WaveformResult(seismograms,
                                  new StringTreeLeaf(this, false, reason));
    }
}