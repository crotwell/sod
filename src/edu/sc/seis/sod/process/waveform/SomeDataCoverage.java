package edu.sc.seis.sod.process.waveform;

import org.w3c.dom.Element;
import edu.iris.Fissures.IfEvent.EventAccessOperations;
import edu.iris.Fissures.IfNetwork.Channel;
import edu.iris.Fissures.IfSeismogramDC.RequestFilter;
import edu.iris.Fissures.seismogramDC.LocalSeismogramImpl;
import edu.sc.seis.fissuresUtil.display.DisplayUtils;
import edu.sc.seis.fissuresUtil.display.MicroSecondTimeRange;
import edu.sc.seis.fissuresUtil.time.RangeTool;
import edu.sc.seis.sod.CookieJar;
import edu.sc.seis.sod.status.StringTreeLeaf;

/**
 * @author groves Created on Sep 8, 2004
 */
public class SomeDataCoverage implements WaveformProcess {

    public SomeDataCoverage() {}

    public SomeDataCoverage(Element config) {}

    public WaveformResult process(EventAccessOperations event,
                                  Channel channel,
                                  RequestFilter[] original,
                                  RequestFilter[] available,
                                  LocalSeismogramImpl[] seismograms,
                                  CookieJar cookieJar) {
        MicroSecondTimeRange[] seisTimeRanges = new MicroSecondTimeRange[seismograms.length];
        for(int i = 0; i < seisTimeRanges.length; i++) {
            seisTimeRanges[i] = new MicroSecondTimeRange(seismograms[i].getBeginTime(),
                                                         seismograms[i].getEndTime());
        }
        MicroSecondTimeRange[] rfTimeRanges = new MicroSecondTimeRange[original.length];
        for(int i = 0; i < rfTimeRanges.length; i++) {
            rfTimeRanges[i] = new MicroSecondTimeRange(original[i]);
        }
        for(int i = 0; i < seisTimeRanges.length; i++) {
            MicroSecondTimeRange curSeisTimeRange = seisTimeRanges[i];
            for(int j = 0; j < rfTimeRanges.length; j++) {
                MicroSecondTimeRange rfTimeRange = rfTimeRanges[j];
                if(RangeTool.areOverlapping(curSeisTimeRange, rfTimeRange)) {
                    StringTreeLeaf leaf = new StringTreeLeaf(this,
                                                             true,
                                                             "Some of the data received overlapped the requested time");
                    return new WaveformResult(seismograms, leaf);
                }
            }
        }
        return new WaveformResult(seismograms,
                                  new StringTreeLeaf(this,
                                                     false,
                                                     "No received seismograms matched the original data request"));
    }
}