package edu.sc.seis.sod.subsetter.station;

import edu.iris.Fissures.network.StationImpl;
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

    public StringTree accept(StationImpl station, NetworkSource network) throws Exception {
        return ns.accept(station.getNetworkAttrImpl());
    }

    private NetworkSubsetter ns;
}