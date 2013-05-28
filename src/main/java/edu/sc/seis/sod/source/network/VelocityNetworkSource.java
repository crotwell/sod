package edu.sc.seis.sod.source.network;

import java.util.List;

import edu.iris.Fissures.IfNetwork.NetworkId;
import edu.iris.Fissures.network.ChannelImpl;
import edu.iris.Fissures.network.NetworkAttrImpl;
import edu.iris.Fissures.network.StationImpl;
import edu.sc.seis.fissuresUtil.cache.CacheNetworkAccess;
import edu.sc.seis.sod.velocity.network.VelocityChannel;
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
    public CacheNetworkAccess getNetwork(NetworkAttrImpl attr) {
        return getWrapped().getNetwork(attr);
    }

    @Override
    public List<? extends NetworkAttrImpl> getNetworks() {
        // TODO: this is not really what we want as it is not a Velocity
        return getWrapped().getNetworks();
    }

    @Override
    public List<? extends StationImpl> getStations(NetworkAttrImpl net) {
        return VelocityStation.wrapList(getWrapped().getStations(net));
    }
}
