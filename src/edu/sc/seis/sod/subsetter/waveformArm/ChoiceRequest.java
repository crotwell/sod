package edu.sc.seis.sod.subsetter.waveformArm;

import edu.sc.seis.sod.*;
import edu.sc.seis.TauP.*;

import edu.iris.Fissures.IfEvent.*;
import edu.iris.Fissures.event.*;
import edu.iris.Fissures.IfNetwork.*;
import edu.iris.Fissures.network.*;
import edu.iris.Fissures.Location;
import edu.iris.Fissures.model.*;
import edu.iris.Fissures.IfSeismogramDC.*;

import java.util.*;

import org.w3c.dom.*;

/**
 * sample xml file
 *<pre>
 *    &lt;choiceRequest&gt;
 *     &lt;eventStationChoice&gt;
 *       &lt;eventStationDistance&gt;
 *          &lt;distanceRange&gt;
 *              &lt;unit&gt;DEGREE&lt;/unit&gt;
 *              &lt;min&gt;30&lt;/min&gt;
 *          &lt;/distanceRange&gt;
 *       &lt;/eventStationDistance&gt;
 *     &lt;eventStationChoice&gt;
 *  &lt;phaseRequest&gt;
 *      &lt;beginPhase&gt;ttp&lt;/beginPhase&gt;
 *          &lt;beginOffset&gt;
 *          &lt;unit&gt;SECOND&lt;/unit&gt;
 *          &lt;value&gt;-120&lt;/value&gt;
 *      &lt;/beginOffset&gt;
 *      &lt;endPhase&gt;tts&lt;/endPhase&gt;
 *      &lt;endOffset&gt;
 *          &lt;unit&gt;SECOND&lt;/unit&gt;
 *          &lt;value&gt;600&lt;/value&gt;
 *      &lt;/endOffset&gt;
 *  &lt;/phaseRequest&gt;
 *     &lt;eventStationChoice&gt;
 *       &lt;eventStationDistance&gt;
 *          &lt;distanceRange&gt;
 *              &lt;unit&gt;DEGREE&lt;/unit&gt;
 *              &lt;max&gt;30&lt;/max&gt;
 *          &lt;/distanceRange&gt;
 *       &lt;/eventStationDistance&gt;
 *     &lt;/eventStationChoice&gt;
 *     &lt;eventStationChoice&gt;
 *  &lt;phaseRequest&gt;
 *      &lt;beginPhase&gt;ttp&lt;/beginPhase&gt;
 *          &lt;beginOffset&gt;
 *          &lt;unit&gt;SECOND&lt;/unit&gt;
 *          &lt;value&gt;-120&lt;/value&gt;
 *      &lt;/beginOffset&gt;
 *      &lt;endPhase&gt;ttp&lt;/endPhase&gt;
 *      &lt;endOffset&gt;
 *          &lt;unit&gt;HOUR&lt;/unit&gt;
 *          &lt;value&gt;1&lt;/value&gt;
 *      &lt;/endOffset&gt;
 *  &lt;/phaseRequest&gt;
 *     &lt;/eventStationChoice&gt;
 *    &lt;/choiceRequest&gt;
 *</pre>
 */



public class ChoiceRequest implements RequestGenerator{

    /**
     * Creates a new <code>ChoiceRequest</code> instance.
     *
     * @param config an <code>Element</code> value
     */
    public ChoiceRequest (Element config) throws ConfigurationException{

        NodeList childNodes = config.getChildNodes();
        Node node;
        for(int counter = 0; counter < childNodes.getLength(); counter++) {
            node = childNodes.item(counter);
            if(node instanceof Element) {
                Element element = (Element)node;
                if (element.getTagName().equals("eventChannelChoice")) {
                    choices.add(new EventChannelChoice(element));
                } else if (element.getTagName().equals("eventStationChoice")) {
                    choices.add(new EventStationChoice(element));
                } else if (element.getTagName().equals("otherwise")) {
                    otherwise =
                        (RequestGenerator)SodUtil.load((Element)node,
                                                       "waveFormArm");
                } // end of else
            }
        }
    }

