package edu.sc.seis.sod.subsetter.networkArm;

import edu.iris.Fissures.IfNetwork.Channel;
import edu.sc.seis.sod.ConfigurationException;
import java.util.Iterator;
import org.w3c.dom.Element;


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
public final class ChannelNOT extends  NetworkLogicalSubsetter
    implements ChannelSubsetter {

    public ChannelNOT (Element config) throws ConfigurationException {
        super(config);
    }

    public boolean accept(Channel e) throws Exception{
        Iterator it = filterList.iterator();
        if (it.hasNext()) {
            ChannelSubsetter filter = (ChannelSubsetter)it.next();
            if ( filter.accept(e)) { return false; }
        }
        return true;
    }

}// ChannelNOT
