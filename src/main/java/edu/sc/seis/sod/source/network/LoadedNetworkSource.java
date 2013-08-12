package edu.sc.seis.sod.source.network;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;

import edu.iris.Fissures.IfNetwork.ChannelId;
import edu.iris.Fissures.IfNetwork.ChannelNotFound;
import edu.iris.Fissures.IfNetwork.Instrumentation;
import edu.iris.Fissures.model.QuantityImpl;
import edu.iris.Fissures.network.ChannelIdUtil;
import edu.iris.Fissures.network.ChannelImpl;
import edu.iris.Fissures.network.NetworkAttrImpl;
import edu.iris.Fissures.network.NetworkIdUtil;
import edu.iris.Fissures.network.StationIdUtil;
import edu.iris.Fissures.network.StationImpl;
import edu.sc.seis.fissuresUtil.sac.InvalidResponse;
import edu.sc.seis.sod.source.SodSourceException;


public class LoadedNetworkSource extends WrappingNetworkSource implements NetworkSource {

    public LoadedNetworkSource(NetworkSource wrapped, List<? extends StationImpl> allStations, StationImpl sta) {
        super(wrapped);
        this.sta = sta;
        this.allStations = allStations;
    }

    @Override
    public List<? extends ChannelImpl> getChannels(StationImpl station) throws SodSourceException {
        if (StationIdUtil.areEqual(station, sta)) {
            if (chans == null) {
                this.chans = getWrapped().getChannels(sta);
            }
            ArrayList<ChannelImpl> out = new ArrayList<ChannelImpl>();
            out.addAll(chans);
            return out;
        }
        return getWrapped().getChannels(station);
    }

    @Override
    public Instrumentation getInstrumentation(ChannelImpl chan) throws ChannelNotFound, InvalidResponse, SodSourceException {
        instrumentationLoaded.add(ChannelIdUtil.toString(chan.getId()));
        return getWrapped().getInstrumentation(chan);
    }

    @Override
    public QuantityImpl getSensitivity(ChannelImpl chan) throws ChannelNotFound, InvalidResponse, SodSourceException {
        instrumentationLoaded.add(ChannelIdUtil.toString(chan.getId()));
        return getWrapped().getSensitivity(chan);
    }

    @Override
    public List<? extends StationImpl> getStations(NetworkAttrImpl net) throws SodSourceException {
        if (NetworkIdUtil.areEqual(net.getId(), sta.getNetworkAttr().getId())) {
            return allStations;
        }
        return getWrapped().getStations(net);
    }
    
    public boolean isInstrumentationLoaded(ChannelId chan) {
        return instrumentationLoaded.contains(ChannelIdUtil.toString(chan));
    }
    
    StationImpl sta;
    List<? extends StationImpl> allStations;
    List<? extends ChannelImpl> chans = null;
    HashSet<String> instrumentationLoaded = new HashSet<String>();
}
