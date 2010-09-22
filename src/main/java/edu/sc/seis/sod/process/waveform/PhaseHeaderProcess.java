package edu.sc.seis.sod.process.waveform;

import java.util.List;

import org.w3c.dom.Element;

import edu.iris.Fissures.IfEvent.EventAccessOperations;
import edu.iris.Fissures.IfNetwork.Channel;
import edu.iris.Fissures.network.ChannelImpl;
import edu.sc.seis.TauP.Arrival;
import edu.sc.seis.TauP.TauModelException;
import edu.sc.seis.TauP.TauP_SetSac;
import edu.sc.seis.fissuresUtil.bag.TauPUtil;
import edu.sc.seis.fissuresUtil.cache.CacheEvent;
import edu.sc.seis.fissuresUtil.cache.EventUtil;
import edu.sc.seis.fissuresUtil.display.configuration.DOMHelper;
import edu.sc.seis.seisFile.sac.SacTimeSeries;

public class PhaseHeaderProcess implements SacProcess {

    private static final int DEFAULT_T_HEADER = 0;

    private static final String DEFAULT_MODEL = "prem";

    private static final String DEFAULT_PHASE = "ttp";

    
    public PhaseHeaderProcess(String model, String phaseName, int tHeader, int arrivalIndex) {
        this.model = model;
        this.phaseName = phaseName;
        this.tHeader = tHeader;
        this.arrivalIndex = arrivalIndex;
        // shift to zero based index, so positive index minus one, neg index stays same
        if (this.arrivalIndex > 0) { this.arrivalIndex -= 1; }
    }

    public PhaseHeaderProcess(Element phaseEl) {
        this.model = DOMHelper.extractText(phaseEl, "model", DEFAULT_MODEL);
        this.phaseName = DOMHelper.extractText(phaseEl, "phaseName", DEFAULT_PHASE);
        String header = DOMHelper.extractText(phaseEl, "tHeader", ""+DEFAULT_T_HEADER);
        if (header.equalsIgnoreCase("a")) {
            this.tHeader = TauP_SetSac.A_HEADER;
        } else {
            this.tHeader = DOMHelper.extractInt(phaseEl, "tHeader", DEFAULT_T_HEADER);
        }
        arrivalIndex = DOMHelper.extractInt(phaseEl, "arrivalIndex", 1);
        // shift to zero based index, so positive index minus one, neg index stays same
        if (arrivalIndex > 0) { arrivalIndex -= 1; }
    }

    public void process(SacTimeSeries sac,
                        CacheEvent event,
                        ChannelImpl channel) {
        try {
            List<Arrival> arrivals = TauPUtil.getTauPUtil(model)
                    .calcTravelTimes(channel.getSite().getLocation(),
                                     EventUtil.extractOrigin(event),
                                     new String[] {phaseName});
            if ( arrivalIndex >= 0 && arrivals.size() > arrivalIndex) {
                TauP_SetSac.setSacTHeader(sac, tHeader, arrivals.get(arrivalIndex));
            } else if ( arrivalIndex < 0 && arrivals.size() > -1*arrivalIndex) {
                TauP_SetSac.setSacTHeader(sac, tHeader, arrivals.get(arrivals.size()+arrivalIndex));
            }
        } catch(TauModelException e) {
            logger.warn("Problem setting travel times for " + phaseName
                    + " in " + model, e);
        }
    }

    String model;

    String phaseName;

    int tHeader;
    
    int arrivalIndex = 0;

    private static final org.apache.log4j.Logger logger = org.apache.log4j.Logger.getLogger(PhaseHeaderProcess.class);
}
