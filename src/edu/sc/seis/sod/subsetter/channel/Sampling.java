package edu.sc.seis.sod.subsetter.channel;

import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import edu.iris.Fissures.IfNetwork.Channel;
import edu.iris.Fissures.model.SamplingImpl;
import edu.sc.seis.sod.ConfigurationException;
import edu.sc.seis.sod.SodUtil;
import edu.sc.seis.sod.subsetter.Interval;
import edu.sc.seis.sod.subsetter.RangeSubsetter;

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
