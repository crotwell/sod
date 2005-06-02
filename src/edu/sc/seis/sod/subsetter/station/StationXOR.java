package edu.sc.seis.sod.subsetter.station;

import org.w3c.dom.Element;
import edu.iris.Fissures.IfNetwork.NetworkAccess;
import edu.iris.Fissures.IfNetwork.Station;
import edu.sc.seis.sod.ConfigurationException;

public final class StationXOR extends StationLogicalSubsetter implements
        StationSubsetter {

    public StationXOR(Element config) throws ConfigurationException {
        super(config);
    }

    public boolean accept(Station e, NetworkAccess network) throws Exception {
        StationSubsetter filterA = (StationSubsetter)subsetters.get(0);
        StationSubsetter filterB = (StationSubsetter)subsetters.get(1);
        return (filterA.accept(e, null) != filterB.accept(e, null));
    }
}// StationXOR
