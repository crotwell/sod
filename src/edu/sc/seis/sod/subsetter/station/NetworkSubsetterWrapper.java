package edu.sc.seis.sod.subsetter.station;

import edu.iris.Fissures.IfNetwork.NetworkAccess;
import edu.iris.Fissures.IfNetwork.Station;
import edu.sc.seis.sod.subsetter.network.NetworkSubsetter;

/**
 * @author groves Created on Mar 6, 2005
 */
public class NetworkSubsetterWrapper implements StationSubsetter {

    public NetworkSubsetterWrapper(NetworkSubsetter ns) {
        this.ns = ns;
    }

    public boolean accept(Station station, NetworkAccess network) throws Exception {
        return ns.accept(station.my_network).isSuccess();
    }

    private NetworkSubsetter ns;
}