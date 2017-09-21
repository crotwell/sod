package edu.sc.seis.sod.process.waveform;

import org.w3c.dom.Element;

import edu.sc.seis.seisFile.fdsnws.stationxml.Channel;
import edu.sc.seis.sod.ConfigurationException;
import edu.sc.seis.sod.hibernate.eventpair.MeasurementStorage;
import edu.sc.seis.sod.model.event.CacheEvent;
import edu.sc.seis.sod.model.seismogram.LocalSeismogramImpl;
import edu.sc.seis.sod.model.seismogram.RequestFilter;
import edu.sc.seis.sod.status.StringTreeLeaf;
import edu.sc.seis.sod.subsetter.requestGenerator.PhaseRequest;
import edu.sc.seis.sod.util.time.CoverageTool;

public class PhaseDataCoverage implements WaveformProcess {

    public PhaseDataCoverage(Element config) throws ConfigurationException {
        phaseRequest = new PhaseRequest(config);
    }

    public WaveformResult accept(CacheEvent event,
                                  Channel channel,
                                  RequestFilter[] original,
                                  RequestFilter[] available,
                                  LocalSeismogramImpl[] seismograms,
                                  MeasurementStorage cookieJar) throws Exception {
        RequestFilter req = phaseRequest.generateRequest(event, channel);
        RequestFilter[] uncovered = CoverageTool.notCovered(new RequestFilter[] { req },
                                                            seismograms);
        return new WaveformResult(seismograms,
                                  new StringTreeLeaf(this,
                                                     uncovered.length == 0));
    }

    private PhaseRequest phaseRequest;
}
