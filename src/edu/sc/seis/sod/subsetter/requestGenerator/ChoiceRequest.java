package edu.sc.seis.sod.subsetter.requestGenerator;

import java.util.LinkedList;
import java.util.List;
import org.apache.log4j.Logger;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import edu.iris.Fissures.IfNetwork.Channel;
import edu.iris.Fissures.IfSeismogramDC.RequestFilter;
import edu.sc.seis.fissuresUtil.cache.CacheEvent;
import edu.sc.seis.fissuresUtil.display.configuration.DOMHelper;
import edu.sc.seis.sod.ConfigurationException;
import edu.sc.seis.sod.CookieJar;
import edu.sc.seis.sod.SodElement;
import edu.sc.seis.sod.SodUtil;
import edu.sc.seis.sod.status.StringTree;
import edu.sc.seis.sod.subsetter.eventChannel.EventChannelSubsetter;

public class ChoiceRequest implements RequestGenerator {

    public ChoiceRequest(Element config) throws ConfigurationException {
        NodeList choiceNodes = DOMHelper.extractNodes(config, "choice");
        for(int i = 0; i < choiceNodes.getLength(); i++) {
            choices.add(new Choice((Element)choiceNodes.item(i)));
        }
        if(DOMHelper.hasElement(config, "otherwise")) {
            Element otherwiseEl = DOMHelper.extractElement(config,
                                                           "otherwise/*");
            otherwise = (RequestGenerator)SodUtil.load(otherwiseEl,
                                                       "requestGenerator");
        }
    }

    public RequestFilter[] generateRequest(CacheEvent event,
                                           Channel channel,
                                           CookieJar cookieJar)
            throws Exception {
        for(int i = 0; i < choices.size(); i++) {
            Choice c = (Choice)choices.get(i);
            if(c.accept(event, channel, cookieJar).isSuccess()) {
                logger.debug("Generating request from choice " + i);
                return c.generateRequest(event, channel, cookieJar);
            }
        } // end of while (it.hasNext())
        if(otherwise != null) {
            logger.debug("Generating request with otherwise");
            return otherwise.generateRequest(event, channel, cookieJar);
        } else {
            logger.debug("Generating no request");
            return new RequestFilter[0];
        } // end of else
    }

    protected List choices = new LinkedList();

    protected RequestGenerator otherwise = null;

    class Choice implements RequestGenerator, EventChannelSubsetter {

        RequestGenerator requestGenerator;

        Choice(Element config) throws ConfigurationException {
            NodeList childNodes = config.getChildNodes();
            Node node;
            for(int counter = 0; counter < childNodes.getLength(); counter++) {
                node = childNodes.item(counter);
                if(node instanceof Element) {
                    SodElement sodElement = (SodElement)SodUtil.load((Element)node,
                                                                     new String[] {"requestGenerator",
                                                                                   "eventChannel"});
                    if(sodElement instanceof RequestGenerator) {
                        requestGenerator = (RequestGenerator)sodElement;
                    } else if(sodElement instanceof EventChannelSubsetter) {
                        eventChannelSubsetter = (EventChannelSubsetter)sodElement;
                    }
                } // end of else
            }
        }

        public RequestFilter[] generateRequest(CacheEvent event,
                                               Channel channel,
                                               CookieJar cookieJar)
                throws Exception {
            return requestGenerator.generateRequest(event, channel, cookieJar);
        }

        public StringTree accept(CacheEvent event,
                                 Channel channel,
                                 CookieJar cookieJar) throws Exception {
            return eventChannelSubsetter.accept(event, channel, cookieJar);
        }

        EventChannelSubsetter eventChannelSubsetter;
    }

    private static Logger logger = Logger.getLogger(ChoiceRequest.class);
}// PhaseRequest
