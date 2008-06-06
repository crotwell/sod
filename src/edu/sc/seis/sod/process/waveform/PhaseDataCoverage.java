package edu.sc.seis.sod.process.waveform;

import org.w3c.dom.Element;

import edu.iris.Fissures.IfSeismogramDC.RequestFilter;
import edu.iris.Fissures.network.ChannelImpl;
import edu.iris.Fissures.seismogramDC.LocalSeismogramImpl;
import edu.sc.seis.fissuresUtil.cache.CacheEvent;
import edu.sc.seis.fissuresUtil.time.CoverageTool;
import edu.sc.seis.sod.ConfigurationException;
import edu.sc.seis.sod.CookieJar;
import edu.sc.seis.sod.status.StringTreeLeaf;
import edu.sc.seis.sod.subsetter.requestGenerator.PhaseRequest;

public class PhaseDataCoverage implements WaveformProcess {

    public PhaseDataCoverage(Element config) throws ConfigurationException {
        phaseRequest = new PhaseRequest(config);
    }

    public WaveformResult process(CacheEvent event,
                                  ChannelImpl channel,
                                  RequestFilter[] original,
                                  RequestFilter[] available,
                                  LocalSeismogramImpl[] seismograms,
                                  CookieJar cookieJar) throws Exception {
        RequestFilter req = phaseRequest.generateRequest(event, channel);
        RequestFilter[] uncovered = CoverageTool.notCovered(new RequestFilter[] { req },
                                                            seismograms);
        return new WaveformResult(seismograms,
                                  new StringTreeLeaf(this,
                                                     uncovered.length == 0));
    }

    private PhaseRequest phaseRequest;
}