    /**
     * Describe <code>generateRequest</code> method here.
     *
     * @param event an <code>EventAccessOperations</code> value
     * @param network a <code>NetworkAccess</code> value
     * @param channel a <code>Channel</code> value
     * @param cookies a <code>CookieJar</code> value
     * @return a <code>RequestFilter[]</code> value
     */
    public RequestFilter[] generateRequest(EventAccessOperations event,
                                           NetworkAccess network,
                                           Channel channel,
                                           CookieJar cookies) throws Exception{
        Iterator it = choices.iterator();
        while (it.hasNext()) {
            Choice c = (Choice)it.next();
            if (c.accept(event, network, channel, cookies)) {
                return c.generateRequest(event, network, channel, cookies);
            } // end of if (c.accept(event, network, channel, cookies))
        } // end of while (it.hasNext())
        if (otherwise != null) {
            return otherwise.generateRequest(event,
                                             network,
                                             channel,
                                             cookies);
        } else {
            return new RequestFilter[0];
        } // end of else
    }

    protected List choices = new LinkedList();

    protected RequestGenerator otherwise = null;

    abstract class Choice implements RequestGenerator, EventChannelSubsetter {
        RequestGenerator requestGenerator;
    }

    class EventChannelChoice extends Choice {
        EventChannelChoice(Element config)  throws ConfigurationException {
            NodeList childNodes = config.getChildNodes();
            Node node;
            for(int counter = 0; counter < childNodes.getLength(); counter++) {
                node = childNodes.item(counter);
                if(node instanceof Element) {
                    SodElement sodElement =
                        (SodElement)SodUtil.load((Element)node, "waveformArm");
                    if (sodElement instanceof RequestGenerator) {
                        requestGenerator = (RequestGenerator)sodElement;
                    } else if (sodElement instanceof EventChannelSubsetter) {
                        eventChannelSubsetter =
                            (EventChannelSubsetter)sodElement;
                    }
                } // end of else
            }
        }

        public RequestFilter[] generateRequest(EventAccessOperations event,
                                               NetworkAccess network,
                                               Channel channel,
                                               CookieJar cookies)
            throws Exception  {
            return requestGenerator.generateRequest(event,
                                                    network,
                                                    channel,
                                                    cookies);
        }

        public boolean accept(EventAccessOperations event,
                              NetworkAccess network,
                              Channel channel,
                              CookieJar cookies) throws Exception {
            return eventChannelSubsetter.accept(event,
                                                network,
                                                channel,
                                                cookies);
        }

        EventChannelSubsetter eventChannelSubsetter;
    }

    class EventStationChoice extends Choice {
        EventStationChoice(Element config) throws ConfigurationException {
            NodeList childNodes = config.getChildNodes();
            Node node;
            for(int counter = 0; counter < childNodes.getLength(); counter++) {
                node = childNodes.item(counter);
                if(node instanceof Element) {
                    SodElement sodElement =
                        (SodElement)SodUtil.load((Element)node, "waveformArm");
                    if (sodElement instanceof RequestGenerator) {
                        requestGenerator = (RequestGenerator)sodElement;
                    } else if (sodElement instanceof EventStationSubsetter) {
                        eventStationSubsetter =
                            (EventStationSubsetter)sodElement;
                    }
                } // end of else
            }
        }

        public RequestFilter[] generateRequest(EventAccessOperations event,
                                               NetworkAccess network,
                                               Channel channel,
                                               CookieJar cookies)
            throws Exception  {
            return requestGenerator.generateRequest(event,
                                                    network,
                                                    channel,
                                                    cookies);
        }

        public boolean accept(EventAccessOperations event,
                              NetworkAccess network,
                              Channel channel,
                              CookieJar cookies) throws Exception {
            return eventStationSubsetter.accept(event,
                                                network,
                                                channel.my_site.my_station,
                                                cookies);
        }

        EventStationSubsetter eventStationSubsetter;
    }

}// PhaseRequest
