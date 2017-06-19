package edu.sc.seis.sod.subsetter.channel;

import edu.sc.seis.seisFile.fdsnws.stationxml.Response;
import edu.sc.seis.sod.hibernate.ChannelNotFound;
import edu.sc.seis.sod.model.station.ChannelImpl;
import edu.sc.seis.sod.model.station.Instrumentation;
import edu.sc.seis.seisFile.fdsnws.stationxml.InvalidResponse;
import edu.sc.seis.sod.source.network.NetworkSource;
import edu.sc.seis.sod.status.Fail;
import edu.sc.seis.sod.status.StringTree;

public abstract class AbstractResponseSubsetter implements ChannelSubsetter {

	public StringTree accept(ChannelImpl channel, NetworkSource network)
			throws Exception {
		Response response;
		try {
		    response = network.getResponse(channel);
			Response.checkResponse(response);
		} catch (ChannelNotFound e) {
			return new Fail(this, "No instrumentation");
		} catch (InvalidResponse e) {
			return new Fail(this, "Invalid response: "+e.getMessage());
		}
		return accept(response);
	}

	protected abstract StringTree accept(Response response);

}
