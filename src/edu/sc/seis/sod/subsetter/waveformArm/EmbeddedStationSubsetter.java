package edu.sc.seis.sod.subsetter.waveformArm;

import edu.sc.seis.sod.*;
import edu.sc.seis.sod.subsetter.networkArm.*;

import edu.iris.Fissures.IfEvent.*;
import edu.iris.Fissures.event.*;

import edu.iris.Fissures.IfNetwork.*;
import edu.iris.Fissures.network.*;

import org.w3c.dom.*;

/**
 * specifies the embeddedOriginSubsetter
 *<pre>
 * &lt;embeddedOriginSubsetter&gt;
 *              &lt;stationArea&gt;
 *                           &lt;boxArea&gt;
 *                                    &lt;latitudeRange&gt;
 *                                                   &lt;min&gt;30&lt;/min&gt;
 *                                                   &lt;max&gt;33&lt;/max&gt;
 *                                    &lt;/latitudeRange&gt;
 *                                    &lt;longitudeRange&gt;
 *                                                   &lt;min&gt;-100&lt;/min&gt;
 *                                                   &lt;max&gt;100&lt;/max&gt;
 *                                    &lt;/longitudeRange&gt;
 *                           &lt;/boxArea&gt;
 *              &lt;/stationArea&gt;
 * &lt;/embeddedOriginSubsetter&gt;
 *</pre>
 */

public class EmbeddedStationSubsetter implements EventStationSubsetter{
    public EmbeddedStationSubsetter (Element config) throws ConfigurationException{
        NodeList childNodes = config.getChildNodes();
        for(int counter = 0; counter < childNodes.getLength(); counter++) {
            Node node = childNodes.item(counter);
            if(node instanceof Element) {
                stationSubsetter = (StationSubsetter) SodUtil.load((Element)node,
                                                                   "networkArm");
                break;
            }
        }

    }

    public boolean accept(EventAccessOperations eventAccess, Station station) throws Exception{
        return stationSubsetter.accept(station);
    }

    private StationSubsetter stationSubsetter = null;
}// EmbeddedStationSubsetter
