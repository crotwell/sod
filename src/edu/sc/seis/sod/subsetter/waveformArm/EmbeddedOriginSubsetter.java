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
 *     &lt;magnitudeRange&gt;
 *           &lt;description&gt;describes magnitude&lt;/description&gt;
 *           &lt;magType&gt;mb&lt;/magType&gt;
 *           &lt;min&gt;5.5&lt;/min&gt;
 *     &lt;/magnitudeRange&gt;
 * &lt;/embeddedOriginSubsetter&gt;
 *</pre>
 */


public class EmbeddedOriginSubsetter implements EventStationSubsetter{
    /**
     * Creates a new <code>EmbeddedOriginSubsetter</code> instance.
     *
     * @param config an <code>Element</code> value
     */
    public EmbeddedOriginSubsetter (Element config) throws ConfigurationException{
	
	NodeList childNodes = config.getChildNodes();
	Node node;
	for(int counter = 0; counter < childNodes.getLength(); counter++) {
		node = childNodes.item(counter);
		if(node instanceof Element) {
		    originSubsetter = (OriginSubsetter) SodUtil.load((Element)node,
								 "edu.sc.seis.sod.subsetter.eventArm");
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

	return originSubsetter.accept(eventAccess, eventAccess.get_preferred_origin(), cookies);
	
    }
 
  
    private OriginSubsetter originSubsetter = null;
    
}// EmbeddedOriginSubsetter
