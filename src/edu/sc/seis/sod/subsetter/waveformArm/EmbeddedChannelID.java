package edu.sc.seis.sod.subsetter.waveform;

import edu.sc.seis.sod.*;
import java.util.*;
import org.w3c.dom.*;
import edu.iris.Fissures.IfNetwork.*;
import edu.iris.Fissures.network.*;
import edu.iris.Fissures.IfEvent.*;
import edu.iris.Fissures.network.*;
import edu.iris.Fissures.*;

/**
 * EmbeddedEventStation.java
 *
 *
 * Created: Wed Oct 30 11:54:58 2002
 *
 * @author <a href="mailto:">Philip Crotwell</a>
 * @version
 */

public class EmbeddedChannelID  implements EventChannelSubsetter{
    public EmbeddedChannelID(Element config) throws ConfigurationException{
	
	NodeList childNodes = config.getChildNodes();
	Node node;
	for(int counter = 0; counter < childNodes.getLength(); counter++) {
	    node = childNodes.item(counter);
	    if(node instanceof Element) {
		channelIdSubsetter = 
		    (ChannelIdSubsetter) SodUtil.load((Element)node,
						    networkArmPackage);
		break;
	    }
	}
    }

    /**
     * Describe <code>accept</code> method here.
     *
     * @param o an <code>EventAccessOperations</code> value
     * @param network a <code>NetworkAccess</code> value
     * @param channel a <code>Channel</code> value
     * @param cookies a <code>CookieJar</code> value
     * @return a <code>boolean</code> value
     * @exception Exception if an error occurs
     */
    public boolean accept(EventAccessOperations o,
			  NetworkAccess network,
			  Channel channel,  
			  CookieJar cookies) 
	throws Exception
    {
	return channelIdSubsetter.accept(channel.get_id(), 
					 cookies);
    }

    ChannelIdSubsetter channelIdSubsetter;

}// EmbeddedChannelId
