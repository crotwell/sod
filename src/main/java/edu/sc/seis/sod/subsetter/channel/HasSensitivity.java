package edu.sc.seis.sod.subsetter.channel;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import edu.sc.seis.sod.hibernate.ChannelNotFound;
import edu.sc.seis.sod.model.common.QuantityImpl;
import edu.sc.seis.sod.model.station.Instrumentation;
import edu.sc.seis.seisFile.fdsnws.stationxml.Channel;
import edu.sc.seis.seisFile.fdsnws.stationxml.InvalidResponse;
import edu.sc.seis.sod.source.SodSourceException;
import edu.sc.seis.sod.source.network.NetworkSource;
import edu.sc.seis.sod.status.Fail;
import edu.sc.seis.sod.status.StringTree;
import edu.sc.seis.sod.status.StringTreeLeaf;

public class HasSensitivity implements ChannelSubsetter {

    public StringTree accept(Channel channel, NetworkSource network) {
        try {
            QuantityImpl sens = network.getSensitivity(channel);
            return new StringTreeLeaf(this, Instrumentation.isValidSensitivity(sens));
        } catch(ChannelNotFound e) {
            return new Fail(this, "No instrumentation");
        } catch (InvalidResponse e) {
            return new Fail(this, "Invalid instrumentation: "+e.getMessage());
        } catch(SodSourceException e) {
            return new Fail(this, "Error getting instrumentation: "+e.getMessage());
        }
    }

    private Logger logger = LoggerFactory.getLogger(HasResponse.class);
}
