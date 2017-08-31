package edu.sc.seis.sod.subsetter.requestGenerator;

import java.time.Duration;

import org.w3c.dom.Element;

import edu.sc.seis.TauP.TauModelException;
import edu.sc.seis.seisFile.fdsnws.stationxml.Channel;
import edu.sc.seis.sod.ConfigurationException;
import edu.sc.seis.sod.DOMHelper;
import edu.sc.seis.sod.SodUtil;
import edu.sc.seis.sod.hibernate.eventpair.CookieJar;
import edu.sc.seis.sod.model.event.CacheEvent;
import edu.sc.seis.sod.model.seismogram.RequestFilter;

public class PhaseRequest implements RequestGenerator {

    public PhaseRequest(Element config) throws ConfigurationException {
        String model = DOMHelper.extractText(config, "model", "prem");
        String beginPhase = DOMHelper.extractText(config, "beginPhase");
        String endPhase = DOMHelper.extractText(config, "endPhase");
        Duration beginOffset = null;
        Duration endOffset;
        double beginOffsetRatio = 1;
        double endOffsetRatio;
        Duration beginOffsetRatioMinimum = null;
        Duration endOffsetRatioMinimum;
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
                    phaseReq = new edu.sc.seis.sod.bag.PhaseRequest(beginPhase,
                                                                             beginOffset,
                                                                             endPhase,
                                                                             endOffsetRatio,
                                                                             endOffsetRatioMinimum,
                                                                             negateEndOffsetRatio,
                                                                             model);
                } else {
                    phaseReq = new edu.sc.seis.sod.bag.PhaseRequest(beginPhase,
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
                    phaseReq = new edu.sc.seis.sod.bag.PhaseRequest(beginPhase,
                                                                             beginOffset,
                                                                             endPhase,
                                                                             endOffset,
                                                                             model);
                } else {
                    phaseReq = new edu.sc.seis.sod.bag.PhaseRequest(beginPhase,
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

    public PhaseRequest(String beginPhase,
                        Duration beginOffset,
                        String endPhase,
                        Duration endOffset,
                        String model) throws TauModelException {
        phaseReq = new edu.sc.seis.sod.bag.PhaseRequest(beginPhase, beginOffset, endPhase, endOffset, model);
    }

    public RequestFilter[] generateRequest(CacheEvent event,
                                           Channel channel,
                                           CookieJar jar) throws Exception {
        RequestFilter rf =  generateRequest(event, channel);
        if(rf == null) {
            return new RequestFilter[0];
        }
        return new RequestFilter[] {rf};
    }

    public RequestFilter generateRequest(CacheEvent event,
                                           Channel channel) throws Exception {
        return phaseReq.generateRequest(event, channel);
    }
    
    public edu.sc.seis.sod.bag.PhaseRequest getPhaseReq() {
        return phaseReq;
    }

    private edu.sc.seis.sod.bag.PhaseRequest phaseReq;
}// PhaseRequest
