package edu.sc.seis.sod.subsetter.channel;

import org.w3c.dom.Element;

import edu.iris.Fissures.model.SamplingImpl;
import edu.iris.Fissures.model.TimeInterval;
import edu.iris.Fissures.model.UnitImpl;
import edu.iris.Fissures.network.ChannelImpl;
import edu.sc.seis.sod.ConfigurationException;
import edu.sc.seis.sod.SodUtil;
import edu.sc.seis.sod.source.network.NetworkSource;
import edu.sc.seis.sod.status.StringTree;
import edu.sc.seis.sod.status.StringTreeLeaf;
import edu.sc.seis.sod.subsetter.RangeSubsetter;

public class Sampling extends RangeSubsetter implements ChannelSubsetter {

    public Sampling(Element config) throws ConfigurationException {
        super(config, 0, Integer.MAX_VALUE);
        TimeInterval interval = SodUtil.loadTimeInterval(SodUtil.getElement(config,
                                                                            "interval"));
        min = getHertz((int)getMinValue(), interval);
        max = getHertz((int)getMaxValue(), interval);
    }

    public StringTree accept(ChannelImpl channel, NetworkSource network)
            throws Exception {
        return new StringTreeLeaf(this, accept((SamplingImpl)channel.getSamplingInfo()));
    }

    public boolean accept(SamplingImpl channelSampling) {
        return accept(getHertz(channelSampling));
    }

    double getHertz(int val, TimeInterval interval) {
        return getHertz(new SamplingImpl(val, interval));
    }

    double getHertz(SamplingImpl sampling) {
        return sampling.getFrequency().getValue(UnitImpl.HERTZ);
    }
}
