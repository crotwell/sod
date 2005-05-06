package edu.sc.seis.sod.subsetter.dataCenter;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import edu.iris.Fissures.IfEvent.EventAccessOperations;
import edu.iris.Fissures.IfNetwork.Channel;
import edu.iris.Fissures.IfSeismogramDC.RequestFilter;
import edu.sc.seis.fissuresUtil.cache.ProxySeismogramDC;
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
public class ChoiceDataCenter implements SeismogramDCLocator {

    public ChoiceDataCenter(Element config) throws ConfigurationException {
        NodeList choiceNodes = DOMHelper.extractNodes(config, "choice");
        for(int i = 0; i < choiceNodes.getLength(); i++) {
            choices.add(new Choice((Element)choiceNodes.item(i)));
        }
        Element otherwiseEl = DOMHelper.extractElement(config, "otherwise/*");
        otherwise = (SeismogramDCLocator)SodUtil.load(otherwiseEl, "dataCenter");
    }

    public ProxySeismogramDC getSeismogramDC(EventAccessOperations event,
                                             Channel channel,
                                             RequestFilter[] infilters,
                                             CookieJar cookieJar)
            throws Exception {
        Iterator it = choices.iterator();
        while(it.hasNext()) {
            Choice cur = (Choice)it.next();
            if(cur.accept(event, channel, cookieJar).isSuccess()) {
                System.out.println("GOING WITH CHOICE");
                return cur.getSeismogramDC(event, channel, infilters, cookieJar);
            }
        }
        System.out.println("GOT OTHERWISE");
        return otherwise.getSeismogramDC(event, channel, infilters, cookieJar);
    }

    private class Choice implements EventChannelSubsetter, SeismogramDCLocator {

        Choice(Element config) throws ConfigurationException {
            NodeList childNodes = config.getChildNodes();
            Node node;
            for(int counter = 0; counter < childNodes.getLength(); counter++) {
                node = childNodes.item(counter);
                if(node instanceof Element) {
                    SodElement sodElement = (SodElement)SodUtil.load((Element)node,
                                                                     new String[] {"dataCenter",
                                                                                   "eventChannel"});
                    if(sodElement instanceof SeismogramDCLocator) {
                        locator = (SeismogramDCLocator)sodElement;
                    } else if(sodElement instanceof EventChannelSubsetter) {
                        eventChannelSubsetter = (EventChannelSubsetter)sodElement;
                    }
                } // end of else
            }
        }

        public ProxySeismogramDC getSeismogramDC(EventAccessOperations event,
                                                 Channel channel,
                                                 RequestFilter[] infilters,
                                                 CookieJar cookieJar)
                throws Exception {
            return locator.getSeismogramDC(event, channel, infilters, cookieJar);
        }

        public StringTree accept(EventAccessOperations event,
                                 Channel channel,
                                 CookieJar cookieJar) throws Exception {
            return eventChannelSubsetter.accept(event, channel, cookieJar);
        }

        SeismogramDCLocator locator;

        EventChannelSubsetter eventChannelSubsetter;
    }

    private List choices = new ArrayList();

    private SeismogramDCLocator otherwise;
}
