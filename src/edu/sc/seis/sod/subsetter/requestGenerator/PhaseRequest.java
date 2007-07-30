package edu.sc.seis.sod.subsetter.requestGenerator;

import org.w3c.dom.Element;

import edu.iris.Fissures.IfEvent.EventAccessOperations;
import edu.iris.Fissures.IfNetwork.Channel;
import edu.iris.Fissures.IfSeismogramDC.RequestFilter;
import edu.iris.Fissures.model.TimeInterval;
import edu.sc.seis.TauP.TauModelException;
import edu.sc.seis.fissuresUtil.display.configuration.DOMHelper;
import edu.sc.seis.sod.ConfigurationException;
import edu.sc.seis.sod.CookieJar;
import edu.sc.seis.sod.SodUtil;

public class PhaseRequest implements RequestGenerator {

    public PhaseRequest(Element config) throws ConfigurationException {
        String model = DOMHelper.extractText(config, "model", "prem");
        String beginPhase = DOMHelper.extractText(config, "beginPhase");
        String endPhase = DOMHelper.extractText(config, "endPhase");
        TimeInterval beginOffset = null;
        TimeInterval endOffset;
        double beginOffsetRatio = 1;
        double endOffsetRatio;
        TimeInterval beginOffsetRatioMinimum = null;
        TimeInterval endOffsetRatioMinimum;
        boolean negateBeginOffsetRatio = false, negateEndOffsetRatio = false;
        Element beginEl = DOMHelper.extractElement(config, "beginOffset");
        if(DOMHelper.hasElement(beginEl, "ratio")) {
            beginOffsetRatio = DOMHelper.extractDouble(beginEl, "ratio", 1.0);
            beginOffsetRatioMinimum = SodUtil.loadTimeInterval(DOMHelper.getElement(beginEl,
                                                                                    "minimum"));
            if(DOMHelper.hasElement(beginEl, "negative")) {
                negateBeginOffsetRatio = true;
            }
        } else {
            beginOffset = SodUtil.loadTimeInterval(beginEl);
        }
        try {
            Element endEl = DOMHelper.extractElement(config, "endOffset");
            if(DOMHelper.hasElement(endEl, "ratio")) {
                endOffsetRatio = DOMHelper.extractDouble(endEl, "ratio", 1.0);
                endOffsetRatioMinimum = SodUtil.loadTimeInterval(DOMHelper.getElement(endEl,
                                                                                      "minimum"));
                if(DOMHelper.hasElement(endEl, "negative")) {
                    negateEndOffsetRatio = true;
                }
                if(beginOffset != null) {
                    phaseReq = new edu.sc.seis.fissuresUtil.bag.PhaseRequest(beginPhase,
                                                                             beginOffset,
                                                                             endPhase,
                                                                             endOffsetRatio,
                                                                             endOffsetRatioMinimum,
                                                                             negateEndOffsetRatio,
                                                                             model);
                } else {
                    phaseReq = new edu.sc.seis.fissuresUtil.bag.PhaseRequest(beginPhase,
                                                                             beginOffsetRatio,
                                                                             beginOffsetRatioMinimum,
                                                                             negateBeginOffsetRatio,
                                                                             endPhase,
                                                                             endOffsetRatio,
                                                                             endOffsetRatioMinimum,
                                                                             negateEndOffsetRatio,
                                                                             model);
                }
            } else {
                endOffset = SodUtil.loadTimeInterval(endEl);
                if(beginOffset != null) {
                    phaseReq = new edu.sc.seis.fissuresUtil.bag.PhaseRequest(beginPhase,
                                                                             beginOffset,
                                                                             endPhase,
                                                                             endOffset,
                                                                             model);
                } else {
                    phaseReq = new edu.sc.seis.fissuresUtil.bag.PhaseRequest(beginPhase,
                                                                             beginOffsetRatio,
                                                                             beginOffsetRatioMinimum,
                                                                             negateBeginOffsetRatio,
                                                                             endPhase,
                                                                             endOffset,
                                                                             model);
                }
            }
        } catch(TauModelException e) {
            throw new ConfigurationException("Problem with TauPUtil.", e);
        }
    }

    public RequestFilter[] generateRequest(EventAccessOperations event,
                                           Channel channel,
                                           CookieJar jar) throws Exception {
        RequestFilter rf = phaseReq.generateRequest(event, channel);
        if(rf == null) {
            return new RequestFilter[0];
        }
        return new RequestFilter[] {rf};
    }

    private edu.sc.seis.fissuresUtil.bag.PhaseRequest phaseReq;
}// PhaseRequest
