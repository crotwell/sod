package edu.sc.seis.sod.mock;

import java.time.Duration;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import edu.sc.seis.seisFile.fdsnws.stationxml.Channel;
import edu.sc.seis.seisFile.fdsnws.stationxml.InvalidResponse;
import edu.sc.seis.seisFile.fdsnws.stationxml.Network;
import edu.sc.seis.seisFile.fdsnws.stationxml.Response;
import edu.sc.seis.seisFile.fdsnws.stationxml.Station;
import edu.sc.seis.sod.hibernate.ChannelNotFound;
import edu.sc.seis.sod.mock.station.MockNetworkAccess;
import edu.sc.seis.sod.model.common.QuantityImpl;
import edu.sc.seis.sod.model.station.NetworkIdUtil;
import edu.sc.seis.sod.model.station.StationId;
import edu.sc.seis.sod.source.SodSourceException;
import edu.sc.seis.sod.source.network.AbstractNetworkSource;
import edu.sc.seis.sod.source.network.NetworkQueryConstraints;
import edu.sc.seis.sod.source.network.NetworkSource;


public class MockNetworkSource extends AbstractNetworkSource implements NetworkSource {

    Duration refresh;
    
    public MockNetworkSource(Duration refresh) {
        super("MockNetworkSource", 0);
        this.refresh = refresh;
    }
    
    public MockNetworkSource() {
        this(Duration.ofDays(3));
    }

    @Override
    public String getName() {
        return "MockNetworkSource";
    }

    @Override
    public Duration getRefreshInterval() {
        return refresh;
    }

    @Override
    public List<? extends Network> getNetworks() {
        ArrayList<Network> out = new ArrayList<Network>();
        for (int i = 0; i < nets.length; i++) {
                out.add( (Network)nets[i].get_attributes());
        }
        return out;
    }

    @Override
    public List<? extends Station> getStations(Network net) {
        for (int i = 0; i < nets.length; i++) {
            if (NetworkIdUtil.areEqual(nets[i].get_attributes().getNetworkId(), net.getNetworkId())) {
                return Arrays.asList((Station[])nets[i].retrieve_stations());
            }
        }
        return null;
    }

    @Override
    public List<? extends Channel> getChannels(Station station) {
        for (int i = 0; i < nets.length; i++) {
            if (NetworkIdUtil.areEqual(nets[i].get_attributes().getNetworkId(), station.getNetwork().getNetworkId())) {
                return Arrays.asList((Channel[])nets[i].retrieve_for_station(StationId.of(station)));
            }
        }
        return null;
    }
    
    @Override
    public void setConstraints(NetworkQueryConstraints constraints) {
        //no op
    }

    MockNetworkAccess[] nets = new MockNetworkAccess[] { MockNetworkAccess.createNetworkAccess(),
                                                 MockNetworkAccess.createOtherNetworkAccess(),
                                                 MockNetworkAccess.createManySplendoredNetworkAccess() };

    @Override
    public Response getResponse(Channel chanId) throws ChannelNotFound, InvalidResponse, SodSourceException {
        throw new ChannelNotFound("Not implemented", chanId);
    }
    
}
