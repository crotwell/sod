package edu.sc.seis.sod.subsetter.waveFormArm;

import edu.sc.seis.sod.*;

import edu.iris.Fissures.IfEvent.*;
import edu.iris.Fissures.event.*;

import edu.iris.Fissures.IfNetwork.*;
import edu.iris.Fissures.network.*;

import org.w3c.dom.*;

/**
 * EmbeddedOriginSubsetter.java
 *
 *
 * Created: Mon Apr  8 15:52:51 2002
 *
 * @author <a href="mailto:">Srinivasa Telukutla</a>
 * @version
 */

public class EmbeddedOriginSubsetter implements EventStationSubsetter, Subsetter{
    public EmbeddedOriginSubsetter (Element config){
	
	NodeList childNodes = config.getChildNodes();
	Node node;
	for(int counter = 0; counter < childNodes.getLength(); counter++) {
	    try {
		node = childNodes.item(counter);
		if(node instanceof Element) {
		    //  System.out.println("The tag name is "+((Element)childNodes.item(0)).getTagName());
		    originSubsetter = (OriginSubsetter) SodUtil.load((Element)node,
								 "edu.sc.seis.sod.subsetter.eventArm");
		    break;
		}
	    } catch(ConfigurationException ce) {

		System.out.println("Caught configuration Exception in EmbeddedOriginSubsetter");
	    }
	}
	
    }
    
    public boolean accept(EventAccessOperations eventAccess, NetworkAccess network, Station station, CookieJar cookies) {

	//return originSubsetter.accept(null, cookies);

    }
 
  
    private OriginSubsetter originSubsetter = null;
    
}// EmbeddedOriginSubsetter
