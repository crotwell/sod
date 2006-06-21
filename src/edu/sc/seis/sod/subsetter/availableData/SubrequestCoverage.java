package edu.sc.seis.sod.subsetter.availableData;

import org.w3c.dom.Element;
import org.w3c.dom.NodeList;
import edu.iris.Fissures.IfEvent.EventAccessOperations;
import edu.iris.Fissures.IfNetwork.Channel;
import edu.iris.Fissures.IfSeismogramDC.RequestFilter;
import edu.sc.seis.fissuresUtil.display.configuration.DOMHelper;
import edu.sc.seis.sod.ConfigurationException;
import edu.sc.seis.sod.CookieJar;
import edu.sc.seis.sod.SodUtil;
import edu.sc.seis.sod.status.StringTree;
import edu.sc.seis.sod.subsetter.requestGenerator.RequestGenerator;

public class SubrequestCoverage implements AvailableDataSubsetter {

    public SubrequestCoverage(Element el) throws ConfigurationException {
        NodeList els = DOMHelper.getElements(el, "*");
        subrequest = (RequestGenerator)SodUtil.load((Element)els.item(0),
                                                    "requestGenerator");
        if(els.getLength() > 1) {
            coverageChecker = (AvailableDataSubsetter)SodUtil.load((Element)els.item(1),
                                                                   "availableData");
        }
    }

    public StringTree accept(EventAccessOperations ev,
                             Channel chan,
                             RequestFilter[] original,
                             RequestFilter[] available,
                             CookieJar cookies) throws Exception {
        RequestFilter[] sub = subrequest.generateRequest(ev, chan, cookies);
        return coverageChecker.accept(ev, chan, sub, available, cookies);
    }

    private AvailableDataSubsetter coverageChecker = new FullCoverage();

    private RequestGenerator subrequest;
}
