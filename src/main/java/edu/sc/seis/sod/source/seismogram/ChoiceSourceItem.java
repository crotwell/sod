package edu.sc.seis.sod.source.seismogram;

import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import edu.sc.seis.seisFile.fdsnws.stationxml.Channel;
import edu.sc.seis.sod.ConfigurationException;
import edu.sc.seis.sod.SodElement;
import edu.sc.seis.sod.SodUtil;
import edu.sc.seis.sod.hibernate.eventpair.CookieJar;
import edu.sc.seis.sod.model.event.CacheEvent;
import edu.sc.seis.sod.model.seismogram.RequestFilter;
import edu.sc.seis.sod.status.StringTree;
import edu.sc.seis.sod.subsetter.Subsetter;
import edu.sc.seis.sod.subsetter.eventChannel.EventChannelLogicalSubsetter;
import edu.sc.seis.sod.subsetter.eventChannel.EventChannelSubsetter;
import edu.sc.seis.sod.subsetter.origin.OriginTimeRange;


public class ChoiceSourceItem implements EventChannelSubsetter, SeismogramSourceLocator {

    public ChoiceSourceItem(Element config) throws ConfigurationException {
        NodeList childNodes = config.getChildNodes();
        Node node;
        for(int counter = 0; counter < childNodes.getLength(); counter++) {
            node = childNodes.item(counter);
            if ( ! (node instanceof Element)) { continue; }
            SodElement sodElement = (SodElement)SodUtil.load((Element)node,
                                                             new String[] {"seismogram",
                                                                           "eventChannel",
                                                                           "eventStation",
                                                                           "channel",
                                                                           "station",
                                                                           "network",
                                                                           "origin"});
            if(sodElement instanceof SeismogramSourceLocator) {
                locator = (SeismogramSourceLocator)sodElement;
            } else  {
                eventChannelSubsetter = EventChannelLogicalSubsetter.createSubsetter((Subsetter)sodElement);
            }
        }
    }

    public ChoiceSourceItem(OriginTimeRange subsetter, SeismogramSourceLocator source) throws ConfigurationException {
        eventChannelSubsetter = EventChannelLogicalSubsetter.createSubsetter(subsetter);
        locator = source;
    }

    public SeismogramSource getSeismogramSource(CacheEvent event,
                                             Channel channel,
                                             RequestFilter[] infilters,
                                             CookieJar cookieJar)
            throws Exception {
        return locator.getSeismogramSource(event, channel, infilters, cookieJar);
    }

    public StringTree accept(CacheEvent event,
                             Channel channel,
                             CookieJar cookieJar) throws Exception {
        return eventChannelSubsetter.accept(event, channel, cookieJar);
    }

    SeismogramSourceLocator locator;

    EventChannelSubsetter eventChannelSubsetter;
}

