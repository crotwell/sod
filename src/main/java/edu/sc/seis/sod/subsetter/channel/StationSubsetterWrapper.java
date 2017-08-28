package edu.sc.seis.sod.subsetter.channel;

import edu.sc.seis.seisFile.fdsnws.stationxml.Channel;
import edu.sc.seis.seisFile.fdsnws.stationxml.Station;
import edu.sc.seis.sod.source.network.NetworkSource;
import edu.sc.seis.sod.status.StringTree;
import edu.sc.seis.sod.subsetter.station.StationSubsetter;


public class StationSubsetterWrapper implements ChannelSubsetter {

    public StationSubsetterWrapper(StationSubsetter staSub) {
        this.staSub = staSub;
    }

    public StringTree accept(Channel channel, NetworkSource network)
            throws Exception {
        return staSub.accept((Station)channel.getStation(), network);
    }
    
    StationSubsetter staSub;
}
