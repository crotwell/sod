package edu.sc.seis.sod.source.seismogram;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import edu.iris.Fissures.IfSeismogramDC.RequestFilter;
import edu.iris.Fissures.network.ChannelImpl;
import edu.sc.seis.fissuresUtil.cache.CacheEvent;
import edu.sc.seis.fissuresUtil.display.configuration.DOMHelper;
import edu.sc.seis.sod.ConfigurationException;
import edu.sc.seis.sod.CookieJar;
import edu.sc.seis.sod.SodElement;
import edu.sc.seis.sod.SodUtil;
import edu.sc.seis.sod.status.StringTree;
import edu.sc.seis.sod.subsetter.eventChannel.EventChannelSubsetter;

/**
 * @author groves Created on May 4, 2005
 */
public class ChoiceSource implements SeismogramSourceLocator {

    public ChoiceSource(Element config) throws ConfigurationException {
        NodeList choiceNodes = DOMHelper.extractNodes(config, "choice");
        for(int i = 0; i < choiceNodes.getLength(); i++) {
            choices.add(new Choice((Element)choiceNodes.item(i)));
        }
        Element otherwiseEl = DOMHelper.extractElement(config, "otherwise/*");
        otherwise = (SeismogramSourceLocator)SodUtil.load(otherwiseEl, "seismogram");
    }

    public SeismogramSource getSeismogramSource(CacheEvent event,
                                             ChannelImpl channel,
                                             RequestFilter[] infilters,
                                             CookieJar cookieJar)
            throws Exception {
        Iterator<Choice> it = choices.iterator();
        while(it.hasNext()) {
            Choice cur = it.next();
            if(cur.accept(event, channel, cookieJar).isSuccess()) {
                return cur.getSeismogramSource(event, channel, infilters, cookieJar);
            }
        }
        return otherwise.getSeismogramSource(event, channel, infilters, cookieJar);
    }

    private class Choice implements EventChannelSubsetter, SeismogramSourceLocator {

        Choice(Element config) throws ConfigurationException {
            NodeList childNodes = config.getChildNodes();
            Node node;
            for(int counter = 0; counter < childNodes.getLength(); counter++) {
                node = childNodes.item(counter);
                if(node instanceof Element) {
                    SodElement sodElement = (SodElement)SodUtil.load((Element)node,
                                                                     new String[] {"dataCenter",
                                                                                   "eventChannel"});
                    if(sodElement instanceof SeismogramSourceLocator) {
                        locator = (SeismogramSourceLocator)sodElement;
                    } else if(sodElement instanceof EventChannelSubsetter) {
                        eventChannelSubsetter = (EventChannelSubsetter)sodElement;
                    }
                } // end of else
            }
        }

        public SeismogramSource getSeismogramSource(CacheEvent event,
                                                 ChannelImpl channel,
                                                 RequestFilter[] infilters,
                                                 CookieJar cookieJar)
                throws Exception {
            return locator.getSeismogramSource(event, channel, infilters, cookieJar);
        }

        public StringTree accept(CacheEvent event,
                                 ChannelImpl channel,
                                 CookieJar cookieJar) throws Exception {
            return eventChannelSubsetter.accept(event, channel, cookieJar);
        }

        SeismogramSourceLocator locator;

        EventChannelSubsetter eventChannelSubsetter;
    }

    private List<Choice> choices = new ArrayList<Choice>();

    private SeismogramSourceLocator otherwise;
}