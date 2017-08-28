package edu.sc.seis.sod.subsetter.station;

import org.w3c.dom.Element;

import edu.sc.seis.seisFile.fdsnws.stationxml.Station;
import edu.sc.seis.sod.model.common.DistAz;
import edu.sc.seis.sod.model.common.UnitImpl;
import edu.sc.seis.sod.source.network.NetworkSource;
import edu.sc.seis.sod.status.Fail;
import edu.sc.seis.sod.status.Pass;
import edu.sc.seis.sod.status.StringTree;
import edu.sc.seis.sod.subsetter.origin.AbstractOriginPoint;

public class StationPointBackAzimuth extends AbstractOriginPoint implements
        StationSubsetter {

    public StationPointBackAzimuth(Element config) throws Exception {
        super(config);
    }

    public StringTree accept(Station station, NetworkSource network) {
        double oLat = station.getLatitude().getValue();
        double oLon = station.getLongitude().getValue();
        DistAz distaz = new DistAz(oLat, oLon, latitude, longitude);
        if(getMin().convertTo(UnitImpl.DEGREE).get_value() <= distaz.getBaz()
                && getMax().convertTo(UnitImpl.DEGREE).get_value() >= distaz.getBaz()) {
            return new Pass(this);
        } else {
            return new Fail(this, "reject back azimuth " + station + " distaz=" + distaz.getBaz());
        }
    }
}