package edu.sc.seis.sod.subsetter.channel;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import edu.sc.seis.sod.hibernate.ChannelNotFound;
import edu.sc.seis.sod.model.common.QuantityImpl;
import edu.sc.seis.sod.model.station.ChannelIdUtil;
import edu.sc.seis.sod.model.station.ChannelImpl;
import edu.sc.seis.sod.model.station.Instrumentation;
import edu.sc.seis.seisFile.fdsnws.stationxml.InvalidResponse;
import edu.sc.seis.sod.model.station.Response;
import edu.sc.seis.sod.model.station.Stage;
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
            QuantityImpl sensitivity = network.getSensitivity(channel);
            if(Instrumentation.isValidSensitivity(sensitivity)) {
                return new Pass(this);
            }
            // try via instrumentation
            instrumentation = network.getInstrumentation(channel);
        } catch(ChannelNotFound e) {
            return new Fail(this, "No instrumentation");
        } catch (InvalidResponse e) {
            return new Fail(this, "Invalid instrumentation: "+ e.getMessage());
        }
        if(Instrumentation.isValid(instrumentation)) {
            return new Pass(this);
        }
        Response resp = instrumentation.the_response;
        Stage[] stages = resp.stages;
        if(stages.length == 0) {
            return new StringTreeLeaf(this, false, "No stages in the response of "
                                      + ChannelIdUtil.toString(channel.get_id()));
        }
        Instrumentation.repairResponse(instrumentation.the_response);
        return new StringTreeLeaf(this, Instrumentation.isValid(instrumentation.the_response));
    }

    private Logger logger = LoggerFactory.getLogger(RepairSensitivity.class);
}
