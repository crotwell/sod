package edu.sc.seis.sod.source.network;

import java.util.List;

import edu.sc.seis.seisFile.fdsnws.stationxml.Network;
import edu.sc.seis.seisFile.fdsnws.stationxml.Station;
import edu.sc.seis.sod.source.SodSourceException;
import edu.sc.seis.sod.velocity.network.VelocityChannel;
import edu.sc.seis.sod.velocity.network.VelocityStation;


public class VelocityNetworkSource  {

    public VelocityNetworkSource(NetworkSource network) {
        this.networkSource = network;
    }

    public List<VelocityChannel> getChannels(Station station) throws SodSourceException {
        return VelocityChannel.wrap(getWrapped().getChannels(station));
    }

    public List<? extends Network> getNetworks() throws SodSourceException {
        // TODO: this is not really what we want as it is not a Velocity
        return getWrapped().getNetworks();
    }

    public List<VelocityStation> getStations(Network net) throws SodSourceException {
        return VelocityStation.wrapList(getWrapped().getStations(net));
    }
    
    public NetworkSource getWrapped() {
        return networkSource;
    }
    
    NetworkSource networkSource;
}
