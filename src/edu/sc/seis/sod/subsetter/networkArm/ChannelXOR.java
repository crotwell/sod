package edu.sc.seis.sod.subsetter.networkArm;

import edu.sc.seis.sod.*;
import java.util.*;
import org.w3c.dom.*;
import edu.iris.Fissures.IfNetwork.*;
import edu.iris.Fissures.network.*;
import edu.iris.Fissures.*;

/**
 * channelXOR contains a sequence of channelSubsetters. The minimum value of the sequence is 2 and
 * the max value of the sequence is 2.
 *
 * sample xml file
 *<body><pre><bold>
 *&lt;channelXOR&gt;
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
 * &lt;/channelXOR&gt;
 * </bold></pre></body>
 *
 * @author <a href="mailto:">Srinivasa Telukutla</a>
 * @version 1.0
 */
public final class ChannelXOR
    extends  NetworkLogicalSubsetter
    implements ChannelSubsetter {

    /**
     * Creates a new <code>ChannelXOR</code> instance.
     *
     * @param config an <code>Element</code> value
     * @exception ConfigurationException if an error occurs
     */
    public ChannelXOR (Element config) throws ConfigurationException {
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
    public boolean accept(NetworkAccess network, Channel channel,  CookieJar cookies) throws Exception{
        ChannelSubsetter filterA = (ChannelSubsetter)filterList.get(0);
        ChannelSubsetter filterB = (ChannelSubsetter)filterList.get(1);
        return ( filterA.accept(network, channel, cookies) != filterB.accept(network, channel, cookies));

    }

}// ChannelXOR
