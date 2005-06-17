package edu.sc.seis.sod.subsetter.channel;

import edu.iris.Fissures.IfNetwork.Channel;
import edu.iris.Fissures.IfNetwork.Instrumentation;
import edu.iris.Fissures.IfNetwork.NetworkAccess;
import edu.iris.Fissures.IfNetwork.Response;
import edu.iris.Fissures.IfNetwork.Stage;
import edu.sc.seis.fissuresUtil.bag.ResponseGain;

public class RepairSensitivity implements ChannelSubsetter {

    public boolean accept(Channel channel, NetworkAccess network)
            throws Exception {
        Instrumentation instrumentation = network.retrieve_instrumentation(channel.get_id(),
                                                                           channel.get_id().begin_time);
        if(instrumentation == null) {
            return false;
        }
        if(ResponseGain.isValid(instrumentation.the_response.the_sensitivity)) {
            return true;
        }
        Response resp = instrumentation.the_response;
        Stage[] stages = resp.stages;
        if(stages.length == 0) {
            return false;
        }
        float sensitivity = stages[0].the_gain.gain_factor;
        for(int i = 1; i < stages.length; i++) {
            if(stages[i - 1].the_gain.frequency != stages[i].the_gain.frequency) {
                return false;
            }
            sensitivity *= stages[i].the_gain.gain_factor;
        }
        resp.the_sensitivity.sensitivity_factor = sensitivity;
        resp.the_sensitivity.frequency = stages[0].the_gain.frequency;
        return true;
    }
}
