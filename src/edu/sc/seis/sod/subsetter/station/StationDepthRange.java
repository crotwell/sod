package edu.sc.seis.sod.subsetter.station;

import org.w3c.dom.Element;
import edu.iris.Fissures.IfNetwork.Station;
import edu.iris.Fissures.model.QuantityImpl;

public class StationDepthRange extends edu.sc.seis.sod.subsetter.DepthRange
        implements StationSubsetter {

    public StationDepthRange(Element config) throws Exception {
        super(config);
    }

    public boolean accept(Station station) {
        QuantityImpl actualDepth = (QuantityImpl)station.my_location.depth;
        if(actualDepth.greaterThanEqual(getMinDepth())
                && actualDepth.lessThanEqual(getMaxDepth())) {
            return true;
        } else return false;
    }
}// StationDepthRange
