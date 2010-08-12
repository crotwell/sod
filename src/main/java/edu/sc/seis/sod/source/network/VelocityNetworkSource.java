package edu.sc.seis.sod.source.network;

import java.util.List;

import org.w3c.dom.Element;

import edu.iris.Fissures.IfNetwork.ChannelId;
import edu.iris.Fissures.IfNetwork.ChannelNotFound;
import edu.iris.Fissures.IfNetwork.Instrumentation;
import edu.iris.Fissures.IfNetwork.NetworkId;
import edu.iris.Fissures.IfNetwork.NetworkNotFound;
import edu.iris.Fissures.IfNetwork.Sensitivity;
import edu.iris.Fissures.network.ChannelImpl;
import edu.iris.Fissures.network.NetworkAttrImpl;
import edu.iris.Fissures.network.StationImpl;
import edu.sc.seis.fissuresUtil.cache.CacheNetworkAccess;
import edu.sc.seis.fissuresUtil.cache.InstrumentationInvalid;
import edu.sc.seis.sod.velocity.network.VelocityChannel;
import edu.sc.seis.sod.velocity.network.VelocityNetwork;
import edu.sc.seis.sod.velocity.network.VelocityStation;


public class VelocityNetworkSource extends WrappingNetworkSource implements NetworkSource {

    public VelocityNetworkSource(NetworkSource wrapped) {
        super(wrapped);
    }

    @Override
    public List<? extends ChannelImpl> getChannels(StationImpl station) {
        return VelocityChannel.wrap(getWrapped().getChannels(station));
    }

    @Override
    public Instrumentation getInstrumentation(ChannelId chanId) throws ChannelNotFound, InstrumentationInvalid {
        return getWrapped().getInstrumentation(chanId);
    }

    @Override
    public CacheNetworkAccess getNetwork(NetworkAttrImpl attr) {
        return getWrapped().getNetwork(attr);
    }

    @Override
    public List<? extends CacheNetworkAccess> getNetworkByName(String name) throws NetworkNotFound {
        return getWrapped().getNetworkByName(name);
    }

    @Override
    public List<? extends CacheNetworkAccess> getNetworks() {
        // TODO: this is not really what we want as it is not a Velocity
        return getWrapped().getNetworks();
    }

    @Override
    public Sensitivity getSensitivity(ChannelId chanId) throws ChannelNotFound, InstrumentationInvalid {
        return getWrapped().getSensitivity(chanId);
    }

    @Override
    public List<? extends StationImpl> getStations(NetworkId net) {
        return VelocityStation.wrapList(getWrapped().getStations(net));
    }
}
