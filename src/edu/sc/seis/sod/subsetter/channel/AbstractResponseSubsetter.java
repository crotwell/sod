package edu.sc.seis.sod.subsetter.channel;

import org.w3c.dom.Element;

import edu.iris.Fissures.IfNetwork.Channel;
import edu.iris.Fissures.IfNetwork.ChannelNotFound;
import edu.iris.Fissures.IfNetwork.Instrumentation;
import edu.iris.Fissures.IfNetwork.Response;
import edu.sc.seis.fissuresUtil.cache.InstrumentationInvalid;
import edu.sc.seis.fissuresUtil.cache.InstrumentationLoader;
import edu.sc.seis.fissuresUtil.cache.ProxyNetworkAccess;
import edu.sc.seis.sod.status.Fail;
import edu.sc.seis.sod.status.StringTree;

public abstract class AbstractResponseSubsetter implements ChannelSubsetter {

	public AbstractResponseSubsetter(Element config) {
	}

	public StringTree accept(Channel channel, ProxyNetworkAccess network)
			throws Exception {
		Instrumentation instrumentation;
		try {
			instrumentation = network.retrieve_instrumentation(
					channel.get_id(), channel.get_id().begin_time);
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
