package edu.sc.seis.sod.subsetter.station;

import org.w3c.dom.Element;

import edu.sc.seis.sod.model.common.QuantityImpl;
import edu.sc.seis.sod.model.station.StationImpl;
import edu.sc.seis.sod.source.network.NetworkSource;
import edu.sc.seis.sod.status.Fail;
import edu.sc.seis.sod.status.Pass;
import edu.sc.seis.sod.status.StringTree;

public class StationDepthRange extends edu.sc.seis.sod.subsetter.DepthRange
        implements StationSubsetter {

    public StationDepthRange(Element config) throws Exception {
        super(config);
    }

    public StringTree accept(StationImpl station, NetworkSource network) {
        QuantityImpl actualDepth = (QuantityImpl)station.getLocation().depth;
        if(actualDepth.greaterThanEqual(getMinDepth())
                && actualDepth.lessThanEqual(getMaxDepth())) {
            return new Pass(this);
        } else return new Fail(this);
    }
}// StationDepthRange
