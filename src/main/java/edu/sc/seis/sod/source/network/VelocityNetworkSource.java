package edu.sc.seis.sod.source.network;

import java.util.List;

import edu.sc.seis.seisFile.fdsnws.stationxml.Channel;
import edu.sc.seis.seisFile.fdsnws.stationxml.Network;
import edu.sc.seis.seisFile.fdsnws.stationxml.Station;
import edu.sc.seis.sod.source.SodSourceException;
import edu.sc.seis.sod.velocity.network.VelocityChannel;
import edu.sc.seis.sod.velocity.network.VelocityStation;


public class VelocityNetworkSource extends WrappingNetworkSource implements NetworkSource {

    public VelocityNetworkSource(NetworkSource network) {
        super(network);
    }

    @Override
    public List<? extends Channel> getChannels(Station station) throws SodSourceException {
        return VelocityChannel.wrap(getWrapped().getChannels(station));
    }

    @Override
    public List<? extends Network> getNetworks() throws SodSourceException {
        // TODO: this is not really what we want as it is not a Velocity
        return getWrapped().getNetworks();
    }

    @Override
    public List<? extends Station> getStations(Network net) throws SodSourceException {
        return VelocityStation.wrapList(getWrapped().getStations(net));
    }
}
