package edu.sc.seis.sod.subsetter.eventStation;

import org.w3c.dom.Element;
import edu.iris.Fissures.IfNetwork.Station;
import edu.sc.seis.fissuresUtil.bag.DistAz;
import edu.sc.seis.fissuresUtil.cache.CacheEvent;
import edu.sc.seis.sod.CookieJar;
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
                             Station sta,
                             CookieJar cookieJar) {
        return new StringTreeLeaf(this,
                                  AzimuthUtils.isAzimuthBetween(new DistAz(sta,
                                                                           ev),
                                                                min,
                                                                max));
    }
}// AzimuthRange
