package edu.sc.seis.sod.subsetter.channel;

import org.w3c.dom.Element;

import edu.sc.seis.seisFile.fdsnws.stationxml.Channel;
import edu.sc.seis.sod.ConfigurationException;
import edu.sc.seis.sod.source.network.NetworkSource;
import edu.sc.seis.sod.source.network.VelocityNetworkSource;
import edu.sc.seis.sod.status.StringTree;
import edu.sc.seis.sod.subsetter.AbstractScriptSubsetter;
import edu.sc.seis.sod.velocity.network.VelocityChannel;


public class ChannelScript extends AbstractScriptSubsetter implements ChannelSubsetter {

    public ChannelScript(Element config) throws ConfigurationException {
        super(config);
    }

    @Override
    public StringTree accept(Channel channel, NetworkSource network) throws Exception {
        return runScript(new VelocityChannel(channel), new VelocityNetworkSource(network));
    }

    /** Run the script with the arguments as predefined variables. */
    public StringTree runScript(VelocityChannel channel, VelocityNetworkSource networkSource) throws Exception {
        engine.put("channel", channel);
        engine.put("networkSource", networkSource);
        return eval();
    }
}
