package edu.sc.seis.sod.subsetter.channel;

import org.apache.log4j.Logger;
import edu.iris.Fissures.IfNetwork.Channel;
import edu.iris.Fissures.IfNetwork.ChannelNotFound;
import edu.iris.Fissures.network.ChannelIdUtil;
import edu.sc.seis.fissuresUtil.cache.InstrumentationInvalid;
import edu.sc.seis.fissuresUtil.cache.ProxyNetworkAccess;
import edu.sc.seis.sod.status.Fail;
import edu.sc.seis.sod.status.Pass;
import edu.sc.seis.sod.status.StringTree;
import edu.sc.seis.sod.status.StringTreeLeaf;

public class HasResponse implements ChannelSubsetter {

    public StringTree accept(Channel channel, ProxyNetworkAccess network) {
        try {
            network.retrieve_instrumentation(channel.get_id(),
                                             channel.get_id().begin_time);
            return new Pass(this);
        } catch(ChannelNotFound e) {
            return new Fail(this, "No instrumentation");
        } catch (InstrumentationInvalid e) {
            return new Fail(this, "Invalid instrumentation");
        }
    }

    private Logger logger = Logger.getLogger(HasResponse.class);
}
