package edu.sc.seis.sod.source.network;

import java.util.List;

import edu.iris.Fissures.IfNetwork.NetworkId;
import edu.iris.Fissures.IfNetwork.NetworkNotFound;
import edu.iris.Fissures.model.TimeInterval;
import edu.iris.Fissures.network.ChannelImpl;
import edu.iris.Fissures.network.NetworkAttrImpl;
import edu.iris.Fissures.network.StationImpl;
import edu.sc.seis.fissuresUtil.cache.CacheNetworkAccess;

public abstract class WrappingNetworkSource implements NetworkSource {

    public WrappingNetworkSource(NetworkSource wrapped) {
        this.wrapped = wrapped;
    }

    private NetworkSource wrapped;

    public NetworkSource getWrapped() {
        return wrapped;
    }

    @Override
    public List<? extends ChannelImpl> getChannels(StationImpl station) {
        return getWrapped().getChannels(station);
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
        return getWrapped().getNetworks();
    }

    @Override
    public List<? extends StationImpl> getStations(NetworkId net) {
        return getWrapped().getStations(net);
    }

    @Override
    public TimeInterval getRefreshInterval() {
        return getWrapped().getRefreshInterval();
    }
    
    @Override
    public String getDNS() {
        return getWrapped().getDNS();
    }

    @Override
    public String getName() {
        return getWrapped().getName();
    }
}