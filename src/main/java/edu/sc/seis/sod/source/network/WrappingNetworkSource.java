package edu.sc.seis.sod.source.network;

import java.util.List;

import edu.iris.Fissures.IfNetwork.ChannelNotFound;
import edu.iris.Fissures.IfNetwork.Instrumentation;
import edu.iris.Fissures.IfNetwork.NetworkNotFound;
import edu.iris.Fissures.model.QuantityImpl;
import edu.iris.Fissures.model.TimeInterval;
import edu.iris.Fissures.network.ChannelImpl;
import edu.iris.Fissures.network.NetworkAttrImpl;
import edu.iris.Fissures.network.StationImpl;
import edu.sc.seis.fissuresUtil.cache.CacheNetworkAccess;
import edu.sc.seis.fissuresUtil.sac.InvalidResponse;
import edu.sc.seis.sod.source.SodSourceException;

public abstract class WrappingNetworkSource extends AbstractNetworkSource implements NetworkSource {

    public WrappingNetworkSource(AbstractNetworkSource wrapped) {
        super(wrapped);
        this.wrapped = wrapped;
    }

    private NetworkSource wrapped;

    public NetworkSource getWrapped() {
        return wrapped;
    }

    @Override
    public List<? extends ChannelImpl> getChannels(StationImpl station) throws SodSourceException {
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
    public List<? extends NetworkAttrImpl> getNetworks() throws SodSourceException {
        return getWrapped().getNetworks();
    }

    @Override
    public List<? extends StationImpl> getStations(NetworkAttrImpl net) throws SodSourceException {
        return getWrapped().getStations(net);
    }

    @Override
    public QuantityImpl getSensitivity(ChannelImpl chanId) throws ChannelNotFound, InvalidResponse, SodSourceException {
        return getWrapped().getSensitivity(chanId);
    }

    @Override
    public Instrumentation getInstrumentation(ChannelImpl chanId) throws ChannelNotFound, InvalidResponse, SodSourceException {
        return getWrapped().getInstrumentation(chanId);
    }

    @Override
    public TimeInterval getRefreshInterval() {
        return getWrapped().getRefreshInterval();
    }

    @Override
    public String getName() {
        return getWrapped().getName();
    }

    @Override
    public void setConstraints(NetworkQueryConstraints constraints) {
        getWrapped().setConstraints(constraints);
    }
}
