package edu.sc.seis.sod.source.network;

import java.time.Duration;
import java.util.List;

import edu.sc.seis.seisFile.fdsnws.stationxml.Response;
import edu.sc.seis.seisFile.fdsnws.stationxml.Station;
import edu.sc.seis.sod.hibernate.ChannelNotFound;
import edu.sc.seis.sod.model.common.QuantityImpl;
import edu.sc.seis.sod.model.common.TimeInterval;
import edu.sc.seis.sod.model.station.Instrumentation;
import edu.sc.seis.seisFile.fdsnws.stationxml.Channel;
import edu.sc.seis.seisFile.fdsnws.stationxml.InvalidResponse;
import edu.sc.seis.seisFile.fdsnws.stationxml.Network;
import edu.sc.seis.sod.source.SodSourceException;

public abstract class WrappingNetworkSource extends AbstractNetworkSource implements NetworkSource {

    public WrappingNetworkSource(NetworkSource wrapped) {
        super(wrapped);
        this.wrapped = wrapped;
    }

    private NetworkSource wrapped;

    public NetworkSource getWrapped() {
        return wrapped;
    }

    @Override
    public List<? extends Channel> getChannels(Station station) throws SodSourceException {
        return getWrapped().getChannels(station);
    }

    @Override
    public List<? extends Network> getNetworks() throws SodSourceException {
        return getWrapped().getNetworks();
    }

    @Override
    public List<? extends Station> getStations(Network net) throws SodSourceException {
        return getWrapped().getStations(net);
    }

    @Override
    public QuantityImpl getSensitivity(Channel chanId) throws ChannelNotFound, InvalidResponse, SodSourceException {
        return getWrapped().getSensitivity(chanId);
    }

    @Override
    public Response getResponse(Channel chanId) throws ChannelNotFound, InvalidResponse, SodSourceException {
        return getWrapped().getResponse(chanId);
    }

    @Override
    public Duration getRefreshInterval() {
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
