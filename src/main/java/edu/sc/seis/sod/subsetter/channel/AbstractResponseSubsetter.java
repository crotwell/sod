package edu.sc.seis.sod.subsetter.channel;

import edu.iris.Fissures.IfNetwork.ChannelNotFound;
import edu.iris.Fissures.IfNetwork.Instrumentation;
import edu.iris.Fissures.IfNetwork.Response;
import edu.iris.Fissures.network.ChannelImpl;
import edu.sc.seis.fissuresUtil.cache.InstrumentationInvalid;
import edu.sc.seis.fissuresUtil.cache.InstrumentationLoader;
import edu.sc.seis.fissuresUtil.cache.ProxyNetworkAccess;
import edu.sc.seis.sod.source.network.NetworkSource;
import edu.sc.seis.sod.status.Fail;
import edu.sc.seis.sod.status.StringTree;

public abstract class AbstractResponseSubsetter implements ChannelSubsetter {

	public StringTree accept(ChannelImpl channel, NetworkSource network)
			throws Exception {
		Instrumentation instrumentation;
		try {
			instrumentation = network.getInstrumentation(channel.get_id());
		} catch (ChannelNotFound e) {
			return new Fail(this, "No instrumentation");
		} catch (InstrumentationInvalid e) {
			return new Fail(this, "Invalid instrumentation");
		}
		if (!InstrumentationLoader.isValid(instrumentation)) {
			return new Fail(this, "Invalid instrumentation");
		}
		return accept(instrumentation.the_response);
	}

	protected abstract StringTree accept(Response response);

}
