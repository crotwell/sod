package edu.sc.seis.sod.subsetter.waveformArm;

import edu.sc.seis.sod.*;
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
 * OriginOffsetRequest.java
 *
 *
 * Created: Wed Apr  2 16:06:39 2003
 *
 * @author <a href="mailto:crotwell@owl.seis.sc.edu">Philip Crotwell</a>
 * @version 1.0
 */
public class OriginOffsetRequest  implements RequestGenerator {
    public OriginOffsetRequest (Element config) throws ConfigurationException{
        NodeList childNodes = config.getChildNodes();
        for(int counter = 0; counter < childNodes.getLength(); counter++) {
            Node node = childNodes.item(counter);
            if(node instanceof Element) {
                Element element = (Element)node;
                if(element.getTagName().equals("beginOffset")) {
                    SodElement sodElement = (SodElement) SodUtil.load(element,
                                                                      waveformArmPackage);
                    beginOffset = (BeginOffset)sodElement;
                } else if(element.getTagName().equals("endOffset")) {
                    SodElement sodElement =
                        (SodElement) SodUtil.load(element,
                                                  waveformArmPackage);
                    endOffset = (EndOffset)sodElement;
                }
            }
        }
    }

    public RequestFilter[] generateRequest(EventAccessOperations event,
                                           Channel channel, CookieJar cookieJar) throws Exception{
        Origin origin = null;
        origin = event.get_preferred_origin();

        edu.iris.Fissures.Time originTime = origin.origin_time;
        MicroSecondDate originDate = new MicroSecondDate(originTime);
        TimeInterval bInterval = beginOffset.getTimeInterval();

        TimeInterval eInterval = endOffset.getTimeInterval();

        MicroSecondDate bDate = originDate.add(bInterval);
        MicroSecondDate eDate = originDate.add(eInterval);
        RequestFilter[] filters = {new RequestFilter(channel.get_id(),
                                                     bDate.getFissuresTime(),
                                                     eDate.getFissuresTime())};
        return filters;
    }

    private BeginOffset beginOffset;

    private String beginPhase;

    private EndOffset endOffset;
} // OriginOffsetRequest
