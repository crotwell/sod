package edu.sc.seis.sod.subsetter.station;

import edu.iris.Fissures.IfNetwork.Station;
import edu.sc.seis.sod.subsetter.network.NetworkSubsetter;

/**
 * @author groves Created on Mar 6, 2005
 */
public class NetworkSubsetterWrapper implements StationSubsetter {

    public NetworkSubsetterWrapper(NetworkSubsetter ns) {
        this.ns = ns;
    }

    public boolean accept(Station station) throws Exception {
        return ns.accept(station.my_network);
    }

    private NetworkSubsetter ns;
}