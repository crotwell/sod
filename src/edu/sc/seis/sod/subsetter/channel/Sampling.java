package edu.sc.seis.sod.subsetter.channel;

import org.w3c.dom.Element;
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
        TimeInterval interval = SodUtil.loadTimeInterval(SodUtil.getElement(config,
                                                                            "timeInterval"));
        min = getHertz((int)getMinValue(), interval);
        max = getHertz((int)getMaxValue(), interval);
    }

    public boolean accept(Channel channel, ProxyNetworkAccess network)
            throws Exception {
        return accept((SamplingImpl)channel.sampling_info);
    }

    public boolean accept(SamplingImpl channelSampling) {
        return accept(getHertz(channelSampling));
    }

    double getHertz(int val, TimeInterval interval) {
        return getHertz(new SamplingImpl((int)getMinValue(), interval));
    }

    double getHertz(SamplingImpl sampling) {
        return sampling.getFrequency().getValue(UnitImpl.HERTZ);
    }
}
