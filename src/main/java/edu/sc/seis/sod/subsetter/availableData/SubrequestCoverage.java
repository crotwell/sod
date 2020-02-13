package edu.sc.seis.sod.subsetter.availableData;

import javax.xml.xpath.XPathException;

import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

import edu.sc.seis.seisFile.fdsnws.stationxml.Channel;
import edu.sc.seis.sod.ConfigurationException;
import edu.sc.seis.sod.DOMHelper;
import edu.sc.seis.sod.SodUtil;
import edu.sc.seis.sod.hibernate.eventpair.MeasurementStorage;
import edu.sc.seis.sod.model.event.CacheEvent;
import edu.sc.seis.sod.model.seismogram.RequestFilter;
import edu.sc.seis.sod.status.StringTree;
import edu.sc.seis.sod.subsetter.requestGenerator.RequestGenerator;

public class SubrequestCoverage implements AvailableDataSubsetter {

    public SubrequestCoverage(Element el) throws ConfigurationException {
        try {
			NodeList els = DOMHelper.getElements(el, "*");
			subrequest = (RequestGenerator)SodUtil.load((Element)els.item(0),
			                                            "requestGenerator");
			if(els.getLength() > 1) {
			    coverageChecker = (AvailableDataSubsetter)SodUtil.load((Element)els.item(1),
			                                                           "availableData");
			}
		} catch (XPathException e) {
			throw new ConfigurationException("problem with xpath", e);
		}
    }

    public StringTree accept(CacheEvent ev,
                             Channel chan,
                             RequestFilter[] request,
                             RequestFilter[] available,
                             MeasurementStorage cookies) throws Exception {
        RequestFilter[] sub = subrequest.generateRequest(ev, chan, cookies);
        return coverageChecker.accept(ev, chan, sub, available, cookies);
    }

    private AvailableDataSubsetter coverageChecker = new FullCoverage();

    private RequestGenerator subrequest;
}
