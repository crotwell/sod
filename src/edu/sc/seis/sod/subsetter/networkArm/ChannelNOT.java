package edu.sc.seis.sod.subsetter.networkArm;

import edu.sc.seis.sod.*;
import java.util.*;
import org.w3c.dom.*;
import edu.iris.Fissures.IfNetwork.*;
import edu.iris.Fissures.network.*;
import edu.iris.Fissures.*;


/**
 * channelNOT contains a sequence of channelSubsetters. The minimum value of the sequence is 1 and
 * the max value of the sequence is 1.
 *
 * sample xml file
 *<body><pre><bold>
 *&lt;channelNOT&gt;
 *        &lt;sampling&gt;
 *               &lt;min&gt;1&lt;/min&gt;
 *               &lt;max&gt;40&lt;/max&gt;
 *               &lt;interval&gt;
 *                      &lt;unit&gt;SECOND&lt;/unit&gt;
 *                      &lt;value&lt;1&lt;/value&gt;
 *              &lt;/interval&gt;
 *        &lt;/sampling&gt;
 * &lt;/channelNOT&gt;
 * </bold></pre></body>
 *
 * @author <a href="mailto:">Srinivasa Telukutla</a>
 * @version 1.0
 */
public final class ChannelNOT
    extends  NetworkLogicalSubsetter
    implements ChannelSubsetter {

    /**
     * Creates a new <code>ChannelNOT</code> instance.
     *
     * @param config an <code>Element</code> value
     * @exception ConfigurationException if an error occurs
     */
    public ChannelNOT (Element config) throws ConfigurationException {
    super(config);
    }

    /**
     * Describe <code>accept</code> method here.
     *
     * @param network a <code>NetworkAccess</code> value
     * @param e a <code>Channel</code> value
     * @param cookies a <code>CookieJar</code> value
     * @return a <code>boolean</code> value
     * @exception Exception if an error occurs
     */
    public boolean accept(NetworkAccess network,Channel e,  CookieJar cookies) throws Exception{
    Iterator it = filterList.iterator();
    if (it.hasNext()) {
        ChannelSubsetter filter = (ChannelSubsetter)it.next();
        if ( filter.accept(network, e, cookies)) {
        return false;
        }
    }
    return true;
    }

}// ChannelNOT
