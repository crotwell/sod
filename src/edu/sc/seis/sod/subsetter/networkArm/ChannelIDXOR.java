package edu.sc.seis.sod.subsetter.networkArm;

import edu.sc.seis.sod.*;
import java.util.*;
import org.w3c.dom.*;
import edu.iris.Fissures.IfNetwork.*;
import edu.iris.Fissures.network.*;
import edu.iris.Fissures.*;

/**
 *
 *channelIDXOR contains a sequence of channelSubsetters. The minimum value of the sequence is 2 and
 * the max value of the sequence is 2.
 *
 * sample xml file
 *<body><pre><bold>
 *&lt;channelIDXOR&gt;
 *      &lt;broadband/&gt;
 *      &lt;longPeriod/&gt;
 * &lt;/channelIDXOR&gt;
 * </bold></pre></body>
 *
 */

public class ChannelIDXOR 
    extends  NetworkLogicalSubsetter 
    implements ChannelIdSubsetter {
    
    public ChannelIDXOR (Element config) throws ConfigurationException {
	super(config);
    }

    public boolean accept(ChannelId e,  CookieJar cookies) throws Exception{
	Iterator it = filterList.iterator();
	if (it.hasNext()) {
	    ChannelIdSubsetter filter = (ChannelIdSubsetter)it.next();
	    if ( filter.accept(e, cookies)) {
		return false;
	    }
	}
	return false;
    }

}// ChannelIDXOR
