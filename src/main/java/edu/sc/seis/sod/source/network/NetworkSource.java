package edu.sc.seis.sod.source.network;

import java.util.List;

import edu.iris.Fissures.IfNetwork.ChannelId;
import edu.iris.Fissures.IfNetwork.ChannelNotFound;
import edu.iris.Fissures.IfNetwork.Instrumentation;
import edu.iris.Fissures.IfNetwork.NetworkId;
import edu.iris.Fissures.IfNetwork.NetworkNotFound;
import edu.iris.Fissures.IfNetwork.Sensitivity;
import edu.iris.Fissures.model.TimeInterval;
import edu.iris.Fissures.network.ChannelImpl;
import edu.iris.Fissures.network.NetworkAttrImpl;
import edu.iris.Fissures.network.StationImpl;
import edu.sc.seis.fissuresUtil.cache.CacheNetworkAccess;
import edu.sc.seis.fissuresUtil.sac.InvalidResponse;
import edu.sc.seis.sod.source.Source;

public interface NetworkSource extends Source {

    public TimeInterval getRefreshInterval();

    public abstract CacheNetworkAccess getNetwork(NetworkAttrImpl attr);

    public abstract List<? extends CacheNetworkAccess> getNetworkByName(String name) throws NetworkNotFound;

    public abstract List<? extends NetworkAttrImpl> getNetworks();

    public abstract List<? extends StationImpl> getStations(NetworkId net);

    public abstract List<? extends ChannelImpl> getChannels(StationImpl station);

    public abstract Sensitivity getSensitivity(ChannelId chanId) throws ChannelNotFound, InvalidResponse;

    public abstract Instrumentation getInstrumentation(ChannelId chanId) throws ChannelNotFound, InvalidResponse;
}