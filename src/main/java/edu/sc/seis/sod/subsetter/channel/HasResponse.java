package edu.sc.seis.sod.subsetter.channel;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import edu.sc.seis.sod.hibernate.ChannelNotFound;
import edu.sc.seis.sod.model.station.ChannelImpl;
import edu.sc.seis.seisFile.fdsnws.stationxml.InvalidResponse;
import edu.sc.seis.sod.source.SodSourceException;
import edu.sc.seis.sod.source.network.NetworkSource;
import edu.sc.seis.sod.status.Fail;
import edu.sc.seis.sod.status.Pass;
import edu.sc.seis.sod.status.StringTree;

public class HasResponse implements ChannelSubsetter {

    public StringTree accept(ChannelImpl channel, NetworkSource network) {
        try {
            if (network.getResponse(channel) == null) {
                return new Fail(this, "No response");
            }
            return new Pass(this);
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
