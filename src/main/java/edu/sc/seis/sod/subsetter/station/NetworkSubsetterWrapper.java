package edu.sc.seis.sod.subsetter.station;

import edu.sc.seis.seisFile.fdsnws.stationxml.Station;
import edu.sc.seis.sod.source.network.NetworkSource;
import edu.sc.seis.sod.status.StringTree;
import edu.sc.seis.sod.subsetter.network.NetworkSubsetter;

/**
 * @author groves Created on Mar 6, 2005
 */
public class NetworkSubsetterWrapper implements StationSubsetter {

    public NetworkSubsetterWrapper(NetworkSubsetter ns) {
        this.ns = ns;
    }

    public StringTree accept(Station station, NetworkSource network) throws Exception {
        return ns.accept(station.getNetwork());
    }

    private NetworkSubsetter ns;
}