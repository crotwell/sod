package edu.sc.seis.sod.subsetter.requestGenerator;

import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import edu.iris.Fissures.IfEvent.EventAccessOperations;
import edu.iris.Fissures.IfNetwork.Channel;
import edu.iris.Fissures.IfSeismogramDC.RequestFilter;
import edu.sc.seis.fissuresUtil.display.configuration.DOMHelper;
import edu.sc.seis.sod.ConfigurationException;
import edu.sc.seis.sod.CookieJar;
import edu.sc.seis.sod.SodElement;
import edu.sc.seis.sod.SodUtil;
import edu.sc.seis.sod.status.StringTree;
import edu.sc.seis.sod.subsetter.eventChannel.EventChannelSubsetter;

public class ChoiceRequest implements RequestGenerator {

    public ChoiceRequest(Element config) throws ConfigurationException {
        NodeList childNodes = config.getChildNodes();
        Node node;
        for(int counter = 0; counter < childNodes.getLength(); counter++) {
            node = childNodes.item(counter);
            if(node instanceof Element) {
                Element element = (Element)node;
                if(element.getTagName().equals("choice")) {
                    choices.add(new Choice(element));
                } else if(element.getTagName().equals("otherwise")) {
                    otherwise = (RequestGenerator)SodUtil.load(DOMHelper.extractElement((Element)node,
                                                                                        "*"),
                                                               "requestGenerator");
                } // end of else
            }
        }
    }

    public RequestFilter[] generateRequest(EventAccessOperations event,
                                           Channel channel,
                                           CookieJar cookieJar)
            throws Exception {
        Iterator it = choices.iterator();
        while(it.hasNext()) {
            Choice c = (Choice)it.next();
            if(c.accept(event, channel, cookieJar).isSuccess()) { return c.generateRequest(event,
                                                                                           channel,
                                                                                           cookieJar); }
        } // end of while (it.hasNext())
        if(otherwise != null) {
            return otherwise.generateRequest(event, channel, cookieJar);
        } else {
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

        public RequestFilter[] generateRequest(EventAccessOperations event,
                                               Channel channel,
                                               CookieJar cookieJar)
                throws Exception {
            return requestGenerator.generateRequest(event, channel, cookieJar);
        }

        public StringTree accept(EventAccessOperations event,
                                 Channel channel,
                                 CookieJar cookieJar) throws Exception {
            return eventChannelSubsetter.accept(event, channel, cookieJar);
        }

        EventChannelSubsetter eventChannelSubsetter;
    }
}// PhaseRequest
