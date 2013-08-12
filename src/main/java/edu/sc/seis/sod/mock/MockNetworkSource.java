package edu.sc.seis.sod.mock;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import edu.iris.Fissures.IfNetwork.ChannelNotFound;
import edu.iris.Fissures.IfNetwork.Instrumentation;
import edu.iris.Fissures.IfNetwork.NetworkAccess;
import edu.iris.Fissures.IfNetwork.NetworkId;
import edu.iris.Fissures.IfNetwork.NetworkNotFound;
import edu.iris.Fissures.model.QuantityImpl;
import edu.iris.Fissures.model.TimeInterval;
import edu.iris.Fissures.model.UnitImpl;
import edu.iris.Fissures.network.ChannelImpl;
import edu.iris.Fissures.network.NetworkAttrImpl;
import edu.iris.Fissures.network.NetworkIdUtil;
import edu.iris.Fissures.network.StationImpl;
import edu.sc.seis.fissuresUtil.cache.CacheNetworkAccess;
import edu.sc.seis.fissuresUtil.mockFissures.IfNetwork.MockNetworkAccess;
import edu.sc.seis.fissuresUtil.sac.InvalidResponse;
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
    public CacheNetworkAccess getNetwork(NetworkAttrImpl attr) {
        for (int i = 0; i < nets.length; i++) {
            if (NetworkIdUtil.areEqual(nets[i].get_attributes().getId(), attr.getId())) {
                return new CacheNetworkAccess(nets[i]);
            }
        }
        return null;
    }

    @Override
    public List<? extends CacheNetworkAccess> getNetworkByName(String name) throws NetworkNotFound {
        ArrayList<CacheNetworkAccess> out = new ArrayList<CacheNetworkAccess>();
        for (int i = 0; i < nets.length; i++) {
            if (nets[i].get_attributes().getName().equals( name)) {
                out.add( new CacheNetworkAccess(nets[i]));
            }
        }
        return out;
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
    public Instrumentation getInstrumentation(ChannelImpl chanId) throws ChannelNotFound, InvalidResponse {
        // TODO Auto-generated method stub
        return null;
    }
    
    @Override
    public void setConstraints(NetworkQueryConstraints constraints) {
        //no op
    }

    NetworkAccess[] nets = new NetworkAccess[] { MockNetworkAccess.createNetworkAccess(),
                                                 MockNetworkAccess.createOtherNetworkAccess(),
                                                 MockNetworkAccess.createManySplendoredNetworkAccess() };
    
}
