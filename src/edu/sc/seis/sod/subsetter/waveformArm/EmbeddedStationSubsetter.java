package edu.sc.seis.sod.subsetter.waveFormArm;

import edu.sc.seis.sod.*;

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
    /**
     * Creates a new <code>EmbeddedStationSubsetter</code> instance.
     *
     * @param config an <code>Element</code> value
     */
    public EmbeddedStationSubsetter (Element config) throws ConfigurationException{
	
	NodeList childNodes = config.getChildNodes();
	Node node;
	for(int counter = 0; counter < childNodes.getLength(); counter++) {
		node = childNodes.item(counter);
		if(node instanceof Element) {
		    stationSubsetter = (StationSubsetter) SodUtil.load((Element)node,
								       "edu.sc.seis.sod.subsetter.networkArm");
		    break;
		}
	}
	
    }
    
    /**
     * Describe <code>accept</code> method here.
     *
     * @param eventAccess an <code>EventAccessOperations</code> value
     * @param network a <code>NetworkAccess</code> value
     * @param station a <code>Station</code> value
     * @param cookies a <code>CookieJar</code> value
     * @return a <code>boolean</code> value
     */
    public boolean accept(EventAccessOperations eventAccess, NetworkAccess network, Station station, CookieJar cookies) throws Exception{

	return stationSubsetter.accept(network, station, cookies);
	

    }
 
  
    private StationSubsetter stationSubsetter = null;
    
}// EmbeddedStationSubsetter
