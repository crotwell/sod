package edu.sc.seis.sod.subsetter.channel;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import edu.iris.Fissures.IfNetwork.ChannelNotFound;
import edu.iris.Fissures.network.ChannelImpl;
import edu.sc.seis.fissuresUtil.sac.InvalidResponse;
import edu.sc.seis.sod.source.network.NetworkSource;
import edu.sc.seis.sod.status.Fail;
import edu.sc.seis.sod.status.Pass;
import edu.sc.seis.sod.status.StringTree;

public class HasResponse implements ChannelSubsetter {

    public StringTree accept(ChannelImpl channel, NetworkSource network) {
        try {
            network.getInstrumentation(channel);
            return new Pass(this);
        } catch(ChannelNotFound e) {
            return new Fail(this, "No instrumentation");
        } catch (InvalidResponse e) {
            return new Fail(this, "Invalid instrumentation: "+e.getMessage());
        }
    }

    private Logger logger = LoggerFactory.getLogger(HasResponse.class);
}
