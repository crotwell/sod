package edu.sc.seis.sod.example;

import edu.iris.Fissures.IfNetwork.NetworkAccess;
import edu.iris.Fissures.IfNetwork.Station;
import edu.sc.seis.sod.status.StringTree;
import edu.sc.seis.sod.status.StringTreeLeaf;
import edu.sc.seis.sod.subsetter.station.StationSubsetter;

public class StationSubsetterExample implements StationSubsetter {

    public StringTree accept(Station station, NetworkAccess network)
            throws Exception {
        return new StringTreeLeaf(this, false);
    }
}
