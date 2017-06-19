package edu.sc.seis.sod.source.network;

import java.util.List;

import edu.sc.seis.seisFile.fdsnws.stationxml.Response;
import edu.sc.seis.sod.hibernate.ChannelNotFound;
import edu.sc.seis.sod.model.common.QuantityImpl;
import edu.sc.seis.sod.model.common.TimeInterval;
import edu.sc.seis.sod.model.station.ChannelImpl;
import edu.sc.seis.sod.model.station.Instrumentation;
import edu.sc.seis.seisFile.fdsnws.stationxml.InvalidResponse;
import edu.sc.seis.sod.model.station.NetworkAttrImpl;
import edu.sc.seis.sod.model.station.StationImpl;
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
    public List<? extends ChannelImpl> getChannels(StationImpl station) throws SodSourceException {
        return getWrapped().getChannels(station);
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
    public Response getResponse(ChannelImpl chanId) throws ChannelNotFound, InvalidResponse, SodSourceException {
        return getWrapped().getResponse(chanId);
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
