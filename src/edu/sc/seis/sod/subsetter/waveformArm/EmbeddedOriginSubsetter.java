package edu.sc.seis.sod.subsetter.waveFormArm;

import edu.sc.seis.sod.*;

import edu.iris.Fissures.IfEvent.*;
import edu.iris.Fissures.event.*;

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

public class EmbeddedOriginSubsetter implements Subsetter{
    public EmbeddedOriginSubsetter (Element config){
	
	NodeList childNodes = config.getChildNodes();
	if(childNodes.getLength() > 0) {
	    try {
		originSubsetter = (OriginSubsetter) SodUtil.load((Element)childNodes.item(0),
								 "edu.sc.seis.sod.subsetter.eventArm");
	    } catch(ConfigurationException ce) {

		System.out.println("Caught configuration Exception in EmbeddedOriginSubsetter");
	    }
	}
	
    }
    
    public boolean accept(Origin origin, CookieJar cookies) {

	return originSubsetter.accept(origin, cookies);

    }
 
  
    private OriginSubsetter originSubsetter = null;
    
}// EmbeddedOriginSubsetter
