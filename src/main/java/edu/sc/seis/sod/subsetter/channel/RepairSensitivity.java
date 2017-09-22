package edu.sc.seis.sod.subsetter.channel;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import edu.sc.seis.sod.hibernate.ChannelNotFound;
import edu.sc.seis.sod.model.common.QuantityImpl;
import edu.sc.seis.sod.model.station.ChannelIdUtil;
import edu.sc.seis.seisFile.fdsnws.stationxml.Channel;
import edu.sc.seis.seisFile.fdsnws.stationxml.InstrumentSensitivity;
import edu.sc.seis.seisFile.fdsnws.stationxml.InvalidResponse;
import edu.sc.seis.seisFile.fdsnws.stationxml.Response;
import edu.sc.seis.sod.source.network.NetworkSource;
import edu.sc.seis.sod.status.Fail;
import edu.sc.seis.sod.status.Pass;
import edu.sc.seis.sod.status.StringTree;
import edu.sc.seis.sod.status.StringTreeLeaf;

public class RepairSensitivity implements ChannelSubsetter {

    public StringTree accept(Channel channel, NetworkSource network)
            throws Exception {
        try {
            InstrumentSensitivity sensitivity = channel.getResponse() == null ? null : channel.getResponse().getInstrumentSensitivity();
            if(sensitivity != null && sensitivity.getSensitivityValue() != -1) {
                return new Pass(this);
            }
            // try via instrumentation
            Response response = network.getResponse(channel);
            if(Response.isValid(response)) {
                return new Pass(this);
            }
            if(response.getResponseStageList().size() == 0) {
                return new StringTreeLeaf(this, false, "No stages in the response of "
                        + ChannelIdUtil.toString(channel));
            }
            Response.repairResponse(response);
            return new StringTreeLeaf(this, Response.isValid(response));
        } catch(ChannelNotFound e) {
            return new Fail(this, "No instrumentation");
        } catch (InvalidResponse e) {
            return new Fail(this, "Invalid instrumentation: "+ e.getMessage());
        }
    }

    private Logger logger = LoggerFactory.getLogger(RepairSensitivity.class);
}
