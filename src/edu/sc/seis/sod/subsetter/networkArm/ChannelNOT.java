package edu.sc.seis.sod.subsetter.networkArm;

import edu.sc.seis.sod.*;
import java.util.*;
import org.w3c.dom.*;
import edu.iris.Fissures.IfNetwork.*;
import edu.iris.Fissures.network.*;
import edu.iris.Fissures.*;

/**
 * ChannelNOT.java
 *
 *
 * Created: Thu Mar 14 14:02:33 2002
 *
 * @author <a href="mailto:">Philip Crotwell</a>
 * @version
 */

public class ChannelNOT 
    extends  NetworkLogicalSubsetter 
    implements ChannelSubsetter {
    
    public ChannelNOT (Element config) throws ConfigurationException {
	super(config);
    }

    public boolean accept(Channel e,  CookieJar cookies) {
	Iterator it = filterList.iterator();
	if (it.hasNext()) {
	    ChannelSubsetter filter = (ChannelSubsetter)it.next();
	    if ( filter.accept(e, cookies)) {
		return false;
	    }
	}
	return false;
    }

}// ChannelNOT
