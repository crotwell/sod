package edu.sc.seis.sod.subsetter.eventStation;

import edu.iris.Fissures.IfEvent.EventAccessOperations;
import edu.iris.Fissures.IfNetwork.Station;
import edu.sc.seis.fissuresUtil.bag.DistAz;
import edu.sc.seis.sod.ConfigurationException;
import edu.sc.seis.sod.CookieJar;
import edu.sc.seis.sod.status.StringTree;
import edu.sc.seis.sod.status.StringTreeLeaf;
import edu.sc.seis.sod.subsetter.AzimuthUtils;
import edu.sc.seis.sod.subsetter.RangeSubsetter;
import org.w3c.dom.Element;

public class AzimuthRange extends RangeSubsetter implements
        EventStationSubsetter {

    public AzimuthRange(Element config) throws ConfigurationException {
        super(config);
        min = getMinValue();
        max = getMaxValue();
    }

    public StringTree accept(EventAccessOperations ev,
                          Station sta,
                          CookieJar cookieJar) {
        return new StringTreeLeaf(this, AzimuthUtils.isAzimuthBetween(new DistAz(sta, ev), min, max));
    }

    private float min, max;
}// AzimuthRange
