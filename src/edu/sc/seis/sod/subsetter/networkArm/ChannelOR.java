package edu.sc.seis.sod.subsetter.networkArm;

import edu.sc.seis.sod.*;
import java.util.*;
import org.w3c.dom.*;
import edu.iris.Fissures.IfNetwork.*;
import edu.iris.Fissures.network.*;
import edu.iris.Fissures.*;


/**
 *
 *channelOR contains a sequence of channelSubsetters. The minimum value of the sequence is 0 and
 * the max value of the sequence is unLimited.
 *
 * sample xml file
 *<body><pre><bold>
 *&lt;channelOR&gt;
 *        &lt;sampling&gt;
 *               &lt;min&gt;1&lt;/min&gt;
 *               &lt;max&gt;40&lt;/max&gt;
 *               &lt;interval&gt;
 *                      &lt;unit&gt;SECOND&lt;/unit&gt;
 *                      &lt;value&lt;1&lt;/value&gt;
 *              &lt;/interval&gt;
 *        &lt;/sampling&gt;
 *        &lt;sampling&gt;
 *               &lt;min&gt;20&lt;/min&gt;
 *               &lt;max&gt;60&lt;/max&gt;
 *               &lt;interval&gt;
 *                      &lt;unit&gt;MINUTE&lt;/unit&gt;
 *                      &lt;value&gt;2&lt;/value&gt;
 *              &lt;/interval&gt;
 *        &lt;/sampling&gt;
 * &lt;/channelOR&gt;
 * </bold></pre></body>
 *
 */

public class ChannelOR 
    extends  NetworkLogicalSubsetter 
    implements ChannelSubsetter {
    
    public ChannelOR (Element config) throws ConfigurationException {
	super(config);
    }

    public boolean accept(NetworkAccess network,Channel e,  CookieJar cookies) throws Exception{
	Iterator it = filterList.iterator();
	if (it.hasNext()) {
	    ChannelSubsetter filter = (ChannelSubsetter)it.next();
	    if ( filter.accept(network, e, cookies)) {
		return false;
	    }
	}
	return false;
    }

}// ChannelOR
