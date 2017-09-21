package edu.sc.seis.sod.subsetter.eventStation;

import org.w3c.dom.Element;

import edu.sc.seis.seisFile.fdsnws.stationxml.Station;
import edu.sc.seis.sod.hibernate.eventpair.MeasurementStorage;
import edu.sc.seis.sod.model.common.DistAz;
import edu.sc.seis.sod.model.event.CacheEvent;
import edu.sc.seis.sod.status.StringTree;
import edu.sc.seis.sod.status.StringTreeLeaf;
import edu.sc.seis.sod.subsetter.AzimuthUtils;
import edu.sc.seis.sod.subsetter.RangeSubsetter;

public class BackAzimuthRange extends RangeSubsetter implements
        EventStationSubsetter {

    public BackAzimuthRange(Element config) {
        super(config);
    }

    public StringTree accept(CacheEvent ev,
                             Station sta,
                             MeasurementStorage cookieJar) {
        return new StringTreeLeaf(this,
                                  AzimuthUtils.isBackAzimuthBetween(new DistAz(sta,
                                                                               ev),
                                                                    min,
                                                                    max));
    }
}// BackAzimuthRange
