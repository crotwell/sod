package edu.sc.seis.sod.subsetter.networkArm;

import edu.sc.seis.sod.*;
import java.util.*;
import org.w3c.dom.*;
import edu.iris.Fissures.IfNetwork.*;
import edu.iris.Fissures.network.*;
import edu.iris.Fissures.*;

/**
 *
 *channelIDNOT contains a sequence of channelSubsetters. The minimum value of the sequence is 1 and
 * the max value of the sequence is 1.
 *
 * sample xml file
 *<body><pre><bold>
 *&lt;channelIDNOT&gt;
 *      &lt;broadband/&gt;
 * &lt;/channelIDNOT&gt;
 * </bold></pre></body>
 *
 */

public class ChannelIDNOT 
    extends  NetworkLogicalSubsetter 
    implements ChannelIdSubsetter {
    
    public ChannelIDNOT (Element config) throws ConfigurationException {
	super(config);
    }

    public boolean accept(ChannelId e,  CookieJar cookies) {
	Iterator it = filterList.iterator();
	if (it.hasNext()) {
	    ChannelIdSubsetter filter = (ChannelIdSubsetter)it.next();
	    if ( filter.accept(e, cookies)) {
		return false;
	    }
	}
	return false;
    }

}// ChannelIDNOT
