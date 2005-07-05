package edu.sc.seis.sod.subsetter.channel;

import org.apache.log4j.Logger;
import edu.iris.Fissures.IfNetwork.Channel;
import edu.iris.Fissures.IfNetwork.ChannelNotFound;
import edu.iris.Fissures.IfNetwork.Instrumentation;
import edu.iris.Fissures.IfNetwork.Response;
import edu.iris.Fissures.IfNetwork.Stage;
import edu.iris.Fissures.network.ChannelIdUtil;
import edu.sc.seis.fissuresUtil.bag.ResponseGain;
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
        if(ResponseGain.isValid(network.retrieve_sensitivity(channel.get_id(),
                                                             channel.get_id().begin_time))) {
            return true;
        }
        Response resp = instrumentation.the_response;
        Stage[] stages = resp.stages;
        if(stages.length == 0) {
            logger.debug("No stages in the response of "
                    + ChannelIdUtil.toString(channel.get_id()));
            return false;
        }
        float sensitivity = stages[0].the_gain.gain_factor;
        for(int i = 1; i < stages.length; i++) {
            if(stages[i - 1].the_gain.frequency != stages[i].the_gain.frequency) {
                logger.debug("Different frequencies in the stages of the response of "
                        + ChannelIdUtil.toString(channel.get_id()));
                return false;
            }
            sensitivity *= stages[i].the_gain.gain_factor;
        }
        resp.the_sensitivity.sensitivity_factor = sensitivity;
        resp.the_sensitivity.frequency = stages[0].the_gain.frequency;
        return true;
    }

    private Logger logger = Logger.getLogger(RepairSensitivity.class);
}
