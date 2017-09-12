package edu.sc.seis.sod.source.network;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;

import edu.sc.seis.seisFile.fdsnws.stationxml.Channel;
import edu.sc.seis.seisFile.fdsnws.stationxml.InvalidResponse;
import edu.sc.seis.seisFile.fdsnws.stationxml.Network;
import edu.sc.seis.seisFile.fdsnws.stationxml.Response;
import edu.sc.seis.seisFile.fdsnws.stationxml.Station;
import edu.sc.seis.sod.hibernate.ChannelNotFound;
import edu.sc.seis.sod.model.common.QuantityImpl;
import edu.sc.seis.sod.model.station.ChannelId;
import edu.sc.seis.sod.model.station.ChannelIdUtil;
import edu.sc.seis.sod.model.station.NetworkIdUtil;
import edu.sc.seis.sod.model.station.StationIdUtil;
import edu.sc.seis.sod.source.SodSourceException;


public class LoadedNetworkSource extends WrappingNetworkSource implements NetworkSource {

    public LoadedNetworkSource(NetworkSource wrapped, List<? extends Station> allStations, Station sta) {
        super(wrapped);
        this.sta = sta;
        this.allStations = allStations;
    }

    @Override
    public List<? extends Channel> getChannels(Station station) throws SodSourceException {
        if (StationIdUtil.areEqual(station, sta)) {
            if (chans == null) {
                this.chans = getWrapped().getChannels(sta);
            }
            ArrayList<Channel> out = new ArrayList<Channel>();
            out.addAll(chans);
            return out;
        }
        return getWrapped().getChannels(station);
    }

    @Override
    public Response getResponse(Channel chan) throws ChannelNotFound, InvalidResponse, SodSourceException {
        responseLoaded.add(ChannelIdUtil.toString(chan));
        return getWrapped().getResponse(chan);
    }

    @Override
    public List<? extends Station> getStations(Network net) throws SodSourceException {
        if (NetworkIdUtil.areEqual(net.getId(), sta.getNetwork().getId())) {
            return allStations;
        }
        return getWrapped().getStations(net);
    }
    
    public boolean isResponseLoaded(ChannelId chan) {
        return responseLoaded.contains(ChannelIdUtil.toString(chan));
    }
    
    Station sta;
    List<? extends Station> allStations;
    List<? extends Channel> chans = null;
    HashSet<String> responseLoaded = new HashSet<String>();
}
