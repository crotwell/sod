package edu.sc.seis.sod.subsetter.waveFormArm;

import edu.sc.seis.sod.*;
import java.util.*;
import org.w3c.dom.*;
import edu.iris.Fissures.IfNetwork.*;
import edu.iris.Fissures.network.*;
import edu.iris.Fissures.IfEvent.*;
import edu.iris.Fissures.network.*;
import edu.iris.Fissures.*;


public class EventChannelAND 
    extends  WaveFormLogicalSubsetter 
    implements EventChannelSubsetter {
    
    public EventChannelAND (Element config) throws ConfigurationException {
	super(config);
    }

    public boolean accept(EventAccessOperations o, NetworkAccess  network,  Channel channel,  CookieJar cookies)
	throws Exception{
	Iterator it = filterList.iterator();
	while (it.hasNext()) {
	    EventChannelSubsetter filter = (EventChannelSubsetter)it.next();
	    if (!filter.accept(o, network, channel, cookies)) {
		return false;
	    }
	}
	return true;
    }

}// EventChannelAND
