package edu.sc.seis.sod.subsetter.networkArm;

import edu.sc.seis.sod.*;
import edu.sc.seis.sod.subsetter.*;

import edu.iris.Fissures.IfNetwork.*;
import edu.iris.Fissures.network.*;
import edu.iris.Fissures.model.*;
import edu.iris.Fissures.*;

import org.w3c.dom.*;

/**
 * sample xml file<br>
 * <body>
 * <pre>
 * <bold>
 *  &lt;sampling&gt;
 *      &lt;min&gt;1&lt;/min&gt;
 *      &lt;max&gt;40&lt;/max&gt;
 *      &lt;interval&gt;
 *          &lt;unit&gt;SECOND&lt;/unit&gt;
 *          &lt;value&gt;1&lt;/value&gt;
 *      &lt;/interval&gt;
 *  &lt;/sampling&gt;
 * </bold></pre></body>
 * @author <a href="mailto:">Srinivasa Telukutla</a>
 * @version 1.0
 */
public class Sampling extends RangeSubsetter implements ChannelSubsetter {

    public Sampling(Element config) throws ConfigurationException{
        super(config);
        NodeList children  = config.getChildNodes();
        for(int i = 0; i < children.getLength(); i ++) {
            Node node = children.item(i);
            if(node instanceof Element) {
                String tagName = ((Element)node).getTagName();
                if(tagName.equals("interval"))  {
                    interval = (Interval)SodUtil.load((Element)node, "");
                }
            }
        }
    }

    public boolean accept(Channel channel) throws Exception{
        SamplingImpl channelSampling = (SamplingImpl)channel.sampling_info;
        SamplingImpl minSampling = new SamplingImpl((int)getMinValue(), interval.getTimeInterval());
        SamplingImpl maxSampling = new SamplingImpl((int)getMaxValue(), interval.getTimeInterval());
        if(channelSampling.getFrequency().greaterThanEqual(minSampling.getFrequency()) &&
           channelSampling.getFrequency().lessThanEqual(maxSampling.getFrequency())) {
            return true;
        } else { return false; }
    }

    Interval interval = null;
}//Sampling
