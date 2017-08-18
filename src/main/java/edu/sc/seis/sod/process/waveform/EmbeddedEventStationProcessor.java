package edu.sc.seis.sod.process.waveform;

import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import edu.sc.seis.seisFile.fdsnws.stationxml.Channel;
import edu.sc.seis.seisFile.fdsnws.stationxml.Station;
import edu.sc.seis.sod.ConfigurationException;
import edu.sc.seis.sod.SodUtil;
import edu.sc.seis.sod.hibernate.eventpair.CookieJar;
import edu.sc.seis.sod.model.event.CacheEvent;
import edu.sc.seis.sod.model.seismogram.LocalSeismogramImpl;
import edu.sc.seis.sod.model.seismogram.RequestFilter;
import edu.sc.seis.sod.subsetter.eventStation.EventStationSubsetter;

/**
 * @author crotwell Created on Mar 18, 2005
 */
public class EmbeddedEventStationProcessor implements WaveformProcess {

    public EmbeddedEventStationProcessor(EventStationSubsetter eventStation) {
        this.eventStationSubsetter = eventStation;
    }

    public EmbeddedEventStationProcessor(Element config)
            throws ConfigurationException {
        NodeList childNodes = config.getChildNodes();
        Node node;
        for(int counter = 0; counter < childNodes.getLength(); counter++) {
            node = childNodes.item(counter);
            if(node instanceof Element) {
                eventStationSubsetter = (EventStationSubsetter)SodUtil.load((Element)node,
                                                                            "eventStation");
            }
        }
    }

    public WaveformResult accept(CacheEvent event,
                                  Channel channel,
                                  RequestFilter[] original,
                                  RequestFilter[] available,
                                  LocalSeismogramImpl[] seismograms,
                                  CookieJar cookieJar) throws Exception {
        WaveformResult result = new WaveformResult(seismograms,
                                                   eventStationSubsetter.accept(event,
                                                                                (Station)channel.getStation(),
                                                                                cookieJar));
        return result;
    }

    EventStationSubsetter eventStationSubsetter;
}