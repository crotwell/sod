package edu.sc.seis.sod.mock;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import edu.sc.seis.seisFile.fdsnws.stationxml.Response;
import edu.sc.seis.sod.hibernate.ChannelNotFound;
import edu.sc.seis.sod.mock.station.MockNetworkAccess;
import edu.sc.seis.sod.model.common.QuantityImpl;
import edu.sc.seis.sod.model.common.TimeInterval;
import edu.sc.seis.sod.model.common.UnitImpl;
import edu.sc.seis.sod.model.station.ChannelImpl;
import edu.sc.seis.seisFile.fdsnws.stationxml.InvalidResponse;
import edu.sc.seis.sod.model.station.NetworkAttrImpl;
import edu.sc.seis.sod.model.station.NetworkIdUtil;
import edu.sc.seis.sod.model.station.StationImpl;
import edu.sc.seis.sod.source.SodSourceException;
import edu.sc.seis.sod.source.network.AbstractNetworkSource;
import edu.sc.seis.sod.source.network.NetworkQueryConstraints;
import edu.sc.seis.sod.source.network.NetworkSource;


public class MockNetworkSource extends AbstractNetworkSource implements NetworkSource {

    TimeInterval refresh;
    
    public MockNetworkSource(TimeInterval refresh) {
        super("MockNetworkSource", 0);
        this.refresh = refresh;
    }
    
    public MockNetworkSource() {
        this(new TimeInterval(3, UnitImpl.DAY));
    }

    @Override
    public String getName() {
        return "MockNetworkSource";
    }

    @Override
    public TimeInterval getRefreshInterval() {
        return refresh;
    }

    @Override
    public List<? extends NetworkAttrImpl> getNetworks() {
        ArrayList<NetworkAttrImpl> out = new ArrayList<NetworkAttrImpl>();
        for (int i = 0; i < nets.length; i++) {
                out.add( (NetworkAttrImpl)nets[i].get_attributes());
        }
        return out;
    }

    @Override
    public List<? extends StationImpl> getStations(NetworkAttrImpl net) {
        for (int i = 0; i < nets.length; i++) {
            if (NetworkIdUtil.areEqual(nets[i].get_attributes().getId(), net.getId())) {
                return Arrays.asList((StationImpl[])nets[i].retrieve_stations());
            }
        }
        return null;
    }

    @Override
    public List<? extends ChannelImpl> getChannels(StationImpl station) {
        for (int i = 0; i < nets.length; i++) {
            if (NetworkIdUtil.areEqual(nets[i].get_attributes().getId(), station.getNetworkAttrImpl().getId())) {
                return Arrays.asList((ChannelImpl[])nets[i].retrieve_for_station(station.getId()));
            }
        }
        return null;
    }

    @Override
    public QuantityImpl getSensitivity(ChannelImpl chanId) throws ChannelNotFound, InvalidResponse {
        // TODO Auto-generated method stub
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
    public Response getResponse(ChannelImpl chanId) throws ChannelNotFound, InvalidResponse, SodSourceException {
        throw new ChannelNotFound();
    }
    
}
