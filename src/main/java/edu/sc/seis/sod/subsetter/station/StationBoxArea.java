package edu.sc.seis.sod.subsetter.station;

import org.w3c.dom.Element;

import edu.iris.Fissures.BoxArea;
import edu.iris.Fissures.network.StationImpl;
import edu.sc.seis.fissuresUtil.bag.AreaUtil;
import edu.sc.seis.sod.ConfigurationException;
import edu.sc.seis.sod.SodUtil;
import edu.sc.seis.sod.source.network.NetworkSource;
import edu.sc.seis.sod.status.StringTree;
import edu.sc.seis.sod.status.StringTreeLeaf;

public class StationBoxArea implements StationSubsetter {

    public StationBoxArea(Element el) throws ConfigurationException {
        this.ba = SodUtil.loadBoxArea(el);
    }

    public StringTree accept(StationImpl station, NetworkSource network) {
        return new StringTreeLeaf(this,
                                  AreaUtil.inArea(ba, station.getLocation()));
    }

    private BoxArea ba;
}
