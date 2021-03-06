package edu.sc.seis.sod.subsetter.station;

import org.w3c.dom.Element;

import edu.iris.Fissures.network.StationImpl;
import edu.sc.seis.sod.ConfigurationException;
import edu.sc.seis.sod.SodElement;
import edu.sc.seis.sod.source.network.NetworkSource;
import edu.sc.seis.sod.status.StringTree;
import edu.sc.seis.sod.status.StringTreeLeaf;
import edu.sc.seis.sod.subsetter.AreaSubsetter;

public class StationArea extends AreaSubsetter implements StationSubsetter, SodElement {

    public StationArea(Element config) throws ConfigurationException {
        super(config);
    }

    public StringTree accept(StationImpl e, NetworkSource network) {
        return new StringTreeLeaf(this, super.accept(e.getLocation()));
    }

}
