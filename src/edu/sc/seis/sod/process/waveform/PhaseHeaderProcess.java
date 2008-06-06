package edu.sc.seis.sod.process.waveform;

import org.w3c.dom.Element;
import edu.iris.Fissures.IfEvent.EventAccessOperations;
import edu.iris.Fissures.IfNetwork.Channel;
import edu.sc.seis.TauP.Arrival;
import edu.sc.seis.TauP.TauModelException;
import edu.sc.seis.TauP.TauP_SetSac;
import edu.sc.seis.fissuresUtil.bag.TauPUtil;
import edu.sc.seis.fissuresUtil.cache.EventUtil;
import edu.sc.seis.fissuresUtil.display.configuration.DOMHelper;
import edu.sc.seis.seisFile.sac.SacTimeSeries;

public class PhaseHeaderProcess implements SacProcess {

    private static final int DEFAULT_T_HEADER = 0;

    private static final String DEFAULT_MODEL = "prem";

    private static final String DEFAULT_PHASE = "ttp";

    public PhaseHeaderProcess(Element phaseEl) {
        this.model = DOMHelper.extractText(phaseEl, "model", DEFAULT_MODEL);
        this.phaseName = DOMHelper.extractText(phaseEl, "phaseName", DEFAULT_PHASE);
        String header = DOMHelper.extractText(phaseEl, "tHeader", ""+DEFAULT_T_HEADER);
        if (header.equalsIgnoreCase("a")) {
            this.tHeader = TauP_SetSac.A_HEADER;
        } else {
            this.tHeader = DOMHelper.extractInt(phaseEl, "tHeader", DEFAULT_T_HEADER);
        }
    }

    public void process(SacTimeSeries sac,
                        EventAccessOperations event,
                        Channel channel) {
        try {
            Arrival[] arrivals = TauPUtil.getTauPUtil(model)
                    .calcTravelTimes(channel.getSite().getLocation(),
                                     EventUtil.extractOrigin(event),
                                     new String[] {phaseName});
            if(arrivals.length != 0) {
                TauP_SetSac.setSacTHeader(sac, tHeader, arrivals[0]);
            }
        } catch(TauModelException e) {
            logger.warn("Problem setting travel times for " + phaseName
                    + " in " + model, e);
        }
    }

    String model;

    String phaseName;

    int tHeader;

    private static final org.apache.log4j.Logger logger = org.apache.log4j.Logger.getLogger(PhaseHeaderProcess.class);
}
