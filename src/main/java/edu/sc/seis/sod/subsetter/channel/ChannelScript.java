package edu.sc.seis.sod.subsetter.channel;

import org.w3c.dom.Element;

import edu.iris.Fissures.network.ChannelImpl;
import edu.sc.seis.sod.source.network.NetworkSource;
import edu.sc.seis.sod.status.StringTree;
import edu.sc.seis.sod.subsetter.AbstractScriptSubsetter;


public class ChannelScript extends AbstractScriptSubsetter implements ChannelSubsetter {

    public ChannelScript(Element config) {
        super(config);
    }

    @Override
    public StringTree accept(ChannelImpl channel, NetworkSource network) throws Exception {
        engine.put("channel", channel);
        engine.put("network", network);
        return eval();
    }
}
