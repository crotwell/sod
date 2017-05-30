package edu.sc.seis.sod.source.network;

import java.util.List;

import edu.sc.seis.fissuresUtil.cache.CacheNetworkAccess;
import edu.sc.seis.sod.model.station.ChannelImpl;
import edu.sc.seis.sod.model.station.NetworkAttrImpl;
import edu.sc.seis.sod.model.station.StationImpl;
import edu.sc.seis.sod.source.SodSourceException;
import edu.sc.seis.sod.velocity.network.VelocityChannel;
import edu.sc.seis.sod.velocity.network.VelocityStation;


public class VelocityNetworkSource extends WrappingNetworkSource implements NetworkSource {

    public VelocityNetworkSource(NetworkSource network) {
        super(network);
    }

    @Override
    public List<? extends ChannelImpl> getChannels(StationImpl station) throws SodSourceException {
        return VelocityChannel.wrap(getWrapped().getChannels(station));
    }

    @Override
    public CacheNetworkAccess getNetwork(NetworkAttrImpl attr) {
        return getWrapped().getNetwork(attr);
    }

    @Override
    public List<? extends NetworkAttrImpl> getNetworks() throws SodSourceException {
        // TODO: this is not really what we want as it is not a Velocity
        return getWrapped().getNetworks();
    }

    @Override
    public List<? extends StationImpl> getStations(NetworkAttrImpl net) throws SodSourceException {
        return VelocityStation.wrapList(getWrapped().getStations(net));
    }
}
