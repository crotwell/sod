package edu.sc.seis.sod.process.waveform;

import org.w3c.dom.Element;
import edu.iris.Fissures.IfNetwork.Channel;
import edu.iris.Fissures.IfSeismogramDC.RequestFilter;
import edu.iris.Fissures.model.MicroSecondDate;
import edu.iris.Fissures.seismogramDC.LocalSeismogramImpl;
import edu.sc.seis.fissuresUtil.cache.CacheEvent;
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

    public WaveformResult process(CacheEvent event,
                                  Channel channel,
                                  RequestFilter[] original,
                                  RequestFilter[] available,
                                  LocalSeismogramImpl[] seismograms,
                                  CookieJar cookieJar) {
        if(seismograms.length > 0) {
            MicroSecondDate seisBegin = seismograms[0].getBeginTime();
            MicroSecondDate seisEnd = seismograms[seismograms.length - 1].getEndTime();
            MicroSecondDate requestBegin = new MicroSecondDate(original[0].start_time);
            MicroSecondDate requestEnd = new MicroSecondDate(original[original.length - 1].end_time);
            boolean dataCoversRequestBegin = beforeOrEquals(seisBegin,
                                                            requestBegin);
            boolean dataCoversRequestEnd = beforeOrEquals(requestEnd, seisEnd);
            if(dataCoversRequestBegin && dataCoversRequestEnd) {
                String reason = "Data returned completly covers the request";
                return new WaveformResult(seismograms,
                                          new StringTreeLeaf(this, true, reason));
            }
            String reason;
            if(dataCoversRequestBegin) {
                reason = "The data returned doesn't reach the end of the request";
            } else if(dataCoversRequestEnd) {
                reason = "The data returned doesn't cover the beginning of the request";
            } else {
                reason = "The data returned doesn't overlap the request at all";
            }
            return new WaveformResult(seismograms, new StringTreeLeaf(this,
                                                                      false,
                                                                      reason));
        }
        return new WaveformResult(seismograms,
                                  new StringTreeLeaf(this,
                                                     false,
                                                     "There was no data"));
    }

    private static boolean beforeOrEquals(MicroSecondDate time,
                                          MicroSecondDate otherTime) {
        return time.equals(otherTime) || time.before(otherTime);
    }
}