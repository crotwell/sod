package edu.sc.seis.sod.subsetter.station;

import org.w3c.dom.Element;

import edu.sc.seis.seisFile.fdsnws.stationxml.Station;
import edu.sc.seis.sod.ConfigurationException;
import edu.sc.seis.sod.SodUtil;
import edu.sc.seis.sod.bag.AreaUtil;
import edu.sc.seis.sod.model.common.BoxAreaImpl;
import edu.sc.seis.sod.source.network.NetworkSource;
import edu.sc.seis.sod.status.StringTree;
import edu.sc.seis.sod.status.StringTreeLeaf;

public class StationBoxArea implements StationSubsetter {

    public StationBoxArea(Element el) throws ConfigurationException {
        this.ba = SodUtil.loadBoxArea(el);
    }

    public StringTree accept(Station station, NetworkSource network) {
        return new StringTreeLeaf(this,
                                  AreaUtil.inArea(ba, station.getLocation()));
    }

    private BoxAreaImpl ba;
}
