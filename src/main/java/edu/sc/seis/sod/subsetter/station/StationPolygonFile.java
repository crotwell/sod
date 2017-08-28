package edu.sc.seis.sod.subsetter.station;

import org.w3c.dom.Element;

import edu.sc.seis.seisFile.fdsnws.stationxml.Station;
import edu.sc.seis.sod.ConfigurationException;
import edu.sc.seis.sod.DOMHelper;
import edu.sc.seis.sod.bag.AreaUtil;
import edu.sc.seis.sod.model.common.Location;
import edu.sc.seis.sod.source.network.NetworkSource;
import edu.sc.seis.sod.status.StringTree;
import edu.sc.seis.sod.status.StringTreeLeaf;
import edu.sc.seis.sod.subsetter.AreaSubsetter;

public class StationPolygonFile implements StationSubsetter {

    public StationPolygonFile(Element el) throws ConfigurationException {
        locs = AreaSubsetter.extractPolygon(DOMHelper.extractText(el, "."));
    }

    public StringTree accept(Station station, NetworkSource network) {
        return new StringTreeLeaf(this, AreaUtil.inArea(locs, Location.of(station)));
    }

    private Location[] locs;
}
