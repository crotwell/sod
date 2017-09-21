/**
 * SeismogramOR.java
 * 
 * @author Philip Crotwell
 */
package edu.sc.seis.sod.process.waveform;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.w3c.dom.Element;

import edu.sc.seis.seisFile.fdsnws.stationxml.Channel;
import edu.sc.seis.sod.ConfigurationException;
import edu.sc.seis.sod.LocalSeismogramArm;
import edu.sc.seis.sod.hibernate.eventpair.MeasurementStorage;
import edu.sc.seis.sod.model.event.CacheEvent;
import edu.sc.seis.sod.model.seismogram.LocalSeismogramImpl;
import edu.sc.seis.sod.model.seismogram.RequestFilter;
import edu.sc.seis.sod.status.StringTree;
import edu.sc.seis.sod.status.StringTreeBranch;

public class SeismogramOR extends ForkProcess {

    public SeismogramOR(Element config) throws ConfigurationException {
        super(config);
    }

    public WaveformResult accept(CacheEvent event,
                                  Channel channel,
                                  RequestFilter[] original,
                                  RequestFilter[] available,
                                  LocalSeismogramImpl[] seismograms,
                                  MeasurementStorage cookieJar) throws Exception {
        List reasons = new ArrayList();
        Iterator it = localSeisProcessList.iterator();
        WaveformResult result = new WaveformResult(false, seismograms, this);
        while(it.hasNext() && !result.isSuccess()) {
            WaveformProcess processor = (WaveformProcess)it.next();
            result = LocalSeismogramArm.runProcessorThreadCheck(processor, 
                                                                event,
                                                                channel,
                                                                original,
                                                                available,
                                                                result.getSeismograms(),
                                                                cookieJar);
            reasons.add(result.getReason());
        } // end of while (it.hasNext())
        return new WaveformResult(result.getSeismograms(),
                                  new StringTreeBranch(this,
                                                       result.isSuccess(),
                                                       (StringTree[])reasons.toArray(new StringTree[0])));
    }
}
