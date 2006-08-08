package edu.sc.seis.sod.subsetter.channel;

import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import edu.iris.Fissures.IfNetwork.Channel;
import edu.iris.Fissures.model.SamplingImpl;
import edu.iris.Fissures.model.TimeInterval;
import edu.iris.Fissures.model.UnitImpl;
import edu.sc.seis.fissuresUtil.cache.ProxyNetworkAccess;
import edu.sc.seis.sod.ConfigurationException;
import edu.sc.seis.sod.SodUtil;
import edu.sc.seis.sod.subsetter.RangeSubsetter;

public class Sampling extends RangeSubsetter implements ChannelSubsetter {

    public Sampling(Element config) throws ConfigurationException {
        super(config);
        TimeInterval interval = 
            SodUtil.loadTimeInterval(SodUtil.getElement(config, "timeInterval"));
        minSampling = new SamplingImpl((int)getMinValue(),
                                       interval);
        min = minSampling.getFrequency().getValue(UnitImpl.HERTZ);
        maxSampling = new SamplingImpl((int)getMaxValue(),
                                       interval);
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

    private SamplingImpl maxSampling;

    private SamplingImpl minSampling;
}//Sampling
