package edu.sc.seis.sod.subsetter.eventStation;

import org.w3c.dom.Element;

import edu.sc.seis.seisFile.fdsnws.stationxml.Station;
import edu.sc.seis.sod.hibernate.eventpair.MeasurementStorage;
import edu.sc.seis.sod.model.event.CacheEvent;
import edu.sc.seis.sod.status.StringTree;
import edu.sc.seis.sod.status.StringTreeLeaf;

public class PassEventStation implements EventStationSubsetter {

    public PassEventStation() {}

    public PassEventStation(Element config) {}

    public StringTree accept(CacheEvent o,
                             Station station,
                          MeasurementStorage cookieJar) {
        return new StringTreeLeaf(this, true);
    }
}