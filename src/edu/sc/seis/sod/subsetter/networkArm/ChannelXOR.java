package edu.sc.seis.sod.subsetter.networkArm;

import edu.iris.Fissures.IfNetwork.Channel;
import edu.sc.seis.sod.ConfigurationException;
import org.w3c.dom.Element;

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
public final class ChannelXOR extends  NetworkLogicalSubsetter
    implements ChannelSubsetter {

    public ChannelXOR (Element config) throws ConfigurationException {
        super(config);
    }

    public boolean accept(Channel channel) throws Exception{
        ChannelSubsetter filterA = (ChannelSubsetter)filterList.get(0);
        ChannelSubsetter filterB = (ChannelSubsetter)filterList.get(1);
        return ( filterA.accept( channel) != filterB.accept(channel));

    }

}// ChannelXOR
