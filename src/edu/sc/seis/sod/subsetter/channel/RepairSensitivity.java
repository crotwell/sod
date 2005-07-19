package edu.sc.seis.sod.subsetter.channel;

import org.apache.log4j.Logger;
import edu.iris.Fissures.IfNetwork.Channel;
import edu.iris.Fissures.IfNetwork.ChannelNotFound;
import edu.iris.Fissures.IfNetwork.Instrumentation;
import edu.iris.Fissures.IfNetwork.Response;
import edu.iris.Fissures.IfNetwork.Stage;
import edu.iris.Fissures.network.ChannelIdUtil;
import edu.sc.seis.fissuresUtil.bag.ResponseGain;
import edu.sc.seis.fissuresUtil.cache.InstrumentationLoader;
import edu.sc.seis.fissuresUtil.cache.ProxyNetworkAccess;

public class RepairSensitivity implements ChannelSubsetter {

    public boolean accept(Channel channel, ProxyNetworkAccess network)
            throws Exception {
        Instrumentation instrumentation;
        try {
            instrumentation = network.retrieve_instrumentation(channel.get_id(),
                                                               channel.get_id().begin_time);
        } catch(ChannelNotFound e) {
            logger.debug("No instrumentation for "
                    + ChannelIdUtil.toString(channel.get_id()));
            return false;
        }
        if(InstrumentationLoader.isValid(instrumentation)) {
            return true;
        }
        Response resp = instrumentation.the_response;
        Stage[] stages = resp.stages;
        if(stages.length == 0) {
            logger.debug("No stages in the response of "
                    + ChannelIdUtil.toString(channel.get_id()));
            return false;
        }
        InstrumentationLoader.repairResponse(instrumentation.the_response);
        return InstrumentationLoader.isValid(instrumentation.the_response);
    }

    private Logger logger = Logger.getLogger(RepairSensitivity.class);
}
