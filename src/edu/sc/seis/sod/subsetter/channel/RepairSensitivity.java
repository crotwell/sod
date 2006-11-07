package edu.sc.seis.sod.subsetter.channel;

import org.apache.log4j.Logger;
import edu.iris.Fissures.IfNetwork.Channel;
import edu.iris.Fissures.IfNetwork.ChannelNotFound;
import edu.iris.Fissures.IfNetwork.Instrumentation;
import edu.iris.Fissures.IfNetwork.Response;
import edu.iris.Fissures.IfNetwork.Stage;
import edu.iris.Fissures.network.ChannelIdUtil;
import edu.sc.seis.fissuresUtil.bag.ResponseGain;
import edu.sc.seis.fissuresUtil.cache.InstrumentationInvalid;
import edu.sc.seis.fissuresUtil.cache.InstrumentationLoader;
import edu.sc.seis.fissuresUtil.cache.ProxyNetworkAccess;
import edu.sc.seis.sod.status.Fail;
import edu.sc.seis.sod.status.Pass;
import edu.sc.seis.sod.status.StringTree;
import edu.sc.seis.sod.status.StringTreeLeaf;

public class RepairSensitivity implements ChannelSubsetter {

    public StringTree accept(Channel channel, ProxyNetworkAccess network)
            throws Exception {
        Instrumentation instrumentation;
        try {
            instrumentation = network.retrieve_instrumentation(channel.get_id(),
                                                               channel.get_id().begin_time);
        } catch(ChannelNotFound e) {
            return new Fail(this, "No instrumentation");
        } catch (InstrumentationInvalid e) {
            return new Fail(this, "Invalid instrumentation");
        }
        if(InstrumentationLoader.isValid(instrumentation)) {
            return new Pass(this);
        }
        Response resp = instrumentation.the_response;
        Stage[] stages = resp.stages;
        if(stages.length == 0) {
            logger.debug("No stages in the response of "
                    + ChannelIdUtil.toString(channel.get_id()));
            return new StringTreeLeaf(this, false);
        }
        InstrumentationLoader.repairResponse(instrumentation.the_response);
        return new StringTreeLeaf(this, InstrumentationLoader.isValid(instrumentation.the_response));
    }

    private Logger logger = Logger.getLogger(RepairSensitivity.class);
}
