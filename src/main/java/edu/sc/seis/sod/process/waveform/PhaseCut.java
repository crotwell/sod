package edu.sc.seis.sod.process.waveform;

import java.util.LinkedList;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Element;

import edu.sc.seis.sod.ConfigurationException;
import edu.sc.seis.sod.bag.Cut;
import edu.sc.seis.sod.hibernate.eventpair.CookieJar;
import edu.sc.seis.sod.model.common.FissuresException;
import edu.sc.seis.sod.model.common.MicroSecondDate;
import edu.sc.seis.sod.model.event.CacheEvent;
import edu.sc.seis.sod.model.seismogram.LocalSeismogramImpl;
import edu.sc.seis.sod.model.seismogram.RequestFilter;
import edu.sc.seis.sod.model.station.ChannelImpl;
import edu.sc.seis.sod.status.StringTreeLeaf;
import edu.sc.seis.sod.subsetter.requestGenerator.PhaseRequest;

/**
 * Cuts seismograms relative to phases. Created: Wed Nov 6 17:58:10 2002
 * 
 * @author <a href="mailto:crotwell@seis.sc.edu">Philip Crotwell </a>
 * @version $Id: PhaseCut.java 22054 2011-02-16 16:51:38Z crotwell $
 */
public class PhaseCut implements WaveformProcess {

    public PhaseCut(Element config) throws ConfigurationException {
        this.config = config;
        // use existing PhaseRequest class to calculate phase times
        phaseRequest = new PhaseRequest(config);
    }

    public PhaseCut(PhaseRequest phaseRequest) throws ConfigurationException {
        this.phaseRequest = phaseRequest;
    }

    /**
     * Cuts the seismograms based on phase arrivals.
     */
    public WaveformResult accept(CacheEvent event,
                                  ChannelImpl channel,
                                  RequestFilter[] original,
                                  RequestFilter[] available,
                                  LocalSeismogramImpl[] seismograms,
                                  CookieJar cookieJar) throws Exception {
        RequestFilter[] cutRequest = phaseRequest.generateRequest(event,
                                                                  channel,
                                                                  cookieJar);
        LocalSeismogramImpl[] cutSeis = cut(seismograms, cutRequest);
        return new WaveformResult(cutSeis,
                                  new StringTreeLeaf(this, cutSeis.length != 0));
    }

    public static LocalSeismogramImpl[] cut(LocalSeismogramImpl[] seismograms,
                                            RequestFilter[] cuts)
            throws FissuresException {
        List cutSeis = new LinkedList();
        for(int i = 0; i < cuts.length; i++) {
            Cut cut = new Cut(new MicroSecondDate(cuts[i].start_time),
                              new MicroSecondDate(cuts[i].end_time));
           for(int j = 0; j < seismograms.length; j++) {
                // cut returns null if the time interval doesn't overlap
                LocalSeismogramImpl tempSeis = cut.apply(seismograms[j]);
                if(tempSeis != null) {
                    cutSeis.add(tempSeis);
                }
            } // end of for (int i=0; i<seismograms.length; i++)
        }
        return (LocalSeismogramImpl[])cutSeis.toArray(new LocalSeismogramImpl[0]);
    }

    Element config;

    PhaseRequest phaseRequest;

    private static Logger logger = LoggerFactory.getLogger(PhaseRequest.class.getName());
}// PhaseCut
