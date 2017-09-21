/**
 * SeismogramNOT.java
 * 
 * @author Philip Crotwell
 */
package edu.sc.seis.sod.process.waveform;

import java.util.Iterator;

import org.w3c.dom.Element;

import edu.sc.seis.seisFile.fdsnws.stationxml.Channel;
import edu.sc.seis.sod.ConfigurationException;
import edu.sc.seis.sod.LocalSeismogramArm;
import edu.sc.seis.sod.hibernate.eventpair.MeasurementStorage;
import edu.sc.seis.sod.model.event.CacheEvent;
import edu.sc.seis.sod.model.seismogram.LocalSeismogramImpl;
import edu.sc.seis.sod.model.seismogram.RequestFilter;
import edu.sc.seis.sod.status.StringTreeBranch;

public class SeismogramNOT extends ForkProcess {

    public SeismogramNOT(Element config) throws ConfigurationException {
        super(config);
    }

    public WaveformResult accept(CacheEvent event,
                                  Channel channel,
                                  RequestFilter[] original,
                                  RequestFilter[] available,
                                  LocalSeismogramImpl[] seismograms,
                                  MeasurementStorage cookieJar) throws Exception {
        WaveformResult result;
        Iterator it = localSeisProcessList.iterator();
        WaveformProcess process = (WaveformProcess)it.next();
        result = LocalSeismogramArm.runProcessorThreadCheck(process, 
                                                            event,
                                                            channel,
                                                            original,
                                                            available,
                                                            seismograms,
                                                            cookieJar);
        return new WaveformResult(result.getSeismograms(),
                                  new StringTreeBranch(this,
                                                       !result.isSuccess(),
                                                       result.getReason()));
    }
}
