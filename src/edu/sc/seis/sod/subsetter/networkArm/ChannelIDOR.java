package edu.sc.seis.sod.subsetter.networkArm;

import edu.sc.seis.sod.*;
import java.util.*;
import org.w3c.dom.*;
import edu.iris.Fissures.IfNetwork.*;
import edu.iris.Fissures.network.*;
import edu.iris.Fissures.*;

/**
 *
 *channelIDOR contains a sequence of channelSubsetters. The minimum value of the sequence is 0 and
 * the max value of the sequence is unLimited.
 *
 * sample xml file
 *<body><pre><bold>
 *&lt;channelIDOR&gt;
 *      &lt;broadband/&gt;
 *      &lt;longPeriod/&gt;
 * &lt;/channelIDOR&gt;
 * </bold></pre></body>
 *
 */

public class ChannelIDOR 
    extends NetworkLogicalSubsetter 
    implements ChannelIdSubsetter {
    
    public ChannelIDOR (Element config) throws ConfigurationException {
	super(config);
    }

    public boolean accept(ChannelId e,  CookieJar cookies) throws Exception{
	Iterator it = filterList.iterator();
	if (it.hasNext()) {
	    ChannelIdSubsetter filter = (ChannelIdSubsetter)it.next();
	    if (!filter.accept(e, cookies)) {
		return false;
	    }
	}
	return true;
    }

}// ChannelIDOR
