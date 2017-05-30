package edu.sc.seis.sod.subsetter.eventStation;

import org.w3c.dom.Element;

import edu.sc.seis.sod.hibernate.eventpair.CookieJar;
import edu.sc.seis.sod.model.common.DistAz;
import edu.sc.seis.sod.model.event.CacheEvent;
import edu.sc.seis.sod.model.station.StationImpl;
import edu.sc.seis.sod.status.StringTree;
import edu.sc.seis.sod.status.StringTreeLeaf;
import edu.sc.seis.sod.subsetter.AzimuthUtils;
import edu.sc.seis.sod.subsetter.RangeSubsetter;

public class AzimuthRange extends RangeSubsetter implements
        EventStationSubsetter {

    public AzimuthRange(Element config) {
        super(config);
    }

    public StringTree accept(CacheEvent ev,
                             StationImpl sta,
                             CookieJar cookieJar) {
        return new StringTreeLeaf(this,
                                  AzimuthUtils.isAzimuthBetween(new DistAz(sta,
                                                                           ev),
                                                                min,
                                                                max));
    }
}// AzimuthRange
