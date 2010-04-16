package edu.sc.seis.sod.source.network;

import java.util.ArrayList;
import java.util.List;

import edu.iris.Fissures.IfNetwork.ChannelId;
import edu.iris.Fissures.IfNetwork.ChannelNotFound;
import edu.iris.Fissures.IfNetwork.Instrumentation;
import edu.iris.Fissures.IfNetwork.NetworkId;
import edu.iris.Fissures.IfNetwork.NetworkNotFound;
import edu.iris.Fissures.IfNetwork.Sensitivity;
import edu.iris.Fissures.network.ChannelImpl;
import edu.iris.Fissures.network.NetworkAttrImpl;
import edu.iris.Fissures.network.NetworkIdUtil;
import edu.iris.Fissures.network.StationIdUtil;
import edu.iris.Fissures.network.StationImpl;
import edu.sc.seis.fissuresUtil.cache.CacheNetworkAccess;
import edu.sc.seis.fissuresUtil.cache.InstrumentationInvalid;


public class LoadedNetworkSource extends NetworkSource {

    public LoadedNetworkSource(NetworkSource wrapped, StationImpl sta) {
        super(wrapped.getDNS(), wrapped.getName(), wrapped.getRetries());
        this.wrapped = wrapped;
        this.sta = sta;
        this.chans = wrapped.getChannels(sta);
        this.allStations = wrapped.getStations(sta.getId().network_id);
    }

    @Override
    public List<ChannelImpl> getChannels(StationImpl station) {
        if (StationIdUtil.areEqual(station, sta)) {
            ArrayList<ChannelImpl> out = new ArrayList<ChannelImpl>();
            out.addAll(chans);
            return out;
        }
        return wrapped.getChannels(station);
    }

    @Override
    public Instrumentation getInstrumentation(ChannelId chanId) throws ChannelNotFound, InstrumentationInvalid {
        return wrapped.getInstrumentation(chanId);
    }

    @Override
    public CacheNetworkAccess getNetwork(NetworkAttrImpl attr) {
        return wrapped.getNetwork(attr);
    }

    @Override
    public List<CacheNetworkAccess> getNetworkByName(String name) throws NetworkNotFound {
        return wrapped.getNetworkByName(name);
    }

    @Override
    public List<CacheNetworkAccess> getNetworks() {
        return wrapped.getNetworks();
    }

    @Override
    public Sensitivity getSensitivity(ChannelId chanId) throws ChannelNotFound, InstrumentationInvalid {
        return wrapped.getSensitivity(chanId);
    }

    @Override
    public List<StationImpl> getStations(NetworkId net) {
        if (NetworkIdUtil.areEqual(net, sta.getNetworkAttr().getId())) {
            return allStations;
        }
        return wrapped.getStations(net);
    }
    
    NetworkSource wrapped;
    StationImpl sta;
    List<StationImpl> allStations;
    List<ChannelImpl> chans;
}
