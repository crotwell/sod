package edu.sc.seis.sod.subsetter.station;

import org.w3c.dom.Element;
import edu.iris.Fissures.IfNetwork.NetworkAccess;
import edu.iris.Fissures.IfNetwork.Station;
import edu.sc.seis.sod.ConfigurationException;
import edu.sc.seis.sod.SodElement;
import edu.sc.seis.sod.subsetter.AreaSubsetter;

public class StationArea extends AreaSubsetter implements StationSubsetter, SodElement {

    public StationArea(Element config) throws ConfigurationException {
        super(config);
    }

    public boolean accept(Station e, NetworkAccess network) {
        return super.accept(e.my_location);
    }

}
