package edu.sc.seis.sod.subsetter.channel;

import org.w3c.dom.Element;

import edu.iris.Fissures.network.ChannelImpl;
import edu.sc.seis.sod.source.network.NetworkSource;
import edu.sc.seis.sod.source.network.VelocityNetworkSource;
import edu.sc.seis.sod.status.StringTree;
import edu.sc.seis.sod.subsetter.AbstractScriptSubsetter;
import edu.sc.seis.sod.velocity.network.VelocityChannel;
import edu.sc.seis.sod.velocity.network.VelocityStation;


public class ChannelScript extends AbstractScriptSubsetter implements ChannelSubsetter {

    public ChannelScript(Element config) {
        super(config);
    }

    @Override
    public StringTree accept(ChannelImpl channel, NetworkSource network) throws Exception {
        return runScript(new VelocityChannel(channel), new VelocityNetworkSource(network));
    }

    /** Run the script with the arguments as predefined variables. */
    public StringTree runScript(VelocityChannel channel, VelocityNetworkSource network) throws Exception {
        engine.put("channel", channel);
        engine.put("networkSource", network);
        return eval();
    }
}
