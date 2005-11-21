package edu.sc.seis.sod.process.waveform;

import java.util.LinkedList;
import java.util.List;
import org.apache.log4j.Category;
import org.w3c.dom.Element;
import edu.iris.Fissures.FissuresException;
import edu.iris.Fissures.IfEvent.EventAccessOperations;
import edu.iris.Fissures.IfNetwork.Channel;
import edu.iris.Fissures.IfSeismogramDC.RequestFilter;
import edu.iris.Fissures.model.MicroSecondDate;
import edu.iris.Fissures.seismogramDC.LocalSeismogramImpl;
import edu.sc.seis.fissuresUtil.bag.Cut;
import edu.sc.seis.sod.ConfigurationException;
import edu.sc.seis.sod.CookieJar;
import edu.sc.seis.sod.status.StringTreeLeaf;
import edu.sc.seis.sod.subsetter.requestGenerator.PhaseRequest;

/**
 * Cuts seismograms relative to phases. Created: Wed Nov 6 17:58:10 2002
 * 
 * @author <a href="mailto:crotwell@seis.sc.edu">Philip Crotwell </a>
 * @version $Id: PhaseCut.java 15296 2005-11-21 18:45:21Z groves $
 */
public class PhaseCut implements WaveformProcess {

    public PhaseCut(Element config) throws ConfigurationException {
        this.config = config;
        // use existing PhaseRequest class to calculate phase times
        phaseRequest = new PhaseRequest(config);
    }

    /**
     * Cuts the seismograms based on phase arrivals.
     */
    public WaveformResult process(EventAccessOperations event,
                                  Channel channel,
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
            logger.debug(cut);
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

    static Category logger = Category.getInstance(PhaseRequest.class.getName());
}// PhaseCut
