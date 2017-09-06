package edu.sc.seis.sod.process.waveform;

import org.w3c.dom.Element;

import edu.sc.seis.seisFile.fdsnws.stationxml.Channel;
import edu.sc.seis.sod.hibernate.eventpair.CookieJar;
import edu.sc.seis.sod.model.event.CacheEvent;
import edu.sc.seis.sod.model.seismogram.LocalSeismogramImpl;
import edu.sc.seis.sod.model.seismogram.RequestFilter;
import edu.sc.seis.sod.model.seismogram.RequestFilterUtil;
import edu.sc.seis.sod.status.StringTreeLeaf;
import edu.sc.seis.sod.util.time.CoverageTool;

/**
 * @author groves Created on Sep 8, 2004
 */
public class FullDataCoverage implements WaveformProcess {

    public FullDataCoverage() {
        super();
    }

    public FullDataCoverage(Element config) {}

    public WaveformResult accept(CacheEvent event,
                                  Channel channel,
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
            reason += notCovered[i].start_time.toString() + " to " + notCovered[i].end_time.toString()+",  ";
        }
        return new WaveformResult(seismograms,
                                  new StringTreeLeaf(this, false, reason));
    }
}