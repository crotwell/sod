package edu.sc.seis.sod.subsetter.channel;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import edu.iris.Fissures.IfNetwork.ChannelNotFound;
import edu.iris.Fissures.IfNetwork.Instrumentation;
import edu.iris.Fissures.IfNetwork.Response;
import edu.iris.Fissures.IfNetwork.Stage;
import edu.iris.Fissures.model.QuantityImpl;
import edu.iris.Fissures.network.ChannelIdUtil;
import edu.iris.Fissures.network.ChannelImpl;
import edu.sc.seis.fissuresUtil.cache.InstrumentationLoader;
import edu.sc.seis.fissuresUtil.sac.InvalidResponse;
import edu.sc.seis.sod.source.network.NetworkSource;
import edu.sc.seis.sod.status.Fail;
import edu.sc.seis.sod.status.Pass;
import edu.sc.seis.sod.status.StringTree;
import edu.sc.seis.sod.status.StringTreeLeaf;

public class RepairSensitivity implements ChannelSubsetter {

    public StringTree accept(ChannelImpl channel, NetworkSource network)
            throws Exception {
        Instrumentation instrumentation;
        try {
            QuantityImpl sensitivity = network.getSensitivity(channel.getId());
            if(InstrumentationLoader.isValidSensitivity(sensitivity)) {
                return new Pass(this);
            }
            // try via instrumentation
            instrumentation = network.getInstrumentation(channel.get_id());
        } catch(ChannelNotFound e) {
            return new Fail(this, "No instrumentation");
        } catch (InvalidResponse e) {
            return new Fail(this, "Invalid instrumentation: "+ e.getMessage());
        }
        if(InstrumentationLoader.isValid(instrumentation)) {
            return new Pass(this);
        }
        Response resp = instrumentation.the_response;
        Stage[] stages = resp.stages;
        if(stages.length == 0) {
            return new StringTreeLeaf(this, false, "No stages in the response of "
                                      + ChannelIdUtil.toString(channel.get_id()));
        }
        InstrumentationLoader.repairResponse(instrumentation.the_response);
        return new StringTreeLeaf(this, InstrumentationLoader.isValid(instrumentation.the_response));
    }

    private Logger logger = LoggerFactory.getLogger(RepairSensitivity.class);
}
