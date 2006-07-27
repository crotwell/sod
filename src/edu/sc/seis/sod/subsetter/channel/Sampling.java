package edu.sc.seis.sod.subsetter.channel;

import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import edu.iris.Fissures.IfNetwork.Channel;
import edu.iris.Fissures.model.SamplingImpl;
import edu.iris.Fissures.model.UnitImpl;
import edu.sc.seis.fissuresUtil.cache.ProxyNetworkAccess;
import edu.sc.seis.sod.ConfigurationException;
import edu.sc.seis.sod.SodUtil;
import edu.sc.seis.sod.subsetter.Interval;
import edu.sc.seis.sod.subsetter.RangeSubsetter;

public class Sampling extends RangeSubsetter implements ChannelSubsetter {

    public Sampling(Element config) throws ConfigurationException {
        super(config);
        NodeList children = config.getChildNodes();
        for(int i = 0; i < children.getLength(); i++) {
            Node node = children.item(i);
            if(node instanceof Element) {
                String tagName = ((Element)node).getTagName();
                if(tagName.equals("interval")) {
                    interval = (Interval)SodUtil.load((Element)node, "");
                }
            }
        }
        minSampling = new SamplingImpl((int)getMinValue(),
                                       interval.getTimeInterval());
        min = minSampling.getFrequency().getValue(UnitImpl.HERTZ);
        maxSampling = new SamplingImpl((int)getMaxValue(),
                                       interval.getTimeInterval());
        max = maxSampling.getFrequency().getValue(UnitImpl.HERTZ);
    }

    public boolean accept(Channel channel, ProxyNetworkAccess network) throws Exception {
        return accept((SamplingImpl)channel.sampling_info);
    }
    
    public boolean accept(SamplingImpl channelSampling) {
        double chanSampling = channelSampling.getFrequency()
                .getValue(UnitImpl.HERTZ);
        return accept(chanSampling);
    }

    Interval interval;

    private SamplingImpl maxSampling;

    private SamplingImpl minSampling;
}//Sampling
