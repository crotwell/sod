package edu.sc.seis.sod.subsetter.channel;

import edu.sc.seis.sod.hibernate.ChannelNotFound;
import edu.sc.seis.sod.model.station.ChannelImpl;
import edu.sc.seis.sod.model.station.Instrumentation;
import edu.sc.seis.sod.model.station.InvalidResponse;
import edu.sc.seis.sod.model.station.Response;
import edu.sc.seis.sod.source.network.NetworkSource;
import edu.sc.seis.sod.status.Fail;
import edu.sc.seis.sod.status.StringTree;

public abstract class AbstractResponseSubsetter implements ChannelSubsetter {

	public StringTree accept(ChannelImpl channel, NetworkSource network)
			throws Exception {
		Instrumentation instrumentation;
		try {
			instrumentation = network.getInstrumentation(channel);
			Instrumentation.checkResponse(instrumentation.the_response);
		} catch (ChannelNotFound e) {
			return new Fail(this, "No instrumentation");
		} catch (InvalidResponse e) {
			return new Fail(this, "Invalid response: "+e.getMessage());
		}
		return accept(instrumentation.the_response);
	}

	protected abstract StringTree accept(Response response);

}
