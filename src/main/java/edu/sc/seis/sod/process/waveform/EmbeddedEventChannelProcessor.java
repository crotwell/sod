package edu.sc.seis.sod.process.waveform;

import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import edu.sc.seis.sod.ConfigurationException;
import edu.sc.seis.sod.SodUtil;
import edu.sc.seis.sod.hibernate.eventpair.CookieJar;
import edu.sc.seis.sod.model.event.CacheEvent;
import edu.sc.seis.sod.model.seismogram.LocalSeismogramImpl;
import edu.sc.seis.sod.model.seismogram.RequestFilter;
import edu.sc.seis.sod.model.station.ChannelImpl;
import edu.sc.seis.sod.subsetter.eventChannel.EventChannelSubsetter;

/**
 * @author crotwell Created on Mar 18, 2005
 */
public class EmbeddedEventChannelProcessor implements WaveformProcess {

    public EmbeddedEventChannelProcessor(EventChannelSubsetter eventChannel) {
        this.eventChannelSubsetter = eventChannel;
    }

    public EmbeddedEventChannelProcessor(Element config)
            throws ConfigurationException {
        NodeList childNodes = config.getChildNodes();
        Node node;
        for(int counter = 0; counter < childNodes.getLength(); counter++) {
            node = childNodes.item(counter);
            if(node instanceof Element) {
                eventChannelSubsetter = (EventChannelSubsetter)SodUtil.load((Element)node,
                                                                            "eventChannel");
            }
        }
    }

    public WaveformResult accept(CacheEvent event,
                                  ChannelImpl channel,
                                  RequestFilter[] original,
                                  RequestFilter[] available,
                                  LocalSeismogramImpl[] seismograms,
                                  CookieJar cookieJar) throws Exception {
        WaveformResult result = new WaveformResult(seismograms,
                                                   eventChannelSubsetter.accept(event,
                                                                                channel,
                                                                                cookieJar));
        return result;
    }

    EventChannelSubsetter eventChannelSubsetter;
}