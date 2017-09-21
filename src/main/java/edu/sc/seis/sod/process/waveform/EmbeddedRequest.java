package edu.sc.seis.sod.process.waveform;

import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import edu.sc.seis.seisFile.fdsnws.stationxml.Channel;
import edu.sc.seis.sod.ConfigurationException;
import edu.sc.seis.sod.SodUtil;
import edu.sc.seis.sod.hibernate.eventpair.MeasurementStorage;
import edu.sc.seis.sod.model.event.CacheEvent;
import edu.sc.seis.sod.model.seismogram.LocalSeismogramImpl;
import edu.sc.seis.sod.model.seismogram.RequestFilter;
import edu.sc.seis.sod.subsetter.request.RequestSubsetter;

/**
 * @author crotwell Created on Mar 18, 2005
 */
public class EmbeddedRequest implements WaveformProcess {

    public EmbeddedRequest(RequestSubsetter request) {
        this.request = request;
    }

    public EmbeddedRequest(Element config)
            throws ConfigurationException {
        NodeList childNodes = config.getChildNodes();
        Node node;
        for(int counter = 0; counter < childNodes.getLength(); counter++) {
            node = childNodes.item(counter);
            if(node instanceof Element) {
                request = (RequestSubsetter)SodUtil.load((Element)node,
                "request");
            }
        }
    }

    public WaveformResult accept(CacheEvent event,
                                  Channel channel,
                                  RequestFilter[] original,
                                  RequestFilter[] available,
                                  LocalSeismogramImpl[] seismograms,
                                  MeasurementStorage cookieJar) throws Exception {
        WaveformResult result = new WaveformResult(seismograms,
                                                   request.accept(event,
                                                                  channel,
                                                                  original,
                                                                  cookieJar));
        return result;
    }

    RequestSubsetter request;
}