package edu.sc.seis.sod.subsetter.waveformArm;

import edu.iris.Fissures.IfEvent.EventAccessOperations;
import edu.iris.Fissures.IfNetwork.Station;
import edu.sc.seis.sod.ConfigurationException;
import edu.sc.seis.sod.SodUtil;
import edu.sc.seis.sod.subsetter.eventArm.OriginSubsetter;
import edu.sc.seis.sod.subsetter.waveformArm.EventStationSubsetter;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

/**
 * specifies the embeddedOriginSubsetter
 *<pre>
 * &lt;embeddedOriginSubsetter&gt;
 *     &lt;magnitudeRange&gt;
 *           &lt;description&gt;describes magnitude&lt;/description&gt;
 *           &lt;magType&gt;mb&lt;/magType&gt;
 *           &lt;min&gt;5.5&lt;/min&gt;
 *     &lt;/magnitudeRange&gt;
 * &lt;/embeddedOriginSubsetter&gt;
 *</pre>
 */


public class EmbeddedOriginSubsetter implements EventStationSubsetter{
    public EmbeddedOriginSubsetter (Element config) throws ConfigurationException{
        NodeList childNodes = config.getChildNodes();
        for(int counter = 0; counter < childNodes.getLength(); counter++) {
            Node  node = childNodes.item(counter);
            if(node instanceof Element) {
                originSubsetter = (OriginSubsetter) SodUtil.load((Element)node,
                                                                 "eventArm");
                break;
            }
        }

    }

    public boolean accept(EventAccessOperations eventAccess, Station station) throws Exception{
        return originSubsetter.accept(eventAccess, eventAccess.get_preferred_origin());
    }

    private OriginSubsetter originSubsetter = null;
}// EmbeddedOriginSubsetter
